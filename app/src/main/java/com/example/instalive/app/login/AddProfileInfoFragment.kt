package com.example.instalive.app.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged

import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.*
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.BuildConfig
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.databinding.FragmentAddProfileInfoBinding
import com.example.instalive.mypicker.listener.TimePickerListener
import com.example.instalive.mypicker.popup.TimePickerPopup
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
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.fragment_add_profile_info.*
import kotlinx.coroutines.launch
import splitties.alertdialog.appcompat.*
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrNull
import splitties.mainhandler.mainHandler
import splitties.systemservices.inputMethodManager
import splitties.views.onClick
import splitties.views.textResource
import java.util.*

@ExperimentalStdlibApi
class AddProfileInfoFragment :
    BaseFragment<AddProfileInfoViewModel, FragmentAddProfileInfoBinding>() {
    var phone: String? by argOrNull()
    var passcode: String? by argOrNull()
    var portrait: String? by argOrNull()
    var source: String by arg()

    private var gender = 1
    private val ageLimit = 18//age limit

    private var currentDate: Calendar? = null

    private var isShowToUser = true
    private var permissionDialog: AlertDialog? = null

    private val months: Array<String>  by lazy {
        resources.getStringArray(R.array.AbbrMonths)
    }
    override fun initViewModel(): AddProfileInfoViewModel {
        return getActivityViewModel(AddProfileInfoViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_add_profile_info, viewModel)
    }

    override fun initData(savedInstanceState: Bundle?) {
        screenName = "login_view"
        toolbar.setNavigationOnClickListener {
            if (SessionPreferences.birthdayError) {
                activity.finish()
            } else {
                hideBirthdayError()
                activity.onBackPressed()
            }
        }

        portrait?.let {
            val options = RequestOptions.bitmapTransform(RoundedCorners(activity.dip(20)))
            Glide.with(activity).load(it)
                .apply(options)
                .into(portraitIV)
        }
        if (SessionPreferences.birthdayError) {
            showBirthdayError()
        } else {
            fullNameInput.requestFocus()
            fullNameInput.showKeyboard()
        }

        if (SessionPreferences.birthday.isNotEmpty()){
            birthdayText.text = SessionPreferences.birthday
        }
        genderText.textResource = R.string.fb_man
        next.onClick {
            currentDate?.let { calendar ->
                if (TimeUtils.checkAdult(calendar.time, ageLimit)) {
                    SessionPreferences.birthdayError = false
                    SessionPreferences.birthday = birthdayText.text.toString()
                    if (fullNameInput.text.toString().endsWith(" ")) {
                        baseToast(getString(R.string.fb_fullname_not_end_with_space))
                    } else if (fullNameInput.text.toString().startsWith(" ")) {
                        baseToast(getString(R.string.fb_fullname_not_start_with_space))
                    } else {
                        fullNameInput.hideKeyboard()
                        viewModel.checkUsernameAvailability(fullNameInput.text.toString())
                        next.isEnabled = false
                    }
                } else {
                    SessionPreferences.birthday = birthdayText.text.toString()
                    showBirthdayError()
                }
            }
        }

        portraitIV.onClick {
            selectImageDialog()
        }
        imageSelector.onClick {
            selectImageDialog()
        }
        genderText.onClick{
            showList()
        }
        birthdayText.onClick{
            fullNameInput.clearFocus()
            fullNameInput.hideKeyboard()
            if (!SessionPreferences.birthdayError) showBirthdayDialog()
        }

        fullNameInput.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->

            if (source.equals(" ") && dstart == 0) {
                return@InputFilter ""
            }

            if (source.contains(" ") && dest.contains(" ")) {
                baseToast(getString(R.string.fb_fullname_only_one_space))
                return@InputFilter ""
            }

            if (source.toString().contains("\n")) {
                return@InputFilter ""
            }

            return@InputFilter source
        }, MyLengthFilter(20) {
            baseToast(getString(R.string.fb_up_to_any_chars, it.toString()))
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
                phone, passcode, fullNameInput.text.toString(), SessionPreferences.birthday, gender
            )
        })

        viewModel.resultData.observe(this, {
            (activity as LoginActivity).portrait = it
            if (portraitIV == null) return@observe
            val options = RequestOptions.bitmapTransform(RoundedCorners(activity.dip(20)))
            Glide.with(activity).load(it)
                .apply(options)
                .into(portraitIV)
            baseToast(R.string.fb_upload_success)
        })
        viewModel.loadingStatsLiveData.observe(this, {
            progress.isVisible = it == StatusEvent.LOADING
        })
        viewModel.errorCodeLiveData.observe(this, {
            if (it == 1161) {
                //Sorry, looks like you are Noy eligible for Fambase.
//                marsToast("Sorry, looks like you are Noy eligible for Fambase.")
                SessionPreferences.birthday = birthdayText.text.toString()
                showBirthdayError()
            } else {
                baseToast(viewModel.errorMessageLiveData.value.toString())
            }
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
        if (!isAdded) return
        val c = context ?: return
        val date = Calendar.getInstance()
        val mdate = Calendar.getInstance()
        mdate[2000, 0] = 1
        val date2 = Calendar.getInstance()
        date2[1900, 1] = 1
        val popup =
            TimePickerPopup(c)
                .setDefaultDate(if (currentDate == null) mdate else currentDate)  //设置默认选中日期
                .setItemDividerWidth(activity.dip(1))
                .setYearRange(1900, date[Calendar.YEAR]) //设置年份范围
                .setDateRange(date2, date) //设置日期范围
                .setTimePickerListener(object : TimePickerListener {
                    override fun onTimeChanged(date: Date?) {
                        //时间改变
                    }

                    override fun onTimeConfirm(date: Date, view: View?) {
                        if (TimeUtils.checkAdult(date, ageLimit)) hideBirthdayError()
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        currentDate = calendar

                        val mYear = calendar.get(Calendar.YEAR)
                        val mMonth = calendar.get(Calendar.MONTH)
                        val mDay = calendar.get(Calendar.DAY_OF_MONTH)

                        val mDate =
                            "${months[mMonth]}/${if (mDay < 10) "0$mDay" else mDay}/${mYear}"
                        // 将选择的日期赋值给TextView
                        birthdayText?.text = mDate
                        next?.isEnabled = true
                    }
                })
        popup.dividerColor = 0xFF788093.toInt()

        XPopup.Builder(c)
            .isDarkTheme(true)
            .asCustom(popup)
            .show()

    }

    private fun showBirthdayError() {
        if (!isAdded) return
        BarUtils.setStatusBarColor(activity, Color.RED)
        appBar?.setBackgroundColor(Color.RED)
        errText?.text = getString(R.string.fb_login_birthday_error)
        Toast.makeText(activity, R.string.fb_login_birthday_error, Toast.LENGTH_SHORT).show()
        next?.isEnabled = false
        SessionPreferences.birthdayError = true
    }

    private fun hideBirthdayError() {
        if (!isAdded) return
        BarUtils.setStatusBarColor(activity, Color.TRANSPARENT)
        appBar?.setBackgroundColor(Color.TRANSPARENT)
        errText?.text = ""
        next?.isEnabled = true
        SessionPreferences.birthdayError = false
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

    private fun showList() {
        val c = context ?: return
        val items = arrayOf(
                c.getString(R.string.fb_man),
                c.getString(R.string.fb_woman),
                c.getString(R.string.fb_cancel)
            )

        AlertDialog.Builder(c)
            .setTitle(c.getString(R.string.fb_login_gender_placeholder))
            .setItems(items) { dialogInterface: DialogInterface?, i: Int ->
                when (i) {
                    0 -> {
                        gender = 1
                        genderText.textResource = R.string.fb_man
                    }
                    1 -> {
                        gender = 2
                        genderText.textResource = R.string.fb_woman
                    }
                    else -> {

                    }
                }
                dialogInterface?.dismiss()
            }.create().show()
    }

    override fun onResume() {
        super.onResume()
//        if (isShowToUser) fullNameInput.requestFocus()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isShowToUser = !hidden
        if (!hidden){
//            mainHandler.postDelayed({
//                fullNameInput?.requestFocus()
//                fullNameInput?.showKeyboard()

//            }, 300)
        }
    }

}