package com.xcape.movie_logger.presentation.watchlist

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.DialogSortBinding
import com.xcape.movie_logger.presentation.common.OnDialogDismissListener

const val WATCHLIST_SORT_DIALOG_TAG = "watchlist_sort_dialog"
const val WATCHLIST_SORT_TYPE = "watchlist_sort_type"
const val WATCHLIST_FILTER_TYPE = "watchlist_filter_type"
const val WATCHLIST_VIEW_TYPE = "watchlist_view_type"

class WatchlistSortDialog : BottomSheetDialogFragment() {
    private var _binding: DialogSortBinding? = null
    private val binding: DialogSortBinding
        get() = _binding!!

    var sortConfigListener: OnSortDialogListener? = null
    var onDialogDismissListener: OnDialogDismissListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DialogSortBinding.inflate(layoutInflater)

        arguments.let { bundle ->
            val sortType = bundle?.getInt(WATCHLIST_SORT_TYPE)
            val filterType = bundle?.getInt(WATCHLIST_FILTER_TYPE)
            val viewType = bundle?.getInt(WATCHLIST_VIEW_TYPE)
            binding.bindRadioGroups(
                sortType = sortType,
                filterType = filterType,
                viewType = viewType
            )
        }
        binding.bindRadioGroupsListeners()

        return binding.root
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogDismissListener?.onDismissDialog(WATCHLIST_SORT_DIALOG_TAG)
    }

    private fun DialogSortBinding.bindRadioGroups(
        sortType: Int?,
        filterType: Int?,
        viewType: Int?
    ) {
        if(sortType == null || filterType == null || viewType == null)
            return

        val sortTypeId = when(sortType) {
            SortType.DESCENDING.ordinal -> R.id.sortDescending
            SortType.ASCENDING.ordinal -> R.id.sortAscending
            else -> -1
        }
        val filterTypeId = when(filterType) {
            FilterType.POPULAR.ordinal -> R.id.filterPopularity
            FilterType.DATE_ADDED.ordinal -> R.id.filterDateAdded
            FilterType.DATE_RELEASED.ordinal -> R.id.filterDateReleased
            else -> -1
        }
        val viewTypeId = when(viewType) {
            ViewType.DETAILED.ordinal -> R.id.viewTypeDetailed
            ViewType.COMPACT.ordinal -> R.id.viewTypeCompact
            else -> -1
        }

        sortRadioButtonGroup.check(sortTypeId)
        filterRadioButtonGroup.check(filterTypeId)
        viewTypeRadioButtonGroup.check(viewTypeId)
    }

    private fun DialogSortBinding.bindRadioGroupsListeners() {
        sortRadioButtonGroup.setOnCheckedChangeListener { _, _ ->
            sortConfigListener?.onUpdateSortConfig(
                checkedFilterType = filterRadioButtonGroup.checkedRadioButtonId,
                checkedSortType = sortRadioButtonGroup.checkedRadioButtonId,
                checkedViewType = viewTypeRadioButtonGroup.checkedRadioButtonId
            )
        }
        filterRadioButtonGroup.setOnCheckedChangeListener { _, _ ->
            sortConfigListener?.onUpdateSortConfig(
                checkedFilterType = filterRadioButtonGroup.checkedRadioButtonId,
                checkedSortType = sortRadioButtonGroup.checkedRadioButtonId,
                checkedViewType = viewTypeRadioButtonGroup.checkedRadioButtonId
            )
        }
        viewTypeRadioButtonGroup.setOnCheckedChangeListener { _, _ ->
            sortConfigListener?.onUpdateSortConfig(
                checkedFilterType = filterRadioButtonGroup.checkedRadioButtonId,
                checkedSortType = sortRadioButtonGroup.checkedRadioButtonId,
                checkedViewType = viewTypeRadioButtonGroup.checkedRadioButtonId
            )
        }
    }

    private fun setupBottomSheet(dialogInterface: DialogInterface) {
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}