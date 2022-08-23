package com.example.instalive.app.web

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.instalive.R
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.bundleOrDefault
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec

class InstaWebActivity : AppCompatActivity() {
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

}