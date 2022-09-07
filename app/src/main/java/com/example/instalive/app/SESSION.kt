package com.example.instalive.app

import android.content.Context
import com.example.baselibrary.utils.SharedPreferencesUtil
import com.example.instalive.api.RetrofitProvider
import com.example.instalive.model.LoginData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.venus.dm.model.UserData
import splitties.preferences.Preferences
import splitties.preferences.edit

object SESSION {

    fun <T> getSpValue(context: Context?, name: String, default: T): T? {
        val c = context ?: return null
        val data: T by SharedPreferencesUtil(
            c,
            name,
            default
        )
        return data
    }

    fun <T> setSpValue(context: Context?, name: String, value: T, default: T) {
        val c = context ?: return
        var data: T by SharedPreferencesUtil(
            c,
            name,
            default
        )
        data = value
    }

    fun saveLoginData(data: LoginData){
        SessionPreferences.apply {
            id = data.id
            userName = data.userName
            nickName = data.nickName
            firstName = data.firstName
            lastName = data.lastName
            portrait = data.portrait
            gender = data.gender
            identity = data.identity
            lat = data.lat
            lon = data.lon
            country = data.country
            region = data.region
            city = data.city
            token = data.token
            state = data.state
            isAnonymous = data.isAnonymous
            isCreate = data.isCreate
        }
        //刷新ua
        RetrofitProvider.generateUA()
    }

    fun retrieveMeInfo(): UserData? {
        return if (SessionPreferences.id.isNotEmpty()) {
            SessionPreferences.let {
                UserData(
                    id = SessionPreferences.id,
                    nickname = SessionPreferences.nickName ?: "",
                    username = SessionPreferences.userName ?: "",
                    portrait = SessionPreferences.portrait ?: "",
                    email = SessionPreferences.email ?: "",
                    bio = SessionPreferences.bio ?: "",
                    mobile = SessionPreferences.phone,
                    location = null,
                    stat = null,
                    relationship = -1,
                    portraitIc = null,
                    muteTimestamp = SessionPreferences.muteTimestamp,
                    gender = SessionPreferences.gender,
                    chatState = SessionPreferences.state,
                    identity = SessionPreferences.identity
                )
            }
        } else {
            null
        }
    }

    fun resetLoginInfo(): LoginData {
        val emptyLoginInfo = LoginData.emptyLoginInfo()
        SessionPreferences.edit(true) {
            id  = ""
            userName = null
            nickName = null
            firstName = null
            lastName = null
            bio = null
            phone = null
            email = null
            portrait = null
            isVerified = false
            initDataJson = null
            portraitIc = null
            pushToken = null
            balance = 0f
            state = 0
            birthday = ""
            gender = 0
            identity = 0
            muteTimestamp = 0L
            divideIncomeState = false
            isFirstRecharging = false
        }
        return emptyLoginInfo
    }
}

object SessionPreferences : Preferences("instaLiveUserState") {
    var id by StringPref("id", "")
    var userName by StringOrNullPref("user_name")
    var nickName by StringOrNullPref("nickname")
    var firstName by StringOrNullPref("first_name")
    var lastName by StringOrNullPref("last_name")
    var bio by StringOrNullPref("bio")
    var twitter by StringOrNullPref("member_twitter")
    var youtube by StringOrNullPref("member_youtube")
    var tiktok by StringOrNullPref("member_tiktok")
    var instagram by StringOrNullPref("member_instagram")
    var phone by StringOrNullPref("mobile_phone")
    var email by StringOrNullPref("email")
    var portrait by StringOrNullPref("portrait")
    var lat by StringOrNullPref("lat", "0.0")
    var lon by StringOrNullPref("lon", "0.0")
    var country by StringOrNullPref("country")
    var countryCode by StringOrNullPref("countryCode")
    var region by StringOrNullPref("region")
    var city by StringOrNullPref("city")
    var timezone by StringOrNullPref("timezone")
    var token by StringPref("token", "")
    var state by IntPref("state", 0)
    var gender by IntPref("gender", 0)
    var identity by IntPref("identity", 0)//role：host or viewer
    var isAnonymous by BoolPref("is_anonymous", false)
    var isCreate by BoolPref("isCreate", false)
    var createdAt by LongPref("created_at", 0)
    var deviceId by StringOrNullPref("device_id")
    var lastLon by StringOrNullPref(key = "last_lon")
    var lastLat by StringOrNullPref("last_lat")
    var lastRegion by StringOrNullPref("last_region")
    var lastCity by StringOrNullPref("last_city")
    var lastLocAcc by StringOrNullPref("last_loc_acc")
    var isVerified by BoolPref("is_verified", false)
    var portraitIc by StringOrNullPref("portrait_ic")
    var pushToken by StringOrNullPref("push_token")
    var muteTimestamp by LongPref("mute_timestamp", 0L)
    var divideIncomeState by BoolPref("divide_income_state", false)
    var isFirstRecharging by BoolPref("first_recharging", false)
    var birthdayError by BoolPref("birthday_error", false)
    var birthday by StringPref("birthday_millis", "")

    //---user stats starts
    var balance by FloatPref("balance", 0f)
    var initDataJson by StringOrNullPref("init_data_json")
    //---user stats end

}

object InstaLivePreferences : Preferences("instaLiveState") {
    var liveRaiseHandDialogShowed by BoolPref("live_raise_hand_dialog_showed", false)
    var countryCodeVersion by IntPref("country_code_version", 0)
    var countryCodeJson by StringOrNullPref("country_code_json", null)

    var stringTemplateVersion by IntPref("string_template_version", 0)
    var stringTemplate by StringOrNullPref("string_template", null)

    var levelIconsVersion by IntPref("level_icon", 0)
    var levelIconsJson by StringOrNullPref("level_icons_json", null)

    var everydayFirstInitTime by LongPref("everyday_first_init_time", 0L)
    var firstInitEver by BoolPref("first_init_ever", false)

    var liveGiftList by StringOrNullPref("live_gifts", null)
    var liveGiftVersion by IntPref("live_gifts_version", 0)
    var liveSendGiftClicked by BoolPref("live_send_gift_clicked", false)
    var liveGiftCache by StringPref("live_gift_cache", "[]")


    fun findGiftCache(imgUrl:String):String?{
        var netPath = imgUrl
        val netPathList = netPath.split("/")
        if (netPathList.size < 2) return null
        netPath = netPathList.last()
        netPath = netPath.replace(".svga", "")+"_"

        val list = Gson().fromJson<List<String>>(
            liveGiftCache,
            object : TypeToken<List<String>>() {}.type
        ).toMutableList()
        return list.firstOrNull { it.contains(netPath) }
    }
}