package com.xcape.movie_logger

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.xcape.movie_logger.databinding.FragmentHomeBinding

class homeFragment : Fragment(), AdapterView.OnItemClickListener {
    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var movies: ArrayList<GridItem>
    private lateinit var ctx: Context


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        if (container != null) {
            ctx = container.context
            homeGridView()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Misc functions
    private fun homeGridView() {
        val gridView = _binding?.homeGridView
        movies = createMovieList()

        gridView?.adapter = GridAdapter(ctx, movies)
        gridView?.onItemClickListener = this
    }

    private fun createMovieList(): ArrayList<GridItem> {
        val movieList: ArrayList<GridItem> = ArrayList()

        movieList.add(GridItem("Django Unchained (2012)", R.drawable.django))
        movieList.add(GridItem("Pulp Fiction (1994)", R.drawable.pulp_fiction))
        movieList.add(GridItem("Hitman's Wife Bodyguard", R.drawable.hitman_wife_bodyguard))
        movieList.add(GridItem("The Adam Project (2022)", R.drawable.the_adam_project))
        movieList.add(GridItem("Avengers: Endgame (2019)", R.drawable.avengers_endgame))
        movieList.add(GridItem("The Northman (2022)", R.drawable.the_northman))

        return movieList
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item: GridItem = movies[position]
        Toast.makeText(ctx, item.title, Toast.LENGTH_SHORT).show()
    }

}