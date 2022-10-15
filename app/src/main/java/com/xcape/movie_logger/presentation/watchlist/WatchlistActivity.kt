package com.xcape.movie_logger.presentation.watchlist

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.view.*
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivityWatchlistBinding
import com.xcape.movie_logger.presentation.common.OnDialogDismissListener
import com.xcape.movie_logger.domain.model.media.WatchlistMedia
import com.xcape.movie_logger.presentation.add_item.ADD_ITEM_DIALOG_TAG
import com.xcape.movie_logger.presentation.add_item.AddItemDialog
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.media_settings.MEDIA_SETTINGS_DIALOG_TAG
import com.xcape.movie_logger.presentation.media_settings.MediaSettingsDialog
import com.xcape.movie_logger.presentation.media_settings.OnUpdateMediaItemListener
import com.xcape.movie_logger.presentation.movie_details.MEDIA_BITMAP
import com.xcape.movie_logger.presentation.movie_details.MEDIA_CATEGORY
import com.xcape.movie_logger.presentation.movie_details.MEDIA_ID
import com.xcape.movie_logger.presentation.movie_details.MovieActivity
import com.xcape.movie_logger.presentation.watchlist.adapter.WatchlistAdapter
import com.xcape.movie_logger.domain.utils.Functions.toByteArray
import com.xcape.movie_logger.presentation.common.OnMediaLongClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface WatchlistMediaClickListener {
    fun onWatchlistMediaClick(
        mediaCategory: String,
        mediaId: String,
        mediaImageView: ImageView,
        mediaImageCard: MaterialCardView
    )

    fun onAddItemDialogToggle(
        position: Int?,
        mediaId: String
    )
}

@AndroidEntryPoint
class WatchlistActivity : AppCompatActivity(), WatchlistMediaClickListener, OnSortDialogListener,
    OnDialogDismissListener, OnUpdateMediaItemListener, AddItemDialog.OnAddConfirmListener {
    private var _binding: ActivityWatchlistBinding? = null
    private val binding: ActivityWatchlistBinding
        get() = _binding!!

    // View Model
    private val watchlistViewModel: WatchlistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowAnimations()

        // Inflate the layout for this fragment
        _binding = ActivityWatchlistBinding.inflate(layoutInflater)

        setSupportActionBar(binding.watchlistToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.bindState(
            uiState = watchlistViewModel.state,
            watchlistData = watchlistViewModel.watchlistData
        )

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_sort_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sortButton) {
            toggleSortSettingDialog(
                hasClicked = true,
                watchlistViewModel.accept
            )
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onWatchlistMediaClick(
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

            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                mediaImageCard,
                "${mediaId}-${mediaCategory}"
            )

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    override fun onAddItemDialogToggle(
        position: Int?,
        mediaId: String
    ) {
        toggleMediaSettingsDialog(
            hasClicked = true,
            itemId = mediaId,
            itemPosition = position,
            onItemModify = watchlistViewModel.accept
        )
    }

    override fun onDismissDialog(tag: String) {
        val actionCallback = watchlistViewModel.accept
        
        when(tag) {
            WATCHLIST_SORT_DIALOG_TAG -> {
                toggleSortSettingDialog(
                    hasClicked = false,
                    onFilterChange = actionCallback
                )
            }
            ADD_ITEM_DIALOG_TAG -> {
                toggleAddItemDialog(
                    hasClicked = false,
                    onItemAdd = actionCallback
                )
            }
            MEDIA_SETTINGS_DIALOG_TAG -> {
                toggleMediaSettingsDialog(
                    hasClicked = false,
                    onItemModify = actionCallback
                )
            }
        }
    }

    override fun onUpdateSortConfig(
        checkedSortType: Int,
        checkedFilterType: Int,
        checkedViewType: Int
    ) {
        val (sortImageRotation, sortType) = when (checkedSortType) {
            R.id.sortDescending -> Pair(180F, SortType.DESCENDING.ordinal)
            R.id.sortAscending -> Pair(0F, SortType.ASCENDING.ordinal)
            else -> Pair(0F, -1)
        }

        val (filterName, filterType) = when (checkedFilterType) {
            R.id.filterPopularity -> Pair(R.string.popularity, FilterType.POPULAR.ordinal)
            R.id.filterDateAdded -> Pair(R.string.date_added, FilterType.DATE_ADDED.ordinal)
            R.id.filterDateReleased -> Pair(R.string.date_released, FilterType.DATE_RELEASED.ordinal)
            else -> Pair(0, -1)
        }

        val viewType = when (checkedViewType) {
            R.id.viewTypeDetailed -> ViewType.DETAILED.ordinal
            R.id.viewTypeCompact -> ViewType.COMPACT.ordinal
            else -> -1
        }

        watchlistViewModel.accept(
            WatchlistUIAction.Sort(
                sortType = sortType,
                filterType = filterType,
                viewType = viewType
            )
        )

        binding.filterTypeChosen.text = applicationContext.resources.getString(filterName)
        binding.sortTypeChosen.rotation = sortImageRotation
    }

    override fun onAddMedia() {
        toggleAddItemDialog(
            hasClicked = true,
            onItemAdd = watchlistViewModel.accept
        )
    }

    override fun onAddConfirm(mediaId: String?) {
        lifecycleScope.launch {
            watchlistViewModel.watchlistData.collect {
                mediaId?.let { id ->
                    val adapter = binding.watchlistRecyclerView.adapter as WatchlistAdapter
                    adapter.deleteItem(adapter.getItemPositionByProperty(id))
                }
                cancel()
            }
        }
    }

    override fun onDeleteMedia() {
        binding.deleteItem(
            uiState = watchlistViewModel.state,
            onItemDelete = watchlistViewModel.accept
        )
    }

    private fun ActivityWatchlistBinding.bindState(
        uiState: StateFlow<WatchlistUIState>,
        watchlistData: Flow<List<WatchlistMedia>>
    ) {
        val watchlistDetailedAdapter = WatchlistAdapter(ViewType.DETAILED.ordinal, this@WatchlistActivity)
        val watchlistCompactAdapter = WatchlistAdapter(ViewType.COMPACT.ordinal, this@WatchlistActivity)
        watchlistRecyclerView.adapter = watchlistDetailedAdapter
        watchlistRecyclerView.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )

        bindList(
            uiState = uiState,
            watchlistData = watchlistData,
            watchlistDetailedAdapter = watchlistDetailedAdapter,
            watchlistCompactAdapter = watchlistCompactAdapter
        )

        bindSortSettingsDialog(uiState = uiState)
        bindAddItemDialog(uiState = uiState)
        bindMediaSettingsDialog(uiState = uiState)
    }

    private fun ActivityWatchlistBinding.bindList(
        uiState: StateFlow<WatchlistUIState>,
        watchlistData: Flow<List<WatchlistMedia>>,
        watchlistDetailedAdapter: WatchlistAdapter,
        watchlistCompactAdapter: WatchlistAdapter
    ) {
        lifecycleScope.launch {
            watchlistData.collect { data ->
                if(data.isEmpty()) {
                    emptyListIndicator.visibility = View.VISIBLE
                    watchlistDataContainer.visibility = View.GONE
                }
                else {
                    emptyListIndicator.visibility = View.GONE
                    watchlistDataContainer.visibility = View.VISIBLE

                    watchlistDetailedAdapter.submitList(data)
                    watchlistCompactAdapter.submitList(data)
                }
            }
        }

        val viewType = uiState
            .map { it.viewType }
            .distinctUntilChanged()

        lifecycleScope.launch {
            // Submit list to adapter
            viewType.collect { type ->
                when (type) {
                    ViewType.DETAILED.ordinal -> watchlistRecyclerView.adapter =
                        watchlistDetailedAdapter
                    ViewType.COMPACT.ordinal -> watchlistRecyclerView.adapter =
                        watchlistCompactAdapter
                }
            }
        }

        filterTypeChosen.setOnSingleClickListener {
            toggleSortSettingDialog(
                hasClicked = true,
                watchlistViewModel.accept
            )
        }
    }

    private fun bindSortSettingsDialog(uiState: StateFlow<WatchlistUIState>) {
        val sortSettingsDialog = WatchlistSortDialog()
        sortSettingsDialog.sortConfigListener = this@WatchlistActivity
        sortSettingsDialog.onDialogDismissListener = this@WatchlistActivity

        val isThisDialogOpened = uiState
            .map { it.isEditingSortConfig }
            .distinctUntilChanged()

        lifecycleScope.launch {
            // Subscribe to sort config changes
            launch {
                uiState.collect {
                    sortSettingsDialog.apply {
                        arguments = Bundle().apply {
                            putInt(WATCHLIST_SORT_TYPE, it.sortType!!)
                            putInt(WATCHLIST_FILTER_TYPE, it.filterType!!)
                            putInt(WATCHLIST_VIEW_TYPE, it.viewType!!)
                        }
                    }
                }
            }

            // Subscribe to when bottom sheet card should show
            launch {
                isThisDialogOpened.collect {
                    if (it) {
                        sortSettingsDialog.show(supportFragmentManager, WATCHLIST_SORT_DIALOG_TAG)
                    }
                }
            }
        }
    }

    private fun bindAddItemDialog(uiState: StateFlow<WatchlistUIState>) {
        val itemToAdd = uiState
            .map { it.lastItemUpdated }
            .distinctUntilChanged()

        val isAddingItem = uiState
            .map { it.isAddingItem }
            .distinctUntilChanged()

        val combinedFlow = combine(itemToAdd, isAddingItem, ::Pair)

        // Subscribe to when add item sheet should open
        lifecycleScope.launch {
            combinedFlow.collectLatest { (item, shouldShow) ->
                if (shouldShow && item != null) {
                    val addItemOnWatchlistDialog = AddItemDialog()
                    addItemOnWatchlistDialog.dialogDismissListener = this@WatchlistActivity
                    addItemOnWatchlistDialog.onAddConfirmListener = this@WatchlistActivity
                    addItemOnWatchlistDialog.apply {
                        arguments = Bundle().apply {
                            putString(MEDIA_ID, item)
                        }
                    }
                    addItemOnWatchlistDialog.show(supportFragmentManager, ADD_ITEM_DIALOG_TAG)
                }
            }
        }
    }

    private fun bindMediaSettingsDialog(uiState: StateFlow<WatchlistUIState>) {
        val mediaSettingsDialog = MediaSettingsDialog()
        mediaSettingsDialog.dialogDismissListener = this@WatchlistActivity
        mediaSettingsDialog.mediaSettingModifyListener = this@WatchlistActivity

        val isThisDialogOpened = uiState
            .map { it.isModifyingItem }
            .distinctUntilChanged()

        // Subscribe to when add item sheet should open
        lifecycleScope.launch {
            isThisDialogOpened.collect {
                if(it) {
                    mediaSettingsDialog.show(supportFragmentManager, MEDIA_SETTINGS_DIALOG_TAG)
                }
            }
        }
    }

    private fun toggleSortSettingDialog(
        hasClicked: Boolean,
        onFilterChange: (WatchlistUIAction.Filter) -> Unit
    ) = onFilterChange(WatchlistUIAction.Filter(isClicked = hasClicked))

    private fun toggleAddItemDialog(
        hasClicked: Boolean,
        onItemAdd: (WatchlistUIAction.Add) -> Unit
    ) = onItemAdd(WatchlistUIAction.Add(isClicked = hasClicked))

    private fun toggleMediaSettingsDialog(
        hasClicked: Boolean,
        itemId: String? = null,
        onItemModify: (WatchlistUIAction.Modify) -> Unit,
        itemPosition: Int? = null
    ) = onItemModify(WatchlistUIAction.Modify(isClicked = hasClicked, itemId = itemId, itemPosition = itemPosition))

    private fun ActivityWatchlistBinding.deleteItem(
        uiState: StateFlow<WatchlistUIState>,
        onItemDelete: (WatchlistUIAction.Delete) -> Unit
    ) {
        val adapter = watchlistRecyclerView.adapter as WatchlistAdapter
        val itemPosition = uiState
            .map { it.lastItemPositionUpdated }
            .distinctUntilChanged()

        lifecycleScope.launch {
            itemPosition.collect { position ->
                position?.let {
                    val item = adapter.getItem(it)
                    adapter.deleteItem(it)

                    onItemDelete(WatchlistUIAction.Delete(
                        isConfirmed = true,
                        trashBin = item
                    ))

                    showUndoConfirmation(
                        message = "\"${item?.title}\" is now deleted",
                        undoCallback = {
                            onItemDelete(WatchlistUIAction.Delete(
                                isConfirmed = false,
                                trashBin = item
                            ))

                            adapter.addItem(it, item!!)
                        },
                        doneCallback = { onItemDelete(WatchlistUIAction.Delete()) }
                    )

                    cancel()
                }
            }
        }
    }

    private fun ActivityWatchlistBinding.showUndoConfirmation(
        message: String,
        length: Int = Snackbar.LENGTH_SHORT,
        undoCallback: () -> Unit,
        doneCallback: () -> Unit
    ) {
        Snackbar.make(root, message, length)
            .setAction("Undo") {
                undoCallback()
            }
            .addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    doneCallback()
                }
            })
            .show()
    }

    private fun setupWindowAnimations() {
        val slide = Slide(Gravity.END)
        slide.duration = 200

        window.enterTransition = slide
        window.exitTransition = slide
    }
}