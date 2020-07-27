package io.github.jpiasecki.shoppinglist.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jpiasecki.shoppinglist.database.User
import io.github.jpiasecki.shoppinglist.database.UsersDao
import io.github.jpiasecki.shoppinglist.other.GlideApp
import io.github.jpiasecki.shoppinglist.remote.UsersRemoteSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class UsersRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usersRemoteSource: UsersRemoteSource,
    private val usersDao: UsersDao
) {
    fun updateProfilePicture() = usersRemoteSource.updateProfilePicture()

    fun getUser(id: String, fetchLists: Boolean = true): LiveData<User> {
        val liveData = MutableLiveData<User>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val localUser = usersDao.getById(id)

            liveData.postValue(localUser)

            if (localUser == null || Calendar.getInstance().timeInMillis - localUser.timestamp > 24 * 60 * 60 * 1000) {
                val remoteUser = usersRemoteSource.getUser(id, fetchLists)

                if (remoteUser != null) {
                    liveData.postValue(remoteUser)

                    usersDao.insert(remoteUser)

                    loadProfileImage(remoteUser) {
                        liveData.postValue(remoteUser)
                    }
                }
            } else {
                loadProfileImage(localUser) {
                    liveData.postValue(localUser)
                }
            }
        }

        return liveData
    }

    fun getUserName(id: String): LiveData<String?> {
        val result = MutableLiveData<String?>(FirebaseAuth.getInstance().currentUser?.displayName)

        GlobalScope.launch(Dispatchers.IO) {
            val user = usersDao.getById(id)

            if (user == null || Calendar.getInstance().timeInMillis - user.timestamp > 24 * 60 * 60 * 1000) {
                val name = usersRemoteSource.getUserName(id)

                usersDao.updateName(id, name)
                usersDao.updateTimestamp(id)

                result.postValue(name)
            } else {
                result.postValue(user.name)
            }
        }

        return result
    }

    fun changeUserName(name: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean>(null)

        FirebaseAuth.getInstance().currentUser?.apply {
            GlobalScope.launch(Dispatchers.IO) {
                if (usersRemoteSource.setUserName(name)) {
                    usersDao.updateName(this@apply.uid, name)

                    result.postValue(true)
                } else {
                    result.postValue(false)
                }
            }
        }

        return result
    }

    suspend fun setUserNameIfNotSet() = usersRemoteSource.setUserNameIfNotSet()

    suspend fun addListToUser(id: String) = usersRemoteSource.addListToUser(id)

    suspend fun removeListFromUser(id: String) = usersRemoteSource.removeListFromUser(id)

    suspend fun getRemoteLists() = usersRemoteSource.getUserShoppingListIds()

    private fun loadProfileImage(user: User, callback: () -> Unit) {
        val ref = Firebase.storage.reference.child("profile_pics/${user.id}")

        // if profile picture file exists, load it
        ref.metadata.addOnSuccessListener {
            GlideApp.with(context).asBitmap().circleCrop().load(ref).into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    user.profilePicture = resource

                    callback()
                }
            })
        }
    }
}