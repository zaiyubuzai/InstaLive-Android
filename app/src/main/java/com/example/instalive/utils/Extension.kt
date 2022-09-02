package com.example.instalive.utils

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.animation.addListener
import com.example.baselibrary.utils.Utils
import com.example.baselibrary.utils.marsToast
import com.example.instalive.BuildConfig
import com.example.instalive.InstaLiveApp
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import splitties.alertdialog.appcompat.*
import com.example.instalive.R
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

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


fun Context.requestStoragePermission(go: () -> Unit, no: (()->Unit)? = null){
    Dexter.withContext(this)
        .withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                if (p0?.areAllPermissionsGranted() == true) {
                    go.invoke()
                } else {
                    alertDialog {
                        titleResource = R.string.photo_permission_dialog_title
                        messageResource = R.string.photo_permission_dialog_message
                        positiveButton(R.string.fb_allow) {
                            Utils.goToAppSettings(context, BuildConfig.APPLICATION_ID)
                        }
                        negativeButton(R.string.fb_dont_allow) {
                            it.dismiss()
                        }
                        onDismiss {
                            no?.invoke()
                        }
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                // Remember to invoke this method when the custom rationale is closed
                // or just by default if you don't want to use any custom rationale.
                token?.continuePermissionRequest()
            }
        })
        .withErrorListener {
        }
        .check()
}

fun Context.requestLivePermission(go: () -> Unit){
    Dexter.withContext(this)
        .withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                if (p0?.areAllPermissionsGranted() == true) {
                    //start live
                    go.invoke()
                } else {
                    val deniedP = p0?.deniedPermissionResponses
                    deniedP?.forEach {
                        if (it.permissionName == Manifest.permission.CAMERA) {
                            alertDialog {
                                titleResource = R.string.camera_permission_dialog_title
                                messageResource =  R.string.camera_permission_dialog_message
                                positiveButton(R.string.go_to_settings) {
                                    Utils.goToAppSettings(context, BuildConfig.APPLICATION_ID)
                                }
                                negativeButton(R.string.not_now) {
                                    it.dismiss()
                                }
                            }.show()
                        } else if (it.permissionName == Manifest.permission.RECORD_AUDIO) {
                            alertDialog {
                                titleResource = R.string.microphone_permission_dialog_title
                                messageResource =  R.string.microphone_permission_dialog_message
                                positiveButton(R.string.go_to_settings) {
                                    Utils.goToAppSettings(context, BuildConfig.APPLICATION_ID)
                                }
                                negativeButton(R.string.not_now) {
                                    it.dismiss()
                                }
                            }.show()
                        }
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?,
            ) {
                p1?.continuePermissionRequest()
            }
        })
        .check()
}