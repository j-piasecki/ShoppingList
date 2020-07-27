package io.github.jpiasecki.shoppinglist.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import java.util.*

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @Exclude
    @PrimaryKey(autoGenerate = false)
    var id: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var owner: String? = null,
    var currency: String? = null,
    var items: List<Item> = emptyList(),
    var users: List<String> = emptyList(),
    var banned: List<String> = emptyList(),
    var timestamp: Long = 0,

    @Exclude
    var keepInSync: Boolean = true
)