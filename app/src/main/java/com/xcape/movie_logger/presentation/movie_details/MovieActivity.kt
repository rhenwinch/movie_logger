package com.xcape.movie_logger.presentation.movie_details

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivityMovieBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.components.custom_extensions.OnLoadingProgressListener
import com.xcape.movie_logger.presentation.components.custom_extensions.activate
import com.xcape.movie_logger.presentation.components.custom_extensions.deactivate
import com.xcape.movie_logger.common.Functions.parseCast
import com.xcape.movie_logger.common.Functions.parseDate
import com.xcape.movie_logger.common.Functions.toBitmap
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

const val MEDIA_ID = "media_id"
const val MEDIA_ITEM = "media_item"
const val MEDIA_BITMAP = "media_bitmap"
const val MEDIA_CATEGORY = "media_category"

@AndroidEntryPoint
class MovieActivity : AppCompatActivity(), OnLoadingProgressListener {
    private var _binding: ActivityMovieBinding? = null
    private val binding: ActivityMovieBinding
        get() = _binding!!

    // View models
    private val movieViewModel: MovieViewModel by viewModels()

    private var isMediaInDB = MediaState.IN_WATCHED_LIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        // Inflate the layout for this fragment
        _binding = ActivityMovieBinding.inflate(layoutInflater)

        intent?.let { it ->
            val mediaImageBitmap = it.getByteArrayExtra(MEDIA_BITMAP)?.toBitmap()
            val mediaId = it.getStringExtra(MEDIA_ID)
            val mediaCategory = it.getStringExtra(MEDIA_CATEGORY)
            val mediaItem = it.getSerializableExtra(MEDIA_ITEM)
            val data = if(mediaItem == null) movieViewModel.mediaData else liveData { mediaItem as MediaInfo }

            setSupportActionBar(binding.movieToolbarLayout.movieToolbar)
            // Get a support ActionBar corresponding to this toolbar and enable the Up button
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            binding.bindItemImage(
                mediaId = mediaId!!,
                mediaBitmap = mediaImageBitmap,
                mediaCategory = mediaCategory!!
            )

            binding.bindViewModel(
                mediaState = movieViewModel.mediaState,
                mediaData = data
            )
        }
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(isMediaInDB == MediaState.IN_WATCHLIST) {
            menuInflater.inflate(R.menu.on_watchlist_menu, menu)
        }
        else if(isMediaInDB == MediaState.NOT_IN_ANY) {
            menuInflater.inflate(R.menu.not_watched_menu, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onRetry() {
        movieViewModel.retryFetch()
    }

    private fun ActivityMovieBinding.bindViewModel(
        mediaState: LiveData<MediaState>,
        mediaData: LiveData<MediaInfo?>
    ) {
        mediaState.observe(this@MovieActivity) {
            isMediaInDB = it
            invalidateOptionsMenu()
        }

        bindMenu()
        bindData(data = mediaData)
    }
    
    private fun ActivityMovieBinding.bindMenu() {
        val toolbar = movieToolbarLayout.movieToolbar

        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.addToWatchlist -> {
                    movieViewModel.saveInWatchlist()
                    true
                }
                R.id.onWatchlist -> {
                    movieViewModel.removeInWatchlist()
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
    }

    private fun ActivityMovieBinding.bindData(data: LiveData<MediaInfo?>) {
        val loadingBar = movieLoadingContainer.loadingBar
        val loadingContainer = movieLoadingContainer.root
        loadingBar.activate(container = loadingContainer)

        data.observe(this@MovieActivity) { media ->
            if(media == null) {
                loadingBar.deactivate(
                    container = loadingContainer,
                    causeIsError = true,
                    listener = this@MovieActivity
                )
            }
            else {
                binding.bindItemText(media)
                loadingBar.deactivate(container = loadingContainer)
            }
        }
    }

    private fun ActivityMovieBinding.bindItemImage(
        mediaCategory: String,
        mediaBitmap: Bitmap? = null,
        mediaId: String
    ) {
        ViewCompat.setTransitionName(
            movieToolbarLayout.mediaImage,
            "${mediaId}-${mediaCategory}"
        )

        val imageView = movieToolbarLayout.mediaImage

        mediaBitmap?.let {
            imageView.setImageBitmap(mediaBitmap)
        }
        startPostponedEnterTransition()
    }

    private fun ActivityMovieBinding.bindItemText(mediaInfoData: MediaInfo?) {
        mediaInfoData?.let {
            val titleView = movieTitleLayout.movieTitle
            val durationView = movieTitleLayout.movieDuration
            val imdbRatingView = movieTitleLayout.movieRatingImdb
            val ratingView = movieTitleLayout.mediaRating
            val ratingLabel = movieTitleLayout.movieRatingLabel

            val genreView = movieDetailsLayout.movieGenre
            val sypnosisView = movieDetailsLayout.movieSypnosis
            val directorView = movieDetailsLayout.movieDirector
            val writerView = movieDetailsLayout.movieWriter
            val actorView = movieDetailsLayout.movieCast

            val trailerPreview = movieTrailerLayout.trailerPlayerPreview

            val duration = if (it.type.substring(0, 2) == "tv") {
                val year = if (it.year.length == 5) it.year + "present" else it.year
                "${it.duration} | $year"
            } else
                "${it.duration} | ${parseDate(it.dateReleased)}"

            titleView.text = it.title
            durationView.text = duration

            imdbRatingView.text = it.rating.toString()
            ratingView.rating = 0F

            genreView.text = it.genres.joinToString(", ")
            sypnosisView.text = it.plotLong ?: it.plotShort
            directorView.text = it.directors.joinToString(", ")
            writerView.text = it.writers.joinToString(", ")
            actorView.text = parseCast(it.casts)


            val imageUrl = it.gallery.thumbnail ?: it.gallery.poster

            Picasso.get()
                .load(imageUrl.replace("_V1_", "_SL350_"))
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .fit()
                .centerCrop()
                .into(trailerPreview)

            it.gallery.trailer?.let {
                movieTrailerLayout.trailerPreviewContainer.setOnSingleClickListener {
                    val trailerDialog =
                        TrailerDialog.newInstance(it.encodings[0].playUrl)
                    trailerDialog.show(
                        supportFragmentManager,
                        TrailerDialog.TRAILER_DIALOG_TAG
                    )
                }
            }

        }
    }
}