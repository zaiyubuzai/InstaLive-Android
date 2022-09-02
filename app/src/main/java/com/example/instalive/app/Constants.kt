package com.example.instalive.app

import com.example.instalive.R
import io.agora.rtc.video.BeautyOptions

object Constants {

    val DEFAULT_BEAUTY_OPTIONS = BeautyOptions(
        BeautyOptions.LIGHTENING_CONTRAST_NORMAL,
        0.7f,
        0.5f,
        0.1f
    )

    const val EXTRA_USERNAME = "username"
    const val EXTRA_LOGIN_SOURCE = "login_source"
//    const val EXTRA_CONVERSATION_ENTITY = "conversation_entity"

    const val DEFAULT_TIME_OUT = 15L

    //event cat
    const val EVENT_BUS_KEY_LIVE_HOST_ACTIONS = "event_bus_live_host_actions"
    const val EVENT_BUS_KEY_NOT_GO_BACK = "event_bus_key_not_go_back"
    const val EVENT_BUS_KEY_TELEPHONY = "event_bus_key_telephony"
    const val EVENT_BUS_KEY_APP_STOP = "event_bus_key_app_stop"
    const val EVENT_BUS_KEY_DEFAULT = "event_bus_key_default"
    const val EVENT_BUS_KEY_LOGOUT = "event_bus_key_logout"
    const val EVENT_BUS_KEY_LOGIN = "event_bus_key_login"
    const val EVENT_BUS_KEY_LIVE = "event_bus_key_live"
    const val EVENT_BUS_KEY_USER = "event_bus_key_user"


    //event themselves
    const val EVENT_BUS_LOGIN_SUCCESS = "login_success"
    const val EVENT_BUS_REPLY: String = "reply"
    const val EVENT_BUS_PROFILE_USER_ID: String = "profile_user_id"


    val EVENT_BUS_TELEPHONY_RINGING = "event_telephony_ringing"
    val EVENT_BUS_TELEPHONY_OFFHOOK = "event_telephony_offhook"
    val EVENT_BUS_TELEPHONY_IDLE = "event_telephony_idle"


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

    const val ITRCT_TYPE_FLIP = 1
    const val ITRCT_TYPE_MAKEUP_ON = 2
    const val ITRCT_TYPE_MAKEUP_OFF = 3
    const val ITRCT_TYPE_LIVE_OFF = 4
    const val ITRCT_TYPE_MUTE_ON = 5
    const val ITRCT_TYPE_MUTE_OFF = 6
    const val ITRCT_TYPE_VIDEO_ON = 7
    const val ITRCT_TYPE_VIDEO_OFF = 8
}