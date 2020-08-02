package io.github.jpiasecki.shoppinglist.ui.main

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Units
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.database.User
import java.lang.Exception
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

class ShoppingListItemsAdapter() : ListAdapter<Item, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}) {

    lateinit var clickCallback: (id: String, view: View) -> Unit
    lateinit var longClickCallback: (item: Item, view: View) -> Unit
    lateinit var itemCompletionChangeCallback: (id: String, completed: Boolean) -> Unit
    lateinit var userListClickCallback: (id: String, view: View) -> Unit

    private val VIEW_TYPE_ITEM = 1
    private val VIEW_TYPE_HEADER = 2

    private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)

    private var usersList: List<User> = emptyList()
    private var shoppingList: ShoppingList = ShoppingList()
    private var priceFormat = NumberFormat.getCurrencyInstance()

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
        if (list.currency != null) {
            try {
                val currency = Currency.getInstance(list.currency)

                priceFormat.currency = currency
                priceFormat.maximumFractionDigits = currency.defaultFractionDigits
                priceFormat.minimumFractionDigits = currency.defaultFractionDigits
            } catch (e: Exception) {}
        }

        if (list.id == shoppingList.id && list.currency != shoppingList.currency)
            notifyDataSetChanged()

        shoppingList = list

        submitList(list.items.filter { !it.deleted }.toMutableList().also {
            it.add(0,
                Item(
                    id = "00000000-0000-0000-0000-000000000000",
                    timestamp = UUID.fromString(list.id).mostSignificantBits + list.getAllUsersNoOwner().size
                )
            )
        })
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

        private val scale = view.context.resources.displayMetrics.density

        fun bind() {
            view.findViewById<TextView>(R.id.row_shopping_list_header_name).text = shoppingList.name

            view.findViewById<ImageView>(R.id.row_shopping_list_header_synced_icon).setImageResource(if (shoppingList.keepInSync) R.drawable.ic_cloud_24 else R.drawable.ic_smartphone_24)

            view.findViewById<ImageView>(R.id.row_shopping_list_header_icon).setImageResource(R.drawable.ic_list_default_24)

            createUserList()
        }

        private fun createUserList() {
            val iconsLayout = view.findViewById<LinearLayout>(R.id.row_shopping_list_header_users)
            iconsLayout.removeAllViews()

            iconsLayout.setOnClickListener {
                userListClickCallback(shoppingList.id, it)
            }

            val userList = shoppingList.getAllUsersNoOwner()

            if (shoppingList.keepInSync && userList.isNotEmpty()) {
                iconsLayout.visibility = View.VISIBLE

                usersList.find { it.id == shoppingList.owner }?.let {
                    addUserToList(iconsLayout, it, true)
                }

                for ((index, userId) in userList.iterator().withIndex()) {
                    usersList.find { it.id == userId && it.id != shoppingList.owner }?.let {
                        addUserToList(iconsLayout, it)
                    }

                    if (index == 20)
                        break
                }

                iconsLayout.layoutAnimation =
                    AnimationUtils.loadLayoutAnimation(
                        view.context,
                        R.anim.users_list_animation
                    )
                iconsLayout.startLayoutAnimation()
            } else {
                iconsLayout.visibility = View.GONE
            }
        }

        private fun addUserToList(layout: LinearLayout, user: User, owner: Boolean = false) {
            user.loadProfileImage(view.context) {
                val img = ImageView(view.context)
                img.setImageBitmap(user.profilePicture)

                img.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).also {
                    if (owner) {
                        it.marginEnd = (8 * scale + 0.5f).roundToInt()
                    }
                }

                layout.addView(img)
            }
        }
    }

    inner class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val item = getItem(position)

            view.findViewById<TextView>(R.id.row_shopping_list_item_name).text = item.name
            view.findViewById<TextView>(R.id.row_shopping_list_item_note).text = item.note
            view.findViewById<TextView>(R.id.row_shopping_list_item_last_update).text = view.context.getString(R.string.last_update, dateFormat.format(Date(item.timestamp)))
            view.findViewById<TextView>(R.id.row_shopping_list_item_quantity).text =
                    view.context.resources.getQuantityString(Units.getStringId(item.unit), item.quantity, item.quantity)

            if (item.price > 0) {
                if (shoppingList.currency == null) {
                    view.findViewById<TextView>(R.id.row_shopping_list_item_price).text =
                        item.price.toString()
                } else {
                    view.findViewById<TextView>(R.id.row_shopping_list_item_price).text =
                        priceFormat.format(item.price).toString()
                }
            } else {
                view.findViewById<TextView>(R.id.row_shopping_list_item_price).text = ""
            }

            view.findViewById<ImageView>(R.id.row_shopping_list_item_icon).setImageResource(R.drawable.ic_item_default_24)

            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box).isChecked = item.completed

            if (item.completed) {
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
                if (!shoppingList.keepInSync || Config.isNetworkConnected(view.context)) {
                    val checkBox =
                        view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box)
                    checkBox.isChecked = !checkBox.isChecked

                    itemCompletionChangeCallback(item.id, checkBox.isChecked)

                    view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility =
                        View.VISIBLE
                } else {
                    showToast(view.context, view.context.getString(R.string.message_need_internet_to_modify_list))
                }
            }

            view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay_hitbox).setOnClickListener {
                if (!shoppingList.keepInSync || Config.isNetworkConnected(view.context)) {
                    val checkBox =
                        view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box)
                    checkBox.isChecked = false

                    itemCompletionChangeCallback(item.id, false)

                    view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility =
                        View.GONE
                } else {
                    showToast(view.context, view.context.getString(R.string.message_need_internet_to_modify_list))
                }
            }

            view.setOnClickListener {
                clickCallback(item.id, it)
            }

            view.setOnLongClickListener {
                longClickCallback(item, it)

                true
            }

            view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay)?.setOnLongClickListener {
                longClickCallback(item, it)

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

                TooltipCompat.setTooltipText(view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon), it.name)
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

                TooltipCompat.setTooltipText(view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon), completedBy.name)
            }
        }
    }

    private fun showToast(context: Context, text: String, length: Int = Toast.LENGTH_SHORT) {
        val toast = Toast.makeText(context, text, length)

        toast.view.findViewById<TextView>(android.R.id.message)?.let {
            it.gravity = Gravity.CENTER
        }

        toast.show()
    }
}