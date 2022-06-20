package com.xcape.movie_logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.xcape.movie_logger.behavior.OnScrollListener
import com.xcape.movie_logger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnScrollListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupNav()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("Here", item.toString())
        return super.onOptionsItemSelected(item)
    }

    // Start of setting up navigation view \\
    private fun setupNav() {
        designNavBar()

        val navBar = binding.bottomNavBar
        val navController = findNavController(R.id.main_host_fragment)
        NavigationUI.setupWithNavController(navBar, navController)

        // Hide the navigation bar when user clicked on search
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.searchFragment)
                hideBottomNav()
            else
                showBottomNav()
        }
    }

    private fun designNavBar() {
        val bottomAppBar = binding.bottomAppBar
        val radius = resources.getDimension(R.dimen.bottomAppBarRadius)

        // Insert a corner radius to the bottom navigation bar using MaterialShapeDrawable
        val bottomAppBarBackground = bottomAppBar.background as MaterialShapeDrawable
        bottomAppBarBackground.shapeAppearanceModel =
            bottomAppBarBackground.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, radius)
            .setTopLeftCorner(CornerFamily.ROUNDED, radius)
            .build()
    }

    private fun showBottomNav() {
        binding.bottomAppBar.performShow()
        binding.floatingAddButton.show()
    }

    private fun hideBottomNav() {
        binding.bottomAppBar.performHide()
        binding.floatingAddButton.hide()
    }

    override fun onScrolled() {
        hideBottomNav()
    }
    // End of setting up navigation view \\
}