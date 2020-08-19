package io.github.jpiasecki.shoppinglist.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.other.changeFragment
import io.github.jpiasecki.shoppinglist.ui.viewmodels.MainViewModel
import java.util.*

@AndroidEntryPoint
class ListsFragment : Fragment() {

    private lateinit var adapter: ShoppingListsAdapter

    private var shoppingLists: List<ShoppingList> = emptyList()
    private val viewModel: MainViewModel by viewModels()

    private var refreshLiveData: LiveData<Boolean?>? = null

    private var lastClickTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lists, container, false)

        adapter = createAdapter()

        initRecyclerView(view)

        viewModel.getAllShoppingLists().observe(viewLifecycleOwner, Observer {
            setCurrentList(it)
        })

        viewModel.getAllUsers().observe(viewLifecycleOwner, Observer {
            adapter.setUsers(it)
        })

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.fragment_lists_refresh_layout)

        refreshLayout.setOnRefreshListener {
            if (refreshLiveData == null) {
                if (viewModel.canSyncListsManually() && Config.isNetworkConnected(context)) {
                    refreshLiveData = viewModel.syncAllLists()

                    refreshLiveData?.observe(viewLifecycleOwner, Observer {
                        if (it != null) {
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
                setCurrentList(it)
            })

            viewModel.getAllUsers().observe(viewLifecycleOwner, Observer {
                adapter.setUsers(it)
            })
        }
    }

    private fun setCurrentList(list: List<ShoppingList>) {
        shoppingLists = if (viewModel.getListsSortType() == Config.SORT_TYPE_ALPHABETICALLY) {
            list.sortedBy { it.name }
        } else {
            list.sortedByDescending { it.timestamp }
        }

        adapter.submitList(shoppingLists.filter { FirebaseAuth.getInstance().currentUser?.uid !in it.banned })

        view?.findViewById<TextView>(R.id.fragment_lists_empty_text)?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()

        setCurrentList(shoppingLists)
    }

    private fun createAdapter() =
        ShoppingListsAdapter().also {
            it.clickCallback = { id ->
                if (Calendar.getInstance().timeInMillis - lastClickTime > 400) {
                    parentFragmentManager.changeFragment(
                        MainActivity.FragmentType.ShoppingList,
                        Bundle().apply {
                            putString(Values.SHOPPING_LIST_ID, id)
                        }).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(null)
                        .commit()

                    lastClickTime = Calendar.getInstance().timeInMillis
                }
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

                        view.findViewById<TextView>(R.id.dialog_shopping_list_options_header_text).text = list.name

                        if (list.keepInSync) {
                            view.findViewById<TextView>(R.id.dialog_shopping_list_create_copy_text).text = getString(R.string.dialog_shopping_list_options_create_local_copy)

                            view.findViewById<View>(R.id.dialog_shopping_list_options_upload).visibility = View.GONE
                        }

                        view.findViewById<View>(R.id.dialog_shopping_list_options_delete)
                            .setOnClickListener { v ->
                                if (!list.keepInSync || Config.isNetworkConnected(it)) {
                                    viewModel.deleteList(list)
                                    dialog.dismiss()
                                } else {
                                    showToast(getString(R.string.message_need_internet_to_modify_list))
                                }
                            }

                        view.findViewById<View>(R.id.dialog_shopping_list_options_upload)
                            .setOnClickListener { v ->
                                if (Config.isNetworkConnected(it)) {
                                    if (FirebaseAuth.getInstance().currentUser != null) {
                                        viewModel.uploadList(list)
                                        dialog.dismiss()
                                    } else {
                                        showToast(getString(R.string.message_not_logged_in))
                                    }
                                } else {
                                    showToast(getString(R.string.message_no_internet_connection))
                                }
                            }

                        view.findViewById<View>(R.id.dialog_shopping_list_create_copy)
                            .setOnClickListener {
                                viewModel.createListCopy(list.id)
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

    private fun showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
        val toast = Toast.makeText(context, text, length)

        toast.view.findViewById<TextView>(android.R.id.message)?.let {
            it.gravity = Gravity.CENTER
        }

        toast.show()
    }
}