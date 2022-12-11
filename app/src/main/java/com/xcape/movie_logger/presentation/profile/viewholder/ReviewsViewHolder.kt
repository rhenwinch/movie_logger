package com.xcape.movie_logger.presentation.profile.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemReviewsBinding
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.presentation.common.BaseViewHolder

class ReviewsViewHolder(binding: ItemReviewsBinding) : BaseViewHolder<WatchedMedia>(binding) {
    private val mediaImage = binding.mediaImage
    private val mediaRatingGiven = binding.reviewCommentHeadline
    private val mediaTitleWithYear = binding.reviewTitleSubHeadline

    override fun <ClickListener> bind(
        item: WatchedMedia?,
        position: Int?,
        listener: ClickListener?
    ) {
        if(item == null)
            return

        val titleWithYear = "${item.title} (${item.mediaInfo?.year})"
        val ratingGiven = "Gave ${item.rating} stars!"

        Picasso.get()
            .load(item.mediaInfo?.gallery?.poster)
            .fit()
            .centerInside()
            .into(mediaImage)

        mediaTitleWithYear.text = titleWithYear
        mediaRatingGiven.text = ratingGiven
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ReviewsViewHolder {
            val binding = ItemReviewsBinding.inflate(inflater, parent, false)
            return ReviewsViewHolder(binding)
        }
    }
}