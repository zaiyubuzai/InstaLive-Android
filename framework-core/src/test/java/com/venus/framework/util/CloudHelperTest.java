package com.venus.framework.util;

import org.junit.Test;

import java.util.regex.Pattern;

import static com.venus.framework.util.CloudHelper.OPT_PARAMS;
import static org.junit.Assert.assertEquals;

import com.venus.framework.util.ImageUrlHelper;

/**
 * Created by ywu on 2017/3/1.
 */
public class CloudHelperTest {

    @Test
    public void testRegexp() {
        String originUrl = "http://res.cloudinary.com/fivemiles/image/upload/v1/p1.jpg";
        String url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_100,f_webp/$2");
        assertEquals("http://res.cloudinary.com/fivemiles/image/upload/w_100,f_webp/v1/p1.jpg", url);

        // without version
        originUrl = "http://res.cloudinary.com/fivemiles/image/upload/p1.jpg";
        url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_100,f_webp/$2");
        assertEquals("http://res.cloudinary.com/fivemiles/image/upload/w_100,f_webp/p1.jpg", url);

        // without type & version
        originUrl = "http://res.cloudinary.com/fivemiles/image/p1.jpg";
        url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_100,f_webp/$2");
        assertEquals("http://res.cloudinary.com/fivemiles/image/w_100,f_webp/p1.jpg", url);
    }

    @Test
    public void testRegexpWithImageInFolder() {
        String originUrl = "http://res.5milesapp.com/image/upload/v1542462314/bid_test/fjwjybxbhvdyhis2.jpg";
        String url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_100,f_webp/$2");
        assertEquals("http://res.5milesapp.com/image/upload/w_100,f_webp/v1542462314/bid_test/fjwjybxbhvdyhis2.jpg", url);
    }

    @Test
    public void testRegexpReplacingTransformation() {
        String originUrl = "http://host/fm/image/upload/c_auto:g,w_0.25,fl_lossy,q_75/v1/p_1.jpg";
        String url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50,f_webp/$2");
        assertEquals("http://host/fm/image/upload/w_50,f_webp/v1/p_1.jpg", url);

        // without version
        originUrl = "http://host/fm/image/upload/c_auto:g,w_100,fl_lossy,q_75/p1.jpg";
        url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50,f_webp/$2");
        assertEquals("http://host/fm/image/upload/w_50,f_webp/p1.jpg", url);

        // without type & version
        originUrl = "http://host/fm/image/w100,fl_lossy,q_75/p1.jpg";
        url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50,f_webp/$2");
        assertEquals("http://host/fm/image/w_50,f_webp/p1.jpg", url);
    }

    @Test
    public void testRegexpReplacingMultipleTransformations() {
        String originUrl = "http://host/fm/image/upload/w_0.25,h_1,q_75/l_brown_sheep,f_auto/l_text:roboto_25_bold:Hello World,y_155/e_shadow/v1/p_1.jpg";
        String url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50,f_webp/$2");
        assertEquals("http://host/fm/image/upload/w_50,f_webp/v1/p_1.jpg", url);
    }

    @Test
    public void testRegexpWithSocialNetwork() {
        String originUrl = "http://host/fm/image/facebook/1.jpg";
        String url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50,f_webp/$2");
        assertEquals("http://host/fm/image/facebook/w_50,f_webp/1.jpg", url);

        // with transformation
        originUrl = "http://host/fm/image/instagram_name/r_max,h_2/1.jpg";
        url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50,f_webp/$2");
        assertEquals("http://host/fm/image/instagram_name/w_50,f_webp/1.jpg", url);

        originUrl = "http://host/fm/image/facebook/f_wdp,t_a50/w_1080,f_webp,fl_lossy,q_75,c_fill/w_300,f_webp,fl_lossy,q_75,c_fill/10822.jpg";
        url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50," + OPT_PARAMS + ",c_fill/$2");
        assertEquals("http://host/fm/image/facebook/w_50,f_webp,fl_lossy,q_75,c_fill/10822.jpg", url);
    }


    @Test
    public void testRegexpUnknownUrlTransformation() {
        String originUrl = "http://host/xyz.txt";
        String url = Pattern.compile(ImageUrlHelper.getCloudImageUrlPattern())
                .matcher(originUrl)
                .replaceAll("$1w_50,f_webp/$2");
        assertEquals("http://host/xyz.txt", url);
    }
}
