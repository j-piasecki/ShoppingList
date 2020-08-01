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

                    return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_VPN))
                }
            }

            return false
        }
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