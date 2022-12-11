package com.xcape.movie_logger.presentation.add_item

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.DialogAddWatchedBinding
import com.xcape.movie_logger.presentation.common.OnDialogDismissListener
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.use_cases.form_validators.InvalidRating
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.movie_details.MEDIA_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ADD_ITEM_DIALOG_TAG = "add_item_dialog"

@AndroidEntryPoint
class AddItemDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAddWatchedBinding? = null
    private val binding: DialogAddWatchedBinding
        get() = _binding!!

    // View models factory
    @Inject
    lateinit var addItemViewModelFactory: AddItemViewModel.AddItemViewModelFactory

    private val addItemViewModel: AddItemViewModel by viewModels {
        AddItemViewModel.provideFactory(
            assistedFactory = addItemViewModelFactory,
            mediaId = arguments?.getString(MEDIA_ID)
        )
    }

    // Listeners
    var dialogDismissListener: OnDialogDismissListener? = null
    var onAddConfirmListener: OnAddConfirmListener? = null

    // Rating jobs
    private var ratingJob: Job? = null

    interface OnAddConfirmListener {
        fun onAddConfirm(mediaId: String?)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DialogAddWatchedBinding.inflate(layoutInflater)

        binding.bindState(
            uiState = addItemViewModel.state,
            uiActions = addItemViewModel.accept,
            mediaData = addItemViewModel.mediaInfo
        )

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogDismissListener?.onDismissDialog(ADD_ITEM_DIALOG_TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { setupBottomSheet(it) }
        return dialog
    }

    private fun DialogAddWatchedBinding.bindState(
        uiState: StateFlow<AddItemUIState>,
        uiActions: (AddItemUIAction) -> Unit,
        mediaData: Flow<MediaInfo?>
    ) {
        lifecycleScope.launch {
            mediaData.collectLatest {
                it?.let {
                    val details = "${it.title} (${it.year})"
                    mediaTitle.text = details

                    Picasso.get()
                        .load(it.gallery.poster.replace("_V1_", "_SL450_"))
                        .fit()
                        .centerInside()
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .into(mediaImagePoster)

                    bindFormSubmit(
                        uiState = uiState,
                        submitCallback = uiActions
                    )
                }
            }
        }

        bindCommentBox(
            uiState = uiState,
            commentBoxChangeCallback = uiActions
        )

        bindRating(
            uiState = uiState,
            ratingChangeCallback = uiActions
        )
    }

    private fun DialogAddWatchedBinding.bindCommentBox(
        uiState: StateFlow<AddItemUIState>,
        commentBoxChangeCallback: (AddItemUIAction) -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.DESTROYED) {
                uiState.collect {
                    if(it.isTyping) {
                        commentBox.text = it.lastCharactersTyped as Editable
                        commentBox.requestFocus()
                    }
                }
            }
        }

        commentBox.addTextChangedListener {
            updateCommentBox(commentBoxChangeCallback)
        }
    }

    private fun DialogAddWatchedBinding.bindRating(
        uiState: StateFlow<AddItemUIState>,
        ratingChangeCallback: (AddItemUIAction) -> Unit
    ) {
        val isRating = uiState
            .map { it.lastRatingGiven }
            .distinctUntilChanged()

        mediaRating.setOnRatingBarChangeListener { _, rating, _ ->
            updateRatingBar(ratingChangeCallback)
            ratingBox.setText(rating.toString())
            ratingBox.setSelection(ratingBox.length())
        }

        ratingBox.addTextChangedListener {
            ratingJob?.cancel()
            ratingJob = lifecycleScope.launch {
                delay(500)
                if(it?.isEmpty() == true) {
                    mediaRating.rating = 0F
                }
                else if(it?.isNotEmpty() == true) {
                    mediaRating.rating = it.toString().toFloat()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.DESTROYED) {
                isRating.collect(mediaRating::setRating)
            }
        }

        lifecycleScope.launch {
            uiState.collect {
                if(it.isRatingError && !it.allErrorsWereConsumed) {
                    val error = it.ratingError as InvalidRating
                    if(error.isGreaterThan5) {
                        ratingBox.setText(5.0F.toString())
                    }
                    else if (error.isLessThan0) {
                        ratingBox.setText(1.0F.toString())
                    }
                    else {
                        ratingErrorText.text = error.message
                    }
                    consumeError(ratingChangeCallback)
                }
            }
        }
    }

    private fun DialogAddWatchedBinding.bindFormSubmit(
        uiState: StateFlow<AddItemUIState>,
        submitCallback: (AddItemUIAction) -> Unit
    ) {
        val isItemAdded = uiState
            .map { it.isAdded }
            .distinctUntilChanged()

        formSubmit.setOnSingleClickListener {
            submitForm(submitCallback)
            lifecycleScope.launch {
                isItemAdded.collect {
                    if(it) {
                        onAddConfirmListener?.onAddConfirm(arguments?.getString(MEDIA_ID))
                        this@AddItemDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun submitForm(onSubmitClick: (AddItemUIAction.Submit) -> Unit)
        = onSubmitClick(AddItemUIAction.Submit(isSubmitting = true))

    private fun consumeError(onErrorConsume: (AddItemUIAction.ErrorConsume) -> Unit)
        = onErrorConsume(AddItemUIAction.ErrorConsume(isConsumed = true))

    private fun DialogAddWatchedBinding.updateCommentBox(onStartTyping: (AddItemUIAction.Typing) -> Unit)
        = onStartTyping(AddItemUIAction.Typing(charactersTyped = commentBox.text.toString()))

    private fun DialogAddWatchedBinding.updateRatingBar(onRatingChange: (AddItemUIAction.Rate) -> Unit)
        = onRatingChange(AddItemUIAction.Rate(ratingGiven = mediaRating.rating))

    private fun setupBottomSheet(dialogInterface: DialogInterface) {
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
