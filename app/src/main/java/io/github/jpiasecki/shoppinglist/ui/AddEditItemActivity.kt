package io.github.jpiasecki.shoppinglist.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.ui.viewmodels.AddEditItemViewModel
import kotlinx.android.synthetic.main.activity_add_edit_item.*

@AndroidEntryPoint
class AddEditItemActivity : AppCompatActivity() {

    private val viewModel: AddEditItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_item)

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)
        val itemId = intent.getStringExtra(Values.ITEM_ID)

        if (listId == null)
            finish()

        if (itemId != null) {
            viewModel.getShoppingList(listId).observe(this, Observer {
                it.items.firstOrNull { it.id == itemId }?.let {
                    activity_add_edit_item_name.setText(it.name)
                    activity_add_edit_item_note.setText(it.note)
                    activity_add_edit_item_quantity.setText(it.quantity.toString())
                }
            })
        }

        activity_add_edit_item_fab.setOnClickListener {
            var item = Item(
                name = activity_add_edit_item_name.text.toString(),
                note = activity_add_edit_item_note.text.toString(),
                quantity = activity_add_edit_item_quantity.text.toString().toInt(),
                addedBy = FirebaseAuth.getInstance().currentUser?.uid
            )

            if (itemId != null) {
                item = item.copy(id = itemId)
            }

            viewModel.addItemToList(listId, item).observe(this, Observer {
                if (it == true) {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (it == false) {
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}