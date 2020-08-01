package io.github.jpiasecki.shoppinglist.ui.editors

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.database.User

class ListUsersAdapter : ListAdapter<ListUsersAdapter.AdapterItem, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<AdapterItem>() {
        override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
            return if (oldItem.header == null) {
                oldItem.user?.id == newItem.user?.id
            } else {
                oldItem.header == newItem.header
            }
        }

        override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
            return if (oldItem.header == null) {
                oldItem.user == newItem.user
            } else {
                oldItem.header == newItem.header
            }
        }
    }
) {
    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_USER = 1

    private var shoppingList: ShoppingList = ShoppingList(id = Values.SHOPPING_LIST_ID_NOT_FOUND)
    private var users: List<User> = emptyList()

    lateinit var userClickCallback: (user: User) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_list_user, parent, false))
        } else {
           HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_list_header, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(position)
        } else if (holder is HeaderViewHolder) {
            holder.bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return if (item.header != null) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_USER
        }
    }

    @Synchronized
    fun setList(list: ShoppingList) {
        shoppingList = list

        updateData()
    }

    @Synchronized
    fun setUsers(list: List<User>) {
        users = list

        updateData()
    }

    private fun updateData() {
        if (shoppingList.id != Values.SHOPPING_LIST_ID_NOT_FOUND && users.isNotEmpty()) {
            val result = ArrayList<AdapterItem>()
            result.add(AdapterItem(Values.USERS_LIST_HEADER_OWNER))
            result.add(AdapterItem(user = users.find { it.id == shoppingList.owner }))

            val listUsers = shoppingList.getAllUsersNoOwnerNoBan()
            if (listUsers.isNotEmpty()) {
                result.add(AdapterItem(Values.USERS_LIST_HEADER_USERS))

                for (userId in listUsers) {
                    users.find { it.id == userId }?.let {
                        result.add(AdapterItem(user = it))
                    }
                }
            }

            if (shoppingList.banned.isNotEmpty()) {
                result.add(AdapterItem(Values.USERS_LIST_HEADER_BANNED))

                result.addAll(shoppingList.banned.map { id ->
                    AdapterItem(user = users.find { it.id == id })
                })
            }

            submitList(result)
        }
    }

    inner class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val text = getItem(position).header

            if (text != null) {
                view.findViewById<TextView>(R.id.row_list_header_text).text = when (text) {
                    Values.USERS_LIST_HEADER_OWNER -> view.context.getString(R.string.activity_list_users_header_owner)
                    Values.USERS_LIST_HEADER_USERS -> view.context.getString(R.string.activity_list_users_header_users)
                    Values.USERS_LIST_HEADER_BANNED -> view.context.getString(R.string.activity_list_users_header_banned)
                    else -> ""
                }
            }
        }
    }

    inner class UserViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val user = getItem(position).user

            if (user != null) {
                view.findViewById<TextView>(R.id.row_list_user_name).text = user.name

                if (user.profilePicture != null) {
                    view.findViewById<ImageView>(R.id.row_list_user_icon)
                        .setImageBitmap(user.profilePicture)
                } else {
                    view.findViewById<ImageView>(R.id.row_list_user_icon).setImageBitmap(null)

                    user.loadProfileImage(view.context) {
                        view.findViewById<ImageView>(R.id.row_list_user_icon)
                            .setImageBitmap(user.profilePicture)
                    }
                }

                view.setOnClickListener {
                    userClickCallback(user)
                }

                view.setOnLongClickListener {
                    userClickCallback(user)

                    true
                }
            } else {
                view.setOnClickListener(null)
                view.setOnLongClickListener(null)
            }
        }
    }

    data class AdapterItem(
        val header: String? = null,
        val user: User? = null
    )
}