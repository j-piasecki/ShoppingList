package io.github.jpiasecki.shoppinglist.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "users")
data class User(
    @get:Exclude
    @PrimaryKey(autoGenerate = false)
    var id: String = "",
    var name: String? = null,

    @get:Exclude
    var lists: ArrayList<String> = ArrayList(),

    @get:Exclude
    var timestamp: Long = Calendar.getInstance().timeInMillis,

    @Ignore
    @get:Exclude
    var profilePicture: Bitmap? = null
)