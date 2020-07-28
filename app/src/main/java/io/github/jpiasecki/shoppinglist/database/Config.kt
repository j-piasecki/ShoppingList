package io.github.jpiasecki.shoppinglist.database

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import java.util.*

class Config(private val context: Context) {
    companion object {
        const val PREFERENCES_NAME = "CONFIG_PREFERENCES"

        const val PROFILE_PICTURE_UPDATE_TIMESTAMP = "PROFILE_PICTURE_UPDATE_TIMESTAMP"
        const val LISTS_METADATA_AUTO_UPDATE_TIMESTAMP = "LISTS_METADATA_AUTO_UPDATE_TIMESTAMP"
    }

    private var preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

    fun getProfilePictureUpdateTimestamp() = preferences.getLong(PROFILE_PICTURE_UPDATE_TIMESTAMP, 0)

    fun getListsMetadataAutoUpdateTimestamp() = preferences.getLong(LISTS_METADATA_AUTO_UPDATE_TIMESTAMP, 0)

    fun updateProfilePictureUpdateTimestamp() = preferences.edit().putLong(
        PROFILE_PICTURE_UPDATE_TIMESTAMP,
        Calendar.getInstance().timeInMillis
    ).apply()

    fun updateListsMetadataAutoUpdateTimestamp() = preferences.edit().putLong(
        LISTS_METADATA_AUTO_UPDATE_TIMESTAMP,
        Calendar.getInstance().timeInMillis
    ).apply()
}