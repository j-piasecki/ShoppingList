package io.github.jpiasecki.shoppinglist.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.User
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

    private var usersList: List<User> = emptyList()

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

    override fun getItemId(position: Int): Long {
        return UUID.fromString(getItem(position).id).mostSignificantBits
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

            view.setOnClickListener {
                clickCallback(getItem(position).id)
            }

            view.setOnLongClickListener {
                Toast.makeText(view.context, "${getItem(position).name} long clicked", Toast.LENGTH_SHORT).show()

                true
            }
        }
    }
}