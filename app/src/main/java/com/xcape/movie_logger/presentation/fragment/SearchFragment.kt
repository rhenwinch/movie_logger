package com.xcape.movie_logger.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.xcape.movie_logger.R

import com.xcape.movie_logger.databinding.FragmentSearchBinding
import com.xcape.movie_logger.presentation.components.setupToolbar

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchToolbar.setupToolbar(
            this,
            requireActivity(),
            withNavigationUp = true
        )
    }
}