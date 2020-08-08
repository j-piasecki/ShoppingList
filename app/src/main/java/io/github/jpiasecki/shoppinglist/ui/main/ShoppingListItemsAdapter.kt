package io.github.jpiasecki.shoppinglist.ui.main

import android.content.Context
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
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.material.checkbox.MaterialCheckBox
import io.github.jpiasecki.shoppinglist.AdProvider
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Icons
import io.github.jpiasecki.shoppinglist.consts.Units
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.database.User
import java.lang.Exception
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

private const val VIEW_TYPE_ITEM = 1
private const val VIEW_TYPE_HEADER = 2
private const val VIEW_TYPE_CATEGORY = 3
private const val VIEW_TYPE_AD = 4

class ShoppingListItemsAdapter() : ListAdapter<ShoppingListItemsAdapter.AdapterItem, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<AdapterItem>() {
    override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem.type == newItem.type && (
                    (oldItem.type == VIEW_TYPE_ITEM && oldItem.item?.id == newItem.item?.id) ||
                    (oldItem.type == VIEW_TYPE_HEADER && oldItem.header == newItem.header) ||
                    (oldItem.type == VIEW_TYPE_CATEGORY && oldItem.category == newItem.category) ||
                    (oldItem.type == VIEW_TYPE_AD))
    }

    override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem.type == newItem.type && (
                    (oldItem.type == VIEW_TYPE_ITEM && oldItem.item == newItem.item) ||
                    (oldItem.type == VIEW_TYPE_HEADER && oldItem.header == newItem.header) ||
                    (oldItem.type == VIEW_TYPE_CATEGORY && oldItem.category == newItem.category) ||
                    (oldItem.type == VIEW_TYPE_AD && oldItem.addId == newItem.addId && oldItem.addId >= 0))
    }
}) {

    data class AdapterItem(
        val type: Int,
        val item: Item? = null,
        val header: Long? = null,
        val category: String? = null,
        var addId: Int = -1
    )

    lateinit var clickCallback: (id: String, view: View) -> Unit
    lateinit var longClickCallback: (item: Item, view: View) -> Unit
    lateinit var itemCompletionChangeCallback: (id: String, completed: Boolean) -> Unit
    lateinit var userListClickCallback: (id: String, view: View) -> Unit

    var timestampSetting = Config.SHOW_TIMESTAMP_WHEN_SYNCED
    var displayAds = true

    private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)

    private var usersList: List<User> = emptyList()
    private var shoppingList: ShoppingList = ShoppingList()
    private var priceFormat = NumberFormat.getCurrencyInstance()

    private var lastCheckTimeStamp = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_header, parent, false))
            VIEW_TYPE_CATEGORY -> CategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_category, parent, false))
            VIEW_TYPE_AD -> AdViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_ad, parent, false))
            else -> ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder)
            holder.bind()
        else if (holder is ItemViewHolder)
            holder.bind(position)
        else if (holder is CategoryViewHolder)
            holder.bind(position)
        else if (holder is AdViewHolder)
            holder.bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
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

        val content = ArrayList<AdapterItem>()
        content.add(AdapterItem(VIEW_TYPE_HEADER, header = 0L + (list.owner?.sumBy { it.toInt() } ?: 0) + (list.name?.sumBy { it.toInt() } ?: 0) + (if (list.keepInSync) 1 else 2) + list.icon + list.getAllUsersNoOwner().size))

        var previousItem: Item? = null

        for ((index, item) in shoppingList.items.iterator().withIndex()) {
            if (previousItem == null ||
                previousItem.completed != item.completed ||
                (previousItem.category != item.category && shoppingList.hasCategory(previousItem.category))) {
                content.add(AdapterItem(VIEW_TYPE_CATEGORY, category = item.category))
            }

            content.add(AdapterItem(VIEW_TYPE_ITEM, item = item))

            if (displayAds && (index + 1) % Values.ITEMS_PER_AD == 0)
                content.add(AdapterItem(VIEW_TYPE_AD))

            previousItem = item
        }

        submitList(content)

        for (i in 0 until itemCount)
            if (getItem(i).type == VIEW_TYPE_CATEGORY)
                notifyItemChanged(i)
    }

    @Synchronized
    fun setUsers(list: List<User>) {
        usersList = list

        for (user in list) {
            for ((index, item) in currentList.iterator().withIndex()) {
                if (item.type == VIEW_TYPE_ITEM && (user.id == item.item?.addedBy || user.id == item.item?.completedBy)) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    inner class CategoryViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val category = getItem(position).category
            val name = shoppingList.getCategoryName(category) ?: view.context.getString(R.string.activity_main_no_category)

            view.findViewById<TextView>(R.id.row_shopping_list_category_text).text = name
        }
    }

    inner class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val scale = view.context.resources.displayMetrics.density

        fun bind() {
            view.findViewById<TextView>(R.id.row_shopping_list_header_name).text = shoppingList.name

            view.findViewById<ImageView>(R.id.row_shopping_list_header_synced_icon).setImageResource(if (shoppingList.keepInSync) R.drawable.ic_cloud_24 else R.drawable.ic_smartphone_24)

            view.findViewById<ImageView>(R.id.row_shopping_list_header_icon).setImageResource(Icons.getListIconId(shoppingList.icon))

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
            val item = getItem(position).item ?: return

            view.findViewById<TextView>(R.id.row_shopping_list_item_name).text = item.name
            view.findViewById<TextView>(R.id.row_shopping_list_item_note).text = item.note
            view.findViewById<TextView>(R.id.row_shopping_list_item_last_update).text = view.context.getString(R.string.last_update, dateFormat.format(Date(item.timestamp)))
            view.findViewById<TextView>(R.id.row_shopping_list_item_quantity).text =
                    view.context.resources.getQuantityString(Units.getStringId(item.unit), item.quantity, item.quantity)
            view.findViewById<ImageView>(R.id.row_shopping_list_item_icon).setImageResource(Icons.getItemIconId(item.icon))

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

            setupCheckBox(item)

            showOrHideViews(item)

            setListeners(item)
        }

        private fun showOrHideViews(item: Item) {
            if (!item.completed && (item.note == null || item.note.isEmpty())) {
                view.findViewById<TextView>(R.id.row_shopping_list_item_note).visibility = View.GONE
            } else {
                view.findViewById<TextView>(R.id.row_shopping_list_item_note).visibility = View.VISIBLE
            }

            when (timestampSetting) {
                Config.SHOW_TIMESTAMP_ALWAYS -> view.findViewById<TextView>(R.id.row_shopping_list_item_last_update).visibility = View.VISIBLE
                Config.SHOW_TIMESTAMP_WHEN_SYNCED -> view.findViewById<TextView>(R.id.row_shopping_list_item_last_update).visibility = if (shoppingList.keepInSync) View.VISIBLE else View.GONE

                else -> view.findViewById<TextView>(R.id.row_shopping_list_item_last_update).visibility = View.GONE
            }

            if (shoppingList.keepInSync) {
                setProfilePictures(item)
            } else {
                view.findViewById<ImageView>(R.id.row_shopping_list_item_added_by_icon).setImageBitmap(null)
                view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).visibility = View.GONE
            }
        }

        private fun setupCheckBox(item: Item) {
            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box).isChecked = item.completed

            if (item.completed) {
                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.VISIBLE
            } else {
                view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility = View.GONE
            }

            view.findViewById<View>(R.id.row_shopping_list_item_check_box_overlay).setOnClickListener {
                if (Calendar.getInstance().timeInMillis - lastCheckTimeStamp > Values.ITEM_COMPLETION_CHANGE_TIMER) {
                    if (!shoppingList.keepInSync || Config.isNetworkConnected(view.context)) {
                        val checkBox =
                            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box)
                        checkBox.isChecked = !checkBox.isChecked

                        itemCompletionChangeCallback(item.id, checkBox.isChecked)

                        view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility =
                            View.VISIBLE
                    } else {
                        showToast(
                            view.context,
                            view.context.getString(R.string.message_need_internet_to_modify_list)
                        )
                    }

                    lastCheckTimeStamp = Calendar.getInstance().timeInMillis
                }
            }

            view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay_hitbox).setOnClickListener {
                if (Calendar.getInstance().timeInMillis - lastCheckTimeStamp > Values.ITEM_COMPLETION_CHANGE_TIMER) {
                    if (!shoppingList.keepInSync || Config.isNetworkConnected(view.context)) {
                        val checkBox =
                            view.findViewById<MaterialCheckBox>(R.id.row_shopping_list_item_completed_check_box)
                        checkBox.isChecked = false

                        itemCompletionChangeCallback(item.id, false)

                        view.findViewById<View>(R.id.row_shopping_list_item_completed_overlay).visibility =
                            View.GONE
                    } else {
                        showToast(
                            view.context,
                            view.context.getString(R.string.message_need_internet_to_modify_list)
                        )
                    }

                    lastCheckTimeStamp = Calendar.getInstance().timeInMillis
                }
            }
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
                view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).visibility = View.GONE
            } else {
                view.findViewById<ImageView>(R.id.row_shopping_list_item_completed_by_icon).visibility = View.VISIBLE

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

        private fun setListeners(item: Item) {
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
    }

    inner class AdViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            if (getItem(position).addId == -1)
                getItem(position).addId = AdProvider.getNextAdId()

            val ad = AdProvider.getAd(getItem(position).addId)

            if (ad != null) {
                setAd(ad)
            } else {
                view.findViewById<UnifiedNativeAdView>(R.id.row_shopping_list_ad_view).visibility = View.GONE
            }
        }

        private fun setAd(ad: UnifiedNativeAd) {
            val adView = view.findViewById<UnifiedNativeAdView>(R.id.row_shopping_list_ad_view)
            adView.visibility = View.VISIBLE
            adView.setNativeAd(ad)

            if (ad.icon == null) {
                view.findViewById<ImageView>(R.id.row_shopping_list_ad_icon).visibility = View.GONE
            } else {
                view.findViewById<ImageView>(R.id.row_shopping_list_ad_icon).apply {
                    visibility = View.VISIBLE
                    setImageDrawable(ad.icon.drawable)
                    adView.iconView = this
                }
            }

            view.findViewById<TextView>(R.id.row_shopping_list_ad_headline).apply {
                text = ad.headline
                adView.headlineView = this
            }
            view.findViewById<TextView>(R.id.row_shopping_list_ad_body).apply {
                text = ad.body
                adView.bodyView = this
            }

            view.findViewById<TextView>(R.id.row_shopping_list_ad_call_to_action).apply {
                text = ad.callToAction
                adView.callToActionView = this
            }

            view.findViewById<TextView>(R.id.row_shopping_list_ad_advertiser).apply {
                text = ad.advertiser
                adView.advertiserView = this
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