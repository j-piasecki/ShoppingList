package io.github.jpiasecki.shoppinglist.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun stringListToJson(list: List<String>): String = Gson().toJson(list)

    @TypeConverter
    fun itemListToJson(list: List<Item>): String = Gson().toJson(list)

    @TypeConverter
    fun categoriesListToJson(list: List<Map<String, String>>): String = Gson().toJson(list)

    @TypeConverter
    fun jsonToStringList(json: String) = ArrayList(Gson().fromJson(json, Array<String>::class.java).toList())

    @TypeConverter
    fun jsonToItemList(json: String) = ArrayList(Gson().fromJson(json, Array<Item>::class.java).toList())

    @TypeConverter
    fun jsonToCategoriesList(json: String) = ArrayList(Gson().fromJson<Array<Map<String, String>>>(json, object : TypeToken<Array<Map<String, String>>>() {}.type).toList())
}