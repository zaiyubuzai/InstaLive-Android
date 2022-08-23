package com.example.baselibrary.utils

import android.util.DisplayMetrics
import android.app.Activity
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.WindowManager
import java.lang.Exception

/**
 * ScreenUtils
 */
object ScreenUtils {
    /**
     * 获取屏幕宽
     *
     * @return
     */
    @JvmStatic
    fun getScreenWidth(context: Context?): Int {
        if (context == null) return 0
        val dm = context.resources.displayMetrics
        return dm.widthPixels
    }

    /**
     * 获取屏幕程序内容高度
     *
     * @return
     */
    @JvmStatic
    fun getScreenHeight(context: Context?): Int {
        if (context == null) return 0
        val dm = context.resources.displayMetrics
        return dm.heightPixels
    }

    /**
     * 获取屏幕实际高度
     *
     * @return
     */
    fun getScreenHeightActual(activity: Activity?): Int {
        if (activity == null) return 0
        val outMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(outMetrics)
        return outMetrics.heightPixels
    }

    @JvmStatic
    fun dp2px(context: Context, dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    fun sp2px(context: Context, sp: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return sp * scale
    }

    /**
     * 底部虚拟按键栏的高度
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun getSoftButtonsBarHeight(activity: Activity): Int {
        val metrics = DisplayMetrics()
        //这个方法获取可能不是真实屏幕的高度
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        //获取当前屏幕的真实高度
        activity.windowManager.defaultDisplay.getRealMetrics(metrics)
        val realHeight = metrics.heightPixels
        return if (realHeight > usableHeight) {
            realHeight - usableHeight
        } else {
            0
        }
    }

    /**
     * 判断软键盘是否显示方法
     * @param activity
     * @return
     */
    fun isSoftShowing(activity: Activity): Boolean {
        //获取当屏幕内容的高度
        val screenHeight = activity.window.decorView.height
        //获取View可见区域的bottom
        val rect = Rect()
        //DecorView即为activity的顶级view
        activity.window.decorView.getWindowVisibleDisplayFrame(rect)
        //考虑到虚拟导航栏的情况（虚拟导航栏情况下：screenHeight = rect.bottom + 虚拟导航栏高度）
        //选取screenHeight*2/3进行判断
        return screenHeight * 2 / 3 > rect.bottom + getSoftButtonsBarHeight(activity)
    }

    fun getDpi(context: Context): Int {
        var dpi = 0
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        val c: Class<*>
        try {
            c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, displayMetrics)
            dpi = displayMetrics.heightPixels
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dpi
    }

    fun getBottomStatusHeight(context: Context): Int {
        val totalHeight = getDpi(context)
        val contentHeight = getScreenHeight(context)
        return totalHeight - contentHeight
    }
}