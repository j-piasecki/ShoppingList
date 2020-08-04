package io.github.jpiasecki.shoppinglist.consts

import io.github.jpiasecki.shoppinglist.R

object Icons {
    const val DEFAULT = 0

    fun getItemIconId(icon: Int): Int {
        return when (icon) {

            else -> R.drawable.ic_item_default_24
        }
    }

    fun getListIconId(icon: Int): Int {
        if (icon == DEFAULT)
            return R.drawable.ic_list_default_24

        return getItemIconId(icon)
    }
}