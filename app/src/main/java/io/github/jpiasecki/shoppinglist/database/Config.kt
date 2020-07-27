package io.github.jpiasecki.shoppinglist.database

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class Config(private val context: Context) {
    companion object {
        const val PREFERENCES_NAME = "CONFIG_PREFERENCES"
    }

    private var preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

}