package com.example.instalive.utils

import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import kotlinx.coroutines.launch

class DebugViewModel : BaseViewModel() {
    val DM_DB_URL = "https://mars-dm-messages-cloud-backup-test.s3.amazonaws.com/gVeAVaM7a2/gVeAVaM7a2_792fdd68-43de-416b-8bbf-14cdab710870.zip"

//    fun downloadDMDbInfo() {
//        viewModelScope.launch {
//            SettingsRepository.downloadAndTackleDbData(DM_DB_URL, object: RemoteEventEmitter {
//                override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                    this@DebugViewModel.onError(code, msg, errorType)
//                }
//
//                override fun onEvent(event: StatusEvent) {
//                    this@DebugViewModel.onEvent(event)
//                }
//            }, {_,_ ->})
//        }
//    }
}