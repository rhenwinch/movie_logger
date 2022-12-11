package com.xcape.movie_logger.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.xcape.movie_logger.domain.model.user.Post
import com.xcape.movie_logger.domain.model.user.User
import com.xcape.movie_logger.presentation.common.CustomAdapterActions
import com.xcape.movie_logger.presentation.home.viewholder.PostInteractListener
import com.xcape.movie_logger.presentation.home.viewholder.PostsViewHolder

class PostsAdapter(
    private val postOwners: MutableList<User> = mutableListOf(),
    private val posts: MutableList<Post> = mutableListOf(),
    private val listener: PostInteractListener
) : Adapter<PostsViewHolder>(), CustomAdapterActions<Post> {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PostsViewHolder.create(inflater, parent)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            item = item,
            position = position,
            user = postOwners.firstOrNull { it.userId == item?.post?.ownerId },
            listener = listener
        )
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun getCurrentList(): List<Post> {
        return posts
    }

    override fun getItem(position: Int): Post? {
        return posts[position] ?: null
    }

    override fun <T : Any> getItemPositionByProperty(property: T): Int {
        return posts.indexOfFirst { property == it.post?.ownerId }
    }

    override fun submitList(newList: List<Post>, isForced: Boolean) {
        val comparator = PostsComparator(
            oldList = posts,
            newList = newList
        )
        val diffResult = DiffUtil.calculateDiff(comparator)

        with(posts) {
            clear()
            addAll(newList)
        }

        diffResult.dispatchUpdatesTo(this)
    }

    override fun addItem(position: Int, item: Post) {
        posts.add(position, item)
        notifyItemInserted(position)
    }

    override fun deleteItem(position: Int) {
        posts.removeAt(position)
        notifyItemRemoved(position)
    }

    fun submitPosts(newPostsOwners: List<User>, newPosts: List<Post>) {
        with(postOwners) {
            clear()
            addAll(newPostsOwners)
        }
        submitList(newPosts)
    }

    //fun modifyLikeOnPost(position: Int, isDisliking: Boolean = false, ) {
    //    if(userId.isEmpty())
    //        throw java.lang.NullPointerException("User ID is missing!")
    //
    //    if(isDisliking)
    //        getItem(position)?.likes?.remove(userId)
    //    else
    //        getItem(position)?.likes?.add(userId)
    //}
}

class PostsComparator(
    private val oldList: List<Post>,
    private val newList: List<Post>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.post?.id + oldItem.post?.ownerId == newItem.post?.id + newItem.post?.ownerId
    }

}