package com.xcape.movie_logger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GridAdapter(
    private val itemList: List<GridItem>,
    private val listener: (GridItem) -> Unit
) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = itemList[position]

        holder.bind(item)
        holder.itemView.setOnClickListener { listener(item) }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return itemList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        val textView: TextView = itemView.findViewById(R.id.itemTitle)

        fun bind(item: GridItem) {
            imageView.setImageResource(item.image)
            textView.text = item.title
        }
    }
}