package io.github.jpiasecki.shoppinglist.consts

import android.content.Context
import io.github.jpiasecki.shoppinglist.R
import java.text.Normalizer
import kotlin.collections.HashMap

object Icons {
    const val DEFAULT = 0
    const val APPLE = 1
    const val BANANA = 2

    private val keywords: HashMap<Int, Array<String>> = HashMap()

    fun getItemIconId(icon: Int): Int {
        return when (icon) {
            APPLE -> R.drawable.ic_apple_24
            BANANA -> R.drawable.ic_banana_24
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
                        currentMatch++
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
    }
}