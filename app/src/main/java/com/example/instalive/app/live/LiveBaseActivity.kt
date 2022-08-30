package com.example.instalive.app.live

import androidx.databinding.ViewDataBinding
import com.example.instalive.app.base.InstaBaseActivity

@ExperimentalStdlibApi
abstract class LiveBaseActivity<VDB : ViewDataBinding>: InstaBaseActivity<LiveViewModel, VDB>() {
}