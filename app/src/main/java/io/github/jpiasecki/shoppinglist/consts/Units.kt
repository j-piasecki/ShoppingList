package io.github.jpiasecki.shoppinglist.consts

import io.github.jpiasecki.shoppinglist.R

object Units {
    const val NO_UNIT = 0
    const val PIECE = 1
    const val BOX = 2
    const val BOTTLE = 3
    const val JAR = 4
    const val CAN = 5
    const val PACK = 6
    const val KILOGRAM = 7
    const val DECAGRAM = 8
    const val GRAM = 9
    const val LITER = 10
    const val MILLILITER = 11

    val ALL = listOf(
        NO_UNIT,
        PIECE,
        BOX,
        BOTTLE,
        JAR,
        CAN,
        PACK,
        KILOGRAM,
        DECAGRAM,
        GRAM,
        LITER,
        MILLILITER
    )

    fun getStringId(unit: Int) = when (unit) {
        PIECE -> R.plurals.unit_piece
        BOX -> R.plurals.unit_box
        BOTTLE -> R.plurals.unit_bottle
        JAR -> R.plurals.unit_jar
        CAN -> R.plurals.unit_can
        PACK -> R.plurals.unit_pack
        KILOGRAM -> R.plurals.unit_kilogram
        DECAGRAM -> R.plurals.unit_decagram
        GRAM -> R.plurals.unit_gram
        LITER -> R.plurals.unit_liter
        MILLILITER -> R.plurals.unit_milliliter
        else -> R.plurals.unit_no_unit
    }
}