package io.github.jpiasecki.shoppinglist.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.AdProvider
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.ui.main.MainActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.SplashViewModel
import java.lang.IllegalArgumentException
import java.util.*

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES && viewModel.isDarkThemeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO && !viewModel.isDarkThemeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_splash)

        AdProvider.config = viewModel.getConfig()
        MobileAds.initialize(applicationContext)
        if (viewModel.areAdsEnabled()) {
            AdProvider.destroyAds()
            AdProvider.loadAds(applicationContext)
        }

        viewModel.onSplashLaunch()

        if (!checkIntentAction()) {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }

        finish()
    }

    private fun checkIntentAction(): Boolean {
        if (intent.action == Intent.ACTION_VIEW) {
            if (Config.isNetworkConnected(this)) {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    val query = intent.data?.query
                        ?: return false.also { showToast(getString(R.string.message_list_url_error)) }
                    val index = query.indexOf("id=")

                    if (index != -1) {
                        try {
                            val listId = query.substring(index + 3)
                            UUID.fromString(listId)

                            startActivity(
                                Intent(this, MainActivity::class.java)
                                    .putExtra(Values.SHOPPING_LIST_ID, listId)
                            )

                            return true
                        } catch (e: IllegalArgumentException) {
                            showToast(getString(R.string.message_list_url_error))
                        }
                    } else {
                        showToast(getString(R.string.message_list_url_error))
                    }
                } else {
                    showToast(getString(R.string.message_not_logged_in))
                }
            } else {
                showToast(getString(R.string.message_no_internet_connection))
            }
        }

        return false
    }

    private fun showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
        val toast = Toast.makeText(this, text, length)

        toast.view.findViewById<TextView>(android.R.id.message)?.let {
            it.gravity = Gravity.CENTER
        }

        toast.show()
    }
}