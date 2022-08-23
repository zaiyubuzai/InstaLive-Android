package com.venus.framework.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import androidx.fragment.app.FragmentActivity;

/**
 * FIXME 与Context相关的操作应放入app module中
 * Created by ywu on 14-10-7.
 */
public final class Utils {

    private Utils() {
        // empty
    }

    // public static Context getContext() {
    //     return AbsApplication.getInstance();
    // }
//
//    public static ApplicationInfo getApplicationInfo() {
//        if (getContext() == null) {
//            return null;
//        }
//
//        try {
//            final Context context = getContext();
//            return context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e("Utils", "get applicationInfo failed", e);
//            return null;
//        }
//    }

    @Nullable
    public static PackageInfo getPackageInfo(@NonNull Context context) {
        try {
            String pkg = context.getPackageName();
            return context.getPackageManager().getPackageInfo(pkg, 0);
        } catch (Exception e) {
            Log.e("Utils", "get packageInfo failed", e);  // 此处只能使用Log，因L依赖getPackageInfo
            return null;
        }
    }

//    public static void removeOnGlobalLayoutListener(View v, OnGlobalLayoutListener listener) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            removeGlobalOnLayoutListenerOldApi(v, listener);
//        } else {
//            removeGlobalOnLayoutListenerNewApi(v, listener);
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    private static void removeGlobalOnLayoutListenerNewApi(View v, OnGlobalLayoutListener listener) {
//        if (v == null) {
//            return;
//        }
//
//        v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
//    }
//
//    @SuppressWarnings("deprecation")
//    private static void removeGlobalOnLayoutListenerOldApi(View v, OnGlobalLayoutListener listener) {
//        if (v == null) {
//            return;
//        }
//
//        v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
//    }
//
//    public static void doOnGlobalLayout(final View v, final Runnable action) {
//        if (v == null) {
//            return;
//        }
//
//        if (v.getMeasuredWidth() == 0) {
//            ViewTreeObserver observer = v.getViewTreeObserver();
//            if (observer.isAlive()) {
//                observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        invokeAction(action);
//                        removeOnGlobalLayoutListener(v, this);
//                    }
//                });
//            }
//        } else {
//            invokeAction(action);
//        }
//    }
//
//    public static void doOnLayoutChange(final View v, final Runnable action) {
//        if (v == null) {
//            return;
//        }
//
//        if (v.getMeasuredWidth() == 0) {
//            v.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    invokeAction(action);
//                    v.removeOnLayoutChangeListener(this);
//                }
//            });
//        } else {
//            invokeAction(action);
//        }
//    }
//
//    private static void invokeAction(Runnable action) {
//        try {
//            action.run();
//        } catch (Exception e) {
//            L.e(e);
//        }
//    }

    public static void setBackground(View v, Bitmap bitmap) {
        setBackground(v, bitmap, 0xff);
    }

    public static void setBackground(View v, Bitmap bitmap, int alpha) {
        v.setBackground(getBitmapDrawable(v.getResources(), bitmap, alpha));
    }

    public static void setBackground(Window v, Bitmap bitmap) {
        BitmapDrawable d = getBitmapDrawable(v.getContext().getResources(), bitmap, 0xff);
        v.setBackgroundDrawable(d);
    }

    private static BitmapDrawable getBitmapDrawable(Resources resources, Bitmap bitmap, int alpha) {
        BitmapDrawable d = new BitmapDrawable(resources, bitmap);
        if (alpha < 0xff && alpha >= 0) {
            d.setAlpha(alpha);
        }
        return d;
    }

    /**
     * Get a concise description text for the active network state, for logging.
     */
    @SuppressLint("MissingPermission")
    public static String getNetworkStateDesc(@NonNull Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) return "";

        NetworkInfo info = connMgr.getActiveNetworkInfo();
        StringBuilder sb = new StringBuilder();
        if (info == null) {
            sb.append("N/A");
        } else {
            sb.append(info.getTypeName())
                .append("|").append(info.getSubtypeName())
                .append("|").append(info.getDetailedState())
                .append("|").append(info.getReason());
        }

        return sb.toString();
    }

    /**
     * From https://github.com/navasmdc/MaterialDesignLibrary/blob/master/MaterialDesign/src/com/gc/materialdesign/utils/Utils.java
     * Convert Dp to Pixel
     */
    public static int dpToPx(float dp, Resources resources) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (dp > 0 && px <= 0) ? 1 : px;
    }

    public static boolean eq(double d1, double d2) {
        return Math.abs(d1 - d2) <= 1e-8;
    }

    public static boolean gt(Integer i1, Integer i2) {
        return i1 != null && i2 != null && i1 > i2;
    }

    public static boolean neq(double d1, double d2) {
        return Math.abs(d1 - d2) > 1e-8;
    }

    public static void setViewEnabledRecursively(View v, boolean enabled) {
        if (v == null) {
            return;
        }

        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                setViewEnabledRecursively(g.getChildAt(i), enabled);
            }
        }
    }

    public static boolean isValidEmail(String email) {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        return emailPattern.matcher(email).matches();
    }

    public static boolean isValidEmail(CharSequence emailCS) {
        if (TextUtils.isEmpty(emailCS)) return false;

        String emailStr = emailCS.toString();
        return isValidEmail(emailStr);
    }

    @NonNull
    public static String encodeUrl(String str) {
        if (isEmpty(str)) {
            return "";
        }

        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            L.e(e);
            return str;
        }
    }

    @NonNull
    public static String decodeUrl(String str) {
        if (isEmpty(str)) {
            return "";
        }

        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            L.e(e);
            return str;
        }
    }

    // public static boolean isPermissionGranted(String permission) {
    //     try {
    //         return PackageManager.PERMISSION_GRANTED ==
    //                 ContextCompat.checkSelfPermission(getContext(), permission);
    //     } catch (Exception e) {
    //         return false;
    //     }
    // }

    /**
     *
     * @param permissions
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     * @see FragmentActivity#onRequestPermissionsResult(int, String[], int[])
     */
    // public static boolean isPermissionsAllGranted(String... permissions) {
    //     if (permissions == null || permissions.length == 0) {
    //         return false;
    //     }

    //     boolean granted = true;
    //     for (String p : permissions) {
    //         granted = granted && isPermissionGranted(p);
    //     }

    //     return granted;
    // }

    // public static boolean isAnyPermissionGranted(String... permissions) {
    //     if (permissions == null || permissions.length == 0) {
    //         return true;
    //     }

    //     for (String p : permissions) {
    //         if (isPermissionGranted(p)) {
    //             return true;
    //         }
    //     }

    //     return false;
    // }

    // public static boolean isExternalStorageMounted() {
    //     return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    // }

    // public static boolean isStorageAccessGranted() {
    //     return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    // }

    /**
     * 安全判断字符串是否为空
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 是否存在任一不为空的字符串
     */
    public static boolean isNotAllEmpty(CharSequence... strs) {
        boolean result = false;

        for (CharSequence s : strs) {
            if (isNotEmpty(s)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * None of the strings is empty
     */
    public static boolean isNotEmpty(CharSequence... strs) {
        boolean result = true;

        for (CharSequence s : strs) {
            if (isEmpty(s)) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * one of params is empty
     */
    public static boolean hasEmpty(CharSequence... strs) {
        boolean result = false;

        for (CharSequence s : strs) {
            if (isEmpty(s)) {
                result = true;
                break;
            }
        }

        return result;
    }

    public static void refreshGallery(Context ctx, File imgFile) {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(imgFile);
            mediaScanIntent.setData(contentUri);
            ctx.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            L.e(e);
        }
    }


    public static String readFileContent(InputStream is) throws IOException {
        StringBuilder buf = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        try {
            String ln;
            while ((ln = reader.readLine()) != null) {
                buf.append(ln);
            }
        } finally {
            reader.close();
        }

        return buf.toString();
    }

    public static String readFileContent(File file) throws IOException {
        return readFileContent(new FileInputStream(file));
    }

    public static void writeFileContent(File file, String content) throws IOException {
        BufferedWriter writer = null;

        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writer.write(content);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * 根据当前日历的顺序给出一周7天的序列
     */
    public static int[] getWeekdays() {
        int[] days = new int[7];
        boolean mondayFirst = Calendar.MONDAY == Calendar.getInstance().getFirstDayOfWeek();

        for (int i = 0; i < 7; i++) {
            days[i] = i + (mondayFirst ? 2 : 1);
        }

        if (mondayFirst) {
            days[6] = Calendar.SUNDAY;
        }

        return days;
    }

    // public static Drawable tintIcon(Context context, @DrawableRes int icon) {
    //     Drawable drawable = context.getResources().getDrawable(icon);

    //     TypedValue typedValue = new TypedValue();
    //     TypedArray a = context.getTheme().obtainStyledAttributes(typedValue.data, new int[]{R.attr.fmIconTint});
    //     int tint = a.getColor(0, 0);
    //     if (tint != 0 && drawable != null) {
    //         //wrap一下fix pre lollipop tint设置不上的问题
    //         drawable = DrawableCompat.wrap(drawable).mutate();
    //         DrawableCompat.setTint(drawable, tint);
    //         return drawable;
    //     }

    //     return drawable;
    // }

    // public static int getDefaultIconTint(Context context) {
    //     TypedValue typedValue = new TypedValue();
    //     TypedArray a = context.getTheme().obtainStyledAttributes(typedValue.data, new int[]{R.attr.fmIconTint});
    //     return a.getColor(0, 0);
    // }

    // public static Drawable tintOptionIcon(Context context, @DrawableRes int icon) {
    //     return tintIcon(context, icon, R.attr.fmOptionIconTintNormal);
    // }

    // public static Drawable tintOptionIcon(Context context, @DrawableRes int icon, boolean isActive) {
    //     return tintIcon(context, icon, isActive ? R.attr.fmOptionIconTintActive : R.attr.fmOptionIconTintNormal);
    // }

    // public static Drawable tintLabelIcon(Context context, @DrawableRes int icon) {
    //     return tintIcon(context, icon, R.attr.fmLabelIconTintNormal);
    // }

    // public static Drawable tintLabelIcon(Context context, @DrawableRes int icon, boolean isActive) {
    //     return tintIcon(context, icon, isActive ? R.attr.fmLabelIconTintActive : R.attr.fmLabelIconTintNormal);
    // }

    // public static Drawable tintIcon(Context context,
    //                                 @DrawableRes int icon,
    //                                 @AttrRes int tintAttr) {
    //     Drawable drawable = ContextCompat.getDrawable(context, icon);

    //     TypedValue typedValue = new TypedValue();
    //     TypedArray a = context.getTheme().obtainStyledAttributes(typedValue.data, new int[]{tintAttr});
    //     int tint = a.getColor(0, 0);
    //     if (tint != 0 && drawable != null) {
    //         drawable = DrawableCompat.wrap(drawable).mutate();
    //         DrawableCompat.setTint(drawable, tint);
    //         return drawable;
    //     }

    //     return drawable;
    // }

//    public static Drawable setTint(Drawable drawable, int color) {
//        drawable = DrawableCompat.wrap(drawable).mutate();
//        DrawableCompat.setTint(drawable, color);
//        return drawable;
//    }
//
//    public static Drawable setTint(Drawable drawable,
//                                   Resources res,
//                                   @ColorRes int colorId) {
//        return setTint(drawable, ResourcesCompat.getColor(res, colorId, null));
//    }
//
//    public static Drawable setTint(Resources res,
//                                   @DrawableRes int drawableId,
//                                   @ColorRes int colorId) {
//        return setTint(ResourcesCompat.getDrawable(res, drawableId, null),
//                ResourcesCompat.getColor(res, colorId, null));
//    }

    // public static Drawable getDefaultNaviBackIcon(Context context) {
    //     final int[] attrs = {android.R.attr.homeAsUpIndicator};
    //     TypedValue typedValue = new TypedValue();
    //     TypedArray a = context.getTheme().obtainStyledAttributes(typedValue.data, attrs);
    //     Drawable icon = a.getDrawable(0);
    //     return setTint(icon, getDefaultIconTint(context));
    // }

    /**
     * Decode Bitmap from a xml drawable
     */
    @Nullable
    public static Bitmap decodeBitmap(Resources res, @DrawableRes int drawableId) {
        Drawable drawable = ResourcesCompat.getDrawable(res, drawableId, null);
        if (drawable == null) {
            return null;
        }

        Bitmap bm = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bm;
    }

    @NonNull
    public static String renderText(String template, Map<String, ? extends CharSequence> data) {
        if (isEmpty(template)) {
            return "";
        }

        String result = template;
        for (Map.Entry<String, ? extends CharSequence> entry : data.entrySet()) {
            String k = entry.getKey();
            if (!k.matches("^\\\\\\{.*\\\\\\}")) {  // 需要包裹在{}中间
                k = "\\{" + k + "\\}";
            }
            String v = isEmpty(entry.getValue()) ? "" : entry.getValue().toString();
            v = v.replaceAll("\\$", "\\\\\\$");  // 避免误将货币符号$当做关键字
            result = result.replaceAll(k, v);
        }
        return result.replaceAll("\\{[0-9a-zA-Z_]+\\}", "");  // 避免留下未替换的变量
    }
    /**
     * SEO的需要,将分享的title编码后作为url的一部分
     */
    public static String normalizeTitle(@NonNull String title) {
        return normalizeTitle(title, Locale.getDefault());
    }

    /**
     * SEO的需要,将分享的title编码后作为url的一部分
     */
    public static String normalizeTitle(@NonNull String title,
                                        @NonNull Locale locale) {
        return title.trim()
                .toLowerCase(locale)
                .replaceAll("[,\\.'\"%&/-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
    }

    /**
     * 把格式化后的title还原
     */
    public static String denormalizeTitle(@NonNull String normalizedTitle) {
        return normalizedTitle.trim().replaceAll("[-+]", " ").replaceAll("\\s{2,}", " ");
    }

    public static String lowerCase(@Nullable final String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase(Locale.getDefault());
    }

    public static String lowerCase(@Nullable final String str, final Locale locale) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase(locale);
    }
}
