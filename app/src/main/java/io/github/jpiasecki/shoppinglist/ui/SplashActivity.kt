package io.github.jpiasecki.shoppinglist.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.AdProvider
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.ui.main.MainActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.SplashViewModel

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
        if (viewModel.areAdsEnabled())
            AdProvider.loadAds(applicationContext)

        viewModel.onSplashLaunch()

        startActivity(
            Intent(this, MainActivity::class.java)
        )

        finish()
    }
}