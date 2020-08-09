package io.github.jpiasecki.shoppinglist.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.ui.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.activity_add_edit_list.*
import kotlinx.android.synthetic.main.activity_settings.*

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(activity_settings_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)


        setupDarkThemeToggle()
        setupPersonalizedAdsToggle()
        setupListsSortTypeSpinner()
        setupItemsSortTypeSpinner()
        setupAutoSetIconsSpinner()
        setupShowTimestampSpinner()
        setupButtons()
    }

    private fun setupDarkThemeToggle() {
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

    private fun setupPersonalizedAdsToggle() {
        activity_settings_personalized_ads_toggle.isChecked = viewModel.getAdsType() == Config.ADS_PERSONALIZED

        activity_settings_personalized_ads_toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setAdsType(if (isChecked) Config.ADS_PERSONALIZED else Config.ADS_NOT_PERSONALIZED)
        }
    }

    private fun setupListsSortTypeSpinner() {
        val values = listOf(getString(R.string.sort_type_newest_first), getString(R.string.sort_type_alphabetically))
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values)

        activity_settings_lists_sort_type_spinner.adapter = adapter
        activity_settings_lists_sort_type_spinner.setSelection(viewModel.getListsSortType())

        activity_settings_lists_sort_type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setListsSortType(position)
            }
        }
    }

    private fun setupItemsSortTypeSpinner() {
        val values = listOf(getString(R.string.sort_type_newest_first), getString(R.string.sort_type_alphabetically))
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values)

        activity_settings_items_sort_type_spinner.adapter = adapter
        activity_settings_items_sort_type_spinner.setSelection(viewModel.getItemsSortType())

        activity_settings_items_sort_type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setItemsSortType(position)
            }
        }
    }

    private fun setupAutoSetIconsSpinner() {
        val values = listOf(getString(R.string.auto_set_icons_always), getString(R.string.auto_set_icons_never), getString(R.string.auto_set_icons_when_new))
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values)

        activity_settings_auto_set_icons_spinner.adapter = adapter
        activity_settings_auto_set_icons_spinner.setSelection(viewModel.getAutoSetIcons())

        activity_settings_auto_set_icons_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setAutoSetIcons(position)
            }
        }
    }

    private fun setupShowTimestampSpinner() {
        val values = listOf(getString(R.string.show_timestamp_always), getString(R.string.show_timestamp_never), getString(R.string.show_timestamp_when_synced))
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, values)

        activity_settings_show_timestamp_spinner.adapter = adapter
        activity_settings_show_timestamp_spinner.setSelection(viewModel.getTimestampDisplay())

        activity_settings_show_timestamp_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setTimestampDisplay(position)
            }
        }
    }

    fun setupButtons() {
        activity_settings_privacy_policy_button.setOnClickListener {
            startActivity(
                Intent(this, WebViewActivity::class.java)
                    .putExtra(Values.WEB_VIEW_TYPE, Values.WEB_VIEW_PRIVACY_POLICY)
            )
        }

        activity_settings_terms_and_conditions_button.setOnClickListener {
            startActivity(
                Intent(this, WebViewActivity::class.java)
                    .putExtra(Values.WEB_VIEW_TYPE, Values.WEB_VIEW_TERMS_AND_CONDITIONS)
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