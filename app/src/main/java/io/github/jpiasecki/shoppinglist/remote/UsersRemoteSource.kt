package io.github.jpiasecki.shoppinglist.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

class UsersRemoteSource(private val context: Context) {

    // update user name in database and upload profile picture if logged in
    fun tryUpdateUserInfo() {
        // if the user is logged in
        FirebaseAuth.getInstance().currentUser?.apply {
            // download their profile picture
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

            // update user name in database
            Firebase.firestore.collection("users").document(this.uid).set(
                mapOf(
                    "name" to this.displayName
                ),
                SetOptions.merge()
            )
        }
    }

    // get user name by id
    suspend fun getUserName(id: String): String {
        val data = Firebase.firestore.collection("users").document(id).get().await()

        return if (data.contains("name")) {
            data["name"] as String
        } else {
            context.getString(R.string.unknown_name);
        }
    }

    // get ids of lists saved remotely by user id
    suspend fun getUserLists(id: String = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND): List<String> {
        val data = Firebase.firestore
            .collection("users")
            .document(id)
            .collection("data")
            .document("private")
            .get()
            .await()

        return if (data.contains("lists")) {
            data["lists"] as List<String>
        } else {
            emptyList()
        }
    }

    // get user object by id
    suspend fun getUser(id: String = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND): User {
        val user = User(id)

        GlobalScope.launch {
            val name= async { getUserName(id) }
            val lists = async { getUserLists(id) }

            user.name = name.await()
            user.lists = lists.await()
        }.join()

        return user
    }

    // set ids of lists saved remotely
    suspend fun setUserLists(user: User) = setUserLists(user.id, user.lists)

    suspend fun setUserLists(id: String = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND, lists: List<String>): Boolean {
        var success = false

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

        return success
    }

    // set user object
    suspend fun setUser(user: User): Boolean {
        var success = true

        val setName = Firebase.firestore
            .collection("users")
            .document(user.id)
            .set(mapOf(
                    "name" to user.name
                ),
                SetOptions.merge()
            ).addOnFailureListener {
                success = false
            }

        if (!setUserLists(user)) {
            success = false
        }

        setName.await()

        return success
    }

    suspend fun addListToUser(list: String): Boolean {
        val id = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND
        var success = false

        Firebase.firestore
            .collection("users")
            .document(id)
            .collection("data")
            .document("private")
            .update("lists", FieldValue.arrayUnion(list))
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }

    suspend fun removeListFromUser(list: String): Boolean {
        val id = FirebaseAuth.getInstance().currentUser?.uid ?: Ids.USER_ID_NOT_FOUND
        var success = false

        Firebase.firestore
            .collection("users")
            .document(id)
            .collection("data")
            .document("private")
            .update("lists", FieldValue.arrayRemove(list))
            .addOnSuccessListener {
                success = true
            }.await()

        return success
    }
}