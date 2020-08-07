package io.github.jpiasecki.shoppinglist.other

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.ui.main.ListsFragment
import io.github.jpiasecki.shoppinglist.ui.main.MainActivity
import io.github.jpiasecki.shoppinglist.ui.main.ShoppingListFragment
import java.text.Normalizer

fun FragmentManager.changeFragment(target: MainActivity.FragmentType, arguments: Bundle? = null): FragmentTransaction {
    val transaction = beginTransaction()

    val targetTag = when (target) {
        MainActivity.FragmentType.Lists -> Values.FRAGMENT_LISTS_TAG
        MainActivity.FragmentType.ShoppingList -> Values.FRAGMENT_SHOPPING_LIST_TAG
    }

    val targetFragment = findFragmentByTag(targetTag) ?: when (target) {
        MainActivity.FragmentType.Lists -> ListsFragment()
        MainActivity.FragmentType.ShoppingList -> ShoppingListFragment()
    }.also {
        transaction.add(R.id.activity_main_frame_layout, it, targetTag)
    }

    targetFragment.arguments = arguments

    primaryNavigationFragment?.let { transaction.hide(it) }
    transaction.show(targetFragment)
    transaction.setPrimaryNavigationFragment(targetFragment)
    transaction.setReorderingAllowed(true)

    return transaction
}

fun String.normalize(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}".toRegex(), "")
        .replace("ł", "l")
        .replace("Ł", "L")
}