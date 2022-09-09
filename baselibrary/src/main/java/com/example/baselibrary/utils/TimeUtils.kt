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


    fun formatMessageTime(timestamp: Long):String {
        var ts = timestamp
        if (timestamp > System.currentTimeMillis() * 1000) {
            ts = timestamp / 10000L
        }
        val span = System.currentTimeMillis() / 1000 - ts / 1000 //距离现在的时间跨度，秒
        return when {
            span < 86400 -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(ts)
            }
            span < 604800 -> {
                SimpleDateFormat("EEEE HH:mm", Locale.getDefault()).format(ts)
            }
            else -> {
                SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.getDefault()).format(ts)
            }
        }
    }

    fun checkAdult(date: Date?, ageLimit: Int): Boolean {
        val current = Calendar.getInstance()
        val birthDay = Calendar.getInstance()
        birthDay.time = date
        val year = current[Calendar.YEAR] - birthDay[Calendar.YEAR]
        if (year > ageLimit) {
            return true
        } else if (year < ageLimit) {
            return false
        }
        // 如果年相等，就比较月份
        val month = current[Calendar.MONTH] - birthDay[Calendar.MONTH]
        if (month > 0) {
            return true
        } else if (month < 0) {
            return false
        }
        // 如果月也相等，就比较天
        val day = current[Calendar.DAY_OF_MONTH] - birthDay[Calendar.DAY_OF_MONTH]
        return day >= 0
    }

}