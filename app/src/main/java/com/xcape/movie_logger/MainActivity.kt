package com.xcape.movie_logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.xcape.movie_logger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
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

    //override fun onSupportNavigateUp(): Boolean {
    //    return findNavController(R.id.main_host_fragment).navigateUp()
    //}

    private fun setupNav() {
        val navBar = binding.bottomNavBar
        val navController = findNavController(R.id.main_host_fragment)

        NavigationUI.setupWithNavController(navBar, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.searchFragment)
                hideBottomNav()
            else
                showBottomNav()
        }
    }

    private fun showBottomNav() {
        binding.bottomNavBar.visibility = View.VISIBLE
        binding.floatingAddButton.visibility = View.VISIBLE
    }

    private fun hideBottomNav() {
        binding.bottomNavBar.visibility = View.GONE
        binding.floatingAddButton.visibility = View.GONE
    }

}