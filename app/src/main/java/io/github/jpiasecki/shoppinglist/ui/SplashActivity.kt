package io.github.jpiasecki.shoppinglist.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.ui.main.MainActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.SplashViewModel

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewModel.onSplashLaunch()

        startActivity(
            Intent(this, MainActivity::class.java)
        )

        finish()
    }
}