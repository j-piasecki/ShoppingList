package io.github.jpiasecki.shoppinglist.consts

import io.github.jpiasecki.shoppinglist.R

object Units {
    const val UNITS_COUNT = 2
    const val NO_UNIT = 0
    const val PIECE = 1

    fun getStringId(unit: Int) = when (unit) {
        PIECE -> R.plurals.unit_piece
        else -> R.plurals.unit_no_unit
    }
}