package io.github.jpiasecki.shoppinglist.database

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.util.*

class Config(private val context: Context) {
    companion object {
        const val PREFERENCES_NAME = "CONFIG_PREFERENCES"

        const val PROFILE_PICTURE_UPDATE_TIMESTAMP = "PROFILE_PICTURE_UPDATE_TIMESTAMP"
        const val LISTS_AUTO_UPDATE_TIMESTAMP = "LISTS_METADATA_AUTO_UPDATE_TIMESTAMP"
        const val LISTS_MANUAL_UPDATE_TIMESTAMP = "LISTS_METADATA_MANUAL_UPDATE_TIMESTAMP"

        const val SETTINGS_DARK_THEME_ENABLED = "SETTINGS_DARK_THEME_ENABLED"
        const val SETTINGS_LISTS_SORT_TYPE = "SETTINGS_LISTS_SORT_TYPE"
        const val SETTINGS_ITEMS_SORT_TYPE = "SETTINGS_ITEMS_SORT_TYPE"
        const val SETTINGS_AUTO_SET_ICONS = "SETTINGS_AUTO_SET_ICONS"

        const val SORT_TYPE_NEWEST = 0
        const val SORT_TYPE_ALPHABETICALLY = 1

        const val AUTO_SET_ALWAYS = 0
        const val AUTO_SET_NEVER = 1
        const val AUTO_SET_WHEN_NEW = 2

        fun isNetworkConnected(context: Context?): Boolean {
            if (context == null)
                return false

            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                val info = manager.activeNetworkInfo

                if (info != null)
                    return info.isConnected && (info.type == ConnectivityManager.TYPE_WIFI || info.type == ConnectivityManager.TYPE_MOBILE || info.type == ConnectivityManager.TYPE_VPN)
            } else {
                val network = manager.activeNetwork

                if (network != null) {
                    val capabilities = manager.getNetworkCapabilities(network)

                    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
            }

            return false
        }
    }

    private var preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

    fun getProfilePictureUpdateTimestamp() = preferences.getLong(PROFILE_PICTURE_UPDATE_TIMESTAMP, 0)

    fun getListsAutoUpdateTimestamp() = preferences.getLong(LISTS_AUTO_UPDATE_TIMESTAMP, 0)

    fun getListsManualUpdateTimestamp() = preferences.getLong(LISTS_MANUAL_UPDATE_TIMESTAMP, 0)

    fun isDarkThemeEnabled() = preferences.getBoolean(SETTINGS_DARK_THEME_ENABLED, false)

    fun getListsSortType() = preferences.getInt(SETTINGS_LISTS_SORT_TYPE, SORT_TYPE_NEWEST)

    fun getItemsSortType() = preferences.getInt(SETTINGS_ITEMS_SORT_TYPE, SORT_TYPE_NEWEST)

    fun getAutoSetIcons() = preferences.getInt(SETTINGS_AUTO_SET_ICONS, AUTO_SET_WHEN_NEW)

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

    fun setDarkThemeEnabled(value: Boolean) = preferences.edit().putBoolean(
        SETTINGS_DARK_THEME_ENABLED,
        value
    ).apply()

    fun setListsSortType(type: Int) = preferences.edit().putInt(
        SETTINGS_LISTS_SORT_TYPE,
        type
    ).apply()

    fun setItemsSortType(type: Int) = preferences.edit().putInt(
        SETTINGS_ITEMS_SORT_TYPE,
        type
    ).apply()

    fun setAutoSetIcons(type: Int) = preferences.edit().putInt(
        SETTINGS_AUTO_SET_ICONS,
        type
    ).apply()
}