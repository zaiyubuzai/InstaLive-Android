package com.example.baselibrary.utils.safe

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Debug
import android.os.Process
import java.io.*
import java.lang.reflect.Field
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.nio.charset.Charset

class SecurityCheckUtil private constructor() {

    /**
     * 获取签名信息
     *
     * @param context
     * @return
     */
    fun getSignature(context: Context): String {
        try {
            val packageInfo = context.packageManager
                .getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            // 通过返回的包信息获得签名数组
            val signatures = packageInfo.signatures
            // 循环遍历签名数组拼接应用签名
            val builder = StringBuilder()
            for (signature in signatures) {
                builder.append(signature.toCharsString())
            }
            // 得到应用签名
            return builder.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 检测app是否为debug版本
     *
     * @param context
     * @return
     */
    fun checkIsDebugVersion(context: Context): Boolean {
        return (context.applicationInfo.flags
                and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * java法检测是否连上调试器
     *
     * @return
     */
    fun checkIsDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected()
    }

    /**
     * usb充电辅助判断
     *
     * @param context
     * @return
     */
    fun checkIsUsbCharging(context: Context): Boolean {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter) ?: return false
        val chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        return chargePlug == BatteryManager.BATTERY_PLUGGED_USB
    }

    /**
     * 拿清单值
     *
     * @param context
     * @param name
     * @return
     */
    fun getApplicationMetaValue(context: Context, name: String?): String? {
        val appInfo = context.applicationInfo
        return appInfo.metaData.getString(name)
    }

    /**
     * 检测本地端口是否被占用
     *
     * @param port
     * @return
     */
    fun isLocalPortUsing(port: Int): Boolean {
        var flag = true
        try {
            flag = isPortUsing("127.0.0.1", port)
        } catch (e: Exception) {
        }
        return flag
    }

    /**
     * 检测任一端口是否被占用
     *
     * @param host
     * @param port
     * @return
     * @throws UnknownHostException
     */
    @Throws(UnknownHostException::class)
    fun isPortUsing(host: String?, port: Int): Boolean {
        var flag = false
        val theAddress = InetAddress.getByName(host)
        try {
            val socket = Socket(theAddress, port)
            flag = true
        } catch (e: IOException) {
        }
        return flag
    }//eng/userdebug版本，自带root权限
    //user版本，继续查su文件
    /**
     * 检查root权限
     *
     * @return
     */
    val isRoot: Boolean
        get() {
            val secureProp = getroSecureProp()
            return if (secureProp == 0) //eng/userdebug版本，自带root权限
                true else isSUExist //user版本，继续查su文件
        }

    private fun getroSecureProp(): Int {
        val secureProp: Int
        val roSecureObj = CommandUtil.getSingleInstance().getProperty("ro.secure")
        secureProp = if (roSecureObj == null) 1 else {
            if ("0" == roSecureObj) 0 else 1
        }
        return secureProp
    }

    private fun getroDebugProp(): Int {
        val debugProp: Int
        val roDebugObj = CommandUtil.getSingleInstance().getProperty("ro.debuggable")
        debugProp = if (roDebugObj == null) 1 else {
            if ("0" == roDebugObj) 0 else 1
        }
        return debugProp
    }

    private val isSUExist: Boolean
        private get() {
            var file: File? = null
            val paths = arrayOf(
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            for (path in paths) {
                file = File(path)
                if (file.exists()) return true
            }
            return false
        }

    /**
     * 通过检查是否已经加载了XP类来检测
     *
     * @return
     */
    @get:Deprecated("")
    val isXposedExists: Boolean
        get() {
            try {
                val xpHelperObj = ClassLoader
                    .getSystemClassLoader()
                    .loadClass(XPOSED_HELPERS)
                    .newInstance()
            } catch (e: InstantiationException) {
                e.printStackTrace()
                return true
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                return true
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return false
            }
            try {
                val xpBridgeObj = ClassLoader
                    .getSystemClassLoader()
                    .loadClass(XPOSED_BRIDGE)
                    .newInstance()
            } catch (e: InstantiationException) {
                e.printStackTrace()
                return true
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                return true
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return false
            }
            return true
        }

    /**
     * 通过主动抛出异常，检查堆栈信息来判断是否存在XP框架
     *
     * @return
     */
    val isXposedExistByThrow: Boolean
        get() = try {
            throw Exception("gg")
        } catch (e: Exception) {
            for (stackTraceElement in e.stackTrace) {
                if (stackTraceElement.className.contains(XPOSED_BRIDGE))
                    true
            }
            false
        }

    /**
     * 尝试关闭XP框架
     * 先通过isXposedExistByThrow判断有没有XP框架
     * 有的话先hookXP框架的全局变量disableHooks
     *
     *
     * 漏洞在，如果XP框架先hook了isXposedExistByThrow的返回值，那么后续就没法走了
     * 现在直接先hookXP框架的全局变量disableHooks
     *
     * @return 是否关闭成功的结果
     */
    fun tryShutdownXposed(): Boolean {
        var xpdisabledHooks: Field? = null
        return try {
            xpdisabledHooks = ClassLoader.getSystemClassLoader()
                .loadClass(XPOSED_BRIDGE)
                .getDeclaredField("disableHooks")
            xpdisabledHooks.isAccessible = true
            xpdisabledHooks[null] = java.lang.Boolean.TRUE
            true
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            false
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            false
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * java读取/proc/uid/status文件里TracerPid的方式来检测是否被调试
     *
     * @return
     */
    fun readProcStatus(): Boolean {
        return try {
            val localBufferedReader =
                BufferedReader(FileReader("/proc/" + Process.myPid() + "/status"))
            var tracerPid = ""
            while (true) {
                val str = localBufferedReader.readLine()
                if (str!!.contains("TracerPid")) {
                    tracerPid = str.substring(str.indexOf(":") + 1, str.length).trim { it <= ' ' }
                    break
                }
                if (str == null) {
                    break
                }
            }
            localBufferedReader.close()
            if ("0" == tracerPid) false else true
        } catch (fuck: Exception) {
            false
        }
    }// 修改长度为256，在做中大精简版时发现包名长度大于32读取到的包名会少字符，导致常驻进程下的初始化操作有问题

    /**
     * 获取当前进程名
     * @return
     */
    val currentProcessName: String?
        get() {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream("/proc/self/cmdline")
                val buffer =
                    ByteArray(256) // 修改长度为256，在做中大精简版时发现包名长度大于32读取到的包名会少字符，导致常驻进程下的初始化操作有问题
                var len = 0
                var b: Int
                while (fis.read().also { b = it } > 0 && len < buffer.size) {
                    buffer[len++] = b.toByte()
                }
                if (len > 0) {
                    return String(buffer, 0, len, Charset.forName("utf-8"))
                }
            } catch (e: Exception) {
            } finally {
                if (fis != null) {
                    try {
                        fis.close()
                    } catch (e: Exception) {
                    }
                }
            }
            return null
        }

    companion object {
        val singleInstance = SecurityCheckUtil()
        private const val XPOSED_HELPERS = "de.robv.android.xposed.XposedHelpers"
        private const val XPOSED_BRIDGE = "de.robv.android.xposed.XposedBridge"
    }
}