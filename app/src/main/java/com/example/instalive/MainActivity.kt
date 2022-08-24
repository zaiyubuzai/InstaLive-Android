package com.example.instalive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.home.HomeActivity
import com.example.instalive.app.login.NotLoginYetActivity
import com.example.instalive.utils.DeeplinkHelper
import com.example.instalive.utils.DeeplinkHelper.DEEPLINK_SCHEME
import com.example.instalive.utils.DeeplinkHelper.HTTPS_SCHEME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.activities.start
import timber.log.Timber

@ExperimentalStdlibApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntentDeeplink()
        lifecycleScope.launch(Dispatchers.IO){
            delay(2000)
            withContext(Dispatchers.Main){
                if (SessionPreferences.id.isEmpty()){
                    start<NotLoginYetActivity> {  }
                    finish()
                } else {
                    start<HomeActivity> {  }
                }
                finish()
            }
        }
    }
    private fun handleIntentDeeplink(): Boolean {
        val uri = intent.data
        Timber.d("uri ： ${uri?:"no uri"}")
        return if (uri != null && (uri.scheme == DEEPLINK_SCHEME || uri.scheme == HTTPS_SCHEME)) {
            DeeplinkHelper.handleDeeplink(uri, this)
        } else {
            false
        }
    }

}