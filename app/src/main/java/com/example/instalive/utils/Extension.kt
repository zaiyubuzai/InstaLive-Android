package com.example.instalive.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.animation.addListener
import com.example.baselibrary.utils.marsToast
import com.example.instalive.InstaLiveApp
import splitties.alertdialog.appcompat.R

fun marsToast(res: Int) {
    InstaLiveApp.appInstance.marsToast(res)
}

fun marsToast(msg: String) {
    InstaLiveApp.appInstance.marsToast(msg)
}

fun View.aAnimatorSet(view: ImageView) {
    val sy = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.3f, 1f).apply {
        duration = 500
        repeatCount = 0
    }
    val sx = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.3f, 1f).apply {
        duration = 500
        repeatCount = 0
    }
    val a = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.5f, 1f).apply {
        duration = 500
        repeatCount = 0
    }
    AnimatorSet().apply {
        addListener(
            onStart = {
//                view.setImageResource(R.drawable.ic_to_first_message)
            },
            onEnd = {
//                view.setImageResource(R.drawable.ic_to_first_message_gray)
            }
        )
        playTogether(sy, sx, a)
        interpolator = LinearInterpolator()
        start()
    }
}
