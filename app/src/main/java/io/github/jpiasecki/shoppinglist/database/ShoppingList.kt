package io.github.jpiasecki.shoppinglist.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import java.util.*

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @get:Exclude
    @PrimaryKey(autoGenerate = false)
    var id: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var owner: String? = null,
    var currency: String? = null,
    @get:Exclude
    var items: MutableList<Item> = mutableListOf(),
    @get:Exclude
    var users: MutableList<String> = mutableListOf(),
    var banned: MutableList<String> = mutableListOf(),
    var timestamp: Long = Calendar.getInstance().timeInMillis,

    @get:Exclude
    var keepInSync: Boolean = true
)