package com.example.instalive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.baselibrary.utils.TimeUtils
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.home.HomeActivity
import com.example.instalive.app.login.NotLoginYetActivity
import com.example.instalive.utils.DeeplinkHelper
import com.example.instalive.utils.DeeplinkHelper.DEEPLINK_SCHEME
import com.example.instalive.utils.DeeplinkHelper.HTTPS_SCHEME
import com.example.instalive.utils.SessionHelper
import com.example.instalive.utils.SysUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.activities.start
import splitties.preferences.edit
import timber.log.Timber

@ExperimentalStdlibApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntentDeeplink()
        if (!InstaLivePreferences.firstInitEver) {
            SessionHelper.init(0)
            InstaLivePreferences.edit {
                firstInitEver = true
            }
        } else {
            if (InstaLivePreferences.everydayFirstInitTime > TimeUtils.getCurrentTimeDate() * 1000) {//当天已经启动过了
                if (InstaLiveApp.appInstance.isColdLaunch) {
                    SessionHelper.init(1)
                } else {
                    SessionHelper.init(2)
                }
            } else {//当天没有启动过了
                if (InstaLiveApp.appInstance.isColdLaunch) {
                    SessionHelper.init(5)
                } else {
                    SessionHelper.init(6)
                }
                InstaLivePreferences.edit {
                    everydayFirstInitTime = System.currentTimeMillis()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            delay(2000)
            withContext(Dispatchers.Main) {
                if (SessionPreferences.id.isEmpty()) {
                    start<NotLoginYetActivity> { }
                    finish()
                } else {
                    start<HomeActivity> { }
                }
                finish()
            }
        }

        Timber.d("device id: ${SysUtils.getUniqueId(this)} ${SysUtils.getUUID()}")
        SysUtils.printUUID()
    }

    private fun handleIntentDeeplink(): Boolean {
        val uri = intent.data
        Timber.d("uri ： ${uri ?: "no uri"}")
        return if (uri != null && (uri.scheme == DEEPLINK_SCHEME || uri.scheme == HTTPS_SCHEME)) {
            DeeplinkHelper.handleDeeplink(uri, this)
        } else {
            false
        }
    }

}