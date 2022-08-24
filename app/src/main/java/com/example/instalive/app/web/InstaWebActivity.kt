package com.example.instalive.app.web

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.views.BaseActivity
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.ActivityInstaWebBinding
import com.example.instalive.utils.DeeplinkHelper
import kotlinx.android.synthetic.main.activity_insta_web.*
import org.json.JSONException
import org.json.JSONObject
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.bundleOrDefault
import splitties.bundle.withExtras
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec

@ExperimentalStdlibApi
class InstaWebActivity : BaseActivity<InstaWebViewModel, ActivityInstaWebBinding>() {

    lateinit var webFragment: InstaWebFragment
    private var menu: Menu? = null
    private var menuUrl: String? = null
    private var mAct: String? = null
    private var isCanBack = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insta_web)
    }

    companion object :
        ActivityIntentSpec<InstaWebActivity, WebExtraSpec> by activitySpec(WebExtraSpec)

    object WebExtraSpec : BundleSpec() {
        var url: String by bundle()
        var sourceFrom: String by bundleOrDefault("")
    }

    override fun initData(savedInstanceState: Bundle?) {
        BarUtils.setStatusBarLightMode(this, false)
        top_toolbar.navigationIcon = getDrawable(R.mipmap.icon_back_white)
        setSupportActionBar(findViewById(R.id.top_toolbar))
        container.setPadding(0, BarUtils.statusBarHeight, 0, 0)
        supportActionBar?.also {
            it.setDisplayHomeAsUpEnabled(true)
        }
        withExtras(WebExtraSpec) {
            webFragment = InstaWebFragment()
            webFragment.sourceFrom = sourceFrom
            supportFragmentManager.beginTransaction()
                .replace(R.id.web_fragment, webFragment)
                .commit()
        }

        webFragment.setTitle {
            supportActionBar?.title = it
        }

        webFragment.setIcon {
            supportActionBar?.setHomeAsUpIndicator(it)
        }

        webFragment.setHideBackIcon1 {
            hideBack()
        }

        webFragment.setShowBackIcon1 {
            supportActionBar?.also {
                it.setDisplayHomeAsUpEnabled(true)
            }
            isCanBack = true
        }


        withExtras(WebExtraSpec) {
//            if (!BuildConfig.DEBUG && !BuildConfig.MARS_WEB_URL.contains(
//                    Uri.parse(url).host
//                        ?: ""
//                )
//            ) {
//                finish()
//            }

            val isHideBack = Uri.parse(url).getBooleanQueryParameter("hide_back", false)
            if (isHideBack) {
                hideBack()
            }

            webFragment.url = url
        }

    }

    private fun hideBack() {
        supportActionBar?.also {
            it.setDisplayHomeAsUpEnabled(false)
        }
        isCanBack = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            100 -> {
                if (menuUrl != null) {
                    DeeplinkHelper.handleDeeplink(Uri.parse(menuUrl), this)
                }
                true
            }
            101 -> {
                if (item.title != null) {
                    val json = try {
                        JSONObject()
                            .put("act", mAct)
                            .toString()
                    } catch (e: JSONException) {
                        ""
                    }
                    webFragment.webView.send(json)
                }
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        //判断回退是否给出提示
        if (isCanBack) {
            if (webFragment.webViewBlack) {
                val json = try {
                    JSONObject()
                        .put("act", "unload")
                        .toString()
                } catch (e: JSONException) {
                    ""
                }
                webFragment.webView.send(json)
            } else {
                if (webFragment.webView.canGoBack()) {
                    webFragment.webView.goBack()
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    fun addMenu(title: String, url: String) {
        if (menuUrl != null) {
            return
        }
        menuUrl = url
        val menu = this.menu?.add(Menu.NONE, 100, Menu.NONE, title)
        menu?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    fun addMenuAction(title: String, act: String, colorStr: String?, isBold: Boolean?) {
        if (mAct != null) {
            return
        }
        mAct = act
        val s = SpannableString(title)

        if (colorStr != null) {
            s.setSpan(ForegroundColorSpan(Color.parseColor(colorStr)), 0, s.length, 0)
        } else {
            s.setSpan(ForegroundColorSpan(Color.parseColor("#ff6500")), 0, s.length, 0)
        }

        if (isBold != null && isBold) {
            s.setSpan(StyleSpan(Typeface.BOLD), 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val menu = this.menu?.add(Menu.NONE, 101, Menu.NONE, s)
        menu?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    fun removeMenu() {
        this.menu?.removeItem(100)
        menuUrl = null

        this.menu?.removeItem(101)
        mAct = null
    }

    override fun initViewModel(): InstaWebViewModel {
        return getActivityViewModel(InstaWebViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_insta_web, viewModel)
    }

}