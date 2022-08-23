package com.example.instalive.app

import splitties.preferences.Preferences

object SESSION {

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
    var isAnonymous by BoolPref("is_anonymous", false)
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