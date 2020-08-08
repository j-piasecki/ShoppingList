package io.github.jpiasecki.shoppinglist

import android.content.Context
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AdProvider {
    companion object {
        private const val PRELOADED_ADS_COUNT = 7

        private val ads = ArrayList<UnifiedNativeAd>()
        private var destroyed = false
        private var nextAdIndex = 0

        fun loadAds(context: Context) {
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

                    adLoader.loadAd(AdRequest.Builder().build())
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

            val result = nextAdIndex
            nextAdIndex = (nextAdIndex + 1) % ads.size

            return result
        }

        fun getAd(id: Int): UnifiedNativeAd? {
            if (ads.isEmpty() || id < 0)
                return null

            return ads[id]
        }
    }
}