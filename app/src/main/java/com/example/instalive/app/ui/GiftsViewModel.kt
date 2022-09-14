package com.example.instalive.app.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.InstaLiveApp
import com.example.instalive.api.DataRepository
import com.example.instalive.api.LiveDataRepository
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.model.GiftData
import com.example.instalive.model.LiveSendGiftResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class GiftsViewModel : BaseViewModel() {
    var giftListLiveData = MutableLiveData<List<GiftData>>()
    var sendGiftLiveData = MutableLiveData<LiveSendGiftResponse>()

    fun sendLiveGift(
        liveId: String,
        giftId: String,
        uuid: String,
        onError: (code: Int, msg: String) -> Unit
    ) {
        viewModelScope.launch {
            val ob = object :
                RemoteEventEmitter {
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    this@GiftsViewModel.onError(code, msg, errorType)
                    onError.invoke(code, msg)
                }

                override fun onEvent(event: StatusEvent) {
                    this@GiftsViewModel.onEvent(event)
                }
            }
            LiveDataRepository.sendLiveGift(
                liveId,
                giftId,
                uuid,
                sendGiftLiveData,
                ob
            )
        }
    }

    fun getGiftList(onError: (code: Int, msg: String) -> Unit) {
        if (InstaLivePreferences.liveGiftList != null) {
            val giftList =
                Gson().fromJson<List<GiftData>>(InstaLivePreferences.liveGiftList, object : TypeToken<List<GiftData>>() {}.type)
            giftListLiveData.postValue(giftList)
            return
        }
        viewModelScope.launch {
            val apiPath =
                InstaLiveApp.appInstance.appInitData.value?.cacheConfig?.liveGiftsCache?.apiPath
                    ?: return@launch
            DataRepository.giftList(apiPath, giftListLiveData, object : RemoteEventEmitter {
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    onError.invoke(code, msg)
                    this@GiftsViewModel.onError(code, msg, errorType)
                }

                override fun onEvent(event: StatusEvent) {
                    this@GiftsViewModel.onEvent(event)
                }
            })
        }
    }
}