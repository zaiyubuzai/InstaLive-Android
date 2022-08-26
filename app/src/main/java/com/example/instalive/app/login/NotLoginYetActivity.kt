package com.example.instalive.app.login

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.Constants.EXTRA_LOGIN_SOURCE
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.databinding.FragmentNotLoginBinding
import com.example.instalive.utils.DMSocketIO
import com.example.instalive.utils.SessionHelper
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.fragment_not_login.*
import kotlinx.coroutines.*
import splitties.activities.start
import splitties.bundle.BundleSpec
import splitties.bundle.bundleOrNull
import splitties.bundle.withExtras
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec
import splitties.views.onClick

@ExperimentalStdlibApi
class NotLoginYetActivity : InstaBaseActivity<NotLoginYetViewModel, FragmentNotLoginBinding>() {
    private var source: String? = null

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalStdlibApi::class)
    override fun initData(savedInstanceState: Bundle?) {
        BarUtils.setStatusBarLightMode(this, false)
        withExtras(NotLoginyetExtraSpec) {
            this@NotLoginYetActivity.source = source
        }

        source = intent.getStringExtra(EXTRA_LOGIN_SOURCE)

        joinNow.onClick {
            start<LoginActivity> {
                putExtra("is_login", false)
                putExtra(EXTRA_LOGIN_SOURCE, source)
            }
        }

        LiveEventBus.get(Constants.EVENT_BUS_KEY_LOGIN).observe(this, {
            if (it == Constants.EVENT_BUS_LOGIN_SUCCESS){
                SessionHelper.init(3)
                DMSocketIO.initSocket()
                finish()
            }
        })
    }

    override fun initViewModel(): NotLoginYetViewModel {
        return getActivityViewModel(NotLoginYetViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_not_login, viewModel)
    }

    companion object :
        ActivityIntentSpec<NotLoginYetActivity, NotLoginyetExtraSpec> by activitySpec(
            NotLoginyetExtraSpec
        )

    object NotLoginyetExtraSpec : BundleSpec() {
        var source: String? by bundleOrNull()
    }
}