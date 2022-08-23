package com.venus.framework.util;

import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Simplified version of DroidParts logger.
 * Using 'verbose' level for debugging package, whose version name ends with 'SNAPSHOT', otherwise using 'warn' level.
 */
public class L {

    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;

    private static final String TAG = "VenusFambase";

    @VisibleForTesting
    public static boolean disabled;

    private static boolean debug;
    private static String _tag;

    protected L() {
    }

    public static void v(Object obj) {
        if (isLoggable(VERBOSE)) {
            log(VERBOSE, obj);
        }
    }

    public static void v(String format, Object... args) {
        if (isLoggable(VERBOSE)) {
            log(VERBOSE, format, args);
        }
    }

    public static void d(Object obj) {
        if (isLoggable(DEBUG)) {
            log(DEBUG, obj);
        }
    }

    public static void d(String format, Object... args) {
        if (isLoggable(DEBUG)) {
            log(DEBUG, format, args);
        }
    }

    public static void i(Object obj) {
        if (isLoggable(INFO)) {
            log(INFO, obj);
        }
    }

    public static void i(String format, Object... args) {
        if (isLoggable(INFO)) {
            log(INFO, format, args);
        }
    }

    public static void w(Object obj) {
        if (isLoggable(WARN)) {
            log(WARN, obj);
        }
    }

    public static void w(String format, Object... args) {
        if (isLoggable(WARN)) {
            log(WARN, format, args);
        }
    }

    public static void e(Object obj) {
        if (isLoggable(ERROR)) {
            log(ERROR, obj);
        }
    }

    public static void e(String format, Object... args) {
        if (isLoggable(ERROR)) {
            log(ERROR, format, args);
        }
    }

    public static void e(String msg, Throwable t) {
        if (isLoggable(ERROR)) {
            log(ERROR, msg, t);
        }
    }

    public static void wtf(Object obj) {
        if (isLoggable(ASSERT)) {
            log(ASSERT, obj);
        }
    }

    public static void wtf(String format, Object... args) {
        if (isLoggable(ASSERT)) {
            log(ASSERT, format, args);
        }
    }

    public static void wtf() {
        if (isLoggable(ASSERT)) {
            log(ASSERT, "WTF");
        }
    }

    public static boolean isLoggable(int level) {
        return !disabled && level >= getLogLevel();
    }

    private static void log(int priority, Object obj) {
        String msg;
        if (obj instanceof Throwable) {
            msg = getStacktrace((Throwable) obj);
        } else {
            msg = String.valueOf(obj);
            if (TextUtils.isEmpty(msg)) {
                msg = "\"\"";
            }
        }
        Log.println(priority, getTag(isDebug()), msg);
    }

    private static String getStacktrace(Throwable obj) {
        String msg;StringWriter sw = new StringWriter();
        obj.printStackTrace(new PrintWriter(sw));
        msg = sw.toString();
        return msg;
    }

    private static void log(int priority, String format, Throwable t) {
        try {
            String msg = format + "\n" + getStacktrace(t);
            Log.println(priority, getTag(isDebug()), msg);
        } catch (Exception e) {
            Log.e(getTag(isDebug()), "log failed", e);
        }
    }

    private static void log(int priority, String format, Object... args) {
        try {
            String msg = String.format(format, args);
            Log.println(priority, getTag(isDebug()), msg);
        } catch (Exception e) {
            Log.e(getTag(isDebug()), "log failed", e);
        }
    }

    public static void setDebug(boolean debug) {
        L.debug = debug;
    }

    public static boolean isDebug() {
        return debug;
    }

    private static int getLogLevel() {
        return isDebug() ? VERBOSE : WARN;
    }

    private static String getTag(boolean debug) {
        if (debug) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
            String c = caller.getClassName();
            String className = c.substring(c.lastIndexOf(".") + 1, c.length());
            StringBuilder buf = new StringBuilder()
                    .append("[")
                    .append(Thread.currentThread().getName())
                    .append("]")
                    .append(className)
                    .append(".")
                    .append(caller.getMethodName())
                    .append("():")
                    .append(caller.getLineNumber());
            return buf.toString();
        } else {
            if (_tag == null) {
                // Context ctx = Utils.getContext();
                // if (ctx != null) {
                //     _tag = ctx.getPackageName();
                // }
            }
            return (_tag != null) ? _tag : TAG;
        }
    }

}
