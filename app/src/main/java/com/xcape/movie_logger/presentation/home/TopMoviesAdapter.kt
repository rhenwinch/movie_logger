package com.xcape.movie_logger.presentation.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.xcape.movie_logger.R
import com.xcape.movie_logger.domain.model.media.MediaInfo


class TopMoviesAdapter(
    private val context: Context?,
    private val listOfMovies: MutableList<MediaInfo>
) : RecyclerView.Adapter<TopMoviesAdapter.TopMoviesViewHolder>() {

    class TopMoviesViewHolder(
        private val context: Context?,
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val movieImageViewFront: ImageView = itemView.findViewById(R.id.movieImageFront)
        private val movieImageViewBack: ImageView = itemView.findViewById(R.id.movieImageBack)
        private val movieTitleTextView: TextView = itemView.findViewById(R.id.movieTitleBack)
        private val movieSypnosisTextView: TextView = itemView.findViewById(R.id.movieSypnosisBodyBack)

        private val frontView: MaterialCardView = itemView.findViewById(R.id.itemMovieLargeFront)
        private val backView: MaterialCardView = itemView.findViewById(R.id.itemMovieLargeBack)

        fun bind(item: MediaInfo) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.slide_out)
            itemView.startAnimation(animation)

            // Check if the values weren't the default values on bind
            revertDefaultVisibilities()

            // Bind the text values to TextViews
            bindTexts(item)

            // Load and bind the image to the ImageView
            bindImages(item)
        }

        private fun bindTexts(item: MediaInfo) {
            //movieTitleTextView.text = item.movieTitle
            //movieSypnosisTextView.text = item.movieSypnosis.let {
            //    val splittedStrings = it?.split(" ")
            //
            //    splittedStrings?.take(19)?.joinToString(" ") + " Read more..."
            //}.toString()
        }

        private fun bindImages(item: MediaInfo) {
            //Picasso
            //    .get()
            //    .load(item.movieImagePoster)
            //    .fit()
            //    .into(movieImageViewFront)
            //
            //Picasso
            //    .get()
            //    .load(item.movieImagePoster)
            //    .fit()
            //    .into(movieImageViewBack)
        }

        private fun revertDefaultVisibilities() {
            if(frontView.visibility == View.GONE)
                frontView.visibility = View.VISIBLE
            if(backView.visibility == View.VISIBLE)
                backView.visibility = View.GONE
        }

        companion object {
            fun create(context: Context?, parent: ViewGroup): TopMoviesViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_media_large, parent, false)
                return TopMoviesViewHolder(context, view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopMoviesViewHolder {
        return TopMoviesViewHolder.create(context, parent)
    }

    override fun onBindViewHolder(holder: TopMoviesViewHolder, position: Int) {
        listOfMovies[position].let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return listOfMovies.size
    }

    private fun itemOnClickListener(position: Int): View.OnClickListener? {
        return View.OnClickListener {

        }
    }
}