package com.example.baselibrary.api

interface RemoteEventEmitter {
    fun onError(code: Int, msg: String, errorType: ErrorType)
    fun onEvent(event: StatusEvent)
}