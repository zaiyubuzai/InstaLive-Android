package com.example.instalive.app.live.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.LiveDataRepository
import com.example.instalive.model.LiveViewerData
import com.venus.dm.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LiveViewerViewModel : BaseViewModel() {

    lateinit var viewerFlow: Flow<PagingData<LiveViewerData>>
    lateinit var roomId: String
    lateinit var rfSource: String
    var personalLiveData = MutableLiveData<UserData?>()

    val unlockedNumberLiveData = MutableLiveData<String>()
    private var _removeItemFlow = MutableStateFlow(mutableListOf<String>())
    private val removedItemsFlow: Flow<MutableList<String>> get() = _removeItemFlow

    fun initPagerFlow(roomId: String) {
        this.roomId = roomId
        this.rfSource = "live_view"
        _removeItemFlow = MutableStateFlow(mutableListOf<String>())
        viewerFlow = Pager(PagingConfig(pageSize = 20, prefetchDistance = 3)) {
            ViewerPagingSource(this, roomId, unlockedNumberLiveData)
        }.flow.cachedIn(viewModelScope)
    }

    fun bindPaging(adapter: PagingDataAdapter<LiveViewerData, LiveViewersDialog.ViewerListViewHolder>) {
        viewModelScope.launch {
            viewerFlow
                .cachedIn(viewModelScope)
                .combine(removedItemsFlow) { pagingData, removed ->
                    pagingData.filter {
                        it.userInfo.userId !in removed
                    }
                }
                .collectLatest {
                    adapter.submitData(it)
                }
        }
    }

    fun remove(item: String) {
        val removes = _removeItemFlow.value
        val list = mutableListOf(item)
        list.addAll(removes)
        _removeItemFlow.value = list
    }

    fun getPersonalData(userId: String, conId: String, isSupporter: Int?, event: (StatusEvent) -> Unit) {
//        viewModelScope.launch {
//            DataRepository.getUserDetailAllowNull(
//                userId,
//                conId,
//                personalLiveData,
//                object : RemoteEventEmitter {
//                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                    }
//
//                    override fun onEvent(event: StatusEvent) {
//                        if (event == StatusEvent.SUCCESS) {
//                            event(event)
//                        }
//                    }
//                },
//                isSupporter
//            )
//        }
    }

//    fun follow(userData: LiveViewerData) {
//        viewModelScope.launch {
//            if (userData.canFollow()) {
//                DataRepository.liveFollow(userData.userId, roomId, rfSource, this@LiveViewerViewModel)
//            } else {
//                DataRepository.unfollow(userData.userId, userData.rfTag, this@LiveViewerViewModel)
//            }
//        }
//    }

    class ViewerPagingSource(val remoteEventEmitter: RemoteEventEmitter, val liveId: String, val unlockedNumberLiveData: MutableLiveData<String>) :
        PagingSource<Int, LiveViewerData>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveViewerData> {
            var limit = 50
            return try {
                LiveDataRepository.liveViewerPagingList(liveId, params.key ?: 0, limit, {
                    limit = it
                }, unlockedNumberLiveData, remoteEventEmitter)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, LiveViewerData>): Int? {
            return 0
        }
    }

}