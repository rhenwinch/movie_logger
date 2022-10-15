package com.xcape.movie_logger.presentation.main_activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivityMainBinding
import com.xcape.movie_logger.presentation.auth.AuthPromptActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    // View model
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setupWindowAnimations()
        setContentView(binding.root)

        binding.bindNavigationBar(
            unseenNotifications = mainViewModel.unseenNotifications,
            unseenFriendRequests = mainViewModel.unseenFriendRequests,
        )
        binding.setupAuthPrompt(isAuthenticated = mainViewModel.isAuthenticated)
    }

    private fun ActivityMainBinding.setupAuthPrompt(isAuthenticated: Boolean) {
        if(!isAuthenticated) {
            val intent = Intent(this@MainActivity, AuthPromptActivity::class.java)

            root.doOnPreDraw {
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this@MainActivity).toBundle())
            }
        }
    }

    private fun setupWindowAnimations() {
        val fade = Fade()
        fade.duration = 1000

        val slide = Slide(Gravity.START)
        slide.duration = 1000

        window.exitTransition = slide
        window.reenterTransition = fade
    }

    private fun ActivityMainBinding.bindNavigationBar(
        unseenNotifications: LiveData<Int>,
        unseenFriendRequests: LiveData<Int>
    ) {
        val navBar = bottomNavBar
        val navController = findNavController(R.id.main_host_navigation)
        NavigationUI.setupWithNavController(navBar, navController)

        unseenNotifications.observe(this@MainActivity) { notifications ->
            if(notifications > 0) {
                navBar.getOrCreateBadge(R.id.notificationsFragment)
                    .number = notifications
            }
            else {
                navBar.removeBadge(R.id.notificationsFragment)
            }
        }

        unseenFriendRequests.observe(this@MainActivity) { friendRequests ->
            if(friendRequests > 0) {
                navBar.getOrCreateBadge(R.id.friendRequestsFragment)
                    .number = friendRequests
            }
            else {
                navBar.removeBadge(R.id.friendRequestsFragment)
            }
        }
    }

}