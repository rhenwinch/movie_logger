package com.xcape.movie_logger

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xcape.movie_logger.database.Movie
import com.xcape.movie_logger.databinding.FragmentHomeBinding
import com.xcape.movie_logger.imdbapi.ApiObject.Companion.getMovieInfo
import com.xcape.movie_logger.imdbapi.ApiObject.Companion.parseDate
import com.xcape.movie_logger.imdbapi.ApiObject.Companion.parseDuration
import com.xcape.movie_logger.imdbapi.ApiObject.Companion.parsePeople
import com.xcape.movie_logger.imdbapi.ApiObject.Companion.searchMovie
import com.xcape.movie_logger.viewmodel.HomeFragmentViewModel
import com.xcape.movie_logger.viewmodel.HomeFragmentViewModelFactory

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

        searchMovie("Spiderman Far From Home") { possibleMovie ->
            try {
                possibleMovie[0]
            }
            catch (e: Error) {
                throw Error("Error accessing searched movies ${e.localizedMessage}")
            }

            println("MOVIE ID: " + possibleMovie[0].id)
            getMovieInfo(possibleMovie[0].id) { movie ->
                movie.id = possibleMovie[0].id
                val listOfCasts = parsePeople(movie.actor)
                val listOfDirs = when(movie.director) {
                    null -> parsePeople(movie.creator)
                    else -> parsePeople(movie.director)
                }

                val dataSource = (activity?.application as MovieApplication).database.movieDatabaseDao

                val movieToAdd = Movie(
                    dateWatched = "02-02-2022",
                    movieTokenID = movie.id,
                    movieTitle = movie.name,
                    movieDesc = movie.description,
                    movieGenre = movie.genre.joinToString(),
                    movieDateReleased = parseDate(movie.datePublished),
                    movieDuration = parseDuration(movie.duration),
                    movieCast = listOfCasts,
                    movieDirectors = listOfDirs,
                    movieImagePoster = movie.image,
                    movieImageThumb = movie.trailer.thumbnailUrl,
                    movieRating = movie.aggregateRating.ratingValue
                )

                dataSource.addMovie(movieToAdd)

                println("ADDED THE MOVIE! => ${dataSource.getLatestMovie()!!.movieTitle}")
            }
        }

        val adapter = GridAdapter()
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(activity?.applicationContext)

        viewModel.allMovies.observe(viewLifecycleOwner) { movie ->
            movie.let { adapter.submitList(it) }
        }

    }


}