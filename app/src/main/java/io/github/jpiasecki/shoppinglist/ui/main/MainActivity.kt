package io.github.jpiasecki.shoppinglist.ui.main

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import androidx.lifecycle.Observer
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.consts.Values.RC_SIGN_IN
import io.github.jpiasecki.shoppinglist.database.*
import io.github.jpiasecki.shoppinglist.ui.AddEditItemActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private enum class FragmentType { Lists, ShoppingList }

    private val viewModel: MainViewModel by viewModels()

    private var currentFragment = FragmentType.Lists

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(activity_main_bottom_app_bar)
        loadUserProfilePicture()

        changeFragment(ListsFragment())

        activity_main_fab.setOnClickListener {
            when (currentFragment) {
                FragmentType.Lists -> {
                    viewModel.createList()
                }

                FragmentType.ShoppingList -> {
                    val fragment = supportFragmentManager.findFragmentById(R.id.activity_main_frame_layout) as ShoppingListFragment

                    startActivity(
                        Intent(
                            this,
                            AddEditItemActivity::class.java
                        ).putExtra(Values.SHOPPING_LIST_ID, fragment.currentList?.id)
                    )
                }
            }
        }

        onFragmentChange(FragmentType.Lists)

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.findFragmentById(R.id.activity_main_frame_layout) is ListsFragment)
                onFragmentChange(FragmentType.Lists)
            else
                onFragmentChange(FragmentType.ShoppingList)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.activity_main_frame_layout)

        when (item.itemId) {
            android.R.id.home -> {
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                        .build(),
                    RC_SIGN_IN
                )
            }

            R.id.menu_import_list -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                viewModel.downloadList(clipboard.primaryClip?.getItemAt(0)?.text.toString()).observe(this, Observer {
                    if (it == false) {
                        Toast.makeText(this, "list ${clipboard.primaryClip?.getItemAt(0)?.text.toString()} doesn't exist", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            R.id.menu_share -> {
                if (currentFragment is ShoppingListFragment) {
                    currentFragment.shareCurrentList()
                }
            }
        }

        return true
    }

    private fun onFragmentChange(type: FragmentType) {
        when (type) {
            FragmentType.Lists -> {
                activity_main_bottom_app_bar.replaceMenu(R.menu.menu_activity_main_lists_bottom_app_bar)
            }

            FragmentType.ShoppingList -> {
                activity_main_bottom_app_bar.replaceMenu(R.menu.menu_activity_main_shopping_list_bottom_app_bar)
            }
        }

        currentFragment = type
    }

    private fun changeFragment(target: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.activity_main_frame_layout, target)

        target.enterTransition = Fade()
        supportFragmentManager.findFragmentById(R.id.activity_main_frame_layout)?.exitTransition = Fade()

        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "logged in ${FirebaseAuth.getInstance().currentUser?.displayName}", Toast.LENGTH_SHORT).show()

            viewModel.setupUser()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main_lists_bottom_app_bar, menu)

        return true
    }

    fun loadUserProfilePicture() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Glide.with(this).asBitmap().load(FirebaseAuth.getInstance().currentUser?.photoUrl)
                .circleCrop().into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        activity_main_bottom_app_bar.navigationIcon = BitmapDrawable(resources, bitmap)
                    }
                })
        }
    }
}