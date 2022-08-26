package com.example.instalive.app.conversation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.ConversationDataRepository
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.model.ConversationListData
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
}