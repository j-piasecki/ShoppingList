package io.github.jpiasecki.shoppinglist.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.Fade
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.other.changeFragment
import io.github.jpiasecki.shoppinglist.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.fragment_lists.*

@AndroidEntryPoint
class ListsFragment : Fragment() {

    private lateinit var adapter: ShoppingListsAdapter

    private var shoppingLists: List<ShoppingList> = emptyList()
    private val viewModel: MainViewModel by viewModels()

    private var refreshLiveData: LiveData<Boolean?>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lists, container, false)

        adapter = createAdapter()

        initRecyclerView(view)

        viewModel.getAllShoppingLists().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)

            shoppingLists = it
        })

        viewModel.getAllUsers().observe(viewLifecycleOwner, Observer {
            adapter.setUsers(it)
        })

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.fragment_lists_refresh_layout)

        refreshLayout.setOnRefreshListener {
            if (refreshLiveData == null) {
                if (viewModel.canSyncMetadataManually()) {
                    refreshLiveData = viewModel.syncAllListsMetadata()

                    refreshLiveData?.observe(viewLifecycleOwner, Observer {
                        if (it == true) {
                            refreshLayout.isRefreshing = false

                            refreshLiveData?.removeObservers(viewLifecycleOwner)
                            refreshLiveData = null
                        }
                    })
                } else {
                    refreshLayout.isRefreshing = false
                }
            }
        }

        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            viewModel.getAllShoppingLists().removeObservers(viewLifecycleOwner)
            viewModel.getAllUsers().removeObservers(viewLifecycleOwner)

            refreshLiveData?.removeObservers(viewLifecycleOwner)
            refreshLiveData = null
        } else {
            viewModel.getAllShoppingLists().observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)

                shoppingLists = it
            })

            viewModel.getAllUsers().observe(viewLifecycleOwner, Observer {
                adapter.setUsers(it)
            })
        }
    }

    private fun createAdapter() =
        ShoppingListsAdapter().also {
            it.clickCallback = { id ->
                parentFragmentManager.changeFragment(
                    MainActivity.FragmentType.ShoppingList,
                    Bundle().apply {
                        putString(Values.SHOPPING_LIST_ID, id)
                    }).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null)
                    .commit()
            }

            it.longClickCallback = { id ->
                val list = shoppingLists.find { it.id == id }

                if (list != null) {
                    context?.let {
                        val dialog = BottomSheetDialog(it)
                        val view =
                            layoutInflater.inflate(R.layout.dialog_shopping_list_options, null)
                        dialog.setContentView(view)
                        dialog.show()

                        view.findViewById<TextView>(R.id.dialog_shopping_list_options_delete)
                            .setOnClickListener {
                                viewModel.deleteList(list)
                                dialog.dismiss()
                            }

                        view.findViewById<TextView>(R.id.dialog_shopping_list_options_upload)
                            .setOnClickListener {
                                viewModel.uploadList(list)
                                dialog.dismiss()
                            }
                    }
                }
            }
        }

    private fun initRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_lists_recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        val animator = recyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }
}