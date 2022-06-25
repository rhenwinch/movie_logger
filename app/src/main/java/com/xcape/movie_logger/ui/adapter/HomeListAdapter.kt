package com.xcape.movie_logger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.xcape.movie_logger.objects.movie.Movie

class HomeListAdapter : ListAdapter<Movie, HomeListAdapter.MovieViewHolder>(MOVIES_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie)
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val movieImageView: ImageView = itemView.findViewById(R.id.movieImage)
        private val movieTitleVIew: TextView = itemView.findViewById(R.id.movieTitle)
        private val movieDurationView: TextView = itemView.findViewById(R.id.movieDuration)
        private val movieCastView: TextView = itemView.findViewById(R.id.movieCast)
        private val movieRatingView: TextView = itemView.findViewById(R.id.movieRating)

        fun bind(item: Movie) {
            // Load and bind the image to the ImageView
            Picasso
                .get()
                .load(item.movieImagePoster)
                .fit()
                .centerCrop()
                .into(movieImageView)

            // Bind the data to our text views
            val movieReleasedInfo =
                if(item.movieDuration != null)
                    item.movieDuration + " | " + item.movieDateReleased
                else
                    "TV Series"

            movieDurationView.text = movieReleasedInfo
            movieTitleVIew.text = item.movieTitle
            movieCastView.text = item.movieCast
            movieRatingView.text = item.movieRating.toString()
        }

        companion object {
            fun create(parent: ViewGroup): MovieViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_grid, parent, false)
                return MovieViewHolder(view)
            }
        }
    }

    companion object {
        private val MOVIES_COMPARATOR = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem.movieTitle == newItem.movieTitle
            }
        }
    }
}