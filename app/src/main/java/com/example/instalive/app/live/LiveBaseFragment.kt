package com.example.instalive.app.live

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.example.baselibrary.views.BaseFragment

abstract class LiveBaseFragment<VMD : ViewModel, VDB : ViewDataBinding>: BaseFragment<VMD, VDB>() {
}