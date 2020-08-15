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
    private val allUsers = usersDao.getAllUsers()

    fun getAllUsers() = allUsers

    fun updateProfilePicture() = usersRemoteSource.updateProfilePicture()

    fun getUser(id: String, fetchLists: Boolean = false): LiveData<User> {
        val liveData = MutableLiveData<User>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val localUser = usersDao.getById(id)

            liveData.postValue(localUser)

            if (localUser == null || Calendar.getInstance().timeInMillis - localUser.timestamp > 24 * 60 * 60 * 1000) {
                val remoteUser = usersRemoteSource.getUser(id, fetchLists)

                if (remoteUser != null) {
                    liveData.postValue(remoteUser)

                    usersDao.insert(remoteUser)

                    remoteUser.loadProfileImage(context) {
                        liveData.postValue(remoteUser)
                    }
                }
            } else {
                localUser.loadProfileImage(context) {
                    liveData.postValue(localUser)
                }
            }
        }

        return liveData
    }

    fun getLocalUser(id: String): LiveData<User> {
        val liveData = MutableLiveData<User>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val localUser = usersDao.getById(id)

            liveData.postValue(localUser)
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

    fun isUserUpToDate(id: String): Boolean {
        val user = usersDao.getById(id) ?: return false

        return Calendar.getInstance().timeInMillis - user.timestamp < 24 * 60 * 60 * 1000
    }

    fun setupUser() {
        GlobalScope.launch(Dispatchers.IO) {
            setUserNameIfNotSet()

            updateProfilePicture()
            usersRemoteSource.createDataIfNotExists()
        }
    }

    suspend fun updateUser(id: String?) {
        if (!isUserUpToDate(id ?: return)) {
            val remoteUser = usersRemoteSource.getUser(id, false)

            if (remoteUser != null) {
                usersDao.insert(remoteUser)
            }
        }

        usersDao.getById(id)?.loadProfileImage(context)
    }

    suspend fun setUserNameIfNotSet() {
        usersRemoteSource.setUserNameIfNotSet()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val localUser = usersDao.getById(uid)

        if (localUser == null) {
            val user = User(id = uid, name = usersRemoteSource.getUserName(uid))

            usersDao.insert(user)
        }
    }

    suspend fun addListToUser(id: String) = usersRemoteSource.addListToUser(id)

    suspend fun removeListFromUser(id: String) = usersRemoteSource.removeListFromUser(id)

    suspend fun getRemoteLists() = usersRemoteSource.getUserShoppingListIds()
}