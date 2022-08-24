package com.example.instalive.app

import com.example.instalive.api.RetrofitProvider
import com.example.instalive.model.LoginData
import splitties.preferences.Preferences
import splitties.preferences.edit

//val id: String,
//    @SerializedName("user_name") val userName: String,
//    @SerializedName("nickname") val nickName: String?,
//    @SerializedName("first_name") val firstName: String?,
//    @SerializedName("last_name") val lastName: String?,
//    val portrait: String?,
//    val gender: Int,
//    val identity: Int,
//    val lat: String?,
//    val lon: String?,
//    val country: String?,
//    val region: String?,
//    val city: String?,
//    val token: String,
//    val state: Int,
//    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_anonymous") val isAnonymous: Boolean,
//    @SerializedName("invite_info") val inviteInfo: Any?,
//    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_create") val isCreate: Boolean,
object SESSION {

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
    //---user stats end

}

object InstaLivePreferences : Preferences("instaLiveState") {
    var countryCodeVersion by IntPref("country_code_version", 0)
    var countryCodeJson by StringOrNullPref("country_code_json", null)
}