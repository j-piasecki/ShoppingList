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

    fun onSplashLaunch() {
        val time = Calendar.getInstance().timeInMillis

        if (time - config.getProfilePictureUpdateTimestamp() > Values.PROFILE_PICTURE_UPDATE_PERIOD) {
            config.updateProfilePictureUpdateTimestamp()
            usersRepository.updateProfilePicture()
        }

        if (time - config.getListsMetadataAutoUpdateTimestamp() > Values.LISTS_METADATA_AUTO_UPDATE_PERIOD) {
            config.updateListsMetadataAutoUpdateTimestamp()
            shoppingListsRepository.syncAllListsMetadata()
        }

        GlobalScope.launch(Dispatchers.IO) {
            for (list in shoppingListsRepository.getAllListsPlain()) {
                usersRepository.updateUser(list.owner)

                for (item in list.items) {
                    usersRepository.updateUser(item.addedBy)
                    usersRepository.updateUser(item.completedBy)
                }
            }
        }
    }
}