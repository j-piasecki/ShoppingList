package io.github.jpiasecki.shoppinglist.consts

import android.content.Context
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.other.normalize
import kotlin.collections.HashMap

object Icons {
    const val ICONS_COUNT = 70

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
    const val WALNUT = 14
    const val BLACKBERRY = 15
    const val ASPARAGUS = 16
    const val AUBERGINE = 17
    const val AVOCADO = 18
    const val BEANS = 19
    const val BEETROOT = 20
    const val BROCCOLI = 21
    const val CABBAGE = 22
    const val CARROT = 23
    const val CAULIFLOWER = 24
    const val CHILI_PEPPER = 25
    const val CORN = 26
    const val GARLIC = 27
    const val GREEN_BEANS = 28
    const val GREEN_PEPPER = 29
    const val LETTUCE = 30
    const val OLIVES = 31
    const val ONION = 32
    const val POTATO = 33
    const val PUMPKIN = 34
    const val RADISH = 35
    const val RED_PEPPER = 36
    const val TOMATO = 37
    const val YELLOW_PEPPER = 38
    const val ZUCCHINI = 39
    const val EGGS = 40
    const val JUICE = 41
    const val MILK = 42
    const val WATER = 43
    const val CUCUMBER = 44
    const val KIWI = 45
    const val MANGO = 46
    const val MELON = 47
    const val BREAD = 48
    const val BREAD_ROLL = 49
    const val CHEESE = 50
    const val COOKIE = 51
    const val MEAT = 52
    const val PASTA = 53
    const val MUSHROOM = 54
    const val ALMOND = 55
    const val BACON = 56
    const val BAGUETTE = 57
    const val BEEF = 58
    const val BUTTER = 59
    const val CHICKEN_BREAST = 60
    const val FISH = 61
    const val HONEY = 62
    const val LENTILS = 63
    const val MAYONNAISE = 64
    const val SAUCE = 65
    const val SAUSAGE = 66
    const val SUGAR = 67
    const val TOILET_PAPER = 68
    const val YOGURT = 69

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
            WALNUT -> R.drawable.ic_walnut_24
            BLACKBERRY -> R.drawable.ic_blackberry_24
            ASPARAGUS -> R.drawable.ic_asparagus_24
            AUBERGINE -> R.drawable.ic_aubergine_24
            AVOCADO -> R.drawable.ic_avocado_24
            BEANS -> R.drawable.ic_beans_24
            BEETROOT -> R.drawable.ic_beetroot_24
            BROCCOLI -> R.drawable.ic_broccoli_24
            CABBAGE -> R.drawable.ic_cabbage_24
            CARROT -> R.drawable.ic_carrot_24
            CAULIFLOWER -> R.drawable.ic_cauliflower_24
            CHILI_PEPPER -> R.drawable.ic_chili_pepper_24
            CORN -> R.drawable.ic_corn_24
            GARLIC -> R.drawable.ic_garlic_24
            GREEN_BEANS -> R.drawable.ic_green_beans_24
            GREEN_PEPPER -> R.drawable.ic_green_pepper_24
            LETTUCE -> R.drawable.ic_lettuce_24
            OLIVES -> R.drawable.ic_olives_24
            ONION -> R.drawable.ic_onion_24
            POTATO -> R.drawable.ic_potato_24
            PUMPKIN -> R.drawable.ic_pumpkin_24
            RADISH -> R.drawable.ic_radish_24
            RED_PEPPER -> R.drawable.ic_red_pepper_24
            TOMATO -> R.drawable.ic_tomato_24
            YELLOW_PEPPER -> R.drawable.ic_yellow_pepper_24
            ZUCCHINI -> R.drawable.ic_zucchini_24
            EGGS -> R.drawable.ic_eggs_24
            JUICE -> R.drawable.ic_juice_24
            MILK -> R.drawable.ic_milk_24
            WATER -> R.drawable.ic_water_24
            CUCUMBER -> R.drawable.ic_cucumber_24
            KIWI -> R.drawable.ic_kiwi_24
            MANGO -> R.drawable.ic_mango_24
            MELON -> R.drawable.ic_melon_24
            BREAD -> R.drawable.ic_bread_24
            BREAD_ROLL -> R.drawable.ic_bread_roll_24
            CHEESE -> R.drawable.ic_cheese_24
            COOKIE -> R.drawable.ic_cookie_24
            MEAT -> R.drawable.ic_meat_24
            PASTA -> R.drawable.ic_pasta_24
            MUSHROOM -> R.drawable.ic_mushroom_24
            ALMOND -> R.drawable.ic_almond_24
            BACON -> R.drawable.ic_bacon_24
            BAGUETTE -> R.drawable.ic_baguette_24
            BEEF -> R.drawable.ic_beef_24
            BUTTER -> R.drawable.ic_butter_24
            CHICKEN_BREAST -> R.drawable.ic_chicken_breasts_24
            FISH -> R.drawable.ic_fish_24
            HONEY -> R.drawable.ic_honey_24
            LENTILS -> R.drawable.ic_lentils_24
            MAYONNAISE -> R.drawable.ic_mayonnaise_24
            SAUCE -> R.drawable.ic_sauce_24
            SAUSAGE -> R.drawable.ic_sausage_24
            SUGAR -> R.drawable.ic_sugar_24
            TOILET_PAPER -> R.drawable.ic_toilet_paper_24
            YOGURT -> R.drawable.ic_yogurt_24

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
        val nameNormal = name.normalize()
        val words = nameNormal.split(" ")

        for (entry in keywords) {
            var currentMatch = 0

            for (keyword in entry.value) {
                for (word in words) {
                    if (word.contains(keyword, ignoreCase = true))
                        currentMatch += keyword.length * (nameNormal.length - nameNormal.indexOf(keyword, ignoreCase = true))
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
        keywords[WALNUT] = context.resources.getStringArray(R.array.walnut_keywords)
        keywords[BLACKBERRY] = context.resources.getStringArray(R.array.blackberry_keywords)
        keywords[ASPARAGUS] = context.resources.getStringArray(R.array.asparagus_keywords)
        keywords[AUBERGINE] = context.resources.getStringArray(R.array.aubergine_keywords)
        keywords[AVOCADO] = context.resources.getStringArray(R.array.avocado_keywords)
        keywords[BEANS] = context.resources.getStringArray(R.array.beans_keywords)
        keywords[BEETROOT] = context.resources.getStringArray(R.array.beetroot_keywords)
        keywords[BROCCOLI] = context.resources.getStringArray(R.array.broccoli_keywords)
        keywords[CABBAGE] = context.resources.getStringArray(R.array.cabbage_keywords)
        keywords[CARROT] = context.resources.getStringArray(R.array.carrot_keywords)
        keywords[CAULIFLOWER] = context.resources.getStringArray(R.array.cauliflower_keywords)
        keywords[CHILI_PEPPER] = context.resources.getStringArray(R.array.chili_pepper_keywords)
        keywords[CORN] = context.resources.getStringArray(R.array.corn_keywords)
        keywords[GARLIC] = context.resources.getStringArray(R.array.garlic_keywords)
        keywords[GREEN_BEANS] = context.resources.getStringArray(R.array.green_beans_keywords)
        keywords[GREEN_PEPPER] = context.resources.getStringArray(R.array.green_pepper_keywords)
        keywords[LETTUCE] = context.resources.getStringArray(R.array.lettuce_keywords)
        keywords[OLIVES] = context.resources.getStringArray(R.array.olives_keywords)
        keywords[ONION] = context.resources.getStringArray(R.array.onion_keywords)
        keywords[POTATO] = context.resources.getStringArray(R.array.potato_keywords)
        keywords[PUMPKIN] = context.resources.getStringArray(R.array.pumpkin_keywords)
        keywords[RADISH] = context.resources.getStringArray(R.array.radish_keywords)
        keywords[RED_PEPPER] = context.resources.getStringArray(R.array.red_pepper_keywords)
        keywords[TOMATO] = context.resources.getStringArray(R.array.tomato_keywords)
        keywords[YELLOW_PEPPER] = context.resources.getStringArray(R.array.yellow_pepper_keywords)
        keywords[ZUCCHINI] = context.resources.getStringArray(R.array.zucchini_keywords)
        keywords[EGGS] = context.resources.getStringArray(R.array.eggs_keywords)
        keywords[JUICE] = context.resources.getStringArray(R.array.juice_keywords)
        keywords[MILK] = context.resources.getStringArray(R.array.milk_keywords)
        keywords[WATER] = context.resources.getStringArray(R.array.water_keywords)
        keywords[CUCUMBER] = context.resources.getStringArray(R.array.cucumber_keywords)
        keywords[KIWI] = context.resources.getStringArray(R.array.kiwi_keywords)
        keywords[MANGO] = context.resources.getStringArray(R.array.mango_keywords)
        keywords[MELON] = context.resources.getStringArray(R.array.melon_keywords)
        keywords[BREAD] = context.resources.getStringArray(R.array.bread_keywords)
        keywords[BREAD_ROLL] = context.resources.getStringArray(R.array.bread_roll_keywords)
        keywords[CHEESE] = context.resources.getStringArray(R.array.cheese_keywords)
        keywords[COOKIE] = context.resources.getStringArray(R.array.cookie_keywords)
        keywords[MEAT] = context.resources.getStringArray(R.array.meat_keywords)
        keywords[PASTA] = context.resources.getStringArray(R.array.pasta_keywords)
        keywords[MUSHROOM] = context.resources.getStringArray(R.array.mushroom_keywords)
        keywords[ALMOND] = context.resources.getStringArray(R.array.almond_keywords)
        keywords[BACON] = context.resources.getStringArray(R.array.bacon_keywords)
        keywords[BAGUETTE] = context.resources.getStringArray(R.array.baguette_keywords)
        keywords[BEEF] = context.resources.getStringArray(R.array.beef_keywords)
        keywords[BUTTER] = context.resources.getStringArray(R.array.butter_keywords)
        keywords[CHICKEN_BREAST] = context.resources.getStringArray(R.array.chicken_breasts_keywords)
        keywords[FISH] = context.resources.getStringArray(R.array.fish_keywords)
        keywords[HONEY] = context.resources.getStringArray(R.array.honey_keywords)
        keywords[LENTILS] = context.resources.getStringArray(R.array.lentils_keywords)
        keywords[MAYONNAISE] = context.resources.getStringArray(R.array.mayonnaise_keywords)
        keywords[SAUCE] = context.resources.getStringArray(R.array.sauce_keywords)
        keywords[SAUSAGE] = context.resources.getStringArray(R.array.sausage_keywords)
        keywords[SUGAR] = context.resources.getStringArray(R.array.sugar_keywords)
        keywords[TOILET_PAPER] = context.resources.getStringArray(R.array.toilet_paper_keywords)
        keywords[YOGURT] = context.resources.getStringArray(R.array.yogurt_keywords)
    }
}