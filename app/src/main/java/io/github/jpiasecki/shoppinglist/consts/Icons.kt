package io.github.jpiasecki.shoppinglist.consts

import android.content.Context
import io.github.jpiasecki.shoppinglist.R
import java.text.Normalizer
import kotlin.collections.HashMap

object Icons {
    const val ICONS_COUNT = 14

    const val DEFAULT = 0
    const val APPLE = 1
    const val BANANA = 2
    const val CHERRY = 3
    const val LEMON = 4
    const val ORANGE = 5
    const val PEAR = 6
    const val PINEAPPLE = 7
    const val PLUM = 8
    const val STRAWBERRY = 9
    const val GRAPES = 10
    const val WATERMELON = 11
    const val RASPBERRY = 12
    const val PEACH = 13

    private val keywords: HashMap<Int, Array<String>> = HashMap()

    fun getItemIconId(icon: Int): Int {
        return when (icon) {
            APPLE -> R.drawable.ic_apple_24
            BANANA -> R.drawable.ic_banana_24
            CHERRY -> R.drawable.ic_cherry_24
            LEMON -> R.drawable.ic_lemon_24
            ORANGE -> R.drawable.ic_orange
            PEAR -> R.drawable.ic_pear_24
            PINEAPPLE -> R.drawable.ic_pineapple_24
            PLUM -> R.drawable.ic_plum_24
            STRAWBERRY -> R.drawable.ic_strawberry
            GRAPES -> R.drawable.ic_grapes_24
            WATERMELON -> R.drawable.ic_watermelon_24
            RASPBERRY -> R.drawable.ic_raspberry_24
            PEACH -> R.drawable.ic_peach_24

            else -> R.drawable.ic_item_default_24
        }
    }

    fun getListIconId(icon: Int): Int {
        if (icon == DEFAULT)
            return R.drawable.ic_list_default_24

        return getItemIconId(icon)
    }

    fun getIconFromName(name: String, context: Context): Int {
        if (keywords.isEmpty())
            loadKeywords(context)

        var result = DEFAULT
        var match = 0
        val words = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
            .replace("ł", "l")
            .replace("Ł", "L")
            .split(" ")

        for (entry in keywords) {
            var currentMatch = 0

            for (keyword in entry.value) {
                for (word in words) {
                    if (word.contains(keyword, ignoreCase = true))
                        currentMatch += keyword.length
                }
            }

            if (currentMatch > match) {
                match = currentMatch
                result = entry.key
            }
        }

        return result
    }

    private fun loadKeywords(context: Context) {
        keywords[APPLE] = context.resources.getStringArray(R.array.apple_keywords)
        keywords[BANANA] = context.resources.getStringArray(R.array.banana_keywords)
        keywords[CHERRY] = context.resources.getStringArray(R.array.cherry_keywords)
        keywords[LEMON] = context.resources.getStringArray(R.array.lemon_keywords)
        keywords[ORANGE] = context.resources.getStringArray(R.array.orange_keywords)
        keywords[PEAR] = context.resources.getStringArray(R.array.pear_keywords)
        keywords[PINEAPPLE] = context.resources.getStringArray(R.array.pineapple_keywords)
        keywords[PLUM] = context.resources.getStringArray(R.array.plum_keywords)
        keywords[STRAWBERRY] = context.resources.getStringArray(R.array.strawberry_keywords)
        keywords[GRAPES] = context.resources.getStringArray(R.array.grapes_keywords)
        keywords[WATERMELON] = context.resources.getStringArray(R.array.watermelon_keywords)
        keywords[RASPBERRY] = context.resources.getStringArray(R.array.raspberry_keywords)
        keywords[PEACH] = context.resources.getStringArray(R.array.peach_keywords)
    }
}