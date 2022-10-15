package com.xcape.movie_logger.presentation.search.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.databinding.ItemSearchBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.presentation.common.BaseViewHolder
import com.xcape.movie_logger.presentation.common.OnMediaClickListener

class SearchResultsViewHolder(
    binding: ItemSearchBinding
) : BaseViewHolder<MediaInfo>(binding) {
    private val searchMovieThumbnailContainer = binding.searchMovieThumbnailContainer
    private val searchCardBase = binding.searchCardBase
    private val searchMovieThumbnail = binding.searchMovieThumbnail
    private val searchMovieTitle = binding.searchMovieTitle
    private val searchMovieExtraDetails = binding.searchMovieExtraDetails
    private val searchMovieRatingBar = binding.searchMovieRatingBar

    override fun <ClickListener> bind(item: MediaInfo?, position: Int?, listener: ClickListener?) {
        if(item == null)
            return

        val imageUrl = item.gallery.thumbnail ?: item.gallery.poster
        val starRatings = ((item.rating / 10) * 5).toFloat()
        var movieExtraDetails = listOf(item.duration, item.year).joinToString(" | ")
        if(item.certificate != null)
            movieExtraDetails = "${item.certificate} | $movieExtraDetails"

        Picasso.get()
            .load(imageUrl.replace("_V1_", "_SL250_"))
            .fit()
            .centerInside()
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .into(searchMovieThumbnail)

        searchMovieTitle.text = item.title
        searchMovieRatingBar.rating = starRatings
        searchMovieExtraDetails.text = movieExtraDetails

        if(listener == null)
            return

        searchMovieThumbnailContainer.setOnClickListener { _ ->
            (listener as OnMediaClickListener).onMediaClick(
                mediaCategory = "search-results",
                mediaId = item.id,
                mediaImageView = searchMovieThumbnail,
                mediaImageCard = searchMovieThumbnailContainer
            )
        }
        searchCardBase.setOnClickListener { _ ->
            (listener as OnMediaClickListener).onMediaClick(
                mediaCategory = "search-results",
                mediaId = item.id,
                mediaImageView = searchMovieThumbnail,
                mediaImageCard = searchMovieThumbnailContainer
            )
        }
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): SearchResultsViewHolder {
            val binding = ItemSearchBinding.inflate(inflater, parent, false)
            return SearchResultsViewHolder(binding)
        }
    }
}