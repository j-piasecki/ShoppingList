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
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.children
import androidx.core.view.size
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.AdProvider
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Icons
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.consts.Values.RC_SIGN_IN
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.User
import io.github.jpiasecki.shoppinglist.other.changeFragment
import io.github.jpiasecki.shoppinglist.ui.editors.AddEditItemActivity
import io.github.jpiasecki.shoppinglist.ui.editors.AddEditListActivity
import io.github.jpiasecki.shoppinglist.ui.SettingsActivity
import io.github.jpiasecki.shoppinglist.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    enum class FragmentType { Lists, ShoppingList }

    private val viewModel: MainViewModel by viewModels()

    private var currentFragment = FragmentType.Lists

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(activity_main_bottom_app_bar)
        loadUserProfilePicture()

        AdProvider.config = viewModel.getConfig()

        initFragments()

        activity_main_fab.setOnClickListener {
            when (currentFragment) {
                FragmentType.Lists -> {
                    startActivity(
                        Intent(
                            this,
                            AddEditListActivity::class.java
                        ),
                        getAnimationBundle(activity_main_fab)
                    )
                }

                FragmentType.ShoppingList -> {
                    val fragment = supportFragmentManager.primaryNavigationFragment as ShoppingListFragment

                    fragment.currentList?.let {
                        if (!it.keepInSync || Config.isNetworkConnected(this)) {
                            startActivity(
                                Intent(
                                    this,
                                    AddEditItemActivity::class.java
                                ).putExtra(Values.SHOPPING_LIST_ID, fragment.currentList?.id),
                                getAnimationBundle(activity_main_fab)
                            )
                        } else {
                            showToast(getString(R.string.message_need_internet_to_modify_list))
                        }
                    }
                }
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.primaryNavigationFragment is ListsFragment)
                onFragmentChange(FragmentType.Lists)
            else
                onFragmentChange(FragmentType.ShoppingList)
        }

        activity_main_bottom_app_bar.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            for ((index, item) in activity_main_bottom_app_bar.menu.children.iterator().withIndex()) {
                val view = activity_main_bottom_app_bar.findViewById<View>(item.itemId)
                view.scaleX = 0f
                view.scaleY = 0f

                val animator = view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(index * Values.BOTTOM_APP_BAR_MENU_ANIMATION_DELAY)
                    .setDuration(Values.BOTTOM_APP_BAR_MENU_ANIMATION_DURATION)
                    .setInterpolator(AccelerateInterpolator())
            }
        }

        checkActionIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        checkActionIntent(intent)
    }

    private fun checkActionIntent(intent: Intent?) {
        val listId = intent?.getStringExtra(Values.SHOPPING_LIST_ID)
        if (listId != null) {
            showImportListDialog(listId)
        }
    }

    private fun showImportListDialog(listId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            if (viewModel.isListDownloaded(listId)) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.message_list_already_downloaded))
                }

                return@launch
            }

            withContext(Dispatchers.Main) {
                val dialog = BottomSheetDialog(this@MainActivity)
                val view = layoutInflater.inflate(R.layout.dialog_import_list, null)

                val metadataLiveData = viewModel.getListMetadata(listId)
                metadataLiveData.observe(this@MainActivity, Observer {
                    if (it != null && it.id != Values.SHOPPING_LIST_ID_NOT_FOUND) {
                        view.findViewById<View>(R.id.dialog_import_list_progress_bar).visibility = View.GONE
                        view.findViewById<View>(R.id.dialog_import_list_layout).visibility = View.VISIBLE

                        view.findViewById<TextView>(R.id.dialog_import_list_name).text = it.name
                        view.findViewById<TextView>(R.id.dialog_import_list_note).text = it.note
                        view.findViewById<ImageView>(R.id.dialog_import_list_icon).setImageResource(Icons.getListIconId(it.icon))

                        it.owner?.let { owner ->
                            val user = User(id = owner)

                            user.loadProfileImage(this@MainActivity) {
                                view.findViewById<ImageView>(R.id.dialog_import_list_owner_icon).setImageBitmap(user.profilePicture)
                            }
                        }

                        metadataLiveData.removeObservers(this@MainActivity)

                        view.findViewById<MaterialButton>(R.id.dialog_import_list_cancel).setOnClickListener {
                            dialog.dismiss()
                        }

                        view.findViewById<MaterialButton>(R.id.dialog_import_list_save).setOnClickListener {
                            viewModel.downloadList(listId)
                                .observe(this@MainActivity, Observer {
                                    if (it == false) {
                                        showToast(getString(R.string.message_list_url_error))
                                    }
                                })

                            dialog.dismiss()
                        }
                    } else if (it == null) {
                        showToast(getString(R.string.message_list_url_error))

                        dialog.dismiss()
                    }
                })

                dialog.setOnCancelListener {
                    metadataLiveData.removeObservers(this@MainActivity)
                }

                dialog.setCancelable(true)
                dialog.setContentView(view)
                dialog.show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentFragment = supportFragmentManager.primaryNavigationFragment

        when (item.itemId) {
            android.R.id.home -> {
                if (FirebaseAuth.getInstance().currentUser == null) {
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                        RC_SIGN_IN
                    )
                } else {
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.dialog_profile_settings, null)
                    dialog.setContentView(view)

                    viewModel.getLocalUser(FirebaseAuth.getInstance().currentUser!!.uid).also {
                        it.observe(this, Observer { user ->
                            if (user != null) {
                                view.findViewById<TextView>(R.id.dialog_profile_settings_username).text = user.name

                                it.removeObservers(this)
                            }
                        })
                    }

                    view.findViewById<MaterialButton>(R.id.dialog_profile_settings_save_button).setOnClickListener {
                        val name = view.findViewById<TextView>(R.id.dialog_profile_settings_username).text.toString().trim()

                        if (name.length >= 3) {
                            viewModel.changeUserName(name)
                            dialog.dismiss()
                        } else {
                            showToast(getString(R.string.message_username_too_short))
                        }
                    }

                    dialog.show()
                }
            }

            R.id.menu_import_list -> {
                if (Config.isNetworkConnected(this)) {
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        val clipboard =
                            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val listId = clipboard.primaryClip?.getItemAt(0)?.text.toString()

                        try {
                            UUID.fromString(listId)

                            showImportListDialog(listId)
                        } catch (e: IllegalArgumentException) {
                            showToast(getString(R.string.message_no_list_id_in_clipboard))
                        }
                    } else {
                        showToast(getString(R.string.message_not_logged_in))
                    }
                } else {
                    showToast(getString(R.string.message_no_internet_connection))
                }
            }

            R.id.menu_share -> {
                if (currentFragment is ShoppingListFragment) {
                    currentFragment.shareCurrentList()
                }
            }

            R.id.menu_edit_list -> {
                if (currentFragment is ShoppingListFragment) {
                    currentFragment.currentList?.let {
                        if (it.owner == FirebaseAuth.getInstance().currentUser?.uid || it.owner == null) {
                            if (!it.keepInSync || Config.isNetworkConnected(this)) {
                                startActivity(
                                    Intent(
                                        this,
                                        AddEditListActivity::class.java
                                    ).putExtra(Values.SHOPPING_LIST_ID, it.id),
                                    getAnimationBundle(
                                        activity_main_bottom_app_bar.findViewById(
                                            item.itemId
                                        )
                                    )
                                )
                            } else {
                                showToast(getString(R.string.message_need_internet_to_modify_list))
                            }
                        } else {
                            showToast(getString(R.string.message_list_edit_no_ownership))
                        }
                    }
                }
            }

            R.id.menu_settings -> {
                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    ),
                    getAnimationBundle(activity_main_bottom_app_bar.findViewById(item.itemId))
                )
            }
        }

        return true
    }

    private fun onFragmentChange(type: FragmentType) {
        when (type) {
            FragmentType.Lists -> {
                for ((index, item) in activity_main_bottom_app_bar.menu.children.iterator().withIndex()) {
                    val animator = activity_main_bottom_app_bar.findViewById<View>(item.itemId)
                        .animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .setStartDelay(index * Values.BOTTOM_APP_BAR_MENU_ANIMATION_DELAY)
                        .setDuration(Values.BOTTOM_APP_BAR_MENU_ANIMATION_DURATION)
                        .setInterpolator(AccelerateInterpolator())

                    if (index == activity_main_bottom_app_bar.menu.size - 1) {
                        animator.withEndAction {
                            activity_main_bottom_app_bar.replaceMenu(R.menu.menu_activity_main_lists_bottom_app_bar)
                        }
                    }
                }
            }

            FragmentType.ShoppingList -> {
                for ((index, item) in activity_main_bottom_app_bar.menu.children.iterator().withIndex()) {
                    val animator = activity_main_bottom_app_bar.findViewById<View>(item.itemId)
                        .animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .setStartDelay(index * Values.BOTTOM_APP_BAR_MENU_ANIMATION_DELAY)
                        .setDuration(Values.BOTTOM_APP_BAR_MENU_ANIMATION_DURATION)
                        .setInterpolator(AccelerateInterpolator())

                    if (index == activity_main_bottom_app_bar.menu.size - 1) {
                        animator.withEndAction {
                            activity_main_bottom_app_bar.replaceMenu(R.menu.menu_activity_main_shopping_list_bottom_app_bar)
                        }
                    }
                }
            }
        }

        currentFragment = type
    }

    private fun initFragments() {
        val transaction = supportFragmentManager.beginTransaction()

        val shoppingListFragment = supportFragmentManager.findFragmentByTag(Values.FRAGMENT_SHOPPING_LIST_TAG) ?: ShoppingListFragment().also {
            transaction.add(R.id.activity_main_frame_layout, it, Values.FRAGMENT_SHOPPING_LIST_TAG)
        }

        val listsFragment = supportFragmentManager.findFragmentByTag(Values.FRAGMENT_LISTS_TAG) ?: ListsFragment().also {
            transaction.add(R.id.activity_main_frame_layout, it, Values.FRAGMENT_LISTS_TAG)
        }

        transaction.hide(shoppingListFragment)
        transaction.show(listsFragment)
        transaction.setPrimaryNavigationFragment(listsFragment)

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        transaction.commit()
    }

    private fun showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
        val toast = Toast.makeText(this, text, length)

        toast.view.findViewById<TextView>(android.R.id.message)?.let {
            it.gravity = Gravity.CENTER
        }

        toast.show()
    }

    override fun onBackPressed() {
        when (currentFragment) {
            FragmentType.ShoppingList -> {
                supportFragmentManager
                    .changeFragment(FragmentType.Lists)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()

                supportFragmentManager.popBackStack()
            }

            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            viewModel.setupUser()
            viewModel.trySettingOwner()
            viewModel.downloadRemoteLists()

            loadUserProfilePicture()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main_lists_bottom_app_bar, menu)

        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        AdProvider.destroyAds()
    }

    override fun onRestart() {
        super.onRestart()

        AdProvider.loadAds(applicationContext)
    }

    private fun loadUserProfilePicture() {
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

    companion object {
        fun getAnimationBundle(v: View): Bundle? {
            val left = 0
            val top = 0
            val width = v.measuredWidth
            val height = v.measuredHeight
            return ActivityOptionsCompat.makeScaleUpAnimation(v, left, top, width, height).toBundle()
        }
    }
}