package io.github.jpiasecki.shoppinglist.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.jpiasecki.shoppinglist.R
import io.github.jpiasecki.shoppinglist.consts.Values
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        setSupportActionBar(activity_web_view_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        val type = intent.getStringExtra(Values.WEB_VIEW_TYPE) ?: Values.WEB_VIEW_PRIVACY_POLICY

        supportActionBar?.title =
            if (type == Values.WEB_VIEW_PRIVACY_POLICY)
                getString(R.string.privacy_policy)
            else
                getString(R.string.terms_and_conditions)

        GlobalScope.launch(Dispatchers.IO) {
            val stream =
                if (type == Values.WEB_VIEW_PRIVACY_POLICY)
                    resources.openRawResource(R.raw.privacy_policy)
                else
                    resources.openRawResource(R.raw.terms_conditions)

            val buffer = ByteArray(stream.available())

            while (stream.read(buffer) != -1);

            val data = String(buffer)

            withContext(Dispatchers.Main) {
                activity_web_view_content.loadData(data, "text/html; charset=utf-8", "UTF-8")
            }
        }
    }
}