package io.github.jpiasecki.shoppinglist.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import io.github.jpiasecki.shoppinglist.consts.Icons
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @get:Exclude
    @PrimaryKey(autoGenerate = false)
    var id: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var owner: String? = null,
    var currency: String? = null,
    var note: String? = null,
    @get:Exclude
    var items: ArrayList<Item> = ArrayList(),
    @get:Exclude
    var users: ArrayList<String> = ArrayList(),
    var banned: ArrayList<String> = ArrayList(),
    var timestamp: Long = Calendar.getInstance().timeInMillis,
    var icon: Int = Icons.DEFAULT,

    @get:Exclude
    var keepInSync: Boolean = true
) {

    @Exclude
    fun getAllUsersNoOwner(): List<String> {
        val list = ArrayList<String>(users)

        for (item in items) {
            if (item.addedBy != null && item.addedBy !in list && item.addedBy != owner)
                list.add(item.addedBy)

            if (item.completedBy != null && item.completedBy !in list && item.completedBy != owner)
                list.add(item.completedBy)
        }

        while (owner in list)
            list.remove(owner)

        return list.distinct()
    }

    @Exclude
    fun getAllUsersNoOwnerNoBan(): List<String> {
        val list = ArrayList<String>(users)

        for (item in items) {
            if (item.addedBy != null && item.addedBy !in list && item.addedBy != owner && item.addedBy !in banned)
                list.add(item.addedBy)

            if (item.completedBy != null && item.completedBy !in list && item.completedBy != owner && item.completedBy !in banned)
                list.add(item.completedBy)
        }

        while (owner in list)
            list.remove(owner)

        return list.distinct()
    }
}