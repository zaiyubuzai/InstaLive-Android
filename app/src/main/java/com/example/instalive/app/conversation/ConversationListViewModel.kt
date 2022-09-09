package com.example.instalive.app.conversation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.ConversationDataRepository
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.model.ConversationListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationListViewModel: BaseViewModel() {

    val dao = InstaLiveDBProvider.db.directMessagingDao()

    val conversationListLD = MutableLiveData<ConversationListData>()

    var conversationsFlow = dao.getConversationsFlow(SessionPreferences.id)

    fun getConversationList(){
        viewModelScope.launch {
            ConversationDataRepository.getConversationList(conversationListLD, this@ConversationListViewModel)
        }
    }

    fun pinConversation(conId: String, result: () -> Unit){
        viewModelScope.launch {
            ConversationDataRepository.pinConversation(conId, result, this@ConversationListViewModel)
        }
    }

    fun unpinConversation(conId: String, result: () -> Unit){
        viewModelScope.launch {
            ConversationDataRepository.unpinConversation(conId, result, this@ConversationListViewModel)
        }
    }

    fun muteOrUnmute(conId: String, mute: Int, muted: (()->Unit)?, unmuted: (()->Unit)?){
        viewModelScope.launch {
            ConversationDataRepository.muteOrUnmute(conId, mute, muted, unmuted, this@ConversationListViewModel)
        }
    }

    fun deleteConversation(conId: String, isReport:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
//            if (isReport)reportConversationLastRead(conId)
            dao.deleteMessageBasedOnConversationId(conId, SessionPreferences.id)
            dao.deleteConversation(conId, SessionPreferences.id)
        }
    }
}