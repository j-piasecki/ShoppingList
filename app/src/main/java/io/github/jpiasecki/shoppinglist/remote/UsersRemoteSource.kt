package io.github.jpiasecki.shoppinglist.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Ids
import io.github.jpiasecki.shoppinglist.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class UsersRemoteSource(private val context: Context) {

    // update user profile picture if logged in
    fun updateProfilePicture() {
        // if the user is logged in
        FirebaseAuth.getInstance().currentUser?.apply {
            // download their profile picture from login provider
            Glide.with(context).asBitmap().circleCrop().load(this.photoUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        GlobalScope.launch(Dispatchers.IO) {
                            // and store it in remote source
                            val iconRef =
                                Firebase.storage.reference.child("profile_pics/${FirebaseAuth.getInstance().currentUser?.uid}")
                            val stream = ByteArrayOutputStream()
                            resource.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            val bytes = stream.toByteArray()
                            iconRef.putBytes(bytes)
                        }
                    }
                })
        }
    }

    // check whether user exists
    private suspend fun exists(id: String): Boolean {
        var result = false

        try {
        Firebase.firestore
            .collection("users")
            .document(id)
            .get()
            .addOnSuccessListener {
                result = it.exists()
            }.await()
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return result
    }

    // get user name by id
    suspend fun getUserName(id: String): String {
        try {
            val data = Firebase.firestore.collection("users").document(id).get().await()

            if (data.contains("name")) {
                return data["name"] as String
            }
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return context.getString(R.string.unknown_name)
    }

    // set user name to their default if not set
    suspend fun setUserNameIfNotSet(): Boolean {
        var success = false

        try {
            FirebaseAuth.getInstance().currentUser?.apply {
                val ref = Firebase.firestore.collection("users").document(this.uid)
                val userData = ref.get().await()

                if (!userData.contains("name")) {
                    ref.set(
                        mapOf(
                            "name" to this.displayName
                        ),
                        SetOptions.merge()
                    ).addOnSuccessListener {
                        success = true
                    }.await()
                }
            }
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return success
    }

    // change user name
    suspend fun setUserName(name: String): Boolean {
        var success = false

        try {
            FirebaseAuth.getInstance().currentUser?.apply {
                Firebase.firestore.collection("users").document(this.uid).set(
                    mapOf(
                        "name" to name
                    ),
                    SetOptions.merge()
                ).addOnSuccessListener {
                    success = true
                }.await()
            }
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return success
    }

    // get ids of lists saved remotely by user id
    suspend fun getUserShoppingListIds(id: String = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND): ArrayList<String> {
        try {
            val data = Firebase.firestore
                .collection("users")
                .document(id)
                .collection("data")
                .document("private")
                .get()
                .await()

            if (data.contains("lists")) {
                return data["lists"] as ArrayList<String>
            }
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return ArrayList()
    }

    // get user object by id
    suspend fun getUser(id: String = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND, fetchLists: Boolean = true): User? {
        if (!exists(id))
            return null

        val user = User(id)

        GlobalScope.launch {
            val name= async { getUserName(id) }

            if (fetchLists) {
                val lists = async { getUserShoppingListIds(id) }
                user.lists = lists.await()
            }

            user.name = name.await()
        }.join()

        return user
    }

    // set ids of lists saved remotely
    suspend fun setUserShoppingListIds(user: User) = setUserShoppingListIds(user.id, user.lists)

    suspend fun setUserShoppingListIds(id: String = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND, lists: List<String>): Boolean {
        var success = false

        try {
            Firebase.firestore
                .collection("users")
                .document(id)
                .collection("data")
                .document("private")
                .set(
                    mapOf(
                        "lists" to lists
                    ),
                    SetOptions.merge()
                ).addOnSuccessListener {
                    success = true
                }.await()
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return success
    }

    // set user object
    suspend fun setUser(user: User): Boolean {
        var success = true

        try {
            val setName = Firebase.firestore
                .collection("users")
                .document(user.id)
                .set(
                    mapOf(
                        "name" to user.name
                    ),
                    SetOptions.merge()
                ).addOnFailureListener {
                    success = false
                }

            if (!setUserShoppingListIds(user)) {
                success = false
            }

            setName.await()
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")

            success = false
        }

        return success
    }

    suspend fun addListToUser(list: String): Boolean {
        val id = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND
        var success = false

        try {
            Firebase.firestore
                .collection("users")
                .document(id)
                .collection("data")
                .document("private")
                .update("lists", FieldValue.arrayUnion(list))
                .addOnSuccessListener {
                    success = true
                }.await()
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return success
    }

    suspend fun removeListFromUser(list: String): Boolean {
        val id = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND
        var success = false

        try {
            Firebase.firestore
                .collection("users")
                .document(id)
                .collection("data")
                .document("private")
                .update("lists", FieldValue.arrayRemove(list))
                .addOnSuccessListener {
                    success = true
                }.await()
        } catch (e: FirebaseFirestoreException) {
            Log.w("A", "Error: ${e.code}")
        }

        return success
    }
}