package io.github.jpiasecki.shoppinglist.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.database.User
import java.text.DateFormat
import java.util.*

class ShoppingListItemsAdapter(
    private val clickCallback: (id: String) -> Unit
) : ListAdapter<Item, ShoppingListItemsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}) {

    private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)

    private var usersList: List<User> = emptyList()
    private var shoppingList: ShoppingList = ShoppingList()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.unbind()
    }

    override fun getItemId(position: Int): Long {
        return UUID.fromString(getItem(position).id).mostSignificantBits
    }

    @Synchronized
    fun setList(list: ShoppingList) {
        shoppingList = list

        submitList(list.items)
    }

    fun setUsers(list: List<User>) {
        usersList = list

        for (user in list) {
            for ((index, item) in currentList.iterator().withIndex()) {
                if (user.id == item.addedBy || user.id == item.completedBy) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            view.findViewById<TextView>(R.id.row_shopping_list_item_name).text = getItem(position).name
            view.findViewById<TextView>(R.id.row_shopping_list_item_note).text = getItem(position).note
            view.findViewById<TextView>(R.id.row_shopping_list_item_quantity).text = "Quantity: ${getItem(position).quantity}"
            view.findViewById<TextView>(R.id.row_shopping_list_item_price).text = getItem(position).price.toString()
            view.findViewById<TextView>(R.id.row_shopping_list_item_last_update).text = view.context.getString(R.string.shopping_list_last_update, dateFormat.format(Date(getItem(position).timestamp)))

            view.findViewById<ImageView>(R.id.row_shopping_list_item_icon).setImageResource(R.drawable.ic_item_default_24)

            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box).isChecked = getItem(position).isCompleted

            if (getItem(position).isCompleted) {
                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.VISIBLE
            } else {
                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.GONE
            }

            if (shoppingList.keepInSync) {
                setProfilePictures(position)
            } else {
                view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon).setImageBitmap(null)
                view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).setImageBitmap(null)
            }

            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box).setOnCheckedChangeListener { buttonView, isChecked ->
                Toast.makeText(buttonView.context, "${getItem(position).name} completed: $isChecked", Toast.LENGTH_SHORT).show()
            }

            view.setOnClickListener {
                clickCallback(getItem(position).id)
            }

            view.setOnLongClickListener {
                Toast.makeText(view.context, "${getItem(position).name} long clicked", Toast.LENGTH_SHORT).show()

                true
            }
        }

        fun unbind() {
            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box).setOnCheckedChangeListener(null)
        }

        private fun setProfilePictures(position: Int) {
            usersList.find { it.id == getItem(position).addedBy }?.let {
                if (it.profilePicture != null) {
                    view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon)
                        .setImageBitmap(it.profilePicture)
                } else {
                    view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon).setImageBitmap(null)

                    it.loadProfileImage(view.context) {
                        view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon)
                            .setImageBitmap(it.profilePicture)
                    }
                }
            }

            val completedBy = usersList.find { it.id == getItem(position).completedBy }

            if (completedBy == null) {
                view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).setImageBitmap(null)
            } else {
                if (completedBy.profilePicture != null) {
                    view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon)
                        .setImageBitmap(completedBy.profilePicture)
                } else {
                    view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon).setImageBitmap(null)

                    completedBy.loadProfileImage(view.context) {
                        view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon)
                            .setImageBitmap(completedBy.profilePicture)
                    }
                }
            }
        }
    }
}