package io.github.jpiasecki.shoppinglist.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.other.changeFragment
import io.github.jpiasecki.shoppinglist.ui.editors.AddEditItemActivity
import io.github.jpiasecki.shoppinglist.ui.editors.ListUsersActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sign

@AndroidEntryPoint
class ShoppingListFragment : Fragment() {

    private lateinit var adapter: ShoppingListItemsAdapter

    private val viewModel: MainViewModel by viewModels()

    private var listLiveData: LiveData<ShoppingList>? = null
    private var refreshLiveData: LiveData<Boolean?>? = null

    var currentList: ShoppingList? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        adapter = createAdapter()
        initRecyclerView(view)

        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)

                    currentList?.let {
                        viewModel.updateItems(it.id)
                    }
                }
            }
        )

        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        val argumentListId = arguments?.getString(Values.SHOPPING_LIST_ID)

        if (hidden) {
            listLiveData?.removeObservers(viewLifecycleOwner)
            viewModel.getAllUsers().removeObservers(viewLifecycleOwner)
            refreshLiveData?.removeObservers(viewLifecycleOwner)

            refreshLiveData = null
            currentList = null
            adapter.submitList(emptyList())

            viewModel.stopListeningForItemsChanges()
            viewModel.stopListeningForMetadataChanges()
        } else if (argumentListId != null) {
            openList(argumentListId)
        }
    }

    private fun openList(listId: String) {
        viewModel.updateItems(listId) {
            viewModel.tryUpdatingUsers(listId)
            viewModel.startListeningForItemsChanges(listId)
            viewModel.startListeningForMetadataChanges(listId)
        }

        listLiveData?.removeObservers(viewLifecycleOwner)
        listLiveData = viewModel.getShoppingList(listId)
        listLiveData?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                it.items.sortWith(Comparator { o1, o2 ->
                    if ((o1.completed && o2.completed) || (!o1.completed && !o2.completed)) {
                        if (o1.category == o2.category || (o1.category == null && !it.hasCategory(o2.category)) || (o2.category == null && !it.hasCategory(o1.category))) {
                            if (viewModel.getItemsSortType() == Config.SORT_TYPE_ALPHABETICALLY) {
                                o1.name?.compareTo(o2.name ?: "") ?: 0
                            } else {
                                (o2.timestamp - o1.timestamp).sign
                            }
                        } else {
                            it.getCategoryPosition(o1.category) - it.getCategoryPosition(o2.category)
                        }
                    } else if (o1.completed && !o2.completed) {
                        1
                    } else {
                        -1
                    }
                })

                adapter.timestampSetting = viewModel.getTimestampDisplay()
                adapter.displayAds = viewModel.areAdsEnabled()
                adapter.setList(it)
                if (currentList == null)
                    view?.findViewById<RecyclerView>(R.id.fragment_shopping_list_recycler_view)?.apply {
                        layoutAnimation =
                            AnimationUtils.loadLayoutAnimation(
                                context,
                                R.anim.recycler_view_animation
                            )

                        layoutAnimationListener = object : Animation.AnimationListener {
                            override fun onAnimationRepeat(animation: Animation?) {}

                            override fun onAnimationEnd(animation: Animation?) {}

                            override fun onAnimationStart(animation: Animation?) {
                                //wait until there are items in adapter, zero is safe because there is always header
                                if (adapter?.itemCount == 0) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        delay(10)
                                        startLayoutAnimation()
                                    }
                                }
                                //layoutManager?.findViewByPosition(0)?.clearAnimation()
                            }
                        }
                    }

                currentList = it

                view?.findViewById<TextView>(R.id.fragment_shopping_list_empty_text)?.visibility = if (it.items.isEmpty()) View.VISIBLE else View.GONE
            } else {
                showToast(getString(R.string.message_error))

                parentFragmentManager.changeFragment(MainActivity.FragmentType.Lists).addToBackStack(null).commit()
            }
        })

        viewModel.getAllUsers().observe(viewLifecycleOwner, Observer {
            adapter.setUsers(it)
        })
    }

    private fun createAdapter() =
        ShoppingListItemsAdapter().also {
            it.clickCallback = { id, view ->
                currentList?.let {
                    if (!it.keepInSync || Config.isNetworkConnected(context)) {
                        startActivity(
                            Intent(
                                context,
                                AddEditItemActivity::class.java
                            ).putExtra(Values.ITEM_ID, id)
                                .putExtra(Values.SHOPPING_LIST_ID, currentList?.id),
                            MainActivity.getAnimationBundle(view)
                        )
                    } else {
                        showToast(getString(R.string.message_need_internet_to_modify_list))
                    }
                }
            }

            it.itemCompletionChangeCallback = { id, completed ->
                currentList?.let {
                    if (!it.keepInSync || Config.isNetworkConnected(context)) {
                        viewModel.setItemCompleted(it.id, id, completed)
                    } else {
                        showToast(getString(R.string.message_need_internet_to_modify_list))
                    }
                }
            }

            it.longClickCallback = { item, itemView ->
                context?.let { context ->
                    val dialog = BottomSheetDialog(context)
                    val view =
                        layoutInflater.inflate(R.layout.dialog_item_options, null)
                    dialog.setContentView(view)
                    dialog.show()

                    view.findViewById<TextView>(R.id.dialog_item_options_header_text).text = item.name

                    view.findViewById<View>(R.id.dialog_item_options_delete)
                        .setOnClickListener {
                            currentList?.let {
                                if (!it.keepInSync || Config.isNetworkConnected(context)) {
                                    viewModel.removeItemFromList(it.id, item)
                                    dialog.dismiss()
                                } else {
                                    showToast(getString(R.string.message_need_internet_to_modify_list))
                                }
                            }
                        }
                }
            }

            it.userListClickCallback = { listId, view ->
                startActivity(
                    Intent(
                        context,
                        ListUsersActivity::class.java
                    ).putExtra(Values.SHOPPING_LIST_ID, listId),
                    MainActivity.getAnimationBundle(view)
                )
            }
        }

    override fun onPause() {
        super.onPause()

        viewModel.stopListeningForItemsChanges()
        viewModel.stopListeningForMetadataChanges()
    }

    override fun onResume() {
        super.onResume()

        if (!isHidden) {
            if (currentList == null) {
                val argumentListId = arguments?.getString(Values.SHOPPING_LIST_ID)

                if (argumentListId != null)
                    openList(argumentListId)
            } else {
                currentList?.let {
                    viewModel.updateItems(it.id) {
                        viewModel.startListeningForItemsChanges(it.id)
                        viewModel.startListeningForMetadataChanges(it.id)
                    }
                }
            }
        }
    }

    private fun initRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_shopping_list_recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        val animator = recyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    fun shareCurrentList() {
        currentList?.let {
            if (Config.isNetworkConnected(context)) {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    if (!it.keepInSync) {
                        viewModel.uploadList(it)
                    }

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "https://j-piasecki.github.io/shoppinglist/?id=${it.id}")
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, getString(R.string.message_share_list))
                    startActivity(shareIntent)
                } else {
                    showToast(getString(R.string.message_not_logged_in))
                }
            } else {
                showToast(getString(R.string.message_no_internet_connection))
            }
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