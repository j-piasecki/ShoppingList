package io.github.jpiasecki.shoppinglist.ui.editors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Icons

class IconsAdapter(private val listIcon: Boolean, private val callback: (Int) -> Unit) : RecyclerView.Adapter<IconsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_icon, parent, false)
        )
    }

    override fun getItemCount() = Icons.ICONS_COUNT

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            if (listIcon)
                view.findViewById<ImageView>(R.id.row_icon_image).setImageResource(Icons.getListIconId(position))
            else
                view.findViewById<ImageView>(R.id.row_icon_image).setImageResource(Icons.getItemIconId(position))

            view.setOnClickListener {
                callback(position)
            }
        }
    }
}