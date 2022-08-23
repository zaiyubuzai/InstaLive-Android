package com.example.baselibrary.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    /**
     * 一天的秒数
     */
     const val dayMinute = 86400;

    /*
     * 获取当前日期凌晨时间戳
     */
    fun getCurrentTimeDate(): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val data = Date()
        var today = sdf.format(data)
        val daytime: Long = sdf.parse(today).time
        return daytime / 1000
    }

    fun getTimeHMS(time: Long): String{
        val format = SimpleDateFormat("HH:mm:ss")
        format.timeZone = TimeZone.getTimeZone("GMT+0")
        return format.format(time)
    }

    fun isTargetDay(cal: Calendar, targetDay: Int): Boolean {
        val pre = Calendar.getInstance()
        val predate = Date(System.currentTimeMillis())
        pre.time = predate

        if (cal[Calendar.YEAR] == pre[Calendar.YEAR]) {
            val diffDay = (cal[Calendar.DAY_OF_YEAR]
                    - pre[Calendar.DAY_OF_YEAR])
            if (diffDay == targetDay) {
                return true
            }
        }
        return false
    }

    fun secToTime(time: Int): String? {
        var timeStr: String? = null
        val hour = time / 3600
        val minute = time / 60 % 60
        val second = time % 60
        timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second)
        return timeStr
    }

    fun secToMinutes(time: Int): String? {
        var timeStr: String? = null
        val minute = time / 60
        val second = time % 60
        timeStr = unitFormat(minute) + ":" + unitFormat(second)
        return timeStr
    }

    fun unitFormat(i: Int): String {
        var retStr: String? = null
        retStr = if (i >= 0 && i < 10) "0" + Integer.toString(i) else "" + i
        return retStr
    }




}