package com.xcape.movie_logger.presentation.home

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.FragmentHomeBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo

import com.xcape.movie_logger.presentation.components.custom_components.StackingLayoutManager
import com.xcape.movie_logger.presentation.components.custom_components.SwipeCard
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.components.setupToolbar
import com.xcape.movie_logger.presentation.watchlist.WatchlistActivity
import com.xcape.movie_logger.presentation.search.SearchActivity
import com.xcape.movie_logger.presentation.trending.TrendingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val toolbarListener = Toolbar.OnMenuItemClickListener {
            when (it?.itemId) {
                R.id.searchButton -> {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity())

                    // start the new activity
                    startActivity(intent, options.toBundle())
                    true
                }
                else -> false
            }
        }

        binding.homeToolbar.setupToolbar(
            this,
            requireActivity(),
            withNavigationUp = false,
            listener = toolbarListener,
        )

        setupButtons()

        return view
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.initializeCredentials()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeTopMovieRV = binding.homeHeadline.homeHeadlinerMovies
        val listOfMovies = populateAdapter()
        val stackedLayoutManager = StackingLayoutManager(activity?.applicationContext)
        val homeTopMovieAdapter = TopMoviesAdapter(activity?.applicationContext, listOfMovies)

        homeTopMovieRV.adapter = homeTopMovieAdapter
        homeTopMovieRV.layoutManager = stackedLayoutManager

        val simpleItemTouchCallback = SwipeCard(listOfMovies, homeTopMovieAdapter)

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(homeTopMovieRV)
    }

    private fun setupButtons() {
        binding.favoriteButton.setOnSingleClickListener {
            val intent = Intent(requireContext(), WatchlistActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity())

            // start the new activity
            startActivity(intent, options.toBundle())
        }
        binding.trendingButton.setOnSingleClickListener {
            val intent = Intent(requireContext(), TrendingActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity())

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    private fun populateAdapter(): MutableList<MediaInfo> {

        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BZWMyYzFjYTYtNTRjYi00OGExLWE2YzgtOGRmYjAxZTU3NzBiXkEyXkFqcGdeQXVyMzQ0MzA0NTM@._V1_.jpg", "Spiderman: No way home", "\"Spider-Man\" centers on student Peter Parker (Tobey Maguire) who, after being bitten by a genetically-altered spider, gains superhuman strength and the spider-like ability to cling to any surface. He vows to use his abilities to fight crime, coming to understand the words of his beloved Uncle Ben: \"With great power comes great responsibility.\""))
        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BZjhmZTlkOTAtYTE0Yi00Yjg2LTg5M2UtNWNmNTZkZGM4ODRmXkEyXkFqcGdeQXVyMTI4NjgxNTk5._V1_.jpg","Spiderman: Lotus", "yes"))
        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BZDEyN2NhMjgtMjdhNi00MmNlLWE5YTgtZGE4MzNjMTRlMGEwXkEyXkFqcGdeQXVyNDUyOTg3Njg@._V1_.jpg", "Spiderman 1", "yes"))
        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BNTk4ODQ1MzgzNl5BMl5BanBnXkFtZTgwMTMyMzM4MTI@._V1_.jpg"))
        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BMjMyOTM4MDMxNV5BMl5BanBnXkFtZTcwNjIyNzExOA@@._V1_.jpg"))
        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BMjMwNDkxMTgzOF5BMl5BanBnXkFtZTgwNTkwNTQ3NjM@._V1_.jpg"))
        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BOTA5NDYxNTg0OV5BMl5BanBnXkFtZTgwODE5NzU1MTE@._V1_.jpg"))
        //listOfMovies.add(Movie("https://m.media-amazon.com/images/M/MV5BYTk3MDljOWQtNGI2My00OTEzLTlhYjQtOTQ4ODM2MzUwY2IwXkEyXkFqcGdeQXVyNTIzOTk5ODM@._V1_.jpg"))

        return mutableListOf()
    }

}