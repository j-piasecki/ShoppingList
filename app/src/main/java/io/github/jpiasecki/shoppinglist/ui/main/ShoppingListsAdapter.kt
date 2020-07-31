package io.github.jpiasecki.shoppinglist.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.database.User
import java.text.DateFormat
import java.util.*

class ShoppingListsAdapter() : ListAdapter<ShoppingList, ShoppingListsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<ShoppingList>() {
    override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
        return oldItem.owner == newItem.owner &&
                oldItem.timestamp == newItem.timestamp &&
                oldItem.currency == newItem.currency &&
                oldItem.name == newItem.name &&
                oldItem.icon == newItem.icon &&
                oldItem.note == newItem.note &&
                oldItem.keepInSync == newItem.keepInSync
    }
}) {

    private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
    
    private var userList: List<User> = emptyList()

    lateinit var clickCallback: (id: String) -> Unit
    lateinit var longClickCallback: (id: String) -> Unit

    init {
        setHasStableIds(true)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return UUID.fromString(getItem(position).id).mostSignificantBits
    }

    fun setUsers(data: List<User>) {
        userList = data

        for (user in data) {
            for ((index, list) in currentList.iterator().withIndex()) {
                if (list.keepInSync && user.id == list.owner) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val list = getItem(position)
            val owner = userList.firstOrNull { it.id == list.owner }

            view.findViewById<TextView>(R.id.row_shopping_list_name).text = list.name
            view.findViewById<TextView>(R.id.row_shopping_list_note).text = list.note

            view.findViewById<TextView>(R.id.row_shopping_list_last_update).text = view.context.getString(R.string.last_update, dateFormat.format(Date(list.timestamp)))

            view.findViewById<ImageView>(R.id.row_shopping_list_synced_icon).setImageResource(if (list.keepInSync) R.drawable.ic_cloud_24 else R.drawable.ic_smartphone_24)

            view.findViewById<ImageView>(R.id.row_shopping_list_icon).setImageResource(R.drawable.ic_list_default_24)

            if (list.keepInSync) {
                owner?.apply {
                    if (this.profilePicture != null) {
                        view.findViewById<ImageView>(R.id.row_shopping_list_owner_icon)
                            .setImageBitmap(profilePicture)
                    } else {
                        loadProfileImage(view.context) {
                            view.findViewById<ImageView>(R.id.row_shopping_list_owner_icon)
                                .setImageBitmap(profilePicture)

                            notifyItemChanged(adapterPosition)
                        }
                    }
                } ?: view.findViewById<ImageView>(R.id.row_shopping_list_owner_icon)
                    .setImageBitmap(null)
            } else {
                view.findViewById<ImageView>(R.id.row_shopping_list_owner_icon)
                    .setImageBitmap(null)
            }

            view.setOnClickListener {
                clickCallback(list.id)
            }

            view.setOnLongClickListener {
                longClickCallback(list.id)

                true
            }
        }
    }
}