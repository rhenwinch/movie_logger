package com.xcape.movie_logger

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class GridAdapter(context: Context, item: ArrayList<GridItem>): BaseAdapter() {
    private val ctx: Context
    private val listOfItems: ArrayList<GridItem>

    init {
        ctx = context
        listOfItems = item
    }

    override fun getCount(): Int {
        return listOfItems.size
    }

    override fun getItem(position: Int): Any {
        return listOfItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(ctx, R.layout.grid_item, null)

        val title: TextView = view.findViewById(R.id.itemTitle)
        val image: ImageView = view.findViewById(R.id.itemTitle)

        val movie: GridItem = listOfItems[position]

        image.setImageResource(movie.image)
        title.text = movie.title
        return view
    }

}