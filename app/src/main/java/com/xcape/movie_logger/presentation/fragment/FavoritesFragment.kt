package com.xcape.movie_logger.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xcape.movie_logger.R
import com.xcape.movie_logger.databinding.FragmentFavoritesBinding
import com.xcape.movie_logger.presentation.components.setupToolbar

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding: FragmentFavoritesBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavoritesBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbarListener = Toolbar.OnMenuItemClickListener {
            when (it?.itemId) {
                R.id.searchFragment -> {
                    findNavController().navigate(R.id.action_favoritesFragment_to_searchFragment)
                    true
                }
                else -> false
            }
        }
        binding.favoritesToolbar.setupToolbar(
            this,
            requireActivity(),
            withNavigationUp = true,
            listener = toolbarListener,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}