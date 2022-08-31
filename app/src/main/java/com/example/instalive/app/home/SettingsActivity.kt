package com.example.instalive.app.home

import android.os.Bundle
import com.example.baselibrary.views.DataBindingConfig
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
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.activity_settings.*
import splitties.activities.start
import splitties.views.onClick

@ExperimentalStdlibApi
class SettingsActivity : InstaBaseActivity<SettingsViewModel, ActivitySettingsBinding>() {

    override fun initData(savedInstanceState: Bundle?) {
        logout.onClick{
            showCommonProgress()
            viewModel.logout()
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