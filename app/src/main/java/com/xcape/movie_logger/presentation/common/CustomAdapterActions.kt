package com.xcape.movie_logger.presentation.common

interface CustomAdapterActions<Item> {
    fun getCurrentList(): List<Item>

    fun getItem(position: Int): Item?

    fun <T: Any>getItemPositionByProperty(property: T): Int

    fun submitList(
        newList: List<Item>,
        isForced: Boolean = false
    )

    fun addItem(
        position: Int,
        item: Item
    )

    fun deleteItem(position: Int)
}