package com.xcape.movie_logger.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.xcape.movie_logger.common.Constants.APP_TAG
import com.xcape.movie_logger.common.Functions
import com.xcape.movie_logger.data.dto.FCMNotificationPayload
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.domain.model.user.Post
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.model.user.WatchStatus
import com.xcape.movie_logger.domain.repository.remote.*
import com.xcape.movie_logger.domain.use_cases.firebase.NotificationSender
import com.xcape.movie_logger.domain.use_cases.firebase.PostLiker
import com.xcape.movie_logger.domain.use_cases.firebase.RawPostChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postLiker: PostLiker,
    private val rawPostChecker: RawPostChecker,
    private val notificationSender: NotificationSender,
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val friendsRepository: FriendsRepository,
    private val watchlistRepository: WatchlistRepository,
    private val watchedMediasRepository: WatchedMediasRepository,
    private val newsFeedRepository: NewsFeedRepository,
) : ViewModel() {
    private val _postOwners = MutableStateFlow<List<User>>(emptyList())
    val postOwners: StateFlow<List<User>> = _postOwners

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _topMovies = MutableStateFlow<List<WatchedMedia>>(emptyList())
    val topMovies: StateFlow<List<WatchedMedia>> = _topMovies

    init {
        viewModelScope.launch {
            getCurrentAuthUser().collect {
                if(it != null) {
                    getLatestPosts()
                    getPostOwners()
                }
            }
        }
    }

    fun onEvent(event: HomeUIAction) {
        when(event) {
            is HomeUIAction.Like -> {
                if(!event.isDisliking
                    && (authRepository.getAuthUser()?.uid != event.ownerId)) {
                    sendLikeNotification(event.ownerId, event.postId)
                }
                likePost(event.ownerId, event.postId, isDisliking = event.isDisliking)
            }
            is HomeUIAction.AddToWatchlist -> {
                val post = event.post.post!!
                val postPosition = _posts.value.indexOfFirst {
                    it.post!!.id == post.id && it.post.ownerId == post.ownerId
                }

                if(event.isRemoving) {
                    watchlistRepository.deleteWatchlistMediaById(mediaId = post.id)
                    replacePost(
                        index = postPosition,
                        post = event.post.copy(watchStatus = WatchStatus.NOT_WATCHED)
                    )
                    return
                }

                watchlistRepository.insertWatchlistMedia(
                    media = WatchlistMedia(
                        id = post.id,
                        addedOn = Date(),
                        dateReleased = post.dateReleased,
                        rating = post.mediaInfo!!.rating,
                        title = post.title,
                        mediaInfo = post.mediaInfo
                    )
                )
                replacePost(
                    index = postPosition,
                    post = event.post.copy(watchStatus = WatchStatus.WATCHLIST)
                )
            }
            is HomeUIAction.Rate -> {  }
        }
    }

    private fun getCurrentAuthUser(): Flow<FirebaseUser?> {
        return authRepository.getCurrentAuthUser()
    }

    private fun getLatestPosts() {
        viewModelScope.launch {
            launch {
                newsFeedRepository.getLatestPosts()
                    .collect { rawPost ->
                        val isLiked = rawPostChecker.checkIfLiked(rawPost.likes)
                        val watchStatus = rawPostChecker.checkWatchStatus(rawPost.id)
                        val post = Post(
                            isLiked = isLiked,
                            watchStatus = watchStatus,
                            post = rawPost
                        )

                        val indexInList = _posts.value.indexOfFirst { it.post?.ownerId == rawPost.ownerId && it.post.id == rawPost.id }

                        // Check if its already in the list, then update it
                        if(indexInList != -1)
                            replacePost(indexInList, post)
                        else
                            addPost(post)
                    }
            }

            launch {
                getOwnLatestPosts()
                    .collectLatest { myPosts ->
                        _topMovies.update {
                            myPosts.sortedByDescending { it.rating }
                                .take(3)
                        }

                        myPosts.forEach { myPost ->
                            val isLiked = rawPostChecker.checkIfLiked(myPost.likes)
                            val watchStatus = rawPostChecker.checkWatchStatus(myPost.id)
                            val post = Post(
                                isLiked = isLiked,
                                watchStatus = watchStatus,
                                post = myPost
                            )

                            val indexInList = _posts.value.indexOfFirst { it.post?.ownerId == myPost.ownerId && it.post.id == myPost.id }

                            // Check if its already in the list, then update it
                            if(indexInList != -1)
                                replacePost(indexInList, post)
                            else
                                addPost(post)
                        }
                }
            }
        }
    }

    private fun getOwnLatestPosts(): Flow<List<WatchedMedia>> {
        return watchedMediasRepository.getLatestWatchedMedias()
    }

    private fun getCurrentUserId(): Flow<User> {
        return usersRepository.getLiveUser()
    }

    private fun getPostOwners() {
        viewModelScope.launch {
            getCurrentUserId().combine(friendsRepository.getLatestFriends()) { user, others ->
                val owners = mutableListOf<User>()
                owners.add(user)
                owners.addAll(others)
                _postOwners.value = owners
            }.collect()
        }
    }

    private fun likePost(
        ownerId: String,
        postId: String,
        isDisliking: Boolean = false
    ) {
        if(isDisliking) {
            viewModelScope.launch {
                postLiker.onDislike(ownerId, postId) }
        }
        else {
            viewModelScope.launch {
                postLiker.onLike(ownerId, postId) }
        }
    }

    private fun sendLikeNotification(
        ownerId: String,
        postId: String
    ) {
        viewModelScope.launch {
            // Get post owner ID
            val postOwner = usersRepository.getUser(ownerId) ?: throw NullPointerException("User not found!")
            val currentUser = usersRepository.getUser()!!

            // Get post info
            val post = watchedMediasRepository.getWatchedMediaByMediaId(postId, ownerId)

            post?.let { media ->
                val title = "Your review got a like!"
                val message = String.format("%s liked your review on %s", currentUser.username, media.title)

                val notification = Notification(
                    from = currentUser,
                    message = message,
                    mediaId = postId,
                    timestamp = Date(Functions.getCurrentTimestamp())
                )
                postOwner.fcmToken.forEach { token ->
                    notificationSender.send(token = token, to = postOwner,
                        notification = notification,
                        payload = FCMNotificationPayload(
                            title = title,
                            body = message,
                            icon = currentUser.imageProfile
                        ))
                }

                return@launch
            }
            Log.e(APP_TAG, "Error fetching post!")
        }
    }

    private fun addPost(post: Post) {
        _posts.update {
            _posts.value.toMutableList().apply { add(post) }
        }
    }

    private fun replacePost(index: Int, post: Post) {
        _posts.update {
            _posts.value.toMutableList().apply { set(index, post) }
        }
    }
}