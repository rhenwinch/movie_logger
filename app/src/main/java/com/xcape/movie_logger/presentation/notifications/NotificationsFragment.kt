package com.xcape.movie_logger.presentation.notifications

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.xcape.movie_logger.databinding.FragmentNotificationsBinding
import com.xcape.movie_logger.domain.model.user.Notification
import com.xcape.movie_logger.presentation.notifications.adapter.NotificationsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding: FragmentNotificationsBinding
        get() = _binding!!

    private val notificationsViewModel: NotificationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationsBinding.inflate(layoutInflater)

        binding.bindLatestNotifications(
            notifications = notificationsViewModel.notifications
        )

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun FragmentNotificationsBinding.bindLatestNotifications(notifications: LiveData<List<Notification>>) {
        val notificationsAdapter = NotificationsAdapter()
        notificationsRV.adapter = notificationsAdapter

        notifications.observe(viewLifecycleOwner) { list ->
            val notificationsCount = list.size ?: 0
            notificationParentHeader.notifParentTitle.text = String.format("Notifications - %d", notificationsCount)

            notificationsAdapter.submitList(list)
        }

    }
}
