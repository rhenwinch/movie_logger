package com.xcape.movie_logger.presentation.movie_details

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.MimeTypes
import com.xcape.movie_logger.databinding.DialogTrailerBinding
import com.xcape.movie_logger.presentation.common.ScreenProperties
import com.xcape.movie_logger.presentation.components.custom_components.CustomStyledPlayerView
import dagger.hilt.android.AndroidEntryPoint


private const val PLAY_URL = "play_url"

@AndroidEntryPoint
class TrailerDialog : DialogFragment(), Player.Listener, ScreenProperties {
    private var _binding: DialogTrailerBinding? = null
    private val binding get() = _binding!!

    private var mediaUrl: String? = null
    private var mediaPlayer: CustomStyledPlayerView? = null
    private var videoPlayer: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem: Int = 0 // current playback item
    private var playbackPosition: Long = 0 // current playback position
    private val secondsToSeek = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mediaUrl = it.getString(PLAY_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val orientation = resources.configuration.orientation
        _binding = DialogTrailerBinding.inflate(layoutInflater)
        mediaPlayer = binding.trailerCustomPlayerView

        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mediaPlayer!!.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.attributes?.width = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun onStart() {
        super.onStart()
        initializePlayer(mediaUrl)
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializePlayer(mediaVideoUrl: String?) {
        val trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        val doubleTapDetector = GestureDetector(context, DoubleTapGestures())

        mediaPlayer?.doubleTapDetector = doubleTapDetector
        mediaPlayer?.setShowPreviousButton(false)
        mediaPlayer?.setShowNextButton(false)
        mediaVideoUrl?.let {
            videoPlayer = ExoPlayer.Builder(requireContext())
                .setTrackSelector(trackSelector)
                .setSeekBackIncrementMs(secondsToSeek)
                .setSeekForwardIncrementMs(secondsToSeek)
                .build()
                .also { exoPlayer ->
                    mediaPlayer?.player = exoPlayer

                    val mediaItem = MediaItem.Builder()
                        .setUri(it)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()

                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.seekTo(currentItem, playbackPosition)
                    exoPlayer.playWhenReady = playWhenReady
                    exoPlayer.prepare()
                }
        }
    }

    private fun releasePlayer() {
        videoPlayer?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        videoPlayer = null
    }

    inner class DoubleTapGestures : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return if(mediaPlayer!!.isControllerFullyVisible) {
                mediaPlayer!!.hideController()
                true
            } else {
                mediaPlayer!!.showController()
                true
            }
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val screenWidth = getScreenWidth()
            val midPoint = screenWidth / 2
            val touchedXScreen = e.x

            var needsToBeShown = false
            if(mediaPlayer!!.isControllerFullyVisible) {
                mediaPlayer!!.hideController()
                needsToBeShown = true
            }

            val rewindIcon = binding.seekBackward
            val rewindLabel = binding.seekBackwardLabel
            val forwardIcon = binding.seekForward
            val forwardLabel = binding.seekForwardLabel

            val forwardLabelText = "+${secondsToSeek/1000}"
            forwardLabel.text = forwardLabelText
            val rewindLabelText = "-${secondsToSeek/1000}"
            rewindLabel.text = rewindLabelText

            // If double tapped on right
            if(touchedXScreen >= midPoint + 150) {
                forwardIcon.visibility = View.GONE
                forwardIcon.rotation = 0F

                forwardLabel.translationX = -52.5F
                forwardLabel.scaleX = 0F
                forwardLabel.scaleY = 0F

                forwardIcon.animate()
                    .rotation(65F)
                    .setDuration(450)
                    .setListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            forwardLabel.animate()
                                .translationX(0F)
                                .scaleX(1F)
                                .scaleY(1F)
                                .setDuration(400)
                                .start()

                            forwardIcon.visibility = View.VISIBLE
                            videoPlayer!!.seekTo(videoPlayer!!.currentPosition + secondsToSeek)
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            forwardIcon.visibility = View.GONE
                            forwardIcon.rotation = 0F

                            forwardLabel.translationX = -52.5F
                            forwardLabel.scaleX = 0F
                            forwardLabel.scaleY = 0F
                        }
                    })
            }
            // If double tapped on left
            else if(touchedXScreen <= midPoint - 150) {
                rewindIcon.visibility = View.GONE
                rewindIcon.rotation = 0F

                rewindLabel.translationX = 52.5F
                rewindLabel.scaleX = 0F
                rewindLabel.scaleY = 0F

                rewindIcon.animate()
                    .rotation(65F)
                    .setDuration(450)
                    .setListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            rewindLabel.animate()
                                .translationX(0F)
                                .scaleX(1F)
                                .scaleY(1F)
                                .setDuration(400)
                                .start()

                            rewindIcon.visibility = View.VISIBLE
                            videoPlayer!!.seekTo(videoPlayer!!.currentPosition - secondsToSeek)
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            rewindIcon.visibility = View.GONE
                            rewindIcon.rotation = 0F

                            rewindLabel.translationX = 52.5F
                            rewindLabel.scaleX = 0F
                            rewindLabel.scaleY = 0F
                        }
                    })
            }

            if(needsToBeShown) {
                // Delay before showing the controller
                Handler(Looper.getMainLooper()).postDelayed({
                    mediaPlayer!!.showController()
                }, 850L)
            }

            return true
        }
    }

    companion object {
        const val TRAILER_DIALOG_TAG = "TrailerDialog"
        fun newInstance(mediaUrl: String) =
            TrailerDialog().apply {
                arguments = Bundle().apply {
                    putString(PLAY_URL, mediaUrl)
                }
            }
    }
}