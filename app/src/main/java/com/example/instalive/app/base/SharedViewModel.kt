package com.example.instalive.app.base

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.views.BaseViewModel
import com.venus.dm.db.entity.ConversationsEntity
import kotlinx.coroutines.*

class SharedViewModel: BaseViewModel() {

    val verifyCodePhoneLeft = MutableLiveData<Int>()
    var verifyCodePhoneJob: Job? = null
    val phonePasscodeMap = mutableMapOf<String, Long>()

    val currentConversationData = MutableLiveData<ConversationsEntity>()

    fun startGlobalVerifyCodePhoneTicking(timestamp: Long) {
        verifyCodePhoneJob?.cancel()
        verifyCodePhoneJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                if (System.currentTimeMillis() - timestamp < 60 * 1000) {
                    val count = (timestamp + 60 * 1000 - System.currentTimeMillis()).toInt() / 1000
                    for (i in count downTo 0) {
                        verifyCodePhoneLeft.postValue(i)
                        delay(1000)
                    }
                }
            } catch (e: Exception) { }
        }
    }
}