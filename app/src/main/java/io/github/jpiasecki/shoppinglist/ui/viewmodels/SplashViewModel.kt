package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.repositories.ShoppingListsRepository
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SplashViewModel @ViewModelInject constructor(
    private val usersRepository: UsersRepository,
    private val shoppingListsRepository: ShoppingListsRepository,
    private val config: Config
) : ViewModel() {

    fun areAdsEnabled() = config.areAdsEnabled()

    fun isDarkThemeEnabled() = config.isDarkThemeEnabled()

    fun onSplashLaunch() {
        val time = Calendar.getInstance().timeInMillis

        if (time - config.getProfilePictureUpdateTimestamp() > Values.PROFILE_PICTURE_UPDATE_PERIOD) {
            config.updateProfilePictureUpdateTimestamp()
            usersRepository.updateProfilePicture()
        }

        if (time - config.getListsAutoUpdateTimestamp() > Values.LISTS_AUTO_UPDATE_PERIOD) {
            config.updateListsAutoUpdateTimestamp()
            shoppingListsRepository.syncAllLists()
        }

        GlobalScope.launch(Dispatchers.IO) {
            val updated = ArrayList<String>()

            for (list in shoppingListsRepository.getAllListsPlain()) {
                list.owner?.let {
                    if (it !in updated) {
                        usersRepository.updateUser(it)
                        updated.add(it)
                    }
                }

                for (user in list.getAllUsersNoOwner()) {
                    if (user !in updated) {
                        usersRepository.updateUser(user)
                        updated.add(user)
                    }
                }

                if (list.keepInSync) {
                    shoppingListsRepository.deleteUnusedItemsBlocking(list.id)
                }
            }
        }
    }
}