package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.example.baselibrary.api.BaseRemoteRepository
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.http.InstaApi
import com.example.instalive.model.LiveDataInfo
import com.example.instalive.model.LiveViewerData

object LiveDataRepository: ILiveDataRepository, BaseRemoteRepository() {

    val instaApi = RetrofitProvider.baseApi as InstaApi

    override suspend fun liveWithViewerPagingList(
        liveId: String,
        page: Int,
        limit: Int,
        onLimitChange: (Int) -> Unit,
        remoteEventEmitter: RemoteEventEmitter
    ): PagingSource.LoadResult<Int, LiveViewerData> {
//        val response = safeApiCall(remoteEventEmitter) {
//            instaApi.liveWithViewer(liveId, page * limit)
//        }
//        return if (response != null) {
//            if (limit != response.meta.limit) {
//                onLimitChange(response.meta.limit)
//            }
//            val nextKey = if (response.meta.hasNext) {
//                response.meta.nextOffset / response.meta.limit
//            } else {
//                null
//            }
//            PagingSource.LoadResult.Page(
//                data = response.data,
//                prevKey = null,
//                nextKey = nextKey
//            )
//        } else {
//            throw NullPointerException()
//        }
        TODO()
    }

    override suspend fun createLive(
        title: String?,
        desc: String?,
        ticketGiftId: String?,
        divideIncome: Int,
        divideIncomeRate: Int,
        liveDataInfo: MutableLiveData<LiveDataInfo>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        val response = safeApiCall(remoteEventEmitter){
            instaApi.createLive(title, desc, ticketGiftId, divideIncome, divideIncomeRate)
        }
        if (response?.resultOk() == true){
            liveDataInfo.postValue(response.data)
        }
    }
}