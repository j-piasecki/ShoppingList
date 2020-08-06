package io.github.jpiasecki.shoppinglist.ui.editors

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R
import java.util.*
import kotlin.collections.ArrayList

class ListCategoriesAdapter() : RecyclerView.Adapter<ListCategoriesAdapter.ViewHolder>() {

    private var dataSet: ArrayList<Map<String, String>> = ArrayList()
    lateinit var touchHelper: ItemTouchHelper
    lateinit var itemRemovedCallback: (Int, Map<String, String>) -> Unit
    lateinit var itemClickedCallback: (Int, Map<String, String>) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_item_category, parent, false)
        )
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setData(list: ArrayList<Map<String, String>>) {
        dataSet = list
        notifyDataSetChanged()
    }

    fun getCategories() = dataSet

    fun onItemMoved(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(dataSet, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(dataSet, i, i - 1)
            }
        }

        notifyItemMoved(from, to)
    }

    fun onItemRemoved(position: Int) {
        itemRemovedCallback(position, dataSet[position])

        dataSet.removeAt(position)

        notifyItemRemoved(position)
    }

    fun insertItem(position: Int = -1, data: Map<String, String>) {
        if (position == -1) {
            dataSet.add(data)
            notifyItemInserted(dataSet.size - 1)
        } else {
            dataSet.add(position, data)
            notifyItemInserted(position)
        }
    }

    fun updateItem(position: Int, data: Map<String, String>) {
        dataSet[position] = data

        notifyItemChanged(position)
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            view.findViewById<TextView>(R.id.row_item_category_text).text = dataSet[position]["name"]

            view.findViewById<View>(R.id.row_item_category_handle).setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(this)
                    true
                }

                false
            }

            view.findViewById<View>(R.id.row_item_category_layout).setOnClickListener {
                itemClickedCallback(adapterPosition, dataSet[adapterPosition])
            }
        }
    }
}