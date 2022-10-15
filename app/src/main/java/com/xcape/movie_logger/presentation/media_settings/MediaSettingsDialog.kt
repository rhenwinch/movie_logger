package com.xcape.movie_logger.presentation.media_settings

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
import com.xcape.movie_logger.databinding.DialogMediaSettingsBinding
import com.xcape.movie_logger.presentation.common.OnDialogDismissListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener

const val MEDIA_SETTINGS_DIALOG_TAG = "media_settings_dialog"

interface OnUpdateMediaItemListener {
    fun onAddMedia()

    fun onDeleteMedia()
}

class MediaSettingsDialog : BottomSheetDialogFragment() {
    private var _binding: DialogMediaSettingsBinding? = null
    private val binding: DialogMediaSettingsBinding
        get() = _binding!!

    // Listeners
    var dialogDismissListener: OnDialogDismissListener? = null
    var mediaSettingModifyListener: OnUpdateMediaItemListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DialogMediaSettingsBinding.inflate(layoutInflater)

        binding.bindSettings()

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogDismissListener?.onDismissDialog(MEDIA_SETTINGS_DIALOG_TAG)
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

    private fun DialogMediaSettingsBinding.bindSettings() {
        addToWatchedList.setOnSingleClickListener {
            mediaSettingModifyListener?.onAddMedia()
            this@MediaSettingsDialog.dismiss()
        }
        deleteItem.setOnSingleClickListener {
            mediaSettingModifyListener?.onDeleteMedia()
            this@MediaSettingsDialog.dismiss()
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