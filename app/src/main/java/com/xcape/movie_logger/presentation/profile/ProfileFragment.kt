package com.xcape.movie_logger.presentation.profile

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.xcape.movie_logger.databinding.FragmentProfileBinding
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.domain.utils.Functions.toByteArray
import com.xcape.movie_logger.presentation.common.OnMediaClickListener
import com.xcape.movie_logger.presentation.movie_details.MEDIA_BITMAP
import com.xcape.movie_logger.presentation.movie_details.MEDIA_CATEGORY
import com.xcape.movie_logger.presentation.movie_details.MEDIA_ID
import com.xcape.movie_logger.presentation.movie_details.MovieActivity
import com.xcape.movie_logger.presentation.profile.adapter.FriendsAdapter
import com.xcape.movie_logger.presentation.profile.adapter.ReviewsAdapter
import com.xcape.movie_logger.presentation.profile.adapter.WatchlistAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(), OnMediaClickListener {
    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding!!

    // View model
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater)

        binding.bindData(
            userData = profileViewModel.userData,
            friendsList = profileViewModel.friends
        )

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMediaClick(
        mediaCategory: String,
        mediaId: String,
        mediaImageView: ImageView,
        mediaImageCard: MaterialCardView
    ) {
        val mediaImage = mediaImageView.drawable?.toBitmap()?.toByteArray()
        // Proceed only if bitmap is not null

        mediaImage?.let { bitmap ->
            val intent = Intent(requireContext(), MovieActivity::class.java)
            intent.putExtra(MEDIA_ID, mediaId)
            intent.putExtra(MEDIA_CATEGORY, mediaCategory)
            intent.putExtra(MEDIA_BITMAP, bitmap)

            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), mediaImageCard,"${mediaId}-${mediaCategory}")

            // start the new activity
            startActivity(intent, options.toBundle())
        }
    }

    private fun FragmentProfileBinding.bindData(
        userData: LiveData<User?>,
        friendsList: LiveData<List<User>>
    ) {
        val reviewsAdapter = ReviewsAdapter()
        val watchlistAdapter = WatchlistAdapter(this@ProfileFragment)
        val friendsAdapter = FriendsAdapter()

        reviewsRV.adapter = reviewsAdapter
        watchlistRV.adapter = watchlistAdapter
        friendsRV.adapter = friendsAdapter

        userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                val reviewsPreviewSize = 3
                val watchlistPreviewSize = 3

                usernameLabel.text = it.username
                if(it.reviews.isEmpty()) {
                    reviewsEmptyListIndicator.visibility = View.VISIBLE
                }
                else {
                    reviewsEmptyListIndicator.visibility = View.GONE
                    reviewsAdapter.submitList(it.reviews.take(reviewsPreviewSize))
                }

                if(it.watchlist.isEmpty()) {
                    watchlistEmptyListIndicator.visibility = View.VISIBLE
                }
                else {
                    watchlistEmptyListIndicator.visibility = View.GONE
                    watchlistAdapter.submitList(it.watchlist.take(watchlistPreviewSize))
                }
            }
        }

        friendsList.observe(viewLifecycleOwner) { friends ->
            friendsAdapter.submitList(friends)
        }
    }
}