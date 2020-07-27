package io.github.jpiasecki.shoppinglist.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.database.Converters
import io.github.jpiasecki.shoppinglist.database.Database
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = ShoppingList(
            "id1", "nazwa1", "owner1", "pln", listOf(
                Item("mleko", "notatka", "ja", null, false, 1, 0, 1.0, 0),
                Item("cukier", "notatka", "ja", "ktos", true, 1, 0, 1.0, 0)
            ), listOf("user1", "user2"), listOf("user3", "user4"), 5, false)

        val db = Room.databaseBuilder(this, Database::class.java, "db").fallbackToDestructiveMigration().build()
    }
}