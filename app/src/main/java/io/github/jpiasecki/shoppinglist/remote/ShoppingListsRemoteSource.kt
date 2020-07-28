package io.github.jpiasecki.shoppinglist.remote

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
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

    private var defaultSource = Source.DEFAULT
    private val workingOnlineLiveData = MutableLiveData<Boolean>(true)
    
    private fun handleFirestoreException(e: FirebaseFirestoreException) {
        Log.w("ShoppingListsRemote", "Firestore error: ${e.code}")

        if (e.code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED) {
            Log.w("ShoppingListsRemote", "Quota exceeded, switching to cache only")

            defaultSource = Source.CACHE
            workingOnlineLiveData.postValue(false)
        }
    }

    fun getOnlineStatus(): LiveData<Boolean> = workingOnlineLiveData
    
    suspend fun setList(list: ShoppingList): Boolean {
        var success = true

        try {
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
                        .document(
                            FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND
                        )
                        .collection("data")
                        .document("private")
                        .update("lists", FieldValue.arrayUnion(list.id))
                        .await()
                }
            }
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)

            success = false
        }

        return success
    }

    suspend fun deleteList(list: ShoppingList) = deleteList(list.id)

    suspend fun deleteList(id: String): Boolean {
        var success = true

        try {
            val tasksList = ArrayList<Task<Void>>()

            // delete all items from list
            val itemsRef = Firebase.firestore.collection("lists").document(id).collection("items")
            val itemsCollection = itemsRef.get(defaultSource).await()

            for (item in itemsCollection.documents) {
                // add tasks to list
                tasksList.add(itemsRef.document(item.id).delete())
            }

            // delete all users from list
            val usersRef = Firebase.firestore.collection("lists").document(id).collection("users")
            val usersCollection = usersRef.get(defaultSource).await()

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
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)

            success = false
        }

        return success
    }

    suspend fun getListMetadata(id: String): ShoppingList? {
        try {
            val data = Firebase.firestore.collection("lists").document(id).get(defaultSource).await()

            return data.toObject<ShoppingList>()?.apply {
                this.id = id
            }
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return null
    }

    suspend fun getListContent(list: ShoppingList): ShoppingList? {
        try {
            // get all items
            val itemsCollection =
                Firebase.firestore.collection("lists").document(list.id).collection("items").whereEqualTo("deleted", false).get(defaultSource)
                    .await()
            for (itemDoc in itemsCollection.documents) {
                val item = itemDoc.toObject<Item>()

                if (item != null) {
                    list.items.add(item)
                }
            }

            // get all users
            if (list.owner == FirebaseAuth.getInstance().currentUser?.uid) {
                list.users = getUsers(list.id) as ArrayList<String>
            }

            return list
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return null
    }

    suspend fun getList(id: String): ShoppingList? {
        return getListContent(getListMetadata(id) ?: return null)
    }

    suspend fun exists(id: String): Boolean {
        var result = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(id)
                .get(defaultSource)
                .addOnSuccessListener {
                    result = it.exists()
                }.await()
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return result
    }

    suspend fun getTimestamp(id: String): Long {
        var result = Calendar.getInstance().timeInMillis

        try {
            val data = Firebase.firestore.collection("lists").document(id).get(defaultSource).await()

            if (data.contains("timestamp")) {
                result = data["timestamp"] as Long
            }
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return result
    }

    suspend fun changeListOwner(id: String, newOwner: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(id)
                .update("owner", newOwner)
                .addOnSuccessListener {
                    success = true
                }.await()

            updateListTimestamp(id)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun changeListName(id: String, newName: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(id)
                .update("name", newName)
                .addOnSuccessListener {
                    success = true
                }.await()

            updateListTimestamp(id)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun updateListTimestamp(id: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(id)
                .update("timestamp", Calendar.getInstance().timeInMillis)
                .addOnSuccessListener {
                    success = true
                }.await()
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun addUserToList(listId: String, userId: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(listId)
                .collection("users")
                .document(userId)
                .set(mapOf("acc" to true))
                .addOnSuccessListener {
                    success = true
                }.await()

            updateListTimestamp(listId)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun removeUserFromList(listId: String, userId: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(listId)
                .collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener {
                    success = true
                }.await()

            updateListTimestamp(listId)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun getUsers(listId: String): List<String> {
        val result = ArrayList<String>()

        try {
            val data = Firebase.firestore.collection("lists").document(listId).collection("users").get(defaultSource).await()

            for (user in data.documents) {
                result.add(user.id)
            }
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return result
    }

    suspend fun banUserFromList(listId: String, userId: String): Boolean {
        var success = true

        if (!removeUserFromList(listId, userId))
            success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(listId)
                .update("banned", FieldValue.arrayUnion(userId))
                .addOnFailureListener {
                    success = false
                }.await()

            updateListTimestamp(listId)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun unBanUserFromList(listId: String, userId: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(listId)
                .update("banned", FieldValue.arrayRemove(userId))
                .addOnSuccessListener {
                    success = true
                }.await()

            updateListTimestamp(listId)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun addItemToList(listId: String, item: Item, updateIfExists: Boolean = true): Boolean {
        var success = false

        try {
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

            updateListTimestamp(listId)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun removeItemFromList(listId: String, item: Item) = removeItemFromList(listId, item.id)

    suspend fun removeItemFromList(listId: String, itemId: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(listId)
                .collection("items")
                .document(itemId)
                .update(
                    mapOf(
                        "deleted" to true,
                        "timestamp" to Calendar.getInstance().timeInMillis
                    )
                )
                .addOnSuccessListener {
                    success = true
                }.await()

            updateListTimestamp(listId)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun deleteItemFromList(listId: String, itemId: String): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("lists")
                .document(listId)
                .collection("items")
                .document(itemId)
                .delete()
                .addOnSuccessListener {
                    success = true
                }.await()

            updateListTimestamp(listId)
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return success
    }

    suspend fun getUpdatedItems(listId: String, since: Long): MutableList<Item> {
        val result = ArrayList<Item>()

        try {
            val itemsCollection =
                Firebase.firestore.collection("lists").document(listId).collection("items").whereGreaterThan("timestamp", since).get(defaultSource)
                    .await()

            for (itemDoc in itemsCollection.documents) {
                val item = itemDoc.toObject<Item>()

                if (item != null) {
                    result.add(item)
                }
            }
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreException(e)
        }

        return result
    }
}