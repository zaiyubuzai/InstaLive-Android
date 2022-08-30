package com.example.instalive.app.live


import android.os.Bundle
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.ActivityLiveBinding

@ExperimentalStdlibApi
class LiveActivity : LiveBaseActivity<ActivityLiveBinding>() {

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initViewModel(): LiveViewModel {
        return getActivityViewModel(LiveViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_live, viewModel)
    }
}