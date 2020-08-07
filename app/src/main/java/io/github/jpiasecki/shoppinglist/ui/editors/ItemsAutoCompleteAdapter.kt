package io.github.jpiasecki.shoppinglist.ui.editors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Icons
import io.github.jpiasecki.shoppinglist.database.Item

class ItemsAutoCompleteAdapter(context: Context, private val data: List<Item>) :
    ArrayAdapter<Item>(context, R.layout.row_auto_complete_item, data.toMutableList()) {

    private val suggestions = ArrayList<Item>()

    private val filter = object : Filter() {
        override fun convertResultToString(resultValue: Any?): CharSequence {
            val item = resultValue as Item
            return item.name ?: ""
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            suggestions.clear()

            if (constraint != null) {
                val str = constraint.toString()

                for (item in data) {
                    if (item.name?.startsWith(str, ignoreCase = true) == true) {
                        suggestions.add(item)

                        if (suggestions.size >= 15)
                            break
                    }
                }
            }

            val results = FilterResults()
            results.values = suggestions
            results.count = suggestions.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            clear()

            if (results != null) {
                val items = results.values as ArrayList<Item>

                for (item in items) {
                    add(item)
                }
            }

            notifyDataSetChanged()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView ?: LayoutInflater.from(context).inflate(R.layout.row_auto_complete_item, parent, false)
        val item = getItem(position)

        view.findViewById<ImageView>(R.id.row_auto_complete_item_icon).setImageResource(Icons.getItemIconId(item?.icon ?: Icons.DEFAULT))
        view.findViewById<TextView>(R.id.row_auto_complete_item_text).text = item?.name

        return view
    }

    override fun getFilter(): Filter {
        return filter
    }
}