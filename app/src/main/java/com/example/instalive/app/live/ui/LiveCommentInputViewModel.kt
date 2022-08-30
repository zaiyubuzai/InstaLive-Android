package com.example.instalive.app.live.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.Meta
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import com.venus.dm.model.GroupMember
import kotlinx.coroutines.launch

class LiveCommentInputViewModel : BaseViewModel() {
    var meta = MutableLiveData<Meta>()
    var mentionSearchLiveData = MutableLiveData<List<GroupMember>>()
    fun mentionSearch(isRefresh: Boolean, conId: String, keyword: String, isLoading: (Boolean) -> Unit, onError:() -> Unit) {
        viewModelScope.launch {
            DataRepository.mentionSearch(conId, keyword, isRefresh, meta, mentionSearchLiveData, object :
                RemoteEventEmitter {
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    this@LiveCommentInputViewModel.onError(code, msg, errorType)
                    onError()
                }

                override fun onEvent(event: StatusEvent) {
                    this@LiveCommentInputViewModel.onEvent(event)
                    isLoading(event == StatusEvent.LOADING)
                }
            })
        }
    }

    override fun reset(){
        meta = MutableLiveData<Meta>()
        mentionSearchLiveData = MutableLiveData<List<GroupMember>>()
    }
}