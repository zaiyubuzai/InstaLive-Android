package com.example.instalive.app.home

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.example.baselibrary.utils.*
import com.example.baselibrary.views.BaseActivity
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.ActivityCreateEventBinding
import kotlinx.android.synthetic.main.activity_create_event.*
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.positiveButton
import splitties.alertdialog.appcompat.titleResource
import splitties.views.onClick
import splitties.views.textResource
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class CreateEventActivity : BaseActivity<CreateEventViewModel, ActivityCreateEventBinding>() {
    private lateinit var dateCalendar: Calendar

    override fun initData(savedInstanceState: Bundle?) {
        BarUtils.setStatusBarLightMode(this, false)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)
        supportActionBar?.title = ""
        edtEventName.addTextChangedListener {
            updateEventNameTextCount(it)
        }
        edtEventName.doAfterTextChanged {
            publish.isEnabled = it.toString().trim().isNotEmpty()
        }
        edtEventDescription.addTextChangedListener {
            updateEventDescTextCount(it)
        }

        close.onClick {
            onBackPressed()
        }
        publish.onClick {
            if (dateCalendar.timeInMillis < System.currentTimeMillis()) {
                marsToast(R.string.fb_new_event_in_the_past)
            } else {
//                viewModel.createEvent(
//                    SessionPreferences.recentConversationID,
//                    dateCalendar.timeInMillis / 1000,
//                    edtEventName.text.toString(),
//                    edtEventDescription.text.toString()
//                ) {
//                }
            }
        }
        eventDate.onClick {
            showDateDialog()
        }
        eventTime.onClick {
            showTimeDialog()
        }
        edtEventName.filters = arrayOf(mInputFilter, MyLengthFilter(50) {
            marsToast(getString(R.string.fb_up_to_any_chars, it.toString()))
        })
        edtEventDescription.filters = arrayOf(MyLengthFilter(200) {
            marsToast(getString(R.string.fb_up_to_any_chars, it.toString()))
        })
        dateCalendar = Calendar.getInstance()
        val date = Date()
        date.time += 10 * 60 * 1000
        dateCalendar.time = date
        buildDateText(dateCalendar)
        buildTimeText(dateCalendar)

//        viewModel.createEventData.observe(this, {
//            marsToast(R.string.fb_published)
//            finish()
//        })
        viewModel.errorCodeLiveData.observe(this, {
            if (it != 0) marsToast(viewModel.errorMessageLiveData.value.toString())
        })
    }

    private fun buildDateText(calendar: Calendar) {
        if (TimeUtils.isTargetDay(calendar, 0)) {
            eventDate.textResource = R.string.fb_today
            return
        }
        if (TimeUtils.isTargetDay(calendar, 1)) {
            eventDate.textResource = R.string.fb_tomorrow
            return
        }
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val abbrMonthName = SimpleDateFormat("MMM").format(calendar.time)
        //Jan 07, 2022
        val myDate =
            "$abbrMonthName ${if (day < 10) "0$day" else day}, $year"
        eventDate.text = myDate
    }
    @SuppressLint("SimpleDateFormat")
    fun getCurrentMonth(): String? {
        val cal = Calendar.getInstance()
        val month_date = SimpleDateFormat("MMM")
        return month_date.format(cal.time)
    }

    fun setCurrentMonthSpinner(): Int {
        val months: Array<String> = resources.getStringArray(R.array.AbbrMonths)
        return 0
    }

    private fun buildTimeText(calendar: Calendar) {
        val hour = calendar[Calendar.HOUR]
        val minute = calendar[Calendar.MINUTE]
        val amOrPM = calendar[Calendar.AM_PM]
        //9:00 PM
        val myTime =
            "${if (hour == 0) "12" else "$hour"}:${if (minute < 10) "0$minute" else minute} ${if (amOrPM == 0) "AM" else "PM"}"
        eventTime.text = myTime
    }

    private fun showDateDialog() {
        edtEventName.hideKeyboard()
        edtEventDescription.hideKeyboard()
        val datePickerDialog = DatePickerDialog(
            this@CreateEventActivity,
            { view, year, month, dayOfMonth ->
                val ccMin = Calendar.getInstance()
                ccMin.time = Date()
                if (ccMin[Calendar.YEAR] > year
                    || ccMin[Calendar.MONTH] > month
                    || ccMin[Calendar.DAY_OF_MONTH] > dayOfMonth
                ) {
                    //时间选小了
                    view.updateDate(
                        ccMin[Calendar.YEAR],
                        ccMin[Calendar.MONTH],
                        ccMin[Calendar.DAY_OF_MONTH]
                    )
                    marsToast(R.string.fb_new_event_in_the_past)
                } else {
                    dateCalendar.set(Calendar.YEAR, year)
                    dateCalendar.set(Calendar.MONTH, month)
                    dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buildDateText(dateCalendar)
                }
            },
            dateCalendar.get(Calendar.YEAR),
            dateCalendar.get(Calendar.MONTH),
            dateCalendar.get(Calendar.DAY_OF_MONTH)
        )
        val date = Date()
        datePickerDialog.datePicker.minDate = date.time
        datePickerDialog.datePicker.maxDate = date.time + 7 * 24 * 60 * 60 * 1000
        datePickerDialog.show()
    }

    private fun showTimeDialog() {
        edtEventName.hideKeyboard()
        edtEventDescription.hideKeyboard()
        val calendar = dateCalendar
        val timeDialog = TimePickerDialog(
            this,
            R.style.CustomDatePickerDialog,
            { view, hourOfDay, minute ->
                val calendar1 = Calendar.getInstance()
                val date = Date()
                calendar1.time = date
                if (TimeUtils.isTargetDay(dateCalendar, 0)) {
                    if (hourOfDay < calendar1[Calendar.HOUR_OF_DAY]
                        || (hourOfDay == calendar1[Calendar.HOUR_OF_DAY] && minute < calendar1[Calendar.MINUTE])
                    ) {
                        // Too little time
                        showDateTipDialog(R.string.fb_new_event_in_the_past)
                        return@TimePickerDialog
                    }
                } else if (TimeUtils.isTargetDay(dateCalendar, 7)) {
                    if (hourOfDay > calendar1[Calendar.HOUR_OF_DAY]
                        || (hourOfDay == calendar1[Calendar.HOUR_OF_DAY] && minute > calendar1[Calendar.MINUTE])
                    ) {
                        // It's too much time
                        showDateTipDialog(R.string.fb_new_event_within_7_days)
                        return@TimePickerDialog
                    }
                }
                dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                dateCalendar.set(Calendar.MINUTE, minute)
                buildTimeText(dateCalendar)
            }, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], false
        )
        timeDialog.show()
    }

    private fun showDateTipDialog(title: Int) {
        alertDialog {
            titleResource = title
            positiveButton(R.string.fb_confirm) {
                it.dismiss()
            }
        }.show()
    }


    private fun updateEventNameTextCount(it: Editable?) {
        val curLength = it.toString().length
        nameLength.text = "$curLength/50"
        if (curLength >= 50) {
            nameLength.setTextColor(Color.parseColor("#ff3131"))
        } else {
            nameLength.setTextColor(Color.parseColor("#65779e"))
        }
    }

    private fun updateEventDescTextCount(it: Editable?) {
        val curLength = it.toString().length
        descriptionLength.text = "$curLength/200"
        if (curLength >= 200) {
            descriptionLength.setTextColor(Color.parseColor("#ff3131"))
        } else {
            descriptionLength.setTextColor(Color.parseColor("#65779e"))
        }
    }

    override fun initViewModel(): CreateEventViewModel {
        return getActivityViewModel(CreateEventViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_create_event, viewModel)
    }

    private var mInputFilter =
        InputFilter { source, start, end, dest, dstart, dend ->
            Timber.d("source: $source start: $source end: $end dest: $dest dstart: $dstart dend: $dend")
            if(source.contains("\n") || source.contains("  ")){
                //空格和换行都转换为""
                source.toString().replace("\n", "").replace("  ", " ")
            } else {
                null
            }
        }

}