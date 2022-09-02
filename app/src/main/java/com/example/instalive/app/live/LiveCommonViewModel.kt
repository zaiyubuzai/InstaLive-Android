package com.example.instalive.app.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveCommonViewModel: ViewModel() {
    val isMicrophoneUser = MutableLiveData<Boolean>()//本人是否连麦中
    val isMicrophone = MutableLiveData<Boolean>()//直播是否为连麦直播

}