package com.example.instalive

import android.app.Activity
import android.app.Application
import android.util.DisplayMetrics
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.instalive.utils.FlipperInitializer
import com.venus.framework.rest.UrlSignature
import kotlin.properties.Delegates

class InstaLiveApp: Application(), ViewModelStoreOwner {
    companion object{
        var appInstance: InstaLiveApp by Delegates.notNull()
    }

    val timeDiscrepancy: Long = 0
    val activityList = ArrayList<Activity>()

    val screenHeight: Int by lazy {
        val display = splitties.systemservices.windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val height = metrics.heightPixels
        height
    }
    val screenWidth: Int by lazy {
        val display = splitties.systemservices.windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = metrics.widthPixels
        width
    }

    private val factory: ViewModelProvider.Factory by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
    }
    private val store: ViewModelStore by lazy {
        ViewModelStore()
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this

        UrlSignature.initAsync(this)
        if (BuildConfig.DEBUG) {
            FlipperInitializer.initFlipper(this)
        }
    }

    fun addActivity(activity: Activity) {
        activityList.add(activity)
    }

    fun getTopActivity(activity: Activity): Boolean{
        return activity == activityList.last()
    }

    fun removeActivity(activity: Activity) {
        if (activityList.contains(activity)) {
            activityList.remove(activity)
        }
    }

    fun getAppViewModelProvider(): ViewModelProvider {
        return ViewModelProvider(this, factory)
    }

    override fun getViewModelStore(): ViewModelStore {
        return store
    }
}