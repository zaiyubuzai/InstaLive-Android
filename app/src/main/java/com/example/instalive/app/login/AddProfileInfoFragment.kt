package com.example.instalive.app.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged

import androidx.lifecycle.Observer
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.*
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.BuildConfig
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.databinding.FragmentAddProfileInfoBinding
import com.example.instalive.utils.GlideEngine
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
import kotlinx.android.synthetic.main.fragment_add_profile_info.*
import splitties.alertdialog.appcompat.*
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrNull
import splitties.mainhandler.mainHandler
import splitties.systemservices.inputMethodManager
import splitties.views.onClick
import java.util.*

@ExperimentalStdlibApi
class AddProfileInfoFragment :
    BaseFragment<AddProfileInfoViewModel, FragmentAddProfileInfoBinding>() {
    var phone: String? by argOrNull()
    var passcode: String? by argOrNull()
    var source: String by arg()
    var title: String by arg()

    val birthDay = "2000-01-01"
    val gender = 1

    private var isShowToUser = true
    private var permissionDialog: AlertDialog? = null
    override fun initViewModel(): AddProfileInfoViewModel {
        return getActivityViewModel(AddProfileInfoViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_add_profile_info, viewModel)
    }

    override fun initData(savedInstanceState: Bundle?) {
        screenName = "login_view"
        fullNameInput.requestFocus()
        fullNameInput.showKeyboard()
        toolbar.setNavigationOnClickListener {
            activity.onBackPressed()
        }

        next.onClick {
            if (fullNameInput.text.toString().endsWith(" ")) {
                marsToast(getString(R.string.fb_fullname_not_end_with_space))
            } else if (fullNameInput.text.toString().startsWith(" ")) {
                marsToast(getString(R.string.fb_fullname_not_start_with_space))
            } else {
                fullNameInput.hideKeyboard()
                viewModel.checkUsernameAvailability(fullNameInput.text.toString())
                next.isEnabled = false
            }
        }

        portrait.onClick {
            selectImageDialog()
        }
        imageSelector.onClick {
            selectImageDialog()
        }
        genderText.onClick{

        }
        birthdayText.onClick{
            if (!SessionPreferences.birthdayError) showBirthdayDialog()
        }

        viewModel.loadingStatsLiveData.observe(this, Observer {
            progress.isVisible = it == StatusEvent.LOADING
        })

        fullNameInput.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->

            if (source.equals(" ") && dstart == 0) {
                return@InputFilter ""
            }

            if (source.contains(" ") && dest.contains(" ")) {
                marsToast(getString(R.string.fb_fullname_only_one_space))
                return@InputFilter ""
            }

            if (source.toString().contains("\n")) {
                return@InputFilter ""
            }

            return@InputFilter source
        }, MyLengthFilter(20) {
            marsToast(getString(R.string.fb_up_to_any_chars, it.toString()))
        })

        fullNameInput.doAfterTextChanged {
            it?.let {
                next?.isEnabled = it.length > 1
            }
        }
        initObserver()
    }

    private fun initObserver(){
        viewModel.checkUsernameData.observe(this, {
            (activity as LoginActivity).redirectSelectOwnRole(
                phone, passcode, "", fullNameInput.text.toString(), birthDay, gender
            )
        })
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

    private fun showBirthdayDialog() {
        val date = Calendar.getInstance()
        val mdate = Calendar.getInstance()
        mdate[2000, 0] = 1
        val date2 = Calendar.getInstance()
        date2[1900, 1] = 1
//        val popup =
//            TimePickerPopup(activity)
//                .setDefaultDate(if (currentDate == null) mdate else currentDate)  //设置默认选中日期
//                .setItemDividerWidth(activity.dip(1))
//                .setYearRange(1900, date[Calendar.YEAR]) //设置年份范围
//                .setDateRange(date2, date) //设置日期范围
//                .setTimePickerListener(object : TimePickerListener {
//                    override fun onTimeChanged(date: Date?) {
//                        //时间改变
//                    }
//
//                    override fun onTimeConfirm(date: Date, view: View?) {
//                        if (checkAdult(date)) hideBirthdayError()
//                        val calendar = Calendar.getInstance()
//                        calendar.time = date
//                        currentDate = calendar
//
//                        val mYear = calendar.get(Calendar.YEAR)
//                        val mMonth = calendar.get(Calendar.MONTH)
//                        val mDay = calendar.get(Calendar.DAY_OF_MONTH)
//
//                        val mDate =
//                            "${Constants.mouthE[mMonth]}/${if (mDay < 10) "0$mDay" else mDay}/${mYear}"
//                        // 将选择的日期赋值给TextView
//                        birthdayText?.text = mDate
//                        next?.isEnabled = true
//                    }
//                })
//        popup.dividerColor = 0xFF788093.toInt()

//        XPopup.Builder(activity)
//            .isDarkTheme(true)
//            .asCustom(popup)
//            .show()

    }

    private fun selectImageDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
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

    }

    override fun onResume() {
        super.onResume()
        if (isShowToUser) fullNameInput.requestFocus()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isShowToUser = !hidden
        if (!hidden){
            mainHandler.postDelayed({
                fullNameInput?.requestFocus()
                fullNameInput?.showKeyboard()
                inputMethodManager.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }, 300)
        }
    }

}