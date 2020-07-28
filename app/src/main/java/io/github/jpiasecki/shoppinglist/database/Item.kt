package io.github.jpiasecki.shoppinglist.database

import com.google.firebase.firestore.Exclude
import io.github.jpiasecki.shoppinglist.consts.Icons
import io.github.jpiasecki.shoppinglist.consts.Units
import java.util.*


data class Item(
    val name: String? = null,
    val note: String? = null,
    val addedBy: String? = null,
    val completedBy: String? = null,
    val isCompleted: Boolean = false,
    val quantity: Int = 0,
    val unit: Int = Units.NO_UNIT,
    val price: Double = 0.0,
    val icon: Int = Icons.DEFAULT,
    val timestamp: Long = Calendar.getInstance().timeInMillis,
    val deleted: Boolean = false,

    @get:Exclude
    val id: String = UUID.randomUUID().toString()
)