package com.xcape.movie_logger.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionSet
import android.view.Gravity
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.ActivityMainBinding
import com.xcape.movie_logger.presentation.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        homeViewModel.initializeCredentials()
        setContentView(binding.root)
        setupWindowAnimations()
        setupNav()
    }

    private fun setupWindowAnimations() {
        val fade = Fade()
        fade.duration = 1000

        val slide = Slide(Gravity.LEFT)
        slide.duration = 1000

        window.exitTransition = slide

        window.reenterTransition = fade
    }

    // Start of setting up navigation view \\
    private fun setupNav() {
        val navBar = binding.bottomNavBar
        val navController = findNavController(R.id.main_host_fragment)
        NavigationUI.setupWithNavController(navBar, navController)

        // Hide the navigation bar when user clicked on search
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.searchFragment -> hideBottomNav()
                R.id.favoritesFragment -> hideBottomNav()
                else -> showBottomNav()
            }
        }
    }

    private fun showBottomNav() {
        val bottomNavBar = binding.bottomNavBar
        bottomNavBar.clearAnimation()
        bottomNavBar.animate()
            .translationY(0.0F)
            .setDuration(300)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    bottomNavBar.visibility = View.VISIBLE
                }
            })
    }

    private fun hideBottomNav() {
        val bottomNavBar = binding.bottomNavBar
        bottomNavBar.clearAnimation()
        bottomNavBar.animate()
            .translationY(bottomNavBar.height.toFloat())
            .setDuration(300)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    bottomNavBar.visibility = View.GONE
                }
            })
    }
    // End of setting up navigation view \\

}