package com.example.instalive.utils

import com.example.instalive.InstaLiveApp
import com.example.instalive.api.DataRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object SessionHelper {

    @DelicateCoroutinesApi
    @ExperimentalStdlibApi
    fun init(type: Int) {
        GlobalScope.launch {
            DataRepository.init(type, InstaLiveApp.appInstance.appInitData, null)
        }
    }

}