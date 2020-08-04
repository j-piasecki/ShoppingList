package io.github.jpiasecki.shoppinglist.ui.editors

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import kotlinx.android.synthetic.main.activity_select_icon.*
import kotlinx.android.synthetic.main.activity_settings.*

class SelectIconActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_icon)

        setSupportActionBar(activity_select_icon_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        val listId = intent.getStringExtra(Values.SHOPPING_LIST_ID)
        val itemId = intent.getStringExtra(Values.ITEM_ID)
        val name = intent.getStringExtra(Values.NAME)

        activity_select_icon_recycler_view.adapter = IconsAdapter(itemId == null) { icon ->
            setResult(
                Values.RC_SELECT_ICON,
                Intent().putExtra(Values.ICON, icon)
            )

            finish()
        }

        activity_select_icon_recycler_view.layoutManager = GridLayoutManager(this, 4)
        activity_select_icon_recycler_view.setHasFixedSize(true)
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(android.R.anim.fade_in, R.anim.activity_slide_down)
    }
}