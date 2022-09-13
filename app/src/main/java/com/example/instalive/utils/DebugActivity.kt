package com.example.instalive.utils

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.utils.baseToast
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.app.web.InstaWebActivity
import com.example.instalive.databinding.ActivityDebugLayoutBinding
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.activity_debug_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import splitties.intents.start
import splitties.views.onClick

@ExperimentalStdlibApi
class DebugActivity : InstaBaseActivity<DebugViewModel, ActivityDebugLayoutBinding>() {
    override fun initData(savedInstanceState: Bundle?) {
        debugWrapper.setPadding(0, BarUtils.statusBarHeight, 0, 0)

        forceLogout.onClick {
        }
        eventToastSwitch.isChecked
        eventToastSwitch.onClick {
        }
        skipAppealRecord.onClick {
//            start<AppealRecordActivity> {
//            }
        }
        testOpenFaceDetection.onClick {

        }

        throwException.onClick {
            throw RuntimeException("This is a exception")
        }

        testUrl.onClick {
        }
        testUrlJin.onClick {
            XPopup.Builder(this).asInputConfirm("setting test url",
                "",
                "http://192.168.11.223:3000/app_bridge/1232100",
                ""
            ) { url ->
                start(InstaWebActivity) { _, extrasSpec ->
                    extrasSpec.url = url
                }
            }.show()
        }
        restoreMessages.onClick {
//            viewModel.downloadDMDbInfo()
        }
        viewModel.loadingStatsLiveData.observe(this) {
            if (it == StatusEvent.LOADING) {
                showCommonProgress()
            } else {
                hideCommonProgress()
            }
            if (it == StatusEvent.SUCCESS) {
                lifecycleScope.launch(Dispatchers.Main) {
                    baseToast("Messages restore succeed")
                }
            }
        }
    }

    override fun initViewModel(): DebugViewModel {
        return getActivityViewModel(DebugViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_debug_layout, viewModel)
    }
}