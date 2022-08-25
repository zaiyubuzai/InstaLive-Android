package com.example.instalive.app.conversation

import androidx.lifecycle.viewModelScope
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.db.MessageComposer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class MessageBaseViewModel:BaseViewModel() {
    protected val dao = InstaLiveDBProvider.db.directMessagingDao()
    fun buildPromptMessage(
        conversationId: String,
        msg: String,
        showType: Int,
        liveId: String,
        timeToken: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (msg.isNotEmpty()) {
                val message = MessageComposer.composePromptMessage(
                    msg,
                    conversationId,
                    showType,
                    liveId,
                    timeToken = timeToken
                )
                dao.insertOwnerMessageAndShow(message)
            }
        }
    }
}