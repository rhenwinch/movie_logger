package com.xcape.movie_logger.presentation.home

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.FragmentHomeBinding
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.model.media.WatchedMedia
import com.xcape.movie_logger.domain.model.user.Post
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.presentation.common.setOnSingleClickListener
import com.xcape.movie_logger.presentation.components.custom_components.StackingLayoutManager
import com.xcape.movie_logger.presentation.components.custom_components.SwipeCard
import com.xcape.movie_logger.presentation.components.custom_extensions.activate
import com.xcape.movie_logger.presentation.components.custom_extensions.deactivate
import com.xcape.movie_logger.presentation.components.setupToolbar
import com.xcape.movie_logger.presentation.home.adapter.PostsAdapter
import com.xcape.movie_logger.presentation.home.adapter.TopMoviesAdapter
import com.xcape.movie_logger.presentation.home.viewholder.PostInteractListener
import com.xcape.movie_logger.presentation.search.SearchActivity
import com.xcape.movie_logger.presentation.trending.TrendingActivity
import com.xcape.movie_logger.presentation.watchlist.WatchlistActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), Toolbar.OnMenuItemClickListener, PostInteractListener {
    private var _binding: FragmentHomeBinding? = null // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    private val postsAdapter = PostsAdapter(listener = this@HomeFragment)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.homeToolbar.setupToolbar(
            this,
            requireActivity(),
            withNavigationUp = false,
            listener = this,
        )

        binding.bindNewsFeedData(
            postsData = homeViewModel.posts,
            postOwnersData = homeViewModel.postOwners
        )

        binding.bindTopWatchedMovies(topMovies = homeViewModel.topMovies)

        binding.setupButtons()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
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

    override fun onLike(
        ownerId: String,
        mediaId: String,
        position: Int,
        isDisliking: Boolean
    ) {
        //postsAdapter.modifyLikeOnPost(position = position, isDisliking = isDisliking)
        homeViewModel.onEvent(
            event = HomeUIAction.Like(
                ownerId = ownerId,
                postId = mediaId,
                isDisliking = isDisliking
            )
        )
    }

    override fun onRate(
        mediaId: String,
        position: Int,
        isUnrating: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun onWatchlist(
        mediaId: String,
        position: Int,
        isRemoving: Boolean
    ) {
        val item = postsAdapter.getItem(position) ?: return

        homeViewModel.onEvent(
            event = HomeUIAction.AddToWatchlist(
                post = item,
                isRemoving = isRemoving
            )
        )
    }

    private fun FragmentHomeBinding.setupButtons() {
        favoriteButton.setOnSingleClickListener {
            val intent = Intent(requireContext(), WatchlistActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity())

            // start the new activity
            startActivity(intent, options.toBundle())
        }
        trendingButton.setOnSingleClickListener {
            val intent = Intent(requireContext(), TrendingActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity())

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    private fun FragmentHomeBinding.bindNewsFeedData(
        postsData: StateFlow<List<Post>>,
        postOwnersData: StateFlow<List<User>>
    ) {
        postsRV.itemAnimator = null
        postsRV.adapter = postsAdapter
        homeScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                // If user is at bottom
                loadingBar.activate()
                return@OnScrollChangeListener
            }
            loadingBar.deactivate()
        })

        lifecycleScope.launch {
            combine(
                postsData,
                postOwnersData,
                ::Pair
            ).collectLatest { (posts, owners) ->
                postsAdapter.submitPosts(owners, posts)
            }
        }
    }

    private fun FragmentHomeBinding.bindTopWatchedMovies(topMovies: StateFlow<List<WatchedMedia>>) {
        val homeTopMovieRV = homeHeadline.homeHeadlinerMovies
        val stackedLayoutManager = StackingLayoutManager()
        val homeTopMovieAdapter = TopMoviesAdapter()

        homeTopMovieRV.adapter = homeTopMovieAdapter
        homeTopMovieRV.layoutManager = stackedLayoutManager

        lifecycleScope.launch {
            topMovies.collectLatest {
                homeTopMovieAdapter.submitList(it)
            }
        }
    }

}
