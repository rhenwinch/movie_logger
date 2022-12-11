package com.xcape.movie_logger.presentation.add_item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.repository.remote.MediaRepository
import com.xcape.movie_logger.domain.repository.remote.WatchedMediasRepository
import com.xcape.movie_logger.domain.repository.remote.WatchlistRepository
import com.xcape.movie_logger.domain.use_cases.firebase.Authenticator
import com.xcape.movie_logger.domain.use_cases.form_validators.RatingValidator
import com.xcape.movie_logger.domain.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

//@HiltViewModel
class AddItemViewModel @AssistedInject constructor(
    private val auth: Authenticator,
    private val remoteRepository: MediaRepository,
    private val watchlistRepository: WatchlistRepository,
    private val watchedMediasRepository: WatchedMediasRepository,
    @Assisted private val mediaId: String?
) : ViewModel() {

    val state: StateFlow<AddItemUIState>
    val mediaInfo: Flow<MediaInfo?>
    val accept: (AddItemUIAction) -> Unit
    private val ratingValidator = RatingValidator()

    @AssistedFactory
    interface AddItemViewModelFactory {
        fun create(mediaId: String?): AddItemViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: AddItemViewModelFactory,
            mediaId: String?
        ): ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(mediaId) as T
                }
            }
        }
    }

    init {
        val actionStateFlow = MutableSharedFlow<AddItemUIAction>()

        val isTyping = actionStateFlow
            .filterIsInstance<AddItemUIAction.Typing>()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .distinctUntilChanged()
            .onStart { emit(AddItemUIAction.Typing()) }

        val isSubmitting = actionStateFlow
            .filterIsInstance<AddItemUIAction.Submit>()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .distinctUntilChanged()
            .onStart { emit(AddItemUIAction.Submit()) }

        val isRating = actionStateFlow
            .filterIsInstance<AddItemUIAction.Rate>()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .distinctUntilChanged()
            .onStart { emit(AddItemUIAction.Rate()) }

        val isErrorConsumed = actionStateFlow
            .filterIsInstance<AddItemUIAction.ErrorConsume>()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .distinctUntilChanged()
            .onStart { emit(AddItemUIAction.ErrorConsume()) }

        mediaInfo = getMediaInfo(mediaId = mediaId)

        state = combine(
            combine(isTyping, isRating, ::Pair),
            combine(isSubmitting, isErrorConsumed, ::Pair),
            ::Pair
        ).map { (actionsSet1, actionsSet2) ->
            var state = AddItemUIState(
                isAdded = false,
                isTyping = actionsSet1.first.charactersTyped != null,
                allErrorsWereConsumed = actionsSet2.second.isConsumed,
                lastRatingGiven = actionsSet1.second.ratingGiven,
                lastCharactersTyped = actionsSet1.first.charactersTyped
            )

            // Validate forms before submitting
            val ratingValidation = ratingValidator.validate(actionsSet1.second.ratingGiven)
            if(!ratingValidation.isSuccessful && actionsSet1.second.ratingGiven != 0F) {
                state = state.copy(
                    isRatingError = true,
                    allErrorsWereConsumed = false,
                    ratingError = ratingValidation.error
                )
            }

            if(actionsSet2.first.isSubmitting) {
                mediaInfo.collect {
                    val isAdded = addItemMedia(
                        media = it,
                        comment = actionsSet1.first.charactersTyped,
                        rating = actionsSet1.second.ratingGiven
                    )

                    if(isAdded) {
                        state = state.copy(isAdded = true)
                    }
                }
            }

            return@map state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = AddItemUIState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("Clearing!")
    }

    private fun getMediaInfo(mediaId: String?): Flow<MediaInfo?> {
        return flow {
            try {
                // Check if it is in cache first
                val localResult = watchlistRepository.getWatchlistMediaByMediaId(mediaId!!)
                if(localResult != null) {
                    emit(localResult.mediaInfo)
                }
                else {
                    when(val remoteResult = remoteRepository.getMedia(mediaId)) {
                        is Resource.Success -> emit(remoteResult.data)
                        is Resource.Error -> emit(null)
                    }
                }
            }
            catch (e: Exception) {
                emit(null)
            }
        }
    }

    private fun removeFromWatchlist(mediaId: String) {
        watchlistRepository.deleteWatchlistMediaById(mediaId)
    }

    private fun addItemMedia(
        media: MediaInfo?,
        comment: String?,
        rating: Float
    ): Boolean {
        media?.let {
            // Remove item fromUserId watchlist if it was in there
            removeFromWatchlist(it.id)


            val review = WatchedMedia(
                id = it.id,
                ownerId = auth.loggedInUser!!.uid,
                addedOn = Date(),
                dateReleased = it.dateReleased,
                comments = comment,
                rating = rating.toDouble(),
                title = it.title,
                mediaInfo = it
            )

            watchedMediasRepository.insertWatchedMedia(review)
            return true
        }
        return false
    }
}