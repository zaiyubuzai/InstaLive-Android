package com.example.instalive.model

import com.example.instalive.app.InstaLivePreferences
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.venus.framework.util.isNeitherNullNorEmpty
import timber.log.Timber

object InstaLiveStringTemplate {
    var template: StringTemplate? = null

    fun cacheTemplate(template: String) {
        Timber.d("VenusStringTemplate 1 ${template.isEmpty()}")
        InstaLivePreferences.stringTemplate = template
        loadTemplate()
    }

    fun loadTemplate() {
        Timber.d("VenusStringTemplate 2 ${InstaLivePreferences.stringTemplate.isNullOrEmpty()}")
        val template = InstaLivePreferences.stringTemplate
        if (template.isNeitherNullNorEmpty()) {
            InstaLiveStringTemplate.template = try {
                Gson().fromJson(template, StringTemplate::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
//{
//  "result": "ok",
//  "data": {
//    "version": 9,
//    "unsupported_msg_type_tips": "[To view this message, please upgrade the app.] Upgrade now >",
//    "performer_valid_time_options": [
//      {
//        "name": "Within 24h",
//        "value": 86400
//      },
//      {
//        "name": "Within 48h",
//        "value": 172800
//      },
//      {
//        "name": "Within 72h",
//        "value": 259200
//      },
//      {
//        "name": "Within 5d",
//        "value": 432000
//      },
//      {
//        "name": "Within 7d",
//        "value": 604800
//      }
//    ],
//    "performer_take_rate_options": [
//      {
//        "name": "100%",
//        "value": 100
//      },
//      {
//        "name": "90%",
//        "value": 90
//      },
//      {
//        "name": "80%",
//        "value": 80
//      },
//      {
//        "name": "70%",
//        "value": 70
//      },
//      {
//        "name": "60%",
//        "value": 60
//      },
//      {
//        "name": "50%",
//        "value": 50
//      },
//      {
//        "name": "40%",
//        "value": 40
//      },
//      {
//        "name": "30%",
//        "value": 30
//      },
//      {
//        "name": "20%",
//        "value": 20
//      },
//      {
//        "name": "10%",
//        "value": 10
//      },
//      {
//        "name": "0%",
//        "value": 0
//      }
//    ],
//    "live_divide_take_rate_options": [
//      {
//        "name": "60%",
//        "value": 60
//      },
//      {
//        "name": "50%",
//        "value": 50
//      },
//      {
//        "name": "40%",
//        "value": 40
//      },
//      {
//        "name": "30%",
//        "value": 30
//      },
//      {
//        "name": "20%",
//        "value": 20
//      },
//      {
//        "name": "10%",
//        "value": 10
//      },
//      {
//        "name": "Off",
//        "value": 0
//      }
//    ],
//    "bonus_tips_title": "10% Bonus Coins for Every Recharge",
//    "bonus_tips_img": "https://res-cf.joinfambase.com/media/icons/strip_tips_10.png",
//    "bonus_tips_desc": "Great exclusive Offer!\nPay with card, Get 10% extra coins for every recharge",
//    "bonus_tips_button_card": "Pay with Card Get Bonus",
//    "bonus_tips_button_iap": "Pay with Google",
//    "recharge_list_activity_icons": {
//      "big_deal": "https://res-cf.joinfambase.com/media/icons/activity_big_deal.png"
//    },
//    "portrait_icons": {
//      "new_gifter": "https://res-cf.joinfambase.com/media/icons/FirstRechargeIcon.png"
//    },
//    "pvc_pop_info": {
//      "title": "You can voice chat during the video call and send the host gifts.",
//      "desc": ""
//    },
//    "pvc2_pop_info": {
//      "title": "",
//      "desc": "You can voice chat during the video call. If you hang up within 15s, you will not be charged."
//    }
//  }
//}
data class StringTemplate(
    val version: Int,
    @SerializedName("unsupported_msg_type_tips")val unsupportedMsgTypeTips: String,
    @SerializedName("performer_valid_time_options")val performerValidTimeOptions: List<OptionsData> = listOf(),
    @SerializedName("performer_take_rate_options")val performerTake_rateOptions: List<OptionsData> = listOf(),
    @SerializedName("live_divide_take_rate_options")val liveDivideTakeRateOptions: List<OptionsData> = listOf(),
    @SerializedName("bonus_tips_title")val bonusTipsTitle: String,
    @SerializedName("bonus_tips_img")val bonusTipsImg: String,
    @SerializedName("bonus_tips_desc")val bonusTipsDesc: String,
    @SerializedName("bonus_tips_button_card")val bonusTipsButtonCard: String,
    @SerializedName("bonus_tips_button_iap")val bonusTipsButtonIap: String,
    @SerializedName("recharge_list_activity_icons")val rechargeListActivityIcons: RechargeIconsData,
    @SerializedName("portrait_icons")val portraitIcons: PortraitIconsData,
    @SerializedName("pvc_pop_info")val pvcPopInfo: PopInfoData,
    @SerializedName("pvc2_pop_info")val pvc2PopInfo: PopInfoData,
)

data class OptionsData(
    val name: String,
    val value: Int,
)

data class PopInfoData(
    val title: String,
    val desc: String,
)

data class PortraitIconsData(
    @SerializedName("new_gifter")val newGifter: String,
)
data class RechargeIconsData(
    @SerializedName("big_deal")val bigDeal: String,
)