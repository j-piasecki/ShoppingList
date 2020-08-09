package io.github.jpiasecki.shoppinglist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.ui.WebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AdProvider {
    companion object {
        private const val PRELOADED_ADS_COUNT = 7

        lateinit var config: Config

        private val ads = ArrayList<UnifiedNativeAd>()
        private var destroyed = false
        private var nextAdIndex = 0

        fun loadAds(context: Context) {
            val adsType = config.getAdsType()

            if (adsType == Config.ADS_PERSONALIZED)
                loadAds(context, true)
            else if (adsType == Config.ADS_NOT_PERSONALIZED)
                loadAds(context, false)
        }

        private fun loadAds(context: Context, personalized: Boolean) {
            destroyed = false

            for (i in 0 until PRELOADED_ADS_COUNT) {
                GlobalScope.launch(Dispatchers.IO) {
                    val adLoader =
                        AdLoader.Builder(context, context.getString(R.string.admob_ads_id))
                            .forUnifiedNativeAd { unifiedNativeAd ->
                                if (destroyed)
                                    unifiedNativeAd.destroy()
                                else
                                    ads.add(unifiedNativeAd)
                            }
                            .build()

                    if (personalized)
                        adLoader.loadAd(AdRequest.Builder().build())
                    else
                        adLoader.loadAd(AdRequest.Builder()
                            .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply { putString("npa", "1") })
                            .build())
                }
            }
        }

        fun destroyAds() {
            for (ad in ads) {
                ad.destroy()
            }

            ads.clear()
            destroyed = true
        }

        @Synchronized
        fun getNextAdId(): Int {
            if (ads.isEmpty())
                return -1

            val result = nextAdIndex % ads.size
            nextAdIndex = (nextAdIndex + 1) % ads.size

            return result
        }

        fun getAd(id: Int, context: Context): UnifiedNativeAd? {
            if (ads.isEmpty() && config.getAdsType() == Config.ADS_UNDEFINED)
                getConsent(context)

            if (ads.isEmpty() || id < 0)
                return null

            return ads[id]
        }

        private fun getConsent(context: Context) {
            val dialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_ask_consent, null)

            dialog.setContentView(view)
            dialog.setCancelable(false)

            dialog.findViewById<TextView>(R.id.dialog_ask_consent_info)?.setOnClickListener {
                 context.startActivity(
                     Intent(context, WebViewActivity::class.java)
                         .putExtra(Values.WEB_VIEW_TYPE, Values.WEB_VIEW_PRIVACY_POLICY)
                 )
            }

            dialog.findViewById<MaterialButton>(R.id.dialog_ask_consent_button_personalized)?.setOnClickListener {
                config.setAdsType(Config.ADS_PERSONALIZED)
                loadAds(context, true)
                dialog.dismiss()
            }

            dialog.findViewById<MaterialButton>(R.id.dialog_ask_consent_button_non_personalized)?.setOnClickListener {
                config.setAdsType(Config.ADS_NOT_PERSONALIZED)
                loadAds(context, false)
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}