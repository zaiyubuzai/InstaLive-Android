package com.example.instalive.app.conversation

import android.os.Bundle
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.databinding.ActivityMessageBinding

class MessageActivity : InstaBaseActivity<MessageViewModel, ActivityMessageBinding>() {

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initViewModel(): MessageViewModel {
        return getActivityViewModel(MessageViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_message, viewModel)
    }
}