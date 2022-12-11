package com.xcape.movie_logger.presentation.search.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.xcape.movie_logger.databinding.ItemSuggestedKeywordBinding
import com.xcape.movie_logger.domain.model.media.SuggestedMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.search.OnSuggestionClickListener

class SuggestedMediaViewHolder(
    binding: ItemSuggestedKeywordBinding
) : BaseViewHolder<SuggestedMedia>(binding) {
    private val keywordTextView = binding.keywordTextView
    private val rootView = binding.root

    override fun <ClickListener> bind(
        item: SuggestedMedia?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        keywordTextView.text = item.l

        if(listener == null)
            return

        rootView.setOnSingleClickListener {
            (listener as OnSuggestionClickListener).onSuggestionClick(item.l)
        }
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): SuggestedMediaViewHolder {
            val binding = ItemSuggestedKeywordBinding.inflate(inflater, parent, false)
            return SuggestedMediaViewHolder(binding)
        }
    }
}