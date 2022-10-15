package com.xcape.movie_logger.presentation.trending

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivityTrendingBinding
import com.xcape.movie_logger.domain.factory.ChartMediaTypeFactory
import com.xcape.movie_logger.domain.model.base.BaseChartMedia
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.OnMediaLongClickListener
import com.xcape.movie_logger.presentation.components.custom_components.HorizontalMarginItemDecoration
import com.xcape.movie_logger.presentation.components.custom_extensions.OnLoadingProgressListener
import com.xcape.movie_logger.presentation.components.custom_extensions.activate
import com.xcape.movie_logger.presentation.components.custom_extensions.deactivate
import com.xcape.movie_logger.presentation.movie_details.MEDIA_BITMAP
import com.xcape.movie_logger.presentation.movie_details.MEDIA_CATEGORY
import com.xcape.movie_logger.presentation.movie_details.MEDIA_ID
import com.xcape.movie_logger.presentation.movie_details.MovieActivity
import com.xcape.movie_logger.presentation.trending.adapters.TrendingBoxOfficeAdapter
import com.xcape.movie_logger.presentation.trending.adapters.TrendingPagingAdapter
import com.xcape.movie_logger.domain.utils.Functions.toByteArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs


@AndroidEntryPoint
class TrendingActivity : AppCompatActivity(), OnMediaClickListener, OnLoadingProgressListener,
    OnMediaLongClickListener {
    private var _binding: ActivityTrendingBinding? = null
    private val binding: ActivityTrendingBinding
        get() = _binding!!

    // View Model
    private val trendingViewModel: TrendingViewModel by viewModels()

    // Factory to determine the media type
    @Inject
    lateinit var chartMediaTypeFactory: ChartMediaTypeFactory

    // Views
    private lateinit var loadingBar: ProgressBar
    private lateinit var loadingBarContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()

        // Inflate the layout for this fragment
        _binding = ActivityTrendingBinding.inflate(layoutInflater)
        loadingBar = binding.trendingLoadingContainer.loadingBar
        loadingBarContainer = binding.trendingLoadingContainer.root

        setContentView(binding.root)

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        setSupportActionBar(binding.trendingToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.bindState(
            uiState = trendingViewModel.trendingUIState,
            uiActions = trendingViewModel.accept
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        binding.refreshTrending.isRefreshing = false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onMediaClick(
        mediaCategory: String,
        mediaId: String,
        mediaImageView: ImageView,
        mediaImageCard: MaterialCardView
    ) {
        val mediaImage = mediaImageView.drawable?.toBitmap()?.toByteArray()
        // Proceed only if bitmap is not null

        mediaImage?.let { bitmap ->
            val intent = Intent(this, MovieActivity::class.java)
            intent.putExtra(MEDIA_ID, mediaId)
            intent.putExtra(MEDIA_CATEGORY, mediaCategory)
            intent.putExtra(MEDIA_BITMAP, bitmap)

            val options = ActivityOptions.makeSceneTransitionAnimation(this, mediaImageCard,"${mediaId}-${mediaCategory}")

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    override fun onRetry() {
        trendingViewModel.refreshData(needsRetry = true)
    }

    override fun onMediaLongClick(
        position: Int?,
        mediaId: String
    ) {
        TODO("Not yet implemented")
    }

    private fun ActivityTrendingBinding.bindState(
        uiState: StateFlow<TrendingUIState>,
        uiActions: (TrendingUIAction) -> Unit
    ) {
        val popularMoviesAdapter = TrendingPagingAdapter(chartMediaTypeFactory, this@TrendingActivity)
        val popularTVAdapter = TrendingPagingAdapter(chartMediaTypeFactory, this@TrendingActivity)
        val topMoviesAdapter = TrendingPagingAdapter(chartMediaTypeFactory, this@TrendingActivity)
        val topTVAdapter = TrendingPagingAdapter(chartMediaTypeFactory, this@TrendingActivity)
        val boxOfficeAdapter = TrendingBoxOfficeAdapter(this@TrendingActivity)

        // Set adapters
        trendingMovieLayout.popularMoviesRV.adapter = popularMoviesAdapter
        trendingTVLayout.popularTVRV.adapter = popularTVAdapter
        trendingTopMovieLayout.topMovieRV.adapter = topMoviesAdapter
        trendingTopTVLayout.topTopRV.adapter = topTVAdapter
        trendingBoxOfficeLayout.boxOfficeVP.adapter = boxOfficeAdapter

        bindViewPager()

        bindCharts(
            uiState = uiState,
            eventCallback = uiActions,
            popularMoviesAdapter = popularMoviesAdapter,
            popularTVAdapter = popularTVAdapter,
            topMoviesAdapter = topMoviesAdapter,
            topTVAdapter = topTVAdapter,
            boxOfficeAdapter = boxOfficeAdapter
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun ActivityTrendingBinding.bindCharts(
        uiState: StateFlow<TrendingUIState>,
        eventCallback: (TrendingUIAction) -> Unit,
        popularMoviesAdapter: TrendingPagingAdapter,
        popularTVAdapter: TrendingPagingAdapter,
        topMoviesAdapter: TrendingPagingAdapter,
        topTVAdapter: TrendingPagingAdapter,
        boxOfficeAdapter: TrendingBoxOfficeAdapter
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    uiState.collect { state ->
                        (state.popularChartData[POPULAR_MOVIE] as? Flow<PagingData<BaseChartMedia>>)?.collectLatest(
                            popularMoviesAdapter::submitData
                        )
                    }
                }

                launch {
                    uiState.collect { state ->
                        (state.popularChartData[POPULAR_TV] as? Flow<PagingData<BaseChartMedia>>)?.collectLatest(
                            popularTVAdapter::submitData
                        )
                    }
                }

                launch {
                    uiState.collect { state ->
                        (state.topChartData[TOP_MOVIE] as? Flow<PagingData<BaseChartMedia>>)?.collectLatest(
                            topMoviesAdapter::submitData
                        )
                    }
                }

                launch {
                    uiState.collect { state ->
                        (state.topChartData[TOP_TV] as? Flow<PagingData<BaseChartMedia>>)?.collectLatest(
                            topTVAdapter::submitData
                        )
                    }
                }

                launch {
                    uiState.collect { state ->
                        state.boxOfficeData?.collectLatest(boxOfficeAdapter::submitList)
                    }
                }
            }
        }

        val isNotLoading = uiState
            .map { it.hasFinishedFetching }
            .distinctUntilChanged()

        val hasErrors = uiState
            .map { it.hasErrors }
            .distinctUntilChanged()

        val isRefreshing = uiState
            .map { it.isRefreshing }
            .distinctUntilChanged()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Progress Bar
                launch {
                    isNotLoading.collect { isNotLoading ->
                        if(!isNotLoading) {
                            loadingBar.activate(loadingBarContainer)
                        }
                        else {
                            refreshTrending.setOnRefreshListener { onSwipeRefresh(eventCallback) }
                            loadingBar.deactivate(loadingBarContainer)
                        }
                    }
                }

                // Error Progress Bar
                launch {
                    hasErrors.collect {
                        if(it) {
                            trendingViewModel.cancelAllDataFetchJobs()
                            loadingBar.deactivate(
                                container = loadingBarContainer,
                                causeIsError = true,
                                this@TrendingActivity
                            )
                        }
                    }
                }

                // Refresh listener
                launch {
                    isRefreshing.collectLatest {
                        if(it && !refreshTrending.isRefreshing) {
                            binding.root.doOnPreDraw { _ ->
                                refreshTrending.isRefreshing = it
                                onSwipeRefresh(eventCallback)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ActivityTrendingBinding.bindViewPager() {
        // Setup ViewPager2 behavior
        // Implement dot indicators; and Page transformer
        // to preview next and previous pages in a view pager
        val boxOfficeVP = trendingBoxOfficeLayout.boxOfficeVP
        val boxOfficeVPIndicator = trendingBoxOfficeLayout.dotsIndicator
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
    }

    private fun ActivityTrendingBinding.onSwipeRefresh(onRefresh: (TrendingUIAction.Refresh) -> Unit) {
        onRefresh(TrendingUIAction.Refresh(triggerRefresh = true))
        val message =
            if(trendingViewModel.refreshData()) {
                "Everything has been updated!"
            }
            else {
                "Everything has already been updated!"
            }

        Handler(Looper.getMainLooper()).postDelayed({
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT)
                .setAction(R.string.ok) {}
                .show()
            refreshTrending.isRefreshing = false
            onRefresh(TrendingUIAction.Refresh(triggerRefresh = false))
        }, 1500)
    }

    private fun setupWindowAnimations() {
        val slide = Slide(Gravity.RIGHT)
        slide.duration = 200

        window.enterTransition = slide
        window.exitTransition = slide
    }
}