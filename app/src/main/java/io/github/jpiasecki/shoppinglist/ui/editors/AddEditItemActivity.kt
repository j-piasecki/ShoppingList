package io.github.jpiasecki.shoppinglist.ui.editors

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
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
import io.github.jpiasecki.shoppinglist.ui.viewmodels.AddEditItemViewModel
import kotlinx.android.synthetic.main.activity_add_edit_item.*

@AndroidEntryPoint
class AddEditItemActivity : AppCompatActivity() {

    private val viewModel: AddEditItemViewModel by viewModels()
    private var listSynced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_item)

        setSupportActionBar(activity_add_edit_item_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)
        val itemId = intent.getStringExtra(Values.ITEM_ID)

        if (listId == null)
            finish()

        if (itemId != null) {
            viewModel.getShoppingList(listId!!).observe(this, Observer {
                listSynced = it.keepInSync

                it.items.firstOrNull { it.id == itemId }?.let {
                    activity_add_edit_item_name.setText(it.name)
                    activity_add_edit_item_note.setText(it.note)

                    if (it.quantity > 0)
                        activity_add_edit_item_quantity.setText(it.quantity.toString())

                    populateUnitSpinner(it.quantity, it.unit)

                    if (it.price > 0)
                        activity_add_edit_item_price.setText(it.price.toString())

                    activity_add_edit_item_icon.setImageResource(R.drawable.ic_item_default_24)
                }
            })

            supportActionBar?.setTitle(R.string.activity_add_edit_item_edit_item)
        }

        activity_add_edit_item_quantity.addTextChangedListener {
            populateUnitSpinner(it.toString().toIntOrNull() ?: 0, activity_add_edit_item_unit.selectedItemPosition)
        }

        activity_add_edit_item_icon.setOnClickListener {
            showToast("change icon")
        }

        activity_add_edit_item_fab.setOnClickListener {
            var item = Item(
                name = activity_add_edit_item_name.text.toString(),
                note = activity_add_edit_item_note.text.toString(),
                quantity = activity_add_edit_item_quantity.text.toString().toIntOrNull() ?: 0,
                addedBy = FirebaseAuth.getInstance().currentUser?.uid,
                price = activity_add_edit_item_price.text.toString().toDoubleOrNull() ?: 0.0,
                icon = Icons.DEFAULT,
                unit = Units.NO_UNIT
            )

            if (itemId != null) {
                item = item.copy(id = itemId)
            }

            if (!listSynced || Config.isNetworkConnected(this)) {
                viewModel.addItemToList(listId!!, item)
                finish()
            } else {
                showToast(getString(R.string.message_need_internet_to_modify_list))
            }
        }
    }

    private fun populateUnitSpinner(quantity: Int, currentUnit: Int) {
        val list = listOf("Unit 1", "Unit 2", "Unit 3", "Unit 4")
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list)

        activity_add_edit_item_unit.adapter = adapter
        activity_add_edit_item_unit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                showToast("item selected: $position")
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