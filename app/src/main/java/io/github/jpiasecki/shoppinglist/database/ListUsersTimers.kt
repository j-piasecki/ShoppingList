package io.github.jpiasecki.shoppinglist.database

import android.content.Context
import java.util.*

class ListUsersTimers(private val context: Context) {
    companion object {
        const val PREFERENCES_NAME = "TIMERS_PREFERENCES"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getListUpdate(listId: String) = preferences.getLong(listId, 0)

    fun updateList(listId: String) = preferences.edit().putLong(listId, Calendar.getInstance().timeInMillis).apply()
}