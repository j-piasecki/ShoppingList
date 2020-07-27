package io.github.jpiasecki.shoppinglist.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.database.*
import io.github.jpiasecki.shoppinglist.remote.ShoppingListsRemoteSource
import io.github.jpiasecki.shoppinglist.remote.UsersRemoteSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var usersRemoteSource: UsersRemoteSource
    @Inject lateinit var shoppingListsRemoteSource: ShoppingListsRemoteSource

    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_login.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                    .build(),
                RC_SIGN_IN
            )
        }

        var listId = UUID.randomUUID().toString()
        var milkId = ""
        var sugarId = ""
        var appleId = ""

        button_create_list.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity,
                    shoppingListsRemoteSource
                        .setList(
                            ShoppingList(
                                listId,
                                "nazwa listy",
                                FirebaseAuth.getInstance().currentUser?.uid,
                                "pln"
                            )
                        )
                        .toString()
                    , Toast.LENGTH_SHORT).show()
            }
        }

        button_add_milk.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val item = Item("milk", "nice milk", FirebaseAuth.getInstance().currentUser?.uid, quantity = 1)
                milkId = item.id

                Toast.makeText(
                    this@MainActivity,
                    shoppingListsRemoteSource.addItemToList(listId, item).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        button_add_sugar.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val item = Item("sugar", "best sugar", FirebaseAuth.getInstance().currentUser?.uid, quantity = 3)
                sugarId = item.id

                Toast.makeText(
                    this@MainActivity,
                    shoppingListsRemoteSource.addItemToList(listId, item).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        button_add_apples.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val item = Item("apple", "pog apples", FirebaseAuth.getInstance().currentUser?.uid, quantity = 6)
                appleId = item.id

                Toast.makeText(
                    this@MainActivity,
                    shoppingListsRemoteSource.addItemToList(listId, item).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        button_update_milk.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val item = Item("milk", "got milk", FirebaseAuth.getInstance().currentUser?.uid, "p1", true, quantity = 3, id = milkId)

                Toast.makeText(
                    this@MainActivity,
                    shoppingListsRemoteSource.addItemToList(listId, item).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        button_update_apples.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val item = Item("apple", "not enough", FirebaseAuth.getInstance().currentUser?.uid, "p2", true, quantity = 3, id = appleId)

                Toast.makeText(
                    this@MainActivity,
                    shoppingListsRemoteSource.addItemToList(listId, item).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        button_delete_milk.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    shoppingListsRemoteSource.removeItemFromList(listId, milkId).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        button_delete_list.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, shoppingListsRemoteSource.deleteList(listId).toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "logged in ${FirebaseAuth.getInstance().currentUser?.displayName}", Toast.LENGTH_SHORT).show()
        }
    }
}