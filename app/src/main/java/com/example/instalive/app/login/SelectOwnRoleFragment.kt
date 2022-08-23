package com.example.instalive.app.login

import android.os.Bundle
import com.example.instalive.utils.marsToast
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.home.HomeActivity
import com.example.instalive.databinding.FragmentSelectOwnRoleBinding
import kotlinx.android.synthetic.main.fragment_select_own_role.*
import splitties.fragments.start
import splitties.views.onClick

@ExperimentalStdlibApi
class SelectOwnRoleFragment : BaseFragment<SelectOwnRoleViewModel, FragmentSelectOwnRoleBinding>() {
    override fun initViewModel(): SelectOwnRoleViewModel {
        return getActivityViewModel(SelectOwnRoleViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_select_own_role, viewModel)
    }

    override fun initData(savedInstanceState: Bundle?) {
        asHost.onClick{
            marsToast("host")
            start<HomeActivity> {
                this.putExtra(Constants.EXTRA_CUSTOM_ROLE, 1)
            }
            requireActivity().finish()
        }
        asViewer.onClick{
            marsToast("viewer")
            start<HomeActivity> {
                this.putExtra(Constants.EXTRA_CUSTOM_ROLE, 2)
            }
            requireActivity().finish()
        }
    }
}