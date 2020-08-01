package io.github.jpiasecki.shoppinglist.ui.editors

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.ui.viewmodels.AddEditListViewModel
import kotlinx.android.synthetic.main.activity_add_edit_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class AddEditListActivity : AppCompatActivity() {

    private val viewModel: AddEditListViewModel by viewModels()

    private var currencyList: List<Currency> = emptyList()

    private var currentList = ShoppingList(
        owner = FirebaseAuth.getInstance().currentUser?.uid,
        keepInSync = false
    )
    private var createNew = true
    private var listSynced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_list)

        setSupportActionBar(activity_add_edit_list_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        getCurrencies()

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)

        if (listId != null) {
            viewModel.getList(listId).observe(this, Observer {
                currentList = it
                createNew = false
                listSynced = it.keepInSync

                activity_add_edit_list_name.setText(it.name)
                activity_add_edit_list_note.setText(it.note)
                setSelectedCurrency(it.currency)

                activity_add_edit_list_icon.setImageResource(R.drawable.ic_list_default_24)
            })

            supportActionBar?.title = getString(R.string.activity_add_edit_list_edit_list)
        }

        activity_add_edit_list_icon.setOnClickListener {
            Toast.makeText(this, "change icon", Toast.LENGTH_SHORT).show()
        }

        activity_add_edit_list_fab.setOnClickListener {
            currentList.name = activity_add_edit_list_name.text.toString()
            currentList.note = activity_add_edit_list_note.text.toString()
            currentList.currency = currencyList[activity_add_edit_list_currency.selectedItemPosition].currencyCode
            currentList.timestamp = Calendar.getInstance().timeInMillis

            if (createNew) {
                viewModel.createList(currentList)
                finish()
            } else if (!listSynced || Config.isNetworkConnected(this)) {
                viewModel.updateList(currentList)
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.message_need_internet_to_modify_list),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun populateCurrencySpinner() {
        val displayList = ArrayList<String>()
        for (currency in currencyList) {
            displayList.add("${currency.currencyCode} (${currency.displayName})")
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, displayList)

        activity_add_edit_list_currency.adapter = adapter
    }

    private fun setSelectedCurrency(currencyCode: String? = null) {
        if (currencyCode != null) {
            val index = currencyList.indexOfFirst { it.currencyCode == currencyCode }

            if (index != -1) {
                activity_add_edit_list_currency.setSelection(index)

                return
            }
        }

        try {
            val currency = Currency.getInstance(Locale.getDefault())

            currencyList.indexOfFirst { it.currencyCode == currency.currencyCode }.let {
                if (it != -1) {
                    activity_add_edit_list_currency.setSelection(it)
                }
            }
        } catch (e: Exception) {}
    }

    private fun getCurrencies() {
        GlobalScope.launch {
            val list = ArrayList<Currency>()

            for (locale in Locale.getAvailableLocales()) {
                try {
                    val currency = Currency.getInstance(locale)

                    if (list.firstOrNull { it.currencyCode == currency.currencyCode } == null) {
                        list.add(currency)
                    }
                } catch (e: Exception) { }
            }

            list.sortBy { it.currencyCode }

            currencyList = list

            withContext(Dispatchers.Main) {
                populateCurrencySpinner()

                setSelectedCurrency(currentList.currency)
            }
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