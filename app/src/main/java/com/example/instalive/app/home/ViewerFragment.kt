package com.example.instalive.app.home

import android.os.Bundle
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.FragmentViewerBinding

class ViewerFragment : BaseFragment<HomeViewModel, FragmentViewerBinding>()  {
    override fun initViewModel(): HomeViewModel {
        return getActivityViewModel(HomeViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_viewer, viewModel)
    }

    override fun initData(savedInstanceState: Bundle?) {

    }
}