package io.github.jpiasecki.shoppinglist.database

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import java.util.*

class Config(private val context: Context) {
    companion object {
        const val PREFERENCES_NAME = "CONFIG_PREFERENCES"

        const val PROFILE_PICTURE_UPDATE_TIMESTAMP = "PROFILE_PICTURE_UPDATE_TIMESTAMP"
        const val LISTS_AUTO_UPDATE_TIMESTAMP = "LISTS_METADATA_AUTO_UPDATE_TIMESTAMP"
        const val LISTS_MANUAL_UPDATE_TIMESTAMP = "LISTS_METADATA_MANUAL_UPDATE_TIMESTAMP"
    }

    private var preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

    fun getProfilePictureUpdateTimestamp() = preferences.getLong(PROFILE_PICTURE_UPDATE_TIMESTAMP, 0)

    fun getListsAutoUpdateTimestamp() = preferences.getLong(LISTS_AUTO_UPDATE_TIMESTAMP, 0)

    fun getListsManualUpdateTimestamp() = preferences.getLong(LISTS_MANUAL_UPDATE_TIMESTAMP, 0)

    fun updateProfilePictureUpdateTimestamp() = preferences.edit().putLong(
        PROFILE_PICTURE_UPDATE_TIMESTAMP,
        Calendar.getInstance().timeInMillis
    ).apply()

    fun updateListsAutoUpdateTimestamp() = preferences.edit().putLong(
        LISTS_AUTO_UPDATE_TIMESTAMP,
        Calendar.getInstance().timeInMillis
    ).apply()

    fun updateListsManualUpdateTimestamp() = preferences.edit().putLong(
        LISTS_MANUAL_UPDATE_TIMESTAMP,
        Calendar.getInstance().timeInMillis
    ).apply()
}