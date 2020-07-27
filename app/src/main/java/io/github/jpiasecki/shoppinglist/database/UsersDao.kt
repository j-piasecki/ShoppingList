package io.github.jpiasecki.shoppinglist.database

import androidx.room.*
import java.util.*

@Dao
interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE id = (:id)")
    fun getById(id: String): User?

    @Query("UPDATE users SET name = (:name) WHERE id = (:userId)")
    fun updateName(userId: String, name: String)

    @Query("UPDATE users SET timestamp = (:timestamp) WHERE id = (:userId)")
    fun updateTimestamp(userId: String, timestamp: Long = Calendar.getInstance().timeInMillis)
}