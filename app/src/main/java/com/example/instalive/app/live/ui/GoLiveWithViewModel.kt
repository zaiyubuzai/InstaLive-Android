package com.example.instalive.app.live.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.LiveDataRepository
import com.example.instalive.model.LiveViewerData
import com.example.instalive.model.LiveWithInviteEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GoLiveWithViewModel : BaseViewModel() {

    lateinit var viewerFlow: Flow<PagingData<LiveViewerData>>
    lateinit var liveId: String
    var inviteData = MutableLiveData<LiveWithInviteEvent>()
    var agreeLiveWithData = MutableLiveData<Any>()
    var declineLiveWithData = MutableLiveData<Any>()

    fun initPagerFlow(liveId: String) {
        this.liveId = liveId
        viewerFlow = Pager(PagingConfig(pageSize = 20, prefetchDistance = 3)) {
            ViewerPagingSource(this, liveId)
        }.flow.cachedIn(viewModelScope)
    }

    fun goLiveWith(userId: String) {
        viewModelScope.launch {
//            DataRepository.goLiveWith(userId, liveId, inviteData, this@GoLiveWithViewModel)
        }
    }

    fun agreeLiveWith(liveId: String) {
        viewModelScope.launch {
//            DataRepository.agreeLiveWith(liveId, agreeLiveWithData, this@GoLiveWithViewModel)
        }
    }

    fun declineLiveWith(liveId: String) {
        viewModelScope.launch {
//            DataRepository.declineLiveWith(liveId, declineLiveWithData, this@GoLiveWithViewModel)
        }
    }

    class ViewerPagingSource(val remoteEventEmitter: RemoteEventEmitter, val liveId: String) :
        PagingSource<Int, LiveViewerData>() {
        var limit = 50

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveViewerData> {
            return try {
                LiveDataRepository.liveWithViewerPagingList(liveId, params.key ?: 0, limit, {
                    limit = it
                }, remoteEventEmitter)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, LiveViewerData>): Int? {
            return 0
        }
    }
}