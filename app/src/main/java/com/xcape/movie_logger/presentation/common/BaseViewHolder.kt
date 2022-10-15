package com.xcape.movie_logger.presentation.common

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.xcape.movie_logger.presentation.search.OnSuggestionClickListener

abstract class BaseViewHolder<in T>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract fun <ClickListener>bind(
        item: T?,
        position: Int? = null,
        listener: ClickListener? = null
    )
}