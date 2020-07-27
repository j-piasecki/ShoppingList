package io.github.jpiasecki.shoppinglist.database

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {

    @TypeConverter
    fun stringListToJson(list: List<String>) = Gson().toJson(list)

    @TypeConverter
    fun itemListToJson(list: List<Item>) = Gson().toJson(list)

    @TypeConverter
    fun jsonToStringList(json: String) = Gson().fromJson(json, Array<String>::class.java).toList()

    @TypeConverter
    fun jsonToItemList(json: String) = Gson().fromJson(json, Array<Item>::class.java).toList()
}