package com.xcape.movie_logger.presentation.search.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.xcape.movie_logger.databinding.ItemSuggestedKeywordBinding
import com.xcape.movie_logger.domain.model.media.SuggestedMedia
import com.xcape.movie_logger.presentation.search.OnSuggestionClickListener
import com.xcape.movie_logger.presentation.search.viewholders.SuggestedMediaViewHolder

class SuggestedMediaAdapter(
    private val listener: OnSuggestionClickListener
) : ListAdapter<SuggestedMedia, SuggestedMediaViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedMediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SuggestedMediaViewHolder(ItemSuggestedKeywordBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: SuggestedMediaViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(
            item = item,
            listener = listener
        )
    }
    
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<SuggestedMedia>() {
            override fun areItemsTheSame(oldItem: SuggestedMedia, newItem: SuggestedMedia): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: SuggestedMedia, newItem: SuggestedMedia): Boolean {
                return oldItem.l == newItem.l
            }
        }
    }
}