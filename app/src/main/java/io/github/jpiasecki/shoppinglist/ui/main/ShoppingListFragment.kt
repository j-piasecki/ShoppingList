package io.github.jpiasecki.shoppinglist.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.ui.AddEditItemActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.MainViewModel
import kotlin.math.sign

@AndroidEntryPoint
class ShoppingListFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    var currentList: ShoppingList? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)
        val context = context ?: return null

        val listId = arguments?.getString(Values.SHOPPING_LIST_ID)

        val adapter =
            ShoppingListItemsAdapter { id ->
                startActivity(
                    Intent(
                        context,
                        AddEditItemActivity::class.java
                    ).putExtra(Values.ITEM_ID, id)
                        .putExtra(Values.SHOPPING_LIST_ID, listId)
                )
            }

        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_shopping_list_recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        val animator = recyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        if (listId != null) {
            viewModel.updateItems(listId)

            viewModel.getShoppingList(listId).observe(viewLifecycleOwner, Observer {
                it.items.sortWith(Comparator { o1, o2 ->
                    if ((o1.isCompleted && o2.isCompleted) || (!o1.isCompleted && !o2.isCompleted)) {
                        (o2.timestamp - o1.timestamp).sign
                    } else if (o1.isCompleted && !o2.isCompleted) {
                        1
                    } else {
                        -1
                    }
                })

                adapter.setList(it)
                if (currentList == null)
                    recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.recycler_view_animation)

                currentList = it
            })

            viewModel.getAllUsers().observe(viewLifecycleOwner, Observer {
                adapter.setUsers(it)
            })
        }

        return view
    }

    override fun onStart() {
        super.onStart()
    }

    fun shareCurrentList() {
        if (currentList?.keepInSync == true) {
            val clipboard =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    "listId",
                    currentList?.id
                )
            )

            Toast.makeText(context, "copied to clipboard", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(context, "need to upload first", Toast.LENGTH_SHORT)
                .show()
        }
    }
}