package com.example.instalive.app.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.databinding.ActivityHomeBinding
import com.example.instalive.utils.ShareUtility
import kotlinx.android.synthetic.main.activity_home.*
import splitties.views.onClick
import timber.log.Timber

import com.example.baselibrary.utils.marsToast

import android.net.Uri

import android.text.InputFilter
import android.text.InputFilter.LengthFilter

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.conversation.ConversationListActivity
import com.example.instalive.app.ui.OtherProfileDialog
import com.lxj.xpopup.XPopup
import com.venus.dm.model.UserData
import splitties.activities.start

@ExperimentalStdlibApi
class HomeActivity : InstaBaseActivity<HomeViewModel, ActivityHomeBinding>() {

    private val IS_DESTROY = "is_destroy"
    private val CURRENT_ROLE = "current_role"
    private var role = 1//1:host;2:viewer.
    private var isRestartActivity = false

    private var currentFragmentTag: String? = null
    private val hostFragment: HostFragment by lazy {
        HostFragment()
    }

    private val viewerFragment: ViewerFragment by lazy {
        ViewerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //如果长时间待在后台导致Activity销毁，则在此处先移m除之前所有的fragment
        if (savedInstanceState != null && savedInstanceState.getBoolean(IS_DESTROY, false)) {
            isRestartActivity = true
            removeAllFragment()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        role = SessionPreferences.identity

        if (role == 1) {//host
            btnCoins.isVisible = false
            switchFragment(hostFragment)
        } else if (role == 2) {
            switchFragment(viewerFragment)
        }

        btnChargeRole.onClick {
            if (role == 1) {//host
                role = 2
                btnCoins.isVisible = true
                switchFragment(viewerFragment)
            } else if (role == 2) {
                role = 1
                btnCoins.isVisible = false
                switchFragment(hostFragment)
            }
        }
        btnCoins.onClick {}
        btnShare.onClick {
            ShareUtility.shareCopy("instalive://profile?id=${SessionPreferences.id}")
        }
        btnLink.onClick{
            gangUpInvite()
        }

        btnSet.onClick {
            start<SettingsActivity> {  }
        }
        btnDM.onClick {
            start<ConversationListActivity> { }
        }
    }

    private fun gangUpInvite() {
        val inputServer = EditText(this)
        inputServer.filters = arrayOf<InputFilter>(LengthFilter(100))
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Invite").setView(inputServer)
            .setNegativeButton(R.string.fb_cancel, null)
        builder.setPositiveButton(
            R.string.fb_confirm
        ) { _, _ ->
            val sign = inputServer.text.toString()
            val uri = Uri.parse(sign)
            val userId = uri.getQueryParameter("id")
            if (userId != null) {
                viewModel.getUserDetail(userId, null) {
                    showOtherProfileDialog(null, it)
                }
            }
            marsToast(sign)
        }
        builder.show()
    }

    private fun showOtherProfileDialog(userId: String?, userData: UserData?) {
        XPopup.Builder(this)
            .isDestroyOnDismiss(true)
            .asCustom(
                OtherProfileDialog(
                    this,
                    userId,
                    "",
                    userData,
                    0,
                    0,
                    null,
                    null,
                    false,
                    false,
                    false,
                    false,
                    3,
                )
            ).show()
    }

    private fun switchFragment(fragment: Fragment) {
        try {
            val existFragment =
                supportFragmentManager.findFragmentByTag(fragment.javaClass.simpleName)
            val transaction = supportFragmentManager.beginTransaction()

            if (currentFragmentTag != null) {
                val oldFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
                if (oldFragment != null) {
                    transaction.hide(oldFragment)
                }
            }
            currentFragmentTag = if (existFragment == null || isRestartActivity) {
                isRestartActivity = false
                transaction.add(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
                fragment.javaClass.simpleName
            } else {
                transaction.show(existFragment)
                existFragment.javaClass.simpleName
            }
            transaction.commit()
            supportFragmentManager.executePendingTransactions()
            Timber.d("Goal: switch fragment=${currentFragmentTag}")

        } catch (e: IllegalStateException) {
            Timber.e(e)
//            Timber.d("Local: switch error=${e.message}")
        }
    }

    /**
     * 移除所有的fragment
     */
    private fun removeAllFragment() {
        supportFragmentManager.findFragmentByTag(hostFragment.javaClass.simpleName)?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }

        supportFragmentManager.findFragmentByTag(viewerFragment.javaClass.simpleName)?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    override fun initViewModel(): HomeViewModel {
        return getActivityViewModel(HomeViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_home, viewModel)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_DESTROY, true)
        outState.putInt(CURRENT_ROLE, role)
    }

}