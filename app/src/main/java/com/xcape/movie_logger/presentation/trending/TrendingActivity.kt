package com.xcape.movie_logger.presentation.trending

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Fade
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivityTrendingBinding
import com.xcape.movie_logger.domain.model.MediaMetadata
import com.xcape.movie_logger.domain.model.PopularChart
import com.xcape.movie_logger.presentation.components.HorizontalMarginItemDecoration
import com.xcape.movie_logger.presentation.components.OnLoadingProgressListener
import com.xcape.movie_logger.presentation.components.activate
import com.xcape.movie_logger.presentation.components.deactivate
import com.xcape.movie_logger.presentation.movie_details.MOVIE_BITMAP
import com.xcape.movie_logger.presentation.movie_details.MOVIE_CATEGORY
import com.xcape.movie_logger.presentation.movie_details.MOVIE_ID
import com.xcape.movie_logger.presentation.movie_details.MovieActivity
import com.xcape.movie_logger.utils.Constants.TAG
import com.xcape.movie_logger.utils.Functions.toByteArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.round


@AndroidEntryPoint
class TrendingActivity : AppCompatActivity(), OnMovieClickListener, OnLoadingProgressListener {
    private var _binding: ActivityTrendingBinding? = null
    private val binding: ActivityTrendingBinding
        get() = _binding!!
    private val trendingViewModel: TrendingViewModel by viewModels()
    private val popularMoviesAdapter: TrendingPopularAdapter = TrendingPopularAdapter(this)
    private val popularTVAdapter: TrendingPopularAdapter = TrendingPopularAdapter(this)
    private val topMoviesAdapter: TrendingTopAdapter = TrendingTopAdapter(this)
    private val topTVAdapter: TrendingTopAdapter = TrendingTopAdapter(this)
    private val boxOfficeAdapter: TrendingBoxOfficeAdapter = TrendingBoxOfficeAdapter(this)
    private lateinit var loadingBar: ProgressBar
    private lateinit var loadingBarContainer: View
    private lateinit var boxOfficeVP: ViewPager2
    private lateinit var boxOfficeVPIndicator: DotsIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()

        // Inflate the layout for this fragment
        _binding = ActivityTrendingBinding.inflate(layoutInflater)
        loadingBar = binding.trendingLoadingContainer.loadingBar
        loadingBarContainer = binding.trendingLoadingContainer.root
        boxOfficeVP = binding.trendingBoxOfficeLayout.trendingBoxOfficeViewPager
        boxOfficeVPIndicator = binding.trendingBoxOfficeLayout.dotsIndicator

        setContentView(binding.root)

        setSupportActionBar(binding.trendingToolbar)
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // RecyclerViews/ViewPager2/TabLayout/RefreshLayout
        val popularMoviesRV = binding.trendingMovieLayout.trendingPopMovieRecyclerView
        val popularTVRV = binding.trendingTVLayout.trendingPopTVRecyclerView
        val topMovieRV = binding.trendingTopMovieLayout.trendingTopMovieRecyclerView
        val topTopRV = binding.trendingTopTVLayout.trendingTopTVRecyclerView
        val refreshLayout = binding.refreshTrending

        // Set adapters
        popularMoviesRV.adapter = popularMoviesAdapter
        popularTVRV.adapter = popularTVAdapter
        topMovieRV.adapter = topMoviesAdapter
        topTopRV.adapter = topTVAdapter
        boxOfficeVP.adapter = boxOfficeAdapter

        setupViewPager()

        // Load the items
        loadUiStates(popularType = "moviemeter")
        loadUiStates(popularType = "tvmeter")
        loadUiStates(topType = "movie")
        loadUiStates(topType = "tv")
        loadUiStates(boxOfficeState = true)

        // Setup refresh/loading progress bars
        refreshLayout.setOnRefreshListener {
            updateTrendingCharts()
        }
        setupLoadingBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Remove all item decorations on the view pager to avoid glitches
        while (boxOfficeVP.itemDecorationCount > 0) {
            boxOfficeVP.removeItemDecorationAt(0)
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        trendingViewModel.dataProgress.observe(this) {
            if(round(it) < MAX_PROGRESS)
                loadingBar.activate(loadingBarContainer)
        }
    }

    override fun onPause() {
        super.onPause()
        loadingBar.deactivate(loadingBarContainer)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    override fun onMovieClick(
        mediaPosition: Int,
        mediaCategory: String,
        mediaId: String,
        mediaImageView: ImageView,
        mediaImageCard: MaterialCardView
    ) {
        val mediaImage = mediaImageView.drawable?.toBitmap()?.toByteArray()
        // Proceed only if bitmap is not null

        mediaImage?.let { bitmap ->
            val intent = Intent(this, MovieActivity::class.java)
            intent.putExtra(MOVIE_ID, mediaId)
            intent.putExtra(MOVIE_CATEGORY, mediaCategory)
            intent.putExtra(MOVIE_BITMAP, bitmap)

            val options = ActivityOptions.makeSceneTransitionAnimation(this, mediaImageCard,"${mediaId}-${mediaCategory}")

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    override fun onRetry() {
        trendingViewModel.refreshData(needsRetry = true)
    }

    private fun loadUiStates(
        popularType: String? = null,
        topType: String? = null,
        boxOfficeState: Boolean = false
    ) {
        popularType?.let {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    trendingViewModel.listOfPopularStates[popularType]!!.collect { state ->
                        if (state.errorMessage != null) {
                            Log.e(TAG, state.errorMessage)
                            loadingBar.deactivate(
                                container = loadingBarContainer,
                                causeIsError = true,
                                this@TrendingActivity
                            )
                        }
                        else if (state.flowPagingData != null) {
                            attachToAdapter(popularState = state)
                        }
                    }
                }
            }
        }

        topType?.let {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    trendingViewModel.listOfTopStates[topType]!!.collect { state ->
                        if (state.errorMessage != null) {
                            Log.e(TAG, state.errorMessage)
                            loadingBar.deactivate(
                                container = loadingBarContainer,
                                causeIsError = true,
                                this@TrendingActivity
                            )
                        }
                        else if (state.flowPagingData != null) {
                            attachToAdapter(topState = state)
                        }
                    }
                }
            }
        }

        if(boxOfficeState) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    trendingViewModel.boxOfficeState.collect { state ->
                        if (state.errorMessage != null) {
                            Log.e(TAG, state.errorMessage)
                            loadingBar.deactivate(
                                container = loadingBarContainer,
                                causeIsError = true,
                                this@TrendingActivity
                            )
                        }
                        else if (state.flowListData != null) {
                            attachToAdapter(boxOfficeState = state)
                        }
                    }
                }
            }
        }
    }

    private fun attachToAdapter(
        popularState: TrendingUIState<PopularChart>? = null,
        topState: TrendingUIState<MediaMetadata>? = null,
        boxOfficeState: TrendingUIState<MediaMetadata>? = null
    ) {
        popularState?.let { state ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    state.flowPagingData?.collectLatest { pagedData ->
                        when(state.type) {
                            "moviemeter" -> popularMoviesAdapter.submitData(pagedData)
                            "tvmeter" -> popularTVAdapter.submitData(pagedData)
                        }
                    }
                }
            }
        }

        topState?.let { state ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    state.flowPagingData?.collectLatest { pagedData ->
                        when(state.type) {
                            "movie" -> topMoviesAdapter.submitData(pagedData)
                            "tv" -> topTVAdapter.submitData(pagedData)
                        }
                    }
                }
            }
        }

        boxOfficeState?.let { state ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    state.flowListData?.collectLatest { pagedData ->
                       boxOfficeAdapter.submitList(pagedData)
                    }
                    trendingViewModel.viewPagerPosition.observe(this@TrendingActivity) {
                        binding.trendingBoxOfficeLayout.trendingBoxOfficeViewPager.currentItem = it
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        // Setup ViewPager2 behavior
        // Implement dot indicators; and
        // Page transformer to preview next and previous pages in a view pager
        // TabLayoutMediator(boxOfficeTabLayout, boxOfficeVP) { _, _ -> }.attach()
        boxOfficeVPIndicator.attachTo(boxOfficeVP)
        boxOfficeVP.apply {
            offscreenPageLimit = 1

            // https://stackoverflow.com/a/58088398/19371763
            val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
            val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
            val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
            val transform = ViewPager2.PageTransformer { page, position ->
                val scaleFactor = 1 - (0.25F * abs(position))
                val alpha = 0 + (153  * abs(position)).toInt()

                page.translationX = -pageTranslationX * position
                page.foreground = ColorDrawable(Color.argb(alpha, 0, 0, 0))
                page.scaleY = scaleFactor
            }
            setPageTransformer(transform)

            addItemDecoration(HorizontalMarginItemDecoration(context, R.dimen.viewpager_current_item_horizontal_margin))
        }

        boxOfficeVP.registerOnPageChangeCallback(
            object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    trendingViewModel.setViewPagerLastPosition(position)
                }
            }
        )
    }

    private fun setupLoadingBar() {
        trendingViewModel.dataProgress.observe(this) {
            if(round(it) == MAX_PROGRESS.toDouble()) {
                loadingBar.deactivate(loadingBarContainer)
            }
        }
    }

    private fun updateTrendingCharts() {
        val message = if(trendingViewModel.refreshData()) {
            "Charts are now updated!"
        }
        else {
            "Charts are already updated!"
        }
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            binding.refreshTrending.isRefreshing = false
        }, 1500)
    }

    private fun setupWindowAnimations() {
        val slide = Slide(Gravity.RIGHT)
        slide.duration = 200

        window.enterTransition = slide
    }
}