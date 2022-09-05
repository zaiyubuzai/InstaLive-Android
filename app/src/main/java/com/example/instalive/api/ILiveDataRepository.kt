package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.model.*

interface ILiveDataRepository {
    suspend fun liveWithViewerPagingList(
        roomId: String,
        page: Int,
        limit: Int,
        onLimitChange: (Int) -> Unit,
        remoteEventEmitter: RemoteEventEmitter,
    ): PagingSource.LoadResult<Int, LiveViewerData>

    suspend fun createLive(
        title: String?,
        desc: String?,
        ticketGiftId: String?,
        divideIncome: Int?,
        divideIncomeRate: Int?,
        liveDataInfo: MutableLiveData<LiveDataInfo>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun closeLive(
        liveId: String,
        liveData: MutableLiveData<LiveCloseData>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun joinLive(
        liveId: String,
        liveData: MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun raiseHandLive(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun handsDownLive(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )
}