package com.example.instalive.app.login

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
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.databinding.FragmentAddProfileInfoBinding
import com.example.instalive.mypicker.listener.TimePickerListener
import com.example.instalive.mypicker.popup.TimePickerPopup
import com.example.instalive.utils.GlideEngine
import com.example.instalive.utils.requestCameraPermission
import com.example.instalive.utils.requestPhotoPermission
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
import splitties.views.onClick
import splitties.views.textResource
import timber.log.Timber
import java.util.*

@ExperimentalStdlibApi
class AddProfileInfoFragment :
    BaseFragment<AddProfileInfoViewModel, FragmentAddProfileInfoBinding>() {
    var phone: String? by argOrNull()
    var passcode: String? by argOrNull()
    var portrait: String? by argOrNull()
    var source: String by arg()

    private var gender: Int? = null
    private val ageLimit = 18//age limit

    private var portraitLocalPath: String? = null
    private var currentDate: Calendar? = null

    private var isShowToUser = true

    private val months: Array<String> by lazy {
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

        if (SessionPreferences.birthdayError) {
            showBirthdayError()
        } else {
            fullNameInput.requestFocus()
            fullNameInput.showKeyboard()
        }

        if (SessionPreferences.birthday.isNotEmpty()) {
            birthdayText.text = SessionPreferences.birthday
        }

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
            fullNameInput.clearFocus()
            selectImageDialog()
        }
        imageSelector.onClick {
            fullNameInput.clearFocus()
            selectImageDialog()
        }
        genderText.onClick {
            fullNameInput.clearFocus()
            showList()
        }
        birthdayText.onClick {
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
                next?.isEnabled =
                    portrait != null && it.length > 5 && currentDate != null && gender != null
            }
        }
        initObserver()
    }

    private fun initObserver() {
        viewModel.checkUsernameData.observe(this, {
            (activity as LoginActivity).redirectSelectOwnRole(
                phone,
                passcode,
                fullNameInput.text.toString(),
                SessionPreferences.birthday,
                gender ?: 1
            )
        })

        viewModel.resultData.observe(this, {
            (activity as LoginActivity).portrait = it
            portrait = it
            if (portraitIV == null) return@observe
            val c = context ?: return@observe
            val options = RequestOptions.bitmapTransform(RoundedCorners(c.dip(20)))
            Glide.with(c)
                .load(portraitLocalPath)
                .apply(options)
                .into(portraitIV)
            baseToast(R.string.fb_upload_success)
            next?.isEnabled = isCanNext()
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

    private fun isCanNext(): Boolean {
        return portrait != null && fullNameInput.text.toString().length > 5 && currentDate != null && gender != null
    }

    private fun checkPhotoPermission() {
        context?.requestPhotoPermission({ goPickAndCrop() })
    }

    private fun checkCameraPermission() {
        context?.requestCameraPermission({ openCamera() })
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
                        next?.isEnabled = isCanNext()
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
        next?.isEnabled = isCanNext()
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
                1 -> checkPhotoPermission()
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
        portraitLocalPath = path
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
            c.getString(R.string.fb_male),
            c.getString(R.string.fb_female),
            c.getString(R.string.fb_cancel)
        )

        AlertDialog.Builder(c)
            .setTitle(c.getString(R.string.fb_login_gender_placeholder))
            .setItems(items) { dialogInterface: DialogInterface?, i: Int ->
                when (i) {
                    0 -> {
                        gender = 1
                        genderText.textResource = R.string.fb_male
                    }
                    1 -> {
                        gender = 2
                        genderText.textResource = R.string.fb_female
                    }
                    else -> {

                    }
                }
                next?.isEnabled = isCanNext()
                dialogInterface?.dismiss()
            }.create().show()
    }

    override fun onResume() {
        super.onResume()
//        if (isShowToUser) fullNameInput.setText("")

        val mdate = Calendar.getInstance()
        mdate[2000, 0] = 1
        currentDate = mdate

        portrait?.let {
            val c = context ?: return@let
            val options = RequestOptions.bitmapTransform(RoundedCorners(c.dip(20)))
            Glide.with(c)
                .load(it)
                .apply(options)
                .into(portraitIV)
        }

        next.isEnabled = isCanNext()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d("onHiddenChanged hidden:$hidden")
        isShowToUser = !hidden
        if (!hidden) {
            next.isEnabled = isCanNext()
//            mainHandler.postDelayed({
//                fullNameInput?.requestFocus()
//                fullNameInput?.showKeyboard()

//            }, 300)
        }
    }

    override fun onDestroyView() {
        genderText.text = ""
        gender = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.reset()
        Timber.d("onDestroy")
    }
}