package com.example.instalive.app.live


import android.os.Bundle
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.ActivityLiveBinding

@ExperimentalStdlibApi
class LiveAudienceActivity : LiveBaseActivity<ActivityLiveBinding>() {

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun getImplLayoutId(): Int {
        return R.layout.activity_live
    }
}