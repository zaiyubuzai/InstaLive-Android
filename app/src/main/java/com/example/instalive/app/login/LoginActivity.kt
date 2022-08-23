package com.example.instalive.app.login

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.baselibrary.utils.BarUtils
import com.example.instalive.InstaLiveApp
import com.example.instalive.R
import com.example.instalive.app.Constants.EXTRA_LOGIN_SOURCE
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.base.AppBackgroundObserver
import com.example.instalive.app.login.*
import kotlinx.android.synthetic.main.login_activity.*

@ExperimentalStdlibApi
class LoginActivity : AppCompatActivity() {

    var portrait: String? = null

    private val phoneLoginFragment by lazy {
        PhoneLoginFragment().apply {
            source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
            showAppBar = true
        }
    }

    private val newPhoneLoginFragment by lazy {
        NewPhoneLoginFragment().apply {
            source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
            showAppBar = true
        }
    }

    private val originalPhoneFragment by lazy {
        OriginalPhoneFragment()
    }

    private val fullNameFragment by lazy {
        AddProfileInfoFragment().apply {
            source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
        }
    }

    private val selectOwnRoleFragment by lazy {
        SelectOwnRoleFragment()
    }

    var phonePasscodeFragment: PhonePasscodeFragment? = null
    var newPhonePasscodeFragment: PhonePasscodeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InstaLiveApp.appInstance.addActivity(this)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 跟系統表示要渲染 system bar 背景。
        window.statusBarColor = Color.TRANSPARENT
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
        BarUtils.setStatusBarLightMode(this, false)
        setContentView(R.layout.login_activity)
        container.setPadding(0, BarUtils.statusBarHeight, 0, 0)
        if (!SessionPreferences.birthdayError) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragmentContainer,
                    phoneLoginFragment,
                    "login_fragment"
                )
                .addToBackStack("login_fragment")
                .commit()
        }
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(AppBackgroundObserver)
    }

    fun redirectPhonePasscode(phone: String, currentCountryCode: String) {
        val p = PhonePasscodeFragment().apply {
            this.phone = phone
            this.dialCode = currentCountryCode
            this.source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
        }
        phonePasscodeFragment = p
        val fragment = supportFragmentManager.findFragmentByTag(
            "login_fragment"
        )
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .hide(fragment)
                .add(R.id.fragmentContainer, p, "phonePasscodeFragment")
                .addToBackStack("phonePasscodeFragment")
                .commitAllowingStateLoss()
        } else {
            supportFragmentManager.beginTransaction()
                .hide(phoneLoginFragment)
                .add(R.id.fragmentContainer, p, "phonePasscodeFragment")
                .addToBackStack("phonePasscodeFragment")
                .commitAllowingStateLoss()
        }
    }

    fun redirectNewPhonePasscode(
        phone: String,
        currentCountryCode: String,
        oldPhoneNumber: String,
        emailVerifyToken: String
    ) {
        val p = PhonePasscodeFragment().apply {
            this.phone = phone
            this.dialCode = currentCountryCode
            this.source = "lost_access"
            this.newPhone = true
            this.oldPhoneNumber = oldPhoneNumber
            this.emailVerifyToken = emailVerifyToken
        }
        newPhonePasscodeFragment = p
        supportFragmentManager.beginTransaction()
            .hide(newPhoneLoginFragment)
            .add(R.id.fragmentContainer, p, "newPhonePasscodeFragment")
            .addToBackStack("newPhonePasscodeFragment")
            .commit()
    }

    fun redirectEmailPasscode(email: String) {
        val p = PhonePasscodeFragment().apply {
            this.email = email
            this.source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
        }
        phonePasscodeFragment = p
        supportFragmentManager.beginTransaction()
            .hide(phoneLoginFragment)
            .add(R.id.fragmentContainer, p, "emailPasscodeFragment")
            .addToBackStack("emailPasscodeFragment")
            .commit()
    }

    fun redirectFullName(phone: String, passcode: String) {
        phonePasscodeFragment?.let {
            if (fullNameFragment !== null && (fullNameFragment.isAdded || null != supportFragmentManager.findFragmentByTag(
                    "fullNameFragment"
                ))
            ) {
                supportFragmentManager.beginTransaction()
                    .hide(it)
                    .show(fullNameFragment.apply {
                        this.phone = phone
                        this.passcode = passcode
                        this.source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
                        this.title = this@LoginActivity.getString(R.string.fb_login)
                    }).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .hide(it)
                    .add(R.id.fragmentContainer, fullNameFragment.apply {
                        this.phone = phone
                        this.passcode = passcode
                        this.source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
                        this.title = this@LoginActivity.getString(R.string.fb_login)
                    }, "fullNameFragment")
                    .addToBackStack("fullNameFragment")
                    .commit()
            }
            executePT()
        }
    }


    fun redirectOriginalPhone(
        phoneNoCountryCode: String,
        currentCountry: String,
        currentCountryCode: String
    ) {
        if (originalPhoneFragment.isAdded
            || null != supportFragmentManager.findFragmentByTag("originalPhoneFragment")
        ) {
            supportFragmentManager.beginTransaction()
                .hide(phoneLoginFragment)
                .show(originalPhoneFragment.apply {
                    this.currentCountryCode = currentCountryCode
                    this.currentCountry = currentCountry
                    this.source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
                    this.phone = phoneNoCountryCode
                    this.isFirst = true
                })
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .hide(phoneLoginFragment)
                .add(R.id.fragmentContainer, originalPhoneFragment.apply {
                    this.currentCountryCode = currentCountryCode
                    this.currentCountry = currentCountry
                    this.source = intent.getStringExtra(EXTRA_LOGIN_SOURCE) ?: ""
                    this.phone = phoneNoCountryCode
                    this.isFirst = true
                }, "originalPhoneFragment")
                .addToBackStack("originalPhoneFragment")
                .commit()
        }
        executePT()
    }

    fun redirectSelectOwnRole() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(fullNameFragment)
        if (selectOwnRoleFragment.isAdded
            || null != supportFragmentManager.findFragmentByTag("selectOwnRoleFragment")){
            transaction.show(selectOwnRoleFragment.apply {  })
        } else{
            transaction.add(R.id.fragmentContainer, selectOwnRoleFragment.apply {  }).addToBackStack("selectOwnRoleFragment")
        }
        transaction.commit()
        executePT()
    }

    private fun executePT() {
        try {
            supportFragmentManager.executePendingTransactions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        InstaLiveApp.appInstance.removeActivity(this)
    }
}