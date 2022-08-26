package com.example.instalive

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.baselibrary.utils.ScreenshotDetector
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.model.AppInitData
import com.example.instalive.model.InstaLiveStringTemplate
import com.example.instalive.utils.FlipperInitializer
import com.example.instalive.utils.SysUtils
import com.google.gson.Gson
import com.venus.framework.rest.UrlSignature
import splitties.permissions.hasPermission
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class InstaLiveApp: Application(), ViewModelStoreOwner {
    companion object{
        var appInstance: InstaLiveApp by Delegates.notNull()
    }

    var isColdLaunch: Boolean = true
    val timeDiscrepancy: Long = 0
    var startActivityCount = 0
    val activityList = ArrayList<Activity>()
    var appInitData = MutableLiveData<AppInitData?>()
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

    override fun attachBaseContext(base: Context?) {
        //初始化打印日志
        Timber.plant(object : Timber.DebugTree() {
            override fun isLoggable(tag: String?, priority: Int): Boolean {
                return BuildConfig.DEBUG
            }
        })
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
        initLifecycle()
        UrlSignature.initAsync(this)
        if (BuildConfig.DEBUG) {
//            FlipperInitializer.initFlipper(this)
        }

        InstaLiveDBProvider.db.directMessagingDao()

        appInitData.observeForever {

        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        if (SessionPreferences.initDataJson != null) {
            val data = Gson().fromJson(SessionPreferences.initDataJson, AppInitData::class.java)
            appInitData.postValue(data)
        }

        InstaLiveStringTemplate.loadTemplate()

        getDeviceId()
    }

    fun getDeviceId(): String {
        val deviceId = SessionPreferences.deviceId
        if (deviceId != null) {
            return deviceId
        } else {
            try {
                val adId = SysUtils.getUUID()
                return run {
                    val checkADId = adId.replace("0","").replace("-", "")
                    if (checkADId.isEmpty()){
                        val randomUUID = UUID.randomUUID().toString()
                        SessionPreferences.deviceId = randomUUID
                        randomUUID
                    } else {
                        SessionPreferences.deviceId = adId
                        adId
                    }
                }
            } catch (e: Exception) {
                val randomUUID = UUID.randomUUID().toString()
                SessionPreferences.deviceId = randomUUID
                return randomUUID
            }
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

    private fun initLifecycle(){
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
                startActivityCount++
                //如果mFinalCount ==1，说明是从后台到前台
                if (startActivityCount == 1) {
                    Timber.e("onActivityStarted")
                    // 进入前台 改为热启动
                    isColdLaunch = false
                    if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        ScreenshotDetector.start(applicationContext){

                        }
                    }
                }
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
                Timber.d("onActivityStopped $startActivityCount")
                startActivityCount--
                if (startActivityCount <= 0){
                    ScreenshotDetector.stop(applicationContext)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Timber.d("onActivitySaveInstanceState $startActivityCount")
            }

            override fun onActivityDestroyed(activity: Activity) {
                Timber.d("onActivityDestroyed $startActivityCount")
            }

        })
    }
}