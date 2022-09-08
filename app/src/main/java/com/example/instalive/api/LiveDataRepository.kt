package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.example.baselibrary.api.BaseRemoteRepository
import com.example.baselibrary.api.Meta
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.instalive.app.Constants
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.live.ui.GoLiveWithViewModel
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.http.InstaApi
import com.example.instalive.model.*
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.db.entity.MessageEntity
import kotlinx.coroutines.*
import retrofit2.HttpException

object LiveDataRepository : ILiveDataRepository, BaseRemoteRepository() {

    private val instaApi = RetrofitProvider.baseApi as InstaApi
    private val dao = InstaLiveDBProvider.db.directMessagingDao()
    private val pendingMessageJobsMap = mutableMapOf<String, Job>()

    override suspend fun liveWithViewerPagingList(
        liveId: String,
        page: Int,
        limit: Int,
        onLimitChange: (Int) -> Unit,
        remoteEventEmitter: RemoteEventEmitter
    ): PagingSource.LoadResult<Int, LiveViewerData> {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.liveWithViewer(liveId, page * limit)
        }
        return if (response != null) {
            if (limit != response.meta.limit) {
                onLimitChange(response.meta.limit)
            }
            val nextKey = if (response.meta.hasNext) {
                response.meta.nextOffset / response.meta.limit
            } else {
                null
            }
            PagingSource.LoadResult.Page(
                data = response.data,
                prevKey = null,
                nextKey = nextKey
            )
        } else {
            throw NullPointerException()
        }
    }

    override suspend fun createLive(
        title: String?,
        desc: String?,
        ticketGiftId: String?,
        divideIncome: Int?,
        divideIncomeRate: Int?,
        liveDataInfo: MutableLiveData<LiveDataInfo>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.createLive(title, desc, ticketGiftId, divideIncome, divideIncomeRate)
        }
        if (response?.resultOk() == true) {
            liveDataInfo.postValue(response.data)
        }
    }

    override suspend fun closeLive(
        liveId: String,
        liveData: MutableLiveData<LiveCloseData>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.closeLive(liveId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun joinLive(
        liveId: String,
        liveData: MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        try {
            val response = instaApi.joinLive(liveId)
            liveData.postValue(Pair(response.data, null))
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    val body = e.response()?.errorBody()?.string()
                    if (body != null) {
                        val marsError = try {
                            Gson().fromJson(body, JoinLiveError::class.java)
                        } catch (e: java.lang.Exception) {
                            null
                        }
                        liveData.postValue(Pair(null, marsError))
                    }
                }
            }
        }
    }

    override suspend fun raiseHandLive(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.raiseHandLive(liveId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun handsDownLive(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.handDownLive(liveId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun hangUpLive(
        liveId: String,
        targetUserId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ){
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.hangUpLive(liveId, targetUserId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun getLiveList(
        isRefresh: Boolean,
        meta: MutableLiveData<Meta>,
        liveData: MutableLiveData<List<LiveData>>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        var offset = 0
        val metaData = if (isRefresh) Meta() else meta.value
        if (metaData != null) {
            offset = metaData.nextOffset
        }
        if (metaData?.hasNext == false) {
            remoteEventEmitter.onEvent(StatusEvent.SUCCESS)
            return
        }
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.getLiveList(offset)
        }
        if (response != null) {
            processListData(response, meta, liveData, isRefresh)
        }
    }

    override suspend fun getLiveToken(
        liveId: String,
        liveData: MutableLiveData<TokenInfo>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.getLiveToken(liveId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun leaveLive(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter?
    ){
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.leaveLive(liveId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun agreeLiveWith(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.agreeLiveWith(liveId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun rejectLiveWith(
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.rejectLiveWith(liveId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun goLiveWith(
        userId: String,
        liveId: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.goLiveWith(liveId, userId)
        }
        if (response != null) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun liveRefresh(
        liveId: String,
        liveData: MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        try {
            val response = safeApiCall(remoteEventEmitter) {
                instaApi.liveRefresh(liveId)
            }
            if (response != null && response.resultOk()) {
                liveData.postValue(Pair(response.data, null))
            }
        } catch (e: Exception) {
            when (e) {
                is HttpException -> {
                    val body = e.response()?.errorBody()?.string()
                    if (body != null) {
                        val marsError = try {
                            Gson().fromJson(body, JoinLiveError::class.java)
                        } catch (e: java.lang.Exception) {
                            null
                        }
                        liveData.postValue(Pair(null, marsError))
                    }
                }
            }
        }
    }

    override suspend fun sendLiveComment(
        liveId: String,
        msg: String,
        uuid: String,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        //以下为发送30秒失败的逻辑
        //给发出去的消息开个job，如果消息发送成功或失败，直接cancel掉job，
        //如果job没有被cancel掉，30秒后查询数据库看发送状态，如果还是正在发送，就置为失败
        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(30000)
            LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE_MESSAGE).post(LiveMsgEvent(uuid, liveId, MessageEntity.SEND_STATUS_FAILED))
        }
        pendingMessageJobsMap[uuid] = job
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.sendLiveComment(liveId, msg, uuid)
        }
        if (response != null) {
            LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE_MESSAGE).post(LiveMsgEvent(uuid, liveId, MessageEntity.SEND_STATUS_SUCCESS))
        } else {
            LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE_MESSAGE).post(LiveMsgEvent(uuid, liveId, MessageEntity.SEND_STATUS_FAILED))
            pendingMessageJobsMap.remove(uuid)?.cancel()
        }
    }
}