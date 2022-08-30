package com.example.instalive.app

import com.example.instalive.R

object Constants {


    const val EXTRA_USERNAME = "username"
    const val EXTRA_LOGIN_SOURCE = "login_source"
    const val EXTRA_CONVERSATION_ENTITY = "conversation_entity"

    const val DEFAULT_TIME_OUT = 15L

    //event cat
    const val EVENT_BUS_KEY_LOGIN = "key_login"
    const val EVENT_BUS_KEY_LIVE = "key_live"
    const val EVENT_BUS_KEY_USER = "key_user"

    //event themselves
    const val EVENT_BUS_LOGIN_SUCCESS = "login_success"
    const val EVENT_BUS_REPLY: String = "reply"
    const val EVENT_BUS_PROFILE_USER_ID: String = "profile_user_id"

    // 1 直播开启 2 直播结束 3 主播掉线 4 主播重连
    const val LIVE_START = 1
    const val LIVE_END = 2
    const val LIVE_LEAVE = 3
    const val LIVE_RESUME = 4

    val DEFAULT_EMOJI_LIST = listOf(
        R.mipmap.emoji_1,
        R.mipmap.emoji_2,
        R.mipmap.emoji_3,
        R.mipmap.emoji_4,
        R.mipmap.emoji_5,
        R.mipmap.emoji_6,
        R.mipmap.emoji_7
    )
}