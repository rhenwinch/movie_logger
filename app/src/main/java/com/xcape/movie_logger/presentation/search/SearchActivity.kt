package com.xcape.movie_logger.presentation.search

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnPreDraw
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
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
import com.xcape.movie_logger.domain.utils.Functions.toByteArray
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
    private lateinit var loadingBarContainer: View

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
        loadingBarContainer = binding.searchLoadingContainer.root

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.bindState(
            uiState = searchViewModel.state,
            uiActions = searchViewModel.accept,
            searchResultsData = searchViewModel.searchResultsData,
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
        binding.updateSearchResults(searchViewModel.accept)
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

    private fun ActivitySearchBinding.changeSearchIndicator(hasNotSearched: Boolean) {
        val recommendedText = "recommended"
        val searchingText = "search results"

        searchIndicator.text = if (hasNotSearched) {
            recommendedText
        }
        else {
            searchingText
        }
    }

    private fun ActivitySearchBinding.bindState(
        uiState: StateFlow<SearchUIState>,
        uiActions: (SearchUIAction) -> Unit,
        suggestedQueries: LiveData<List<SuggestedMedia>>,
        searchResultsData: Flow<PagingData<MediaInfo>>
    ) {
        val searchResultsAdapter = SearchResultsAdapter(this@SearchActivity)
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
            uiState = uiState,
            queryChangeCallback = uiActions
        )

        bindSearchResults(
            searchResultsAdapter = searchResultsAdapter,
            searchResultsData = searchResultsData,
            uiState = uiState,
            scrollChangeCallback = uiActions
        )
    }

    private fun ActivitySearchBinding.bindSearchBox(
        suggestedQueriesAdapter: SuggestedMediaAdapter,
        suggestedQueries: LiveData<List<SuggestedMedia>>,
        uiState: StateFlow<SearchUIState>,
        queryChangeCallback: (SearchUIAction) -> Unit
    ) {
        // Search box text listener
        searchBox.addTextChangedListener {
            // Delay 500 ms before getting suggested media
            suggestingJob?.cancel()
            suggestingJob = lifecycleScope.launch {
                // Delay for 500 ms to check if user has stopped typing
                delay(500)

                // Change the adapter of the results container and load the data
                updateSuggestedKeywords(queryChangeCallback)
            }
        }

        // Search box enter/key listener
        searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                updateSearchResults(queryChangeCallback)
                searchBoxFocusEraser()
                true
            }
            else {
                false
            }
        }
        searchBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateSearchResults(queryChangeCallback)
                searchBoxFocusEraser()
                true
            }
            else {
                false
            }
        }

        // Search box isFocused listener
        searchBox.setOnFocusChangeListener { _, hasFocus ->
            updateTypingStatus(queryChangeCallback)

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

    private fun ActivitySearchBinding.updateSearchResults(onQuerySubmit: (SearchUIAction.Search) -> Unit) {
        searchBox.text?.trim().let {
            if (it?.isNotEmpty() == true) {
                searchRecyclerView.scrollToPosition(0)
                onQuerySubmit(SearchUIAction.Search(query = it.toString()))
            }
        }
    }

    private fun ActivitySearchBinding.updateSuggestedKeywords(onSuggestionQueryChange: (SearchUIAction.Typing) -> Unit) {
        searchBox.text?.trim().let {
            if (it?.isNotEmpty() == true) {
                onSuggestionQueryChange(SearchUIAction.Typing(charactersTyped = it.toString()))
            }
        }
    }

    private fun ActivitySearchBinding.updateTypingStatus(onSearchBoxFocusChange: (SearchUIAction.FocusOnSearchBox) -> Unit) {
        onSearchBoxFocusChange(SearchUIAction.FocusOnSearchBox(searchBox.hasFocus()))
    }

    private fun ActivitySearchBinding.bindSearchResults(
        searchResultsAdapter: SearchResultsAdapter,
        searchResultsData: Flow<PagingData<MediaInfo>>,
        uiState: StateFlow<SearchUIState>,
        scrollChangeCallback: (SearchUIAction.Scroll) -> Unit
    ) {
        searchRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // If the y coordinate is not on the initial position anymore
                if(dy != 0)
                    scrollChangeCallback(SearchUIAction.Scroll(currentQuery = uiState.value.query))
            }
        })

        val userHasSearched = uiState
            .map { it.hasNotSearchedBefore }
            .distinctUntilChanged()

        val hasNotScrolledForCurrentSearch = uiState
            .map { it.hasNotScrolledForCurrentSearch }
            .distinctUntilChanged()

        val shouldScrollToTop = hasNotScrolledForCurrentSearch
            .distinctUntilChanged()

        lifecycleScope.launch {
            userHasSearched.collect { hasSearched ->
                changeSearchIndicator(hasSearched)
            }
        }

        lifecycleScope.launch {
            searchResultsData.collectLatest(searchResultsAdapter::submitData)
        }

        lifecycleScope.launch {
            shouldScrollToTop.collect { shouldScroll ->
                if (shouldScroll)
                    searchRecyclerView.scrollToPosition(0)
            }
        }
    }

    private fun getStatusBarHeight(): Int {
        val statusBarHeightId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        return Resources.getSystem().getDimensionPixelSize(statusBarHeightId)
    }
}