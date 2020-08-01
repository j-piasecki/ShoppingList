package io.github.jpiasecki.shoppinglist.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.Exclude
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import io.github.jpiasecki.shoppinglist.other.GlideApp
import io.github.jpiasecki.shoppinglist.other.UsersProfilePictures
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.IllegalArgumentException
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
    var timestamp: Long = Calendar.getInstance().timeInMillis
) {
    @get:Exclude
    var profilePicture: Bitmap?
        get() = UsersProfilePictures.getPicture(id)
        set(value) = UsersProfilePictures.setPicture(id, value)

    fun loadProfileImage(context: Context, callback: (() -> Unit)? = null) {
        val ref = Firebase.storage.reference.child("profile_pics/${id}")

        if (profilePicture == null) {
            // if profile picture file exists, load it
            ref.metadata.addOnSuccessListener {
                if (Config.isNetworkConnected(context)) {
                    try {
                        GlideApp.with(context).asBitmap().circleCrop().load(ref)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {}

                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    profilePicture = resource

                                    val file = File(context.cacheDir, "$id.png")
                                    val stream = ByteArrayOutputStream()
                                    resource.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                    file.writeBytes(stream.toByteArray())

                                    callback?.invoke()
                                }
                            })
                    } catch (e: IllegalArgumentException) {}
                } else {
                    try {
                        val stream = FileInputStream(File(context.cacheDir, "$id.png"))

                        profilePicture = BitmapFactory.decodeStream(stream)

                        callback?.invoke()
                    } catch (e: FileNotFoundException) {}
                }
            }
        } else {
            callback?.invoke()
        }
    }
}