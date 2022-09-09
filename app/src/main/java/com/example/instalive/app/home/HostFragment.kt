package com.example.instalive.app.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.utils.Utils
import com.example.baselibrary.utils.dip
import com.example.baselibrary.utils.baseToast
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.BuildConfig
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.live.LiveHostActivity
import com.example.instalive.databinding.FragmentHostBinding
import com.example.instalive.utils.GlideEngine
import com.example.instalive.utils.requestLivePermission
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.fragment_host.*
import kotlinx.coroutines.launch
import splitties.alertdialog.appcompat.*
import splitties.dimensions.dp
import splitties.fragments.start
import splitties.intents.start
import splitties.views.onClick

/**
 * A simple [Fragment] subclass.
 * Use the [HostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalStdlibApi
class HostFragment : BaseFragment<HomeViewModel, FragmentHostBinding>() {

    private var permissionDialog: AlertDialog? = null

    override fun initViewModel(): HomeViewModel {
        return getActivityViewModel(HomeViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_host, viewModel)
    }


    override fun initData(savedInstanceState: Bundle?) {
        name.text = "@${SessionPreferences.nickName}"
        val options = RequestOptions.bitmapTransform(RoundedCorners(context?.dp(20)?:100))
        Glide.with(this).load(SessionPreferences.portrait)
            .apply(options)
            .into(avatar)
        btnDashboard.onClick{

        }
        btnStartLive.onClick{
            context?.requestLivePermission(go={
                start(LiveHostActivity) { _, spec ->
                    spec.isCanLive = true
                    spec.isLiveNeedResume = false
                }
            })
        }
        btnScheduleLive.onClick{
            start<CreateEventActivity> {  }
        }
        avatar.onClick{
            selectImageDialog()
        }
        viewModel.resultData.observe(this, {
            SessionPreferences.portrait = it
            if (avatar == null) return@observe
            val options = RequestOptions.bitmapTransform(RoundedCorners(activity.dip(20)))
            Glide.with(activity).load(it)
                .apply(options)
                .into(avatar)
            baseToast(R.string.fb_upload_success)
            viewModel.updateProfile(it)
        })
    }

    private fun selectImageDialog() {
        val c = context?:return
        val builder: AlertDialog.Builder = AlertDialog.Builder(c)
        //设置标题
        //  builder.setTitle(getString(R.string.image_source))
        //底部的取消按钮
        builder.setNegativeButton(getString(R.string.fb_cancel), null)

        val takePhotoSpan = SpannableString(getString(R.string.fb_take_a_photo))
        takePhotoSpan.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            takePhotoSpan.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val photoGallerySpan = SpannableString(getString(R.string.fb_choose_from_photo_gallery))
        photoGallerySpan.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            photoGallerySpan.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        builder.setItems(
            arrayOf(takePhotoSpan, photoGallerySpan)
        ) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
//                0 -> openCamera()
                1 -> checkPhotoPermission()
//                1 -> goPickAndCrop()
            }
        }
        builder.create().show()
    }

    private fun checkPhotoPermission() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    goPickAndCrop()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    if (p0?.isPermanentlyDenied == true) {
                        showSettingDialog(
                            R.string.fb_photo_permission_dialog_title,
                            R.string.fb_photo_permission_dialog_message
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?,
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

    private fun checkCameraPermission() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    openCamera()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    if (p0?.isPermanentlyDenied == true) {
                        showSettingDialog(
                            R.string.fb_camera_permission_dialog_title,
                            R.string.fb_camera_permission_dialog_message
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?,
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

    private fun showSettingDialog(title: Int, message: Int) {
        if (permissionDialog == null) {
            permissionDialog = context?.alertDialog {
                titleResource = title
                messageResource = message
                positiveButton(R.string.fb_allow) {
                    Utils.goToAppSettings(context, BuildConfig.APPLICATION_ID)
                }
                negativeButton(R.string.fb_dont_allow) {
                    it.dismiss()
                }
                onDismiss {
                    permissionDialog = null
                }
            }
            permissionDialog?.show()
        }
    }

    private fun openCamera() {
        PictureSelector.create(this)
            .openCamera(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .isEnableCrop(true)
            .withAspectRatio(1, 1)
            .cropImageWideHigh(500, 500)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: MutableList<LocalMedia>?) {
                    if (result != null) {
                        doUpload(result[0].cutPath)
                    }
                }

                override fun onCancel() {
                }
            })
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun goPickAndCrop() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .isCamera(false)
            .selectionMode(PictureConfig.SINGLE)
            .isSingleDirectReturn(true)
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .isEnableCrop(true)
            .withAspectRatio(1, 1)
            .cropImageWideHigh(500, 500)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: MutableList<LocalMedia>?) {
                    if (result != null && result.isNotEmpty() && result[0].cutPath != null) {
                        doUpload(result[0].cutPath)
                    }
                }

                override fun onCancel() {
                }
            })
    }

    private fun doUpload(path: String) {
        viewModel.uploadAvatar(path, {
            lifecycleScope.launch {
                showCommonProgress()
            }
        }, {
            lifecycleScope.launch {
                hideCommonProgress()
            }
        })
    }
}