package io.github.jpiasecki.shoppinglist.ui.editors

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.ui.viewmodels.ListUsersViewModel
import kotlinx.android.synthetic.main.activity_list_users.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListUsersActivity : AppCompatActivity() {

    private val viewModel: ListUsersViewModel by viewModels()

    private lateinit var adapter: ListUsersAdapter
    private var shoppingList: ShoppingList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_users)

        setSupportActionBar(activity_list_users_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)

        if (listId == null) {
            finish()
        }

        createAdapter()
        setupRecyclerView()

        viewModel.getShoppingList(listId!!).observe(this, Observer {
            adapter.setList(it)

            shoppingList = it
        })

        viewModel.getAllUsers().observe(this, Observer {
            adapter.setUsers(it)
        })
    }

    private fun createAdapter() {
        adapter = ListUsersAdapter().also {
            it.userClickCallback = { user ->
                shoppingList?.let {  list ->
                    if (list.owner == FirebaseAuth.getInstance().currentUser?.uid && user.id != list.owner) {
                        val dialog = BottomSheetDialog(this)
                        val view = layoutInflater.inflate(R.layout.dialog_user_options, null)

                        view.findViewById<TextView>(R.id.dialog_item_options_header_text).text = user.name

                        if (user.id in list.banned) {
                            view.findViewById<View>(R.id.dialog_user_options_give_ownership).visibility = View.GONE
                            view.findViewById<View>(R.id.dialog_user_options_ban).visibility = View.GONE
                        } else {
                            view.findViewById<View>(R.id.dialog_user_options_unban).visibility = View.GONE
                        }

                        view.findViewById<View>(R.id.dialog_user_options_give_ownership).setOnClickListener {
                            viewModel.changeOwner(list.id, user.id)

                            dialog.dismiss()
                        }

                        view.findViewById<View>(R.id.dialog_user_options_ban).setOnClickListener {
                            viewModel.banUser(list.id, user.id)

                            dialog.dismiss()
                        }

                        view.findViewById<View>(R.id.dialog_user_options_unban).setOnClickListener {
                            viewModel.unBanUser(list.id, user.id)

                            dialog.dismiss()
                        }

                        dialog.setContentView(view)
                        dialog.show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        activity_list_users_recycler_view.adapter = adapter
        activity_list_users_recycler_view.layoutManager = LinearLayoutManager(this)
        activity_list_users_recycler_view.setHasFixedSize(true)

        activity_list_users_recycler_view.apply {
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
                }
            }
        }

        val animator = activity_list_users_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return true
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(android.R.anim.fade_in, R.anim.activity_slide_down)
    }
}