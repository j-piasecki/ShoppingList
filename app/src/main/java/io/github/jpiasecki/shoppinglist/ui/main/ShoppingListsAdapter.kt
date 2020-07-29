package io.github.jpiasecki.shoppinglist.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.database.User
import java.text.DateFormat
import java.util.*

class ShoppingListsAdapter(
    private val clickCallback: (id: String) -> Unit,
    private val longClickCallback: (id: String) -> Unit
) : RecyclerView.Adapter<ShoppingListsAdapter.ViewHolder>() {

    private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)

    private var dataSet: List<ShoppingList> = emptyList()
    private var userList: List<User> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setDataSet(data: List<ShoppingList>) {
        dataSet = data

        notifyDataSetChanged()
    }

    fun setUsers(data: List<User>) {
        userList = data

        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val owner = userList.firstOrNull { it.id == dataSet[position].owner }

            view.findViewById<TextView>(R.id.row_shopping_list_name).text = dataSet[position].name
            view.findViewById<TextView>(R.id.row_shopping_list_note).text = dataSet[position].note

            view.findViewById<TextView>(R.id.row_shopping_list_last_update).text = view.context.getString(R.string.shopping_list_last_update, dateFormat.format(Date(dataSet[position].timestamp)))

            view.findViewById<ImageView>(R.id.row_shopping_list_synced_icon).setImageResource(if (dataSet[position].keepInSync) R.drawable.ic_cloud_24 else R.drawable.ic_smartphone_24)

            view.findViewById<ImageView>(R.id.row_shopping_list_icon).setImageResource(R.drawable.ic_list_default_24)

            if (dataSet[position].keepInSync) {
                owner?.apply {
                    loadProfileImage(view.context) {
                        view.findViewById<ImageView>(R.id.row_shopping_list_owner_icon)
                            .setImageBitmap(profilePicture)
                    }
                }
            } else {
                view.findViewById<ImageView>(R.id.row_shopping_list_owner_icon)
                    .setImageBitmap(null)
            }

            view.setOnClickListener {
                clickCallback(dataSet[position].id)
            }

            view.setOnLongClickListener {
                longClickCallback(dataSet[position].id)

                true
            }
        }
    }
}