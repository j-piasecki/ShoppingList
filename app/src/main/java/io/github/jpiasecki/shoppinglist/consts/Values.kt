package io.github.jpiasecki.shoppinglist.consts

object Values {
    const val USER_ID_NOT_FOUND = "USER_NOT_FOUND"
    const val SHOPPING_LIST_ID_NOT_FOUND = "SHOPPING_LIST_NOT_FOUND"

    const val RC_SIGN_IN = 0

    const val SHOPPING_LIST_ID = "SHOPPING_LIST_ID"
    const val ITEM_ID = "ITEM_ID"

    const val PROFILE_PICTURE_UPDATE_PERIOD = 48 * 60 * 60 * 1000
    const val LISTS_AUTO_UPDATE_PERIOD = 10 * 60 * 1000
    const val LISTS_MANUAL_UPDATE_PERIOD = 30 * 1000
    const val STALE_ITEM_DELETION_DELAY = 5 * 24 * 60 * 60 * 1000

    const val LISTENER_RESTART_TIMER = 10 * 1000
    const val LISTENER_RESTART_LIMIT = 3

    const val FRAGMENT_LISTS_TAG = "LISTS_FRAGMENT"
    const val FRAGMENT_SHOPPING_LIST_TAG = "SHOPPING_LIST_FRAGMENT"

    const val BOTTOM_APP_BAR_MENU_ANIMATION_DURATION = 60L
    const val BOTTOM_APP_BAR_MENU_ANIMATION_DELAY = 20L
}