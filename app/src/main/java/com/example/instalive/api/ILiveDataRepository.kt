package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.model.LiveDataInfo
import com.example.instalive.model.LiveViewerData

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
        divideIncome: Int,
        divideIncomeRate: Int,
        liveDataInfo: MutableLiveData<LiveDataInfo>,
        remoteEventEmitter: RemoteEventEmitter
    )
}