package com.example.instalive.app.live

import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.instalive.api.LiveDataRepository
import com.example.instalive.app.Constants
import com.example.instalive.db.MessageComposer
import com.example.instalive.model.LiveMsgEvent
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.db.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class LiveInteractionViewModel : LiveViewModel() {

    fun sendMessage(liveId: String, msg: String, uuid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            LiveDataRepository.sendLiveComment(
                liveId,
                msg,
                uuid,
                object : RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        this@LiveInteractionViewModel.onError(code, msg, errorType)
//                        buildPromptMessage("", msg, 1, liveId, message.sendTime + 1)
                    }

                    override fun onEvent(event: StatusEvent) {
                        this@LiveInteractionViewModel.onEvent(event)
                    }
                })

        }
    }
}