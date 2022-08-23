package com.example.baselibrary.views

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

abstract class BaseBackgroundObserver : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_START -> listenerOnStart()
            Lifecycle.Event.ON_STOP -> listenerOnStop()
            else -> {}
        }
    }

    abstract fun listenerOnStart()
    abstract fun listenerOnStop()
}