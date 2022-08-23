package com.venus.framework.util;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.TextUtils;

/**
 * Helper class for cloud image process
 * Created by ywu on 14-8-17.
 * @see <a href="https://github.com/3rdStone/5miles-apps/wiki">Transformation规范</a>
 */
public final class CloudHelper {

    public static final String IMG_CLOUD_PREFIX = "http://res.5milesapp.com/image/";

    // optimal http://cloudinary.com/blog/transparent_webp_format_cdn_delivery_based_on_visitors_browsers
    public static final String PARAM_OPT_AUTO_WEBP = "f_webp";
    public static final String PARAM_OPT_PNG_TO_JPG = "fl_lossy";
    public static final String PARAM_OPT_QUALITY_75 = "q_75";
    public static final String OPT_PARAMS = PARAM_OPT_AUTO_WEBP + "," + PARAM_OPT_PNG_TO_JPG + "," + PARAM_OPT_QUALITY_75;

    public static final String DEFAULT_CROP = "fill";

    // 图片按宽度分为以下几档，减少cloudinary上的图片副本
    public static final int[] IMG_WIDTHS = {
            100,
            200,
            300,
            400,
            650,
            800,
            1080,
    };

    private CloudHelper() {}

    public static int normalizeImageWidth(int width) {
        int result = IMG_WIDTHS[IMG_WIDTHS.length - 1];

        for (int w : IMG_WIDTHS) {
            if (width <= w) {
                result = w;
                break;
            }
        }

        return result;
    }

    /**
     * 转换原始Cloudinary图片url，使用crop操作，以便得到合适尺寸的图片
     * @param originUrl 原始url
     * @param width 期望宽度
     * @param height 期望高度，可为null，根据宽度计算，保持长宽比
     * @param crop 裁剪模式，可为null，默认 'fill'
     * @return result image url
     * @see {http://cloudinary.com/documentation/image_transformations#crop_modes}
     */
    private static String toCropUrl(String originUrl,
                                    int width,
                                    @Nullable Integer height,
                                    @Nullable String crop) {
        if (originUrl == null || "null".equalsIgnoreCase(originUrl) || width <= 0) {
            return null;
        }

        int originWidth = width;
        Integer originHeight = height;

        if (height == null || height <= 0) {
            height = null;
        }

        width = normalizeImageWidth(width);

        if (height != null) {
            height = Math.round(originHeight * width / (float)originWidth);
        }

        String params = "w_" + width;
        if (height != null) {
            params += ",h_" + height;
        }
        params += "," + OPT_PARAMS + "," + getCropMode(crop);

        String url = ImageUrlHelper.transformCloudImageUrl(originUrl, params);
        L.v("loading cloud image w:%d -> %s", originWidth, url);
        return url;
    }

    /**
     * 转换原始Cloudinary图片url，使用crop操作，以便得到合适尺寸的图片
     *
     * @param originUrl 原始url
     * @param width     期望宽度
     * @return result image url
     * @see {http://cloudinary.com/documentation/image_transformations#crop_modes}
     */
    public static String toCropUrl(String originUrl, int width) {
        return toCropUrl(originUrl, width, null);
    }

    /**
     * 转换原始Cloudinary图片url，使用crop操作，以便得到合适尺寸的图片
     *
     * @param originUrl 原始url
     * @param width     期望宽度
     * @param crop      裁剪模式，可为null，默认 'fill'
     * @return result image url
     * @see {http://cloudinary.com/documentation/image_transformations#crop_modes}
     */
    public static String toCropUrl(String originUrl,
                                   int width,
                                   @Nullable String crop) {
        return toCropUrl(originUrl, width, null, crop);
    }

    /**
     * 转换原始Cloudinary图片url，使用crop操作，以便得到合适尺寸的图片
     * @param originUrl 原始url
     * @param width 期望宽度, in dps
     * @param crop 裁剪模式，可为null，默认 'pad'
     * @return result image url
     * @see {http://cloudinary.com/documentation/image_transformations#crop_modes}
     */
    public static String toCropUrlInPids(Context context,
                                         String originUrl,
                                         float width,
                                         @Nullable String crop) {
        // converts width/height to pixels
        int widthInPixels = Utils.dpToPx(width, context.getResources());
//        Integer heightInPixels = height != null ?
//                DisplayUtils.toPixels(context,height) : null;
        return toCropUrl(originUrl, widthInPixels, null, crop);
    }

    private static String getCropMode(String crop) {
        if (TextUtils.isEmpty(crop)) {
            crop = DEFAULT_CROP;
        }

        return "c_" + crop;
    }
}
