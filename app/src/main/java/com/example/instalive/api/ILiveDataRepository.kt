package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.example.baselibrary.api.Meta
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

    suspend fun hangUpLive(
        liveId: String,
        targetUserId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun getLiveList(
        isRefresh: Boolean,
        meta: MutableLiveData<Meta>,
        liveData: MutableLiveData<List<LiveData>>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun getLiveToken(
        liveId: String,
        liveData: MutableLiveData<TokenInfo>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun leaveLive(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter?
    )

    suspend fun agreeLiveWith(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun rejectLiveWith(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun goLiveWith(
        userId: String,
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun liveRefresh(
        liveId: String,
        liveData: MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun sendLiveComment(
        liveId: String,
        msg: String,
        uuid: String,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun sendLiveGift(
        liveId: String,
        giftId: String,
        uuid: String,
        liveData: MutableLiveData<LiveSendGiftResponse>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun cancelLiveWith(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter?
    )

    suspend fun liveViewerPagingList(
        liveId: String,
        page: Int,
        limit: Int,
        onLimitChange: (Int) -> Unit,
        unlockedNumberLiveData: MutableLiveData<String>,
        remoteEventEmitter: RemoteEventEmitter
    ): PagingSource.LoadResult<Int, LiveViewerData>

    suspend fun liveShare(
        liveId: String,
        liveData: MutableLiveData<LiveShareData>,
        remoteEventEmitter: RemoteEventEmitter
    )
}