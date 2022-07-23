package com.xcape.movie_logger.presentation.components

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.xcape.movie_logger.R

fun MaterialToolbar.setupToolbar(
    fragment: Fragment,
    activity: Activity,
    withNavigationUp: Boolean = true,
    listener: Toolbar.OnMenuItemClickListener? = null
) {
    if(withNavigationUp) {
        this.navigationIcon = ContextCompat.getDrawable(activity.applicationContext, R.drawable.left_arrow_2)
        this.setNavigationIconTint(
            ContextCompat.getColor(activity.applicationContext,
            R.color.colorOnMediumEmphasis
        ))
        this.setNavigationOnClickListener {
            NavHostFragment.findNavController(fragment = fragment).navigateUp()
        }
    }

    this.setOnMenuItemClickListener(listener)
}