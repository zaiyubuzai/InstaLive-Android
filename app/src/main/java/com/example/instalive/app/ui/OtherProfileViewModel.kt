package com.example.instalive.app.ui

import androidx.lifecycle.viewModelScope
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.ConversationDataRepository
import kotlinx.coroutines.launch

class OtherProfileViewModel: BaseViewModel() {
    fun createConversation(targetUserId: String, result: () -> Unit){
        viewModelScope.launch {
            ConversationDataRepository.createConversation(targetUserId, result, this@OtherProfileViewModel)
        }
    }
}