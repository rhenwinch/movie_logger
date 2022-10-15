package com.xcape.movie_logger.presentation.trending.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.xcape.movie_logger.domain.factory.ChartMediaTypeFactory
import com.xcape.movie_logger.domain.model.base.BaseChartMedia
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.common.BaseViewHolder

class TrendingPagingAdapter (
    private val chartMediaTypeFactory: ChartMediaTypeFactory,
    private val listener: OnMediaClickListener
) : PagingDataAdapter<BaseChartMedia, BaseViewHolder<BaseChartMedia>>(COMPARATOR) {

    override fun onBindViewHolder(holder: BaseViewHolder<BaseChartMedia>, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(
            item = item,
            listener = listener
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<BaseChartMedia> {
        val inflater = LayoutInflater.from(parent.context)
        @Suppress("UNCHECKED_CAST")
        return chartMediaTypeFactory.getChartMediaViewHolder(inflater, parent, viewType) as BaseViewHolder<BaseChartMedia>
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (item != null) {
            return item.getMediaType(chartMediaTypeFactory)
        }
        return super.getItemViewType(position)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<BaseChartMedia>() {
            override fun areItemsTheSame(oldItem: BaseChartMedia, newItem: BaseChartMedia): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: BaseChartMedia, newItem: BaseChartMedia): Boolean {
                return oldItem.equals(newItem)
            }
        }
    }
}