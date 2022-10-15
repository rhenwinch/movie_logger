package com.xcape.movie_logger.presentation.friend_requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.xcape.movie_logger.databinding.FragmentFriendRequestsBinding
import com.xcape.movie_logger.domain.model.user.FriendRequest
import com.xcape.movie_logger.presentation.friend_requests.adapter.FriendRequestsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendRequestsFragment : Fragment() {
    private var _binding: FragmentFriendRequestsBinding? = null
    private val binding: FragmentFriendRequestsBinding
        get() = _binding!!

    private val friendRequestsViewModel: FriendRequestsViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFriendRequestsBinding.inflate(layoutInflater)

        binding.bindLatestFriendRequests(
            friendRequests = friendRequestsViewModel.friendRequests
        )

        return binding.root
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun FragmentFriendRequestsBinding.bindLatestFriendRequests(friendRequests: LiveData<List<FriendRequest>>) {
        val friendRequestsAdapter = FriendRequestsAdapter()
        friendRequestsRV.adapter = friendRequestsAdapter

        friendRequests.observe(viewLifecycleOwner) { list ->
            val friendRequestsCount = list.size ?: 0
            friendRequestParentHeader.notifParentTitle.text = String.format("Friend Requests - %d", friendRequestsCount)

            friendRequestsAdapter.submitList(list)
        }
    }
}

