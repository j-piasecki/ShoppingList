package io.github.jpiasecki.shoppinglist.ui.editors

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Icons
import io.github.jpiasecki.shoppinglist.consts.Units
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.ui.main.MainActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.AddEditItemViewModel
import kotlinx.android.synthetic.main.activity_add_edit_item.*
import kotlinx.coroutines.*

@AndroidEntryPoint
class AddEditItemActivity : AppCompatActivity() {

    private val viewModel: AddEditItemViewModel by viewModels()
    private lateinit var currentList: ShoppingList
    private var listSynced = false
    private var autoSetIcon = true

    private var selectedIcon = Icons.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_item)

        setSupportActionBar(activity_add_edit_item_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        if (viewModel.getAutoSetIcons() == Config.AUTO_SET_NEVER)
            autoSetIcon = false

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)
        val itemId = intent.getStringExtra(Values.ITEM_ID)

        if (listId == null)
            finish()

        tryLoadingItem(listId!!, itemId)

        setupTextListeners()

        setupIcon(listId, itemId)

        setupFab(listId, itemId)

        setupAutoComplete()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Values.RC_SELECT_ICON) {
            val icon = data?.getIntExtra(Values.ICON, -1) ?: -1

            if (icon != -1) {
                selectedIcon = icon
                activity_add_edit_item_icon.setImageResource(Icons.getItemIconId(icon))
            }
        }
    }

    private fun populateCategorySpinner(currentCategory: String? = null) {
        val list = ArrayList<String>()
        list.add(getString(R.string.activity_add_edit_item_no_category))

        for (category in currentList.categories) {
            list.add(category["name"] ?: getString(R.string.activity_add_edit_item_category_error))
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list)

        activity_add_edit_item_category.adapter = adapter

        if (currentCategory != null)
            activity_add_edit_item_category.setSelection(currentList.categories.indexOfFirst { it["id"] == currentCategory } + 1, true)
        else
            activity_add_edit_item_category.setSelection(0, true)
    }

    private fun getSelectedCategory(): String? {
        if (activity_add_edit_item_category.selectedItemPosition == 0)
            return null

        return currentList.categories[activity_add_edit_item_category.selectedItemPosition - 1]["id"]
    }

    private fun populateUnitSpinner(quantity: Int, currentUnit: Int) {
        val list = ArrayList<String>()

        for (unit in Units.ALL) {
            if (unit == Units.NO_UNIT) {
                list.add("-")
            } else {
                list.add(resources.getQuantityString(Units.getStringId(unit), quantity, -1).replace("-1", ""))
            }
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list)

        activity_add_edit_item_unit.adapter = adapter
        activity_add_edit_item_unit.setSelection(Units.ALL.indexOf(currentUnit))
    }

    private fun tryLoadingItem(listId: String, itemId: String?) {
        viewModel.getShoppingList(listId).observe(this, Observer {
            currentList = it
            listSynced = it.keepInSync

            if (itemId != null) {
                supportActionBar?.setTitle(R.string.activity_add_edit_item_edit_item)
                autoSetIcon = viewModel.getAutoSetIcons() == Config.AUTO_SET_ALWAYS

                it.items.firstOrNull { it.id == itemId }?.let {
                    activity_add_edit_item_name.setText(it.name)
                    activity_add_edit_item_note.setText(it.note)

                    if (it.quantity > 0)
                        activity_add_edit_item_quantity.setText(it.quantity.toString())

                    populateCategorySpinner(it.category)
                    populateUnitSpinner(it.quantity, it.unit)

                    if (it.price > 0)
                        activity_add_edit_item_price.setText(it.price.toString())

                    selectedIcon = it.icon
                    activity_add_edit_item_icon.setImageResource(Icons.getItemIconId(it.icon))
                }
            } else {
                populateCategorySpinner()
                populateUnitSpinner(0, Units.NO_UNIT)
            }
        })
    }

    private fun setupTextListeners() {
        activity_add_edit_item_quantity.addTextChangedListener {
            populateUnitSpinner(
                it.toString().toIntOrNull() ?: 0,
                if (activity_add_edit_item_unit.selectedItemPosition >= 0) Units.ALL[activity_add_edit_item_unit.selectedItemPosition] else Units.NO_UNIT
            )
        }

        activity_add_edit_item_name.addTextChangedListener {
            if (autoSetIcon) {
                val newIcon = Icons.getIconFromName(it.toString(), this)

                if (selectedIcon != newIcon) {
                    selectedIcon = newIcon
                    activity_add_edit_item_icon.setImageResource(Icons.getItemIconId(newIcon))
                }
            }
        }
    }

    private fun setupIcon(listId: String, itemId: String?) {
        activity_add_edit_item_icon.setOnClickListener {
            startActivityForResult(
                Intent(this, SelectIconActivity::class.java)
                    .putExtra(Values.SHOPPING_LIST_ID, listId)
                    .putExtra(Values.ITEM_ID, itemId)
                    .putExtra(Values.NAME, activity_add_edit_item_name.text.toString()),
                Values.RC_SELECT_ICON,
                MainActivity.getAnimationBundle(activity_add_edit_item_icon)
            )
        }
    }

    private fun setupFab(listId: String, itemId: String?) {
        activity_add_edit_item_fab.setOnClickListener {
            val name = activity_add_edit_item_name.text.toString().trim()

            if (name.isNotEmpty()) {
                var item = Item(
                    name = name,
                    note = activity_add_edit_item_note.text.toString(),
                    quantity = activity_add_edit_item_quantity.text.toString().toIntOrNull() ?: 0,
                    addedBy = FirebaseAuth.getInstance().currentUser?.uid,
                    price = activity_add_edit_item_price.text.toString().toDoubleOrNull() ?: 0.0,
                    icon = selectedIcon,
                    unit = Units.ALL[activity_add_edit_item_unit.selectedItemPosition],
                    category = getSelectedCategory()
                )

                if (itemId != null) {
                    item = item.copy(id = itemId)
                }

                if (!listSynced || Config.isNetworkConnected(this)) {
                    GlobalScope.launch(Dispatchers.Main) {
                        viewModel.addItemToList(listId, item)
                        delay(50)
                        finish()
                    }
                } else {
                    showToast(getString(R.string.message_need_internet_to_modify_list))
                }
            } else {
                showToast(getString(R.string.message_item_must_have_name))
            }
        }
    }

    private fun setupAutoComplete() {
        GlobalScope.launch(Dispatchers.Main) {
            var items = emptyMap<String?, List<Item>>()

            withContext(Dispatchers.IO) {
                items = viewModel.getForAutoComplete()
            }

            val adapter = ItemsAutoCompleteAdapter(this@AddEditItemActivity, items)
            activity_add_edit_item_name.threshold = 1
            activity_add_edit_item_name.setAdapter(adapter)
            activity_add_edit_item_name.setOnItemClickListener { parent, view, position, id ->
                val allItems = parent.getItemAtPosition(position) as List<Item>
                val item = allItems.firstOrNull()

                if (item != null) {
                    activity_add_edit_item_name.setText(item.name)
                    activity_add_edit_item_note.setText(allItems.firstOrNull { it.note != null }?.note)

                    val quantityItem = allItems.firstOrNull { it.quantity > 0 }
                    if (quantityItem != null)
                        activity_add_edit_item_quantity.setText(quantityItem.quantity.toString())

                    populateUnitSpinner(activity_add_edit_item_quantity.text.toString().toIntOrNull() ?: 0, allItems.firstOrNull { it.unit != Units.NO_UNIT }?.unit ?: Units.NO_UNIT)

                    val priceItem = allItems.firstOrNull { it.price > 0 }
                    if (priceItem != null)
                        activity_add_edit_item_price.setText(priceItem.price.toString())

                    selectedIcon = allItems.firstOrNull { it.icon != Icons.DEFAULT }?.icon ?: Icons.DEFAULT
                    activity_add_edit_item_icon.setImageResource(Icons.getItemIconId(selectedIcon))

                    for (candidate in allItems) {
                        if (currentList.hasCategory(candidate.category)) {
                            populateCategorySpinner(candidate.category)
                            break
                        }
                    }
                }
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