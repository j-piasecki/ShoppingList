package io.github.jpiasecki.shoppinglist.ui.editors

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.ui.viewmodels.ListUsersViewModel

@AndroidEntryPoint
class ListUsersActivity : AppCompatActivity() {

    private val viewModel: ListUsersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_users)

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)

        if (listId == null) {
            finish()
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(android.R.anim.fade_in, R.anim.activity_slide_down)
    }
}