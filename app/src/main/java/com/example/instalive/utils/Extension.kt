package com.example.instalive.utils

import com.example.baselibrary.utils.marsToast
import com.example.instalive.InstaLiveApp

fun marsToast(res: Int) {
    InstaLiveApp.appInstance.marsToast(res)
}

fun marsToast(msg: String) {
    InstaLiveApp.appInstance.marsToast(msg)
}
