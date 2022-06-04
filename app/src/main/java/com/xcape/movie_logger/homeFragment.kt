package com.xcape.movie_logger

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.xcape.movie_logger.database.MovieDatabase
import com.xcape.movie_logger.database.MovieDatabaseDao
import com.xcape.movie_logger.databinding.FragmentHomeBinding
import okhttp3.*
import java.io.IOException

class homeFragment : Fragment() {
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
        val recyclerView = _binding?.homeGridView
        createMovieList("django")

        //val dataSource = MovieDatabase.getInstance(ctx).movieDatabaseDao
        //dataSource.addMovie()

        //recyclerView?.adapter = GridAdapter(dataSource) { item ->
        //    Toast.makeText(ctx, item.title, Toast.LENGTH_SHORT).show()
        //}
    }

    private fun createMovieList(movieKeyword: String): SuggestedMovie? {
        val searchFor = movieKeyword.replace(" ", "_")
        var movieList: SuggestedMovie? = null

        // Get movie id in IMDB
        val IMDBSuggestionURL = "https://v2.sg.media-imdb.com/suggestion/${searchFor[0]}/${searchFor}.json"
        val request = Request.Builder().url(IMDBSuggestionURL).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request!")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                val gson = GsonBuilder().create()
                val parsedJson = gson.fromJson(body, JSONSuggestedMovie::class.java)

                movieList = parsedJson.d[0]
            }
        })

        return movieList
    }
    class JSONSuggestedMovie(val d: List<SuggestedMovie>)

    // l is for title in the JSON body
    class SuggestedMovie(val id: String, val l: String)
}