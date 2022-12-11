package com.xcape.movie_logger.presentation.search

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Slide
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnPreDraw
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivitySearchBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.SuggestedMedia
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.components.custom_extensions.OnLoadingProgressListener
import com.xcape.movie_logger.presentation.movie_details.MEDIA_BITMAP
import com.xcape.movie_logger.presentation.movie_details.MEDIA_CATEGORY
import com.xcape.movie_logger.presentation.movie_details.MEDIA_ID
import com.xcape.movie_logger.presentation.movie_details.MovieActivity
import com.xcape.movie_logger.presentation.search.adapters.SearchResultsAdapter
import com.xcape.movie_logger.presentation.search.adapters.SuggestedMediaAdapter
import com.xcape.movie_logger.common.Functions.toByteArray
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.components.custom_extensions.activate
import com.xcape.movie_logger.presentation.components.custom_extensions.deactivate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : AppCompatActivity(), OnLoadingProgressListener, OnSuggestionClickListener,
    OnMediaClickListener {
    private var _binding: ActivitySearchBinding? = null
    private val binding: ActivitySearchBinding
        get() = _binding!!

    // ViewModel
    private val searchViewModel: SearchViewModel by viewModels()

    // Views
    private lateinit var loadingBar: ProgressBar
    private lateinit var loadingContainer: View

    // Constants
    private var backPressedTwice = false

    private var suggestingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()

        // Inflate the layout for this fragment
        _binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingBar = binding.searchLoadingContainer.loadingBar
        loadingContainer = binding.searchLoadingContainer.root

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.bindState(
            uiState = searchViewModel.state,
            searchResultsData = searchViewModel.searchResults,
            suggestedQueries = searchViewModel.suggestedQueries
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        backPressedTwice = true
        onBackPressed()
        return true
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        (parent as? ViewGroup)?.doOnPreDraw {
            binding.setupSearchResultsContainer()
        }
        return super.onCreateView(parent, name, context, attrs)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onBackPressed() {
        if (backPressedTwice) {
            super.onBackPressed()
            finish()
        }

        backPressedTwice = true
        val searchResultsBottomSheetBehavior = BottomSheetBehavior.from(binding.searchResultsCardContainer)
        if (searchResultsBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            searchResultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (searchResultsBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            super.onBackPressed()
            finish()
        }

        val timeBeforeBackPressReset = 2000L
        Handler(Looper.getMainLooper()).postDelayed(
            { backPressedTwice = false },
            timeBeforeBackPressReset
        )
    }

    override fun onRetry() {
        TODO("Implement retry function")
    }

    override fun onSuggestionClick(item: String) {
        binding.searchBox.setText(item)
        binding.updateSearchResults()
        binding.searchBoxFocusEraser()
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
            val intent = Intent(applicationContext, MovieActivity::class.java)
            intent.putExtra(MEDIA_ID, mediaId)
            intent.putExtra(MEDIA_CATEGORY, mediaCategory)
            intent.putExtra(MEDIA_BITMAP, bitmap)

            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                mediaImageCard,
                "${mediaId}-${mediaCategory}"
            )

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    private fun setupWindowAnimations() {
        val slide = Slide(Gravity.RIGHT)
        slide.duration = 200

        window.enterTransition = slide
        window.exitTransition = slide
    }

    private fun ActivitySearchBinding.setupSearchResultsContainer() {
        // Get the Y layout position of the divider.
        // Layout height has already been subtracted to the original screen size
        // Status bar height is what I need since searchDivider is near top of the layout
        val layoutHeight = root.height
        val statusBarHeight = getStatusBarHeight()
        val coordinates = IntArray(2)
        searchDivider.getLocationInSurface(coordinates)
        val dividerYLocation = layoutHeight - (coordinates[1] - statusBarHeight)
        val searchResultsBottomSheetBehavior = BottomSheetBehavior.from(searchResultsCardContainer)
        searchResultsBottomSheetBehavior.peekHeight = dividerYLocation

        searchResultsBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED && searchResultsBottomSheetBehavior.isHideable) {
                    searchResultsBottomSheetBehavior.isHideable = false
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun ActivitySearchBinding.toggleSearchResultsContainer(
        shouldShow: Boolean,
        duration: Long = 200
    ) {
        val searchResultsBottomSheetBehavior = BottomSheetBehavior.from(searchResultsCardContainer)

        val shouldFadeOut = if (shouldShow) Fade.IN else Fade.OUT
        val fade = Fade(shouldFadeOut)
        fade.duration = duration
        fade.addTarget(suggestedMediaContainer)
        TransitionManager.beginDelayedTransition(binding.root, fade)

         if (shouldShow) {
            suggestedMediaContainer.visibility = View.GONE
            searchResultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            suggestedMediaContainer.visibility = View.VISIBLE
            searchResultsBottomSheetBehavior.isHideable = true
            searchResultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun ActivitySearchBinding.searchBoxFocusEraser() {
        searchBox.clearFocus()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(window?.decorView?.windowToken, 0)
    }

    private fun ActivitySearchBinding.changeSearchIndicator(
        hasNotSearched: Boolean = true,
        isLoading: Boolean = false,
        hasErrors: Boolean = false,
        resultsCount: Int = 0
    ) {
        val recommendedText = "recommended"
        val loadingText = "searching"
        val errorText = "error searching"

        searchIndicator.text =
            if(isLoading)
                loadingText
            else if(hasErrors)
                errorText
            else if (hasNotSearched)
                recommendedText
            else
                String.format("%d results found", resultsCount)
    }

    private fun ActivitySearchBinding.bindState(
        uiState: StateFlow<SearchUIState>,
        suggestedQueries: LiveData<List<SuggestedMedia>>,
        searchResultsData: LiveData<List<MediaInfo>>
    ) {
        val searchResultsAdapter = SearchResultsAdapter(listener = this@SearchActivity)
        val suggestedMediaAdapter = SuggestedMediaAdapter(this@SearchActivity)

        searchRecyclerView.adapter = searchResultsAdapter
        suggestedMediaRecyclerView.adapter = suggestedMediaAdapter
        suggestedMediaRecyclerView.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )

        bindSearchBox(
            suggestedQueriesAdapter = suggestedMediaAdapter,
            suggestedQueries = suggestedQueries,
            uiState = uiState
        )

        bindFilters(uiState = uiState)

        bindSearchResults(
            searchResultsAdapter = searchResultsAdapter,
            searchResultsData = searchResultsData,
            uiState = uiState
        )
    }

    private fun ActivitySearchBinding.bindSearchBox(
        suggestedQueriesAdapter: SuggestedMediaAdapter,
        suggestedQueries: LiveData<List<SuggestedMedia>>,
        uiState: StateFlow<SearchUIState>
    ) {
        // Search box text listener
        searchBox.addTextChangedListener {
            // Delay 500 ms before getting suggested media
            suggestingJob?.cancel()
            suggestingJob = lifecycleScope.launch {
                // Delay for 500 ms to check if user has stopped typing
                delay(500)

                // Change the adapter of the results container and load the data
                updateSuggestedKeywords()
            }
        }

        // Search box enter/key listener
        searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                updateSearchResults()
                searchBoxFocusEraser()
                true
            }
            else {
                false
            }
        }
        searchBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateSearchResults()
                searchBoxFocusEraser()
                true
            }
            else {
                false
            }
        }

        // Search box isFocused listener
        searchBox.setOnFocusChangeListener { _, hasFocus ->
            updateTypingStatus()

            if (hasFocus) {
                toggleSearchResultsContainer(false)
            } else {
                searchBoxFocusEraser()
                toggleSearchResultsContainer(true)
            }
        }

        lifecycleScope.launch {
            // Run only on destroyed state
            repeatOnLifecycle(Lifecycle.State.DESTROYED) {
                uiState.collect { state ->
                    if(state.isTyping) {
                        searchBox.requestFocus()
                        searchBox.setText(state.lastCharactersTyped)
                    }
                    else {
                        searchBox.setText(state.query)
                    }
                }
            }
        }

        suggestedQueries.observe(this@SearchActivity) {
            suggestedQueriesAdapter.submitList(it)
        }
    }

    private fun ActivitySearchBinding.bindFilters(uiState: StateFlow<SearchUIState>) {
        val disabledFilterElevation = resources.getDimension(R.dimen.dp1)
        val enabledFilterTint = resources.getDimension(R.dimen.filterTypeEnabled)

        movieFilterButton.setOnSingleClickListener {
            if(tvShowFilterButton.cardElevation != disabledFilterElevation) {
                if(movieFilterButton.cardElevation == disabledFilterElevation) {
                    updateFilters(SearchFilterType.MoviesAndTvShows)
                    return@setOnSingleClickListener
                }

                updateFilters(SearchFilterType.TvShows)
                return@setOnSingleClickListener
            }

            updateFilters(SearchFilterType.Movies)
        }
        tvShowFilterButton.setOnSingleClickListener {
            if(movieFilterButton.cardElevation != disabledFilterElevation) {
                if(tvShowFilterButton.cardElevation == disabledFilterElevation) {
                    updateFilters(SearchFilterType.MoviesAndTvShows)
                    return@setOnSingleClickListener
                }

                updateFilters(SearchFilterType.Movies)
                return@setOnSingleClickListener
            }

            updateFilters(SearchFilterType.TvShows)
        }
        userFilterButton.setOnSingleClickListener {
            updateFilters(SearchFilterType.UserProfiles)
        }

        val filterType = uiState
            .map { it.filters }
            .distinctUntilChanged()

        lifecycleScope.launch {
            filterType.collectLatest { type ->
                println("Type is: $type")
                when(type) {
                    SearchFilterType.Movies -> {
                        movieFilterButton.cardElevation = enabledFilterTint
                        tvShowFilterButton.cardElevation = disabledFilterElevation
                        userFilterButton.cardElevation = disabledFilterElevation
                    }
                    SearchFilterType.TvShows -> {
                        movieFilterButton.cardElevation = disabledFilterElevation
                        tvShowFilterButton.cardElevation = enabledFilterTint
                        userFilterButton.cardElevation = disabledFilterElevation
                    }
                    SearchFilterType.MoviesAndTvShows -> {
                        movieFilterButton.cardElevation = enabledFilterTint
                        tvShowFilterButton.cardElevation = enabledFilterTint
                        userFilterButton.cardElevation = disabledFilterElevation
                    }
                    SearchFilterType.UserProfiles -> {
                        movieFilterButton.cardElevation = disabledFilterElevation
                        tvShowFilterButton.cardElevation = disabledFilterElevation
                        userFilterButton.cardElevation = enabledFilterTint
                    }
                }
            }
        }
    }

    private fun ActivitySearchBinding.bindSearchResults(
        searchResultsAdapter: SearchResultsAdapter,
        searchResultsData: LiveData<List<MediaInfo>>,
        uiState: StateFlow<SearchUIState>
    ) {
        val userHasNotSearched = uiState
            .map { it.hasNotSearchedBefore }
            .distinctUntilChanged()

        val currentPage = uiState
            .map { it.lastPageQueried }
            .distinctUntilChanged()

        val totalPagesToSurf = uiState
            .map { it.maxPages }
            .distinctUntilChanged()

        val totalResultsObtained = uiState
            .map { it.totalResults }
            .distinctUntilChanged()

        val isLoading = uiState
            .map { it.isLoading }
            .distinctUntilChanged()

        lifecycleScope.launch {
            // Search indicator collector
            launch {
                userHasNotSearched.combine(totalResultsObtained) { hasNotSearched, results ->
                    // -1 results means it is searching
                    if(results == -1) {
                        changeSearchIndicator(isLoading = true)
                    }
                    else {
                        changeSearchIndicator(hasNotSearched = hasNotSearched, resultsCount = results)
                    }
                }.collect()
            }

            // Page number collector
            launch {
                currentPage.combine(totalPagesToSurf) { pageCount, maxPages ->
                    pageNumber.text = pageCount.toString()

                    if(pageCount <= 1) {
                        previousPageImg.setTint(R.color.colorOnMediumEmphasis)
                        previousPageBtn.isClickable = false
                    }
                    else if(pageCount == 2) {
                        previousPageImg.setTint(R.color.colorOnPrimary)
                        previousPageBtn.isClickable = true
                    }

                    if(pageCount >= maxPages) {
                        nextPageImg.setTint(R.color.colorOnMediumEmphasis)
                        nextPageBtn.isClickable = false
                    }
                    else {
                        nextPageImg.setTint(R.color.colorOnPrimary)
                        nextPageBtn.isClickable = true
                    }
                }.collect()
            }

            // Loading collector
            launch {
                isLoading.collect {
                    when(it) {
                        null -> {
                            loadingBar.deactivate(
                                container = loadingContainer,
                                causeIsError = true,
                                listener = this@SearchActivity
                            )
                            changeSearchIndicator(hasErrors = true)
                        }
                        true -> {
                            searchResultsLayoutContainer.visibility = View.GONE
                            loadingBar.activate(container = loadingContainer)
                        }
                        false -> {
                            loadingBar.deactivate(container = loadingContainer, animate = false)
                            searchResultsLayoutContainer.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        nextPageBtn.setOnSingleClickListener { updatePage(goingNext = true) }
        previousPageBtn.setOnSingleClickListener { updatePage(goingNext = false) }

        searchResultsData.observe(this@SearchActivity) { data ->
            searchRecyclerView.scrollToPosition(0)
            searchResultsAdapter.submitList(data ?: emptyList())
        }
    }

    private fun ActivitySearchBinding.updateSearchResults() {
        searchBox.text?.trim().let {
            if (it?.isNotEmpty() == true) {
                searchRecyclerView.scrollToPosition(0)
                searchViewModel.onEvent(SearchUIAction.Search(query = it.toString()))
            }
        }
    }

    private fun ActivitySearchBinding.updateSuggestedKeywords() {
        searchBox.text?.trim().let {
            if (it?.isNotEmpty() == true) {
                searchViewModel.onEvent(SearchUIAction.Typing(charactersTyped = it.toString()))
            }
        }
    }

    private fun ActivitySearchBinding.updateTypingStatus() {
        searchViewModel.onEvent(SearchUIAction.FocusOnSearchBox(searchBox.hasFocus()))
    }

    private fun ActivitySearchBinding.updatePage(goingNext: Boolean) {
        val pageNumber = pageNumber.text.toString().toInt()
        if(goingNext) {
            searchViewModel.onEvent(SearchUIAction.ChangePage(pageNumber + 1))
        }
        // Clicked previous button
        else {
            searchViewModel.onEvent(SearchUIAction.ChangePage(pageNumber - 1))
        }
    }

    private fun updateFilters(filterType: SearchFilterType) {
        searchViewModel.onEvent(SearchUIAction.Filter(filterType))
    }

    private fun getStatusBarHeight(): Int {
        val statusBarHeightId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        return Resources.getSystem().getDimensionPixelSize(statusBarHeightId)
    }

    private fun ImageView.setTint(@ColorRes color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(this, null)
        } else {
            ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(ContextCompat.getColor(context, color)))
        }
    }
}