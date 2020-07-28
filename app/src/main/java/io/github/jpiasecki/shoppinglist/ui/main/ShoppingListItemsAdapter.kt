package io.github.jpiasecki.shoppinglist.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.User

class ShoppingListItemsAdapter(
    private val clickCallback: (id: String) -> Unit
) : RecyclerView.Adapter<ShoppingListItemsAdapter.ViewHolder>() {

    private var dataSet: List<Item> = emptyList()
    private var usersList: List<User> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setData(list: List<Item>) {
        dataSet = list

        notifyDataSetChanged()
    }

    fun setUsers(list: List<User>) {
        usersList = list

        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            view.findViewById<TextView>(R.id.row_shopping_list_item_name).text = dataSet[position].name
            view.findViewById<TextView>(R.id.row_shopping_list_item_note).text = dataSet[position].note
            view.findViewById<TextView>(R.id.row_shopping_list_item_quantity).text = "Quantity: ${dataSet[position].quantity}"

            view.setOnClickListener {
                clickCallback(dataSet[position].id)
            }

            view.setOnLongClickListener {
                Toast.makeText(view.context, "${dataSet[position].name} long clicked", Toast.LENGTH_SHORT).show()

                true
            }
        }
    }
}