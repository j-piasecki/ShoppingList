package io.github.jpiasecki.shoppinglist.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ShoppingList::class, User::class, Item::class], version = 1)
@TypeConverters(Converters::class)
abstract class Database: RoomDatabase() {
    abstract fun shoppingListsDao(): ShoppingListsDao
    abstract fun usersDao(): UsersDao
    abstract fun itemsDao(): ItemsDao
}