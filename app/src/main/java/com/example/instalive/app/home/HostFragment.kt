package com.example.instalive.app.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.FragmentHostBinding
import kotlinx.android.synthetic.main.fragment_host.*
import splitties.fragments.start
import splitties.views.onClick

/**
 * A simple [Fragment] subclass.
 * Use the [HostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HostFragment : BaseFragment<HomeViewModel, FragmentHostBinding>() {

    override fun initViewModel(): HomeViewModel {
        return getActivityViewModel(HomeViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_host, viewModel)
    }

    override fun initData(savedInstanceState: Bundle?) {
        btnDashboard.onClick{

        }
        btnStartLive.onClick{

        }
        btnScheduleLive.onClick{
            start<CreateEventActivity> {  }
        }
    }
}