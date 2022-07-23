package com.xcape.movie_logger.presentation.movie_details

import android.graphics.Bitmap
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat.startPostponedEnterTransition
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.FragmentMovieBinding
import com.xcape.movie_logger.domain.model.Media
import com.xcape.movie_logger.presentation.components.OnLoadingProgressListener
import com.xcape.movie_logger.presentation.components.activate
import com.xcape.movie_logger.presentation.components.deactivate

import com.xcape.movie_logger.presentation.trailer_dialog.TrailerFragment
import com.xcape.movie_logger.utils.Functions.parseCast
import com.xcape.movie_logger.utils.Functions.parseDate
import com.xcape.movie_logger.utils.Functions.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


const val MOVIE_ID = "movie_id"
const val MOVIE_BITMAP = "movie_bitmap"
const val MOVIE_CATEGORY = "movie_category"

@AndroidEntryPoint
class MovieActivity : AppCompatActivity(), OnLoadingProgressListener {
    private val movieViewModel: MovieViewModel by viewModels()
    private var _binding: FragmentMovieBinding? = null
    private val binding: FragmentMovieBinding
        get() = _binding!!

    private var media: Media? = null

    private lateinit var loadingBar: ProgressBar
    private lateinit var loadingContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        // Inflate the layout for this fragment
        _binding = FragmentMovieBinding.inflate(layoutInflater)
        loadingBar = binding.movieLoadingContainer.loadingBar
        loadingContainer = binding.movieLoadingContainer.root
        val movieToolbar = binding.movieToolbarLayout.movieToolbar

        setContentView(binding.root)
        movieToolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        intent?.let { it ->
            val mediaImageBitmap = it.getByteArrayExtra(MOVIE_BITMAP)?.toBitmap()
            val mediaId = it.getStringExtra(MOVIE_ID)
            val mediaCategory = it.getStringExtra(MOVIE_CATEGORY)

            setSupportActionBar(movieToolbar)
            // Get a support ActionBar corresponding to this toolbar and enable the Up button
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            bindItemImage(
                mediaId = mediaId!!,
                mediaBitmap = mediaImageBitmap,
                mediaCategory = mediaCategory!!
            )
        }

        loadUiState()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadingBar.activate(loadingContainer)
    }

    override fun onRetry() {
        movieViewModel.retryMovieRequest()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.addToFavorite -> {
                // Change the icon of the heart to filled heart
                binding.movieToolbarLayout.movieToolbar.menu[0].icon =
                    ContextCompat.getDrawable(this@MovieActivity, R.drawable.heart)

                movieViewModel.movieTitle.observe(this@MovieActivity) {
                    Toast.makeText(
                        this@MovieActivity,
                        "\"$it\" was added to favorites!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUiState() {
        lifecycleScope.launch {
            movieViewModel.state.collect { state ->
                if (state.mediaData != null) {
                    media = state.mediaData
                    bindItemText(media)
                    loadingBar.deactivate(loadingContainer)
                } else if (state.errorMessage != null) {
                    loadingBar.deactivate(
                        loadingContainer,
                        true,
                        this@MovieActivity
                    )
                }
            }
        }
    }

    private fun bindItemImage(
        mediaCategory: String,
        mediaBitmap: Bitmap? = null,
        mediaId: String
    ) {
        ViewCompat.setTransitionName(
            binding.movieToolbarLayout.movieImage,
            "${mediaId}-${mediaCategory}"
        )

        val imageView = binding.movieToolbarLayout.movieImage

        mediaBitmap?.let {
            imageView.setImageBitmap(mediaBitmap)
        }
        startPostponedEnterTransition()
    }

    private fun bindItemText(mediaData: Media?) {
        mediaData?.let {
            val titleView = binding.movieTitleLayout.movieTitle
            val durationView = binding.movieTitleLayout.movieDuration
            val imdbRatingView = binding.movieTitleLayout.movieRatingImdb
            val ratingView = binding.movieTitleLayout.movieRating

            val genreView = binding.movieDetailsLayout.movieGenre
            val sypnosisView = binding.movieDetailsLayout.movieSypnosis
            val directorView = binding.movieDetailsLayout.movieDirector
            val writerView = binding.movieDetailsLayout.movieWriter
            val actorView = binding.movieDetailsLayout.movieCast

            val trailerPreview = binding.movieTrailerLayout.trailerPlayerPreview

            titleView.text = it.title
            durationView.text =
                if (it.type.substring(0, 2) == "tv") {
                    val year =
                        if (it.year.length == 5) {
                            it.year + "present"
                        } else {
                            it.year
                        }
                    "${it.duration} | $year"
                } else
                    "${it.duration} | ${parseDate(it.dateReleased)}"

            imdbRatingView.text = it.rating.toString()
            ratingView.rating = 0F

            genreView.text = it.genres.joinToString(", ")
            sypnosisView.text = it.plotLong ?: it.plotShort
            directorView.text = it.directors.joinToString(", ")
            writerView.text = it.writers.joinToString(", ")
            actorView.text = parseCast(it.casts)

            it.gallery.thumbnail.let { url ->
                Picasso.get()
                    .load(url.replace("_V1_", "_SL350_"))
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .fit()
                    .centerCrop()
                    .into(trailerPreview)

                binding.movieTrailerLayout.trailerPreviewContainer.setOnClickListener { view ->
                    val trailerFragment =
                        TrailerFragment.newInstance(it.gallery.trailer.encodings[0].playUrl)
                    trailerFragment.show(
                        supportFragmentManager,
                        TrailerFragment.TAG
                    )
                }
            }


        }
    }
}