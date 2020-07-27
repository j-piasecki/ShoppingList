package io.github.jpiasecki.shoppinglist.remote

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import io.github.jpiasecki.shoppinglist.consts.Ids
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ShoppingListsRemoteSource(private val context: Context) {

    suspend fun setList(list: ShoppingList): Boolean {
        var success = true

        // create list document and set data to it
        Firebase.firestore.collection("lists").document(list.id).set(list)
            .addOnFailureListener {
                success = false
            }.await()

        // if successful add users and items
        if (success) {
            GlobalScope.launch {
                // add all users to list
                val usersRef = Firebase.firestore
                    .collection("lists")
                    .document(list.id)
                    .collection("users")

                for (user in list.users) {
                    usersRef.document(user)
                        .set(mapOf("acc" to true))
                        .await()
                }

                // add all items to list
                val ref = Firebase.firestore
                    .collection("lists")
                    .document(list.id)
                    .collection("items")

                for (item in list.items) {
                    ref.document(item.id)
                        .set(item)
                        .await()
                }

                // add entry to user profile
                Firebase.firestore
                    .collection("users")
                    .document(FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND)
                    .collection("data")
                    .document("private")
                    .update("lists", FieldValue.arrayUnion(list.id))
                    .await()
            }
        }

        return success
    }

    suspend fun deleteList(list: ShoppingList) = deleteList(list.id)

    suspend fun deleteList(id: String): Boolean {
        var success = true

        val tasksList = ArrayList<Task<Void>>()

        // delete all items from list
        val itemsRef = Firebase.firestore.collection("lists").document(id).collection("items")
        val itemsCollection = itemsRef.get().await()

        for (item in itemsCollection.documents) {
            // add tasks to list
            tasksList.add(itemsRef.document(item.id).delete())
        }

        // delete all users from list
        val usersRef = Firebase.firestore.collection("lists").document(id).collection("users")
        val usersCollection = usersRef.get().await()

        for (user in usersCollection.documents) {
            // add tasks to list
            tasksList.add(usersRef.document(user.id).delete())
        }

        // remove entry from user profile
        Firebase.firestore
            .collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND)
            .collection("data")
            .document("private")
            .update("lists", FieldValue.arrayRemove(id))
            .await()

        // wait for all tasks to finish
        for (task in tasksList)
            task.await()

        // delete list document
        Firebase.firestore.collection("lists").document(id)
            .delete()
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }

    suspend fun getList(id: String): ShoppingList {
        val data = Firebase.firestore.collection("lists").document(id).get().await()
        val list = data.toObject<ShoppingList>() ?: return ShoppingList(Ids.SHOPPING_LIST_ID_NOT_FOUND)

        // get all items
        val itemsCollection = Firebase.firestore.collection("lists").document(id).collection("items").get().await()
        for (itemDoc in itemsCollection.documents) {
            val item = itemDoc.toObject<Item>()

            if (item != null) {
                list.items.add(item)
            }
        }

        // get all users
        val usersCollection = Firebase.firestore.collection("lists").document(id).collection("users").get().await()
        for (userDoc in usersCollection.documents) {
            list.users.add(userDoc.id)
        }

        return list
    }

    suspend fun exists(id: String): Boolean {
        var result = false

        Firebase.firestore
            .collection("lists")
            .document(id)
            .get()
            .addOnSuccessListener {
                result = it.exists()
            }.await()

        return result
    }

    suspend fun changeListOwner(id: String, newOwner: String): Boolean {
        var success = false

        Firebase.firestore
            .collection("lists")
            .document(id)
            .update("owner", newOwner)
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }

    suspend fun changeListName(id: String, newName: String): Boolean {
        var success = false

        Firebase.firestore
            .collection("lists")
            .document(id)
            .update("name", newName)
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }

    suspend fun updateListTimestamp(id: String): Boolean {
        var success = false

        Firebase.firestore
            .collection("lists")
            .document(id)
            .update("timestamp", Calendar.getInstance().timeInMillis)
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }

    suspend fun removeUserFromList(listId: String, userId: String): Boolean {
        var success = false

        Firebase.firestore
            .collection("lists")
            .document(listId)
            .collection("users")
            .document(userId)
            .delete()
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }

    suspend fun banUserFromList(listId: String, userId: String): Boolean {
        var success = true

        if (!removeUserFromList(listId, userId))
            success = false

        Firebase.firestore
            .collection("lists")
            .document(listId)
            .update("banned", FieldValue.arrayUnion(userId))
            .addOnFailureListener {
                success = false
            }.await()

        return success
    }

    suspend fun unBanUserFromList(listId: String, userId: String): Boolean {
        var success = false

        Firebase.firestore
            .collection("lists")
            .document(listId)
            .update("banned", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }

    suspend fun addItemToList(listId: String, item: Item, updateIfExists: Boolean = true): Boolean {
        var success = false

        val ref = Firebase.firestore
            .collection("lists")
            .document(listId)
            .collection("items")
            .document(item.id)

        if (updateIfExists) {
            ref.set(item, SetOptions.merge())
        } else {
            ref.set(item)
        }.addOnSuccessListener {
            success = true
        }.await()

        return success
    }

    suspend fun removeItemFromList(listId: String, item: Item) = removeItemFromList(listId, item.id)

    suspend fun removeItemFromList(listId: String, itemId: String): Boolean {
        var success = false

        Firebase.firestore
            .collection("lists")
            .document(listId)
            .collection("items")
            .document(itemId)
            .delete()
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }
}