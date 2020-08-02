package io.github.jpiasecki.shoppinglist.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.ui.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.activity_add_edit_list.*
import kotlinx.android.synthetic.main.activity_settings.*

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(activity_add_edit_list_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        activity_settings_dark_theme_toggle.isChecked = viewModel.isDarkThemeEnabled()
        activity_settings_dark_theme_toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setDarkThemeEnabled(isChecked)

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return true
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(android.R.anim.fade_in, R.anim.activity_slide_down)
    }
}