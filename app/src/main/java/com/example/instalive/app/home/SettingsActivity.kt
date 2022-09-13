package com.example.instalive.app.home

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.BuildConfig
import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.SESSION
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.app.base.SharedViewModel
import com.example.instalive.app.login.NotLoginYetActivity
import com.example.instalive.databinding.ActivitySettingsBinding
import com.example.instalive.utils.DMSocketIO
import com.example.instalive.utils.DebugActivity
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.activities.start
import splitties.views.onClick

@ExperimentalStdlibApi
class SettingsActivity : InstaBaseActivity<SettingsViewModel, ActivitySettingsBinding>() {

    override fun initData(savedInstanceState: Bundle?) {
        version.text = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"

        logout.onClick{
            showCommonProgress()
            viewModel.logout()
        }

        if (BuildConfig.DEBUG) {
            var versionJob: Job? = null
            var clickedCount = 0
            version.onClick {
                if (clickedCount >= 5) {
                    start<DebugActivity>()
                    return@onClick
                }
                clickedCount++
                if (versionJob == null) {
                    versionJob = lifecycleScope.launch {
                        delay(1000)
                        clickedCount = 0
                        versionJob = null
                    }
                }
            }
        }

        viewModel.logoutData.observe(this, {
            hideCommonProgress()
            val sharedViewModel =
                appInstance.getAppViewModelProvider().get(SharedViewModel::class.java)
            if (SessionPreferences.id.isNotEmpty()) {
                SESSION.resetLoginInfo()
                sharedViewModel.videoMessageQueue.clear()
                sharedViewModel.videoMessageLiveIdQueue.clear()
                sharedViewModel.videoMessageDoingQueue.clear()
                DMSocketIO.releaseSocket()
                start<NotLoginYetActivity> {  }
                LiveEventBus.get(Constants.EVENT_BUS_KEY_LOGOUT)
                    .post(Any())
            }
        })
    }

    override fun initViewModel(): SettingsViewModel {
        return getActivityViewModel(SettingsViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_settings, viewModel)
    }
}