package com.example.instalive.app.login

import android.os.Bundle
import androidx.core.view.isVisible
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.baseToast
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.SESSION
import com.example.instalive.app.home.HomeActivity
import com.example.instalive.databinding.FragmentSelectOwnRoleBinding
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.fragment_select_own_role.*
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrNull
import splitties.fragments.start
import splitties.views.onClick

@ExperimentalStdlibApi
class SelectOwnRoleFragment : BaseFragment<SelectOwnRoleViewModel, FragmentSelectOwnRoleBinding>() {

    var phone: String? by argOrNull()
    var passcode: String? by argOrNull()
    var portrait: String by arg()
    var username: String by arg()
    var birthDay: String by arg()
    var gender: String by arg()

    override fun initViewModel(): SelectOwnRoleViewModel {
        return getActivityViewModel(SelectOwnRoleViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_select_own_role, viewModel)
    }

    override fun initData(savedInstanceState: Bundle?) {
        asHost.onClick{
            baseToast("host")
            viewModel.phoneLogin(phone?:"",passcode?:"", username, portrait, birthDay, gender, "1")
        }
        asViewer.onClick{
            baseToast("viewer")
            viewModel.phoneLogin(phone?:"",passcode?:"", username, portrait, birthDay, gender, "2")
        }

        loadingAnimContainer.onClick{}

        viewModel.loginResponse.observe(this, {
            SESSION.saveLoginData(it)
            start<HomeActivity> {}
            LiveEventBus.get(Constants.EVENT_BUS_KEY_LOGIN).post(Constants.EVENT_BUS_LOGIN_SUCCESS)
            requireActivity().finish()
        })

        viewModel.errorInfo.observe(this, {
            baseToast(it.second.toString())
        })

        viewModel.loadingStatsLiveData.observe(this, {
            loadingAnimContainer?.isVisible = it == StatusEvent.LOADING
        })
    }
}