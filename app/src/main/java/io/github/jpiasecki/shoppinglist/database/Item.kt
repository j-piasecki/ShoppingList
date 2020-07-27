package io.github.jpiasecki.shoppinglist.database

import io.github.jpiasecki.shoppinglist.consts.Icons
import io.github.jpiasecki.shoppinglist.consts.Units


data class Item(
    val name: String? = null,
    val note: String? = null,
    val addedBy: String? = null,
    val completedBy: String? = null,
    val isCompleted: Boolean = false,
    val quantity: Int = 0,
    val unit: Int = Units.NO_UNIT,
    val price: Double = 0.0,
    val icon: Int = Icons.DEFAULT
)