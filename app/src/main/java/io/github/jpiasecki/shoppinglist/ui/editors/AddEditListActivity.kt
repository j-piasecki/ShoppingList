package io.github.jpiasecki.shoppinglist.ui.editors

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Icons
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.ui.main.MainActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.AddEditListViewModel
import kotlinx.android.synthetic.main.activity_add_edit_list.*
import kotlinx.coroutines.*
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
    private var selectedIcon = Icons.DEFAULT

    private lateinit var adapter: ListCategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_list)

        setSupportActionBar(activity_add_edit_list_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        getCurrencies()

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)

        initCategoriesRecyclerView()

        tryLoadingList(listId)

        activity_add_edit_list_add_category_button.setOnClickListener {
            addCategory()
        }

        setupIcon(listId)

        setupFab()
    }

    private fun tryLoadingList(listId: String?) {
        if (listId != null) {
            viewModel.getList(listId).observe(this, Observer {
                currentList = it
                createNew = false
                listSynced = it.keepInSync

                activity_add_edit_list_name.setText(it.name)
                activity_add_edit_list_note.setText(it.note)
                setSelectedCurrency(it.currency)

                selectedIcon = it.icon
                activity_add_edit_list_icon.setImageResource(Icons.getListIconId(it.icon))

                adapter.setData(it.categories)
            })

            supportActionBar?.title = getString(R.string.activity_add_edit_list_edit_list)
        }
    }

    private fun setupIcon(listId: String?) {
        activity_add_edit_list_icon.setOnClickListener {
            startActivityForResult(
                Intent(this, SelectIconActivity::class.java)
                    .putExtra(Values.SHOPPING_LIST_ID, listId)
                    .putExtra(Values.NAME, activity_add_edit_list_name.text.toString()),
                Values.RC_SELECT_ICON,
                MainActivity.getAnimationBundle(activity_add_edit_list_icon)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Values.RC_SELECT_ICON) {
            val icon = data?.getIntExtra(Values.ICON, -1) ?: -1

            if (icon != -1) {
                selectedIcon = icon
                activity_add_edit_list_icon.setImageResource(Icons.getItemIconId(icon))
            }
        }
    }

    private fun addCategory() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_edit_category, null)
        dialog.setContentView(view)

        view.findViewById<MaterialButton>(R.id.dialog_add_category_save_button).setOnClickListener {
            val name = view.findViewById<TextView>(R.id.dialog_add_category_name).text.toString().trim()

            if (name.length >= 3) {
                adapter.insertItem(data = mapOf("name" to name, "id" to UUID.randomUUID().toString()))
                dialog.dismiss()
            } else {
                showToast(getString(R.string.message_category_name_too_short))
            }
        }

        dialog.show()
    }

    private fun editCategory(position: Int, data: Map<String, String>) {
        val id = data["id"] ?: return

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_edit_category, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.dialog_add_category_header).text = getString(R.string.dialog_add_category_header_edit)
        view.findViewById<TextInputEditText>(R.id.dialog_add_category_name).setText(data["name"])

        view.findViewById<MaterialButton>(R.id.dialog_add_category_save_button).setOnClickListener {
            val name = view.findViewById<TextInputEditText>(R.id.dialog_add_category_name).text.toString().trim()

            if (name.length >= 3) {
                adapter.updateItem(position, mapOf("name" to name, "id" to id))
                dialog.dismiss()
            } else {
                showToast(getString(R.string.message_category_name_too_short))
            }
        }

        dialog.show()
    }

    private fun setupFab() {
        activity_add_edit_list_fab.setOnClickListener {
            val name = activity_add_edit_list_name.text.toString().trim()

            if (name.isNotBlank()) {
                currentList.name = name
                currentList.note = activity_add_edit_list_note.text.toString()
                currentList.currency =
                    currencyList[activity_add_edit_list_currency.selectedItemPosition].currencyCode
                currentList.timestamp = Calendar.getInstance().timeInMillis
                currentList.icon = selectedIcon
                currentList.categories = adapter.getCategories()

                if (createNew) {
                    viewModel.createList(currentList)
                    finish()
                } else if (!listSynced || Config.isNetworkConnected(this)) {
                    GlobalScope.launch(Dispatchers.Main) {
                        viewModel.updateList(currentList)
                        delay(50)
                        finish()
                    }
                } else {
                    showToast(getString(R.string.message_need_internet_to_modify_list))
                }
            } else {
                showToast(getString(R.string.message_list_must_have_name))
            }
        }
    }

    private fun initCategoriesRecyclerView() {
        adapter = ListCategoriesAdapter().also {
            it.itemRemovedCallback = { position, data ->
                Snackbar.make(activity_add_edit_list_layout, R.string.activity_add_edit_list_category_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.activity_add_edit_list_undo) {
                        adapter.insertItem(position, data)
                    }.show()
            }

            it.itemClickedCallback = { position, data ->
                editCategory(position, data)
            }
        }

        activity_add_edit_list_recycler_view.adapter = adapter
        activity_add_edit_list_recycler_view.layoutManager = LinearLayoutManager(this)

        val callback = ListCategoriesCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(activity_add_edit_list_recycler_view)
        adapter.touchHelper = touchHelper
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

    private fun showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
        val toast = Toast.makeText(this, text, length)

        toast.view.findViewById<TextView>(android.R.id.message)?.let {
            it.gravity = Gravity.CENTER
        }

        toast.show()
    }
}