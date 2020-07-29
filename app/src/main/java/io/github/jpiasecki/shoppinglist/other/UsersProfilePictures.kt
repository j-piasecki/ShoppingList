package io.github.jpiasecki.shoppinglist.other

import android.graphics.Bitmap

object UsersProfilePictures {
    private val pictures = HashMap<String, Bitmap>()

    @Synchronized
    fun getPicture(id: String): Bitmap? {
        return pictures.getOrElse(id) {
            null
        }
    }

    @Synchronized
    fun setPicture(id: String, bitmap: Bitmap?) {
        pictures[id] = bitmap ?: return
    }
}