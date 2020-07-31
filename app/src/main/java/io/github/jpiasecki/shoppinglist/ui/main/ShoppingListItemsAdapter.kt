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

class ShoppingListItemsAdapter() : ListAdapter<Item, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}) {

    lateinit var clickCallback: (id: String) -> Unit
    lateinit var itemCompletionChangeCallback: (id: String, completed: Boolean) -> Unit

    private val VIEW_TYPE_ITEM = 1
    private val VIEW_TYPE_HEADER = 2

    private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)

    private var usersList: List<User> = emptyList()
    private var shoppingList: ShoppingList = ShoppingList()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_header, parent, false))
            else -> ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder)
            holder.bind()
        else if (holder is ItemViewHolder)
            holder.bind(position)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ItemViewHolder)
            holder.unbind()
    }

    override fun getItemId(position: Int): Long {
        return UUID.fromString(getItem(position).id).mostSignificantBits
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            VIEW_TYPE_HEADER
        else
            VIEW_TYPE_ITEM
    }

    @Synchronized
    fun setList(list: ShoppingList) {
        shoppingList = list

        submitList(list.items.toMutableList().also { it.add(0, Item(id = "00000000-0000-0000-0000-000000000000")) })
    }

    @Synchronized
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

    inner class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind() {
            view.findViewById<TextView>(R.id.row_shopping_list_header_name).text = shoppingList.name

            view.findViewById<ImageView>(R.id.row_shopping_list_header_synced_icon).setImageResource(if (shoppingList.keepInSync) R.drawable.ic_cloud_24 else R.drawable.ic_smartphone_24)

            view.findViewById<ImageView>(R.id.row_shopping_list_header_icon).setImageResource(R.drawable.ic_list_default_24)
        }
    }

    inner class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val item = getItem(position)

            view.findViewById<TextView>(R.id.row_shopping_list_item_name).text = item.name
            view.findViewById<TextView>(R.id.row_shopping_list_item_note).text = item.note
            view.findViewById<TextView>(R.id.row_shopping_list_item_quantity).text = "Quantity: ${item.quantity}"
            view.findViewById<TextView>(R.id.row_shopping_list_item_price).text = item.price.toString()
            view.findViewById<TextView>(R.id.row_shopping_list_item_last_update).text = view.context.getString(R.string.shopping_list_last_update, dateFormat.format(Date(item.timestamp)))

            view.findViewById<ImageView>(R.id.row_shopping_list_item_icon).setImageResource(R.drawable.ic_item_default_24)

            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box).isChecked = item.isCompleted

            if (item.isCompleted) {
                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.VISIBLE
            } else {
                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.GONE
            }

            if (shoppingList.keepInSync) {
                setProfilePictures(item)
            } else {
                view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon).setImageBitmap(null)
                view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).setImageBitmap(null)
            }

            view.findViewById<View>(R.id.row_shopping_list_item_check_box_overlay).setOnClickListener {
                val checkBox =  view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box)
                checkBox.isChecked = !checkBox.isChecked

                itemCompletionChangeCallback(item.id, checkBox.isChecked)

                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.VISIBLE
            }

            view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay_hitbox).setOnClickListener {
                val checkBox =  view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box)
                checkBox.isChecked = false

                itemCompletionChangeCallback(item.id, false)

                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.GONE
            }

            view.setOnClickListener {
                clickCallback(item.id)
            }

            view.setOnLongClickListener {
                Toast.makeText(view.context, "${item.name} long clicked", Toast.LENGTH_SHORT).show()

                true
            }
        }

        fun unbind() {
            view.findViewById<View>(R.id.row_shopping_list_item_check_box_overlay).setOnClickListener(null)
        }

        private fun setProfilePictures(item: Item) {
            usersList.find { it.id == item.addedBy }?.let {
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

            val completedBy = usersList.find { it.id == item.completedBy }

            if (completedBy == null) {
                view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).setImageBitmap(null)
            } else {
                if (completedBy.profilePicture != null) {
                    view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon)
                        .setImageBitmap(completedBy.profilePicture)
                } else {
                    view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).setImageBitmap(null)

                    completedBy.loadProfileImage(view.context) {
                        view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon)
                            .setImageBitmap(completedBy.profilePicture)
                    }
                }
            }
        }
    }
}