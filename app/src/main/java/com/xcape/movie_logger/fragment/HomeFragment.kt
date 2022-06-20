package com.xcape.movie_logger.fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.xcape.movie_logger.GridAdapter
import com.xcape.movie_logger.MovieApplication
import com.xcape.movie_logger.databinding.FragmentHomeBinding
import com.xcape.movie_logger.viewmodel.HomeFragmentViewModel
import com.xcape.movie_logger.viewmodel.HomeFragmentViewModelFactory
import kotlin.math.abs


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: HomeFragmentViewModel by viewModels {
        HomeFragmentViewModelFactory((activity?.application as MovieApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Get the list of movie items we have in the Dao
        homeGridView()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun homeGridView() {
        val recyclerView = _binding?.homeGridView

        //searchMovie("Spiderman Far From Home") { possibleMovie ->
        //    try {
        //        possibleMovie[0]
        //    }
        //    catch (e: Error) {
        //        throw Error("Error accessing searched movies ${e.localizedMessage}")
        //    }
        //
        //    println("MOVIE ID: " + possibleMovie[0].id)
        //    getMovieInfo(possibleMovie[0].id) { movie ->
        //        movie.id = possibleMovie[0].id
        //        val listOfCasts = parsePeople(movie.actor)
        //        val listOfDirs = when(movie.director) {
        //            null -> parsePeople(movie.creator)
        //            else -> parsePeople(movie.director)
        //        }
        //
        //        val dataSource = (activity?.application as MovieApplication).database.movieDatabaseDao
        //
        //        val movieToAdd = Movie(
        //            dateWatched = "02-02-2022",
        //            movieTokenID = movie.id,
        //            movieTitle = movie.name,
        //            movieDesc = movie.description,
        //            movieGenre = movie.genre.joinToString(),
        //            movieDateReleased = parseDate(movie.datePublished),
        //            movieDuration = parseDuration(movie.duration),
        //            movieCast = listOfCasts,
        //            movieDirectors = listOfDirs,
        //            movieImagePoster = movie.image,
        //            movieImageThumb = movie.trailer.thumbnailUrl,
        //            movieRating = movie.aggregateRating.ratingValue
        //        )
        //
        //        dataSource.addMovie(movieToAdd)
        //
        //        println("ADDED THE MOVIE! => ${dataSource.getLatestMovie()!!.movieTitle}")
        //    }
        //}

        val adapter = GridAdapter()
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(activity?.applicationContext)

        viewModel.allMovies.observe(viewLifecycleOwner) { movie ->
            movie.let { adapter.submitList(it) }
        }

    }
}