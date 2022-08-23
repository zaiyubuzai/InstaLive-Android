package com.thirdrock.framework.rest

import android.content.Context
import android.util.Base64
import androidx.test.InstrumentationRegistry
import com.venus.framework.rest.RequestParams
import com.venus.framework.rest.UrlSignature
import junit.framework.TestCase
import org.json.JSONObject
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.Formatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Created by ywu on 15/12/28.
 */
class UrlSignatureTest : TestCase() {

    private lateinit var context: Context

    override fun setUp() {
        super.setUp()
        context = InstrumentationRegistry.getContext()
        UrlSignature.initAsync(context)
    }

    // region Signature for REST api requests testing

    /**
     * 一般情况测试
     */
    @Throws(Exception::class)
    fun testSignUrl() {
        val params = RequestParams()
                .put("zy", 90)
                .put("bb", "oh yeah")

        //        String signature = UrlSignature.signUrl("GET", "https://a.c/d/?x=9 0&name=呵:x", params);
        //        String msg = "GET&https://a.c/d/&bb%3Doh%2520yeah%26name%3D%25E5%2591%25B5:x%26x%3D9%25200%26zy%3D90" + SECRET;
        //        assertEquals(sign(msg), signature);
        //
        //        signature = UrlSignature.signUrl("GET", "https://api.5milesapp.com/api/v2/home/?limit=50&lon=-96.76&lat=32.79", null);
        //        msg = "GET&https://api.5milesapp.com/api/v2/home/&lat%3D32.79%26limit%3D50%26lon%3D-96.76" + SECRET;
        //        assertEquals(sign(msg), signature);

        // encoding disabled
        var signature = UrlSignature.getInstance().signUrl(context, "GET", "https://a.c/d/?x=9 0&name=呵:x", params, null)
        var msg = "GET&https://a.c/d/&bb=oh yeah&name=呵:x&x=9 0&zy=90" + SECRET
        assertEquals(sign(msg), signature)

        //        {
        //            u 'city':[u 'Hacienda Heights'],u 'country':[u 'United States'],u 'region':[u 'CA'],u
        //            'lon':[u '-117.98319244384766'],u 'zipcode':[u ''],u 'ts':[u '1468480884076'],
        //            u 'lat':[u '34.01771926879883'],u 'sign':[u '0a0dc21aadddc600b6f262d287402e90d3d82477']}

        params.clear()
        params.put("city", "Hacienda Heights")
                .put("country", "United States")
                .put("region", "CA")
                .put("lon", -117.98319244384766)
                .put("lat", 34.01771926879883)
                .put("zipcode", "")
                .put("ts", 1468480884076L)
        signature = UrlSignature.getInstance().signUrl(context, "POST", "https://api.5milesapp.com/api/v2/update_location/", params, null)
        msg = "POST&https://api.5milesapp.com/api/v2/update_location/&city=Hacienda Heights&country=United States&lat=34.01771926879883&lon=-117.98319244384766&region=CA&ts=1468480884076&zipcode=" + SECRET
        assertEquals(sign(msg), signature)
    }

    @Throws(Exception::class)
    fun testSignUrlWithEscapedChar() {
        val params = RequestParams()
        val signature: String?
        val msg: String

        // ?q=children&#x27;s courtyard&lon=-97.0100021&lat=32.6899986&refind=0&ts=1471963619519&rf_tag=P685&sign=53471035f4d07c79e53b32bb5eb17aeb7f0c4041
        // encoded q=children%26%23x27;s%20courtyard
        // should be: "children's courtyard"
        params.clear()
        params.put("q", "children&#x27;s courtyard")
                .put("rf_tag", "P685")
                .put("lon", "-97.0100021")
                .put("lat", "32.6899986")
                .put("ts", 1471963619519L)
        signature = UrlSignature.getInstance().signUrl(context, "GET", "https://api.5milesapp.com/api/v2/search_new/", params, null)
        msg = "GET&https://api.5milesapp.com/api/v2/search_new/&lat=32.6899986&lon=-97.0100021&q=children&#x27;s courtyard&rf_tag=P685&ts=1471963619519" + SECRET
        assertEquals(sign(msg), signature)
    }

    /**
     * 以url作为参数值的情况
     */
    @Throws(Exception::class)
    fun testSignUrlWithUrlParam() {
        val params = RequestParams()
                .put("item_id", "w147oqPapkr8Q6M0")
                .put("price", 8.0)
                .put("to_user", "qGErAZxRg0")
                .put("lon", -97.05)
                .put("lat", 32.79)
                .put("appointed_at", 1452893017)
                .put("place_address", "2214 Paddock Way Dr # 900, Grand Prairie, TX 75050, United States")
                .put("place_name", "Pharmerica Long-Term Care")
                .put("address_map_thumb", "https://maps.googleapis.com/maps/api/staticmap?center=32.789591,-97.054729&zoom=16&size=240x180&markers=32.789591,-97.054729&scale=2")

        //        String signature = UrlSignature.getInstance().signUrl("POST", "https://api-test.5milesapp.com/api/v2/appointment/create/", params);
        //        String msg = "POST&https://api-test.5milesapp.com/api/v2/appointment/create/&address_map_thumb%3Dhttps://maps.googleapis.com/maps/api/staticmap%253Fcenter%253D32.789591%252C-97.054729%2526zoom%253D16%2526size%253D240x180%2526markers%253D32.789591%252C-97.054729%2526scale%253D2%26appointed_at%3D1452893017%26item_id%3Dw147oqPapkr8Q6M0%26lat%3D32.79%26lon%3D-97.05%26place_address%3D2214%2520Paddock%2520Way%2520Dr%2520%2523%2520900%252C%2520Grand%2520Prairie%252C%2520TX%252075050%252C%2520United%2520States%26place_name%3DPharmerica%2520Long-Term%2520Care%26price%3D8.0%26to_user%3DqGErAZxRg0" + SECRET;
        //        assertEquals(sign(msg), signature);

        // encoding disabled
        val signature = UrlSignature.getInstance().signUrl(context, "POST", "https://api-test.5milesapp.com/api/v2/appointment/create/", params, null)
        val msg = "POST&https://api-test.5milesapp.com/api/v2/appointment/create/&address_map_thumb=https://maps.googleapis.com/maps/api/staticmap?center=32.789591,-97.054729&zoom=16&size=240x180&markers=32.789591,-97.054729&scale=2&appointed_at=1452893017&item_id=w147oqPapkr8Q6M0&lat=32.79&lon=-97.05&place_address=2214 Paddock Way Dr # 900, Grand Prairie, TX 75050, United States&place_name=Pharmerica Long-Term Care&price=8.0&to_user=qGErAZxRg0" + SECRET
        assertEquals(sign(msg), signature)
    }

    /**
     * 包含保留字符的情况
     */
    @Throws(Exception::class)
    fun testSignUrlWithUnescapsedChars() {
        var signature = UrlSignature.getInstance().signUrl(context, "GET", "http://testserver/api/v2/appointment/list/a b-c_d.e~f*g", null, null)
        //        String msg = "GET&http://testserver/api/v2/appointment/list/a%20b-c_d.e~f*g&" + SECRET;
        //        assertEquals(sign(msg), signature);

        // encoding disabled
        var msg = "GET&http://testserver/api/v2/appointment/list/a b-c_d.e~f*g&" + SECRET
        assertEquals(sign(msg), signature)

        val params = RequestParams()
                .put("share_type", "item")
                .put("\$og_url", "https://test-www.5milesapp.com/item/236ZlJGMnpPp5MRG/b-construction-fulltime")
                .put("\$desktop_url", "https://test-www.5milesapp.com/item/236ZlJGMnpPp5MRG/b-construction-fulltime")
                .put("event_id", "236ZlJGMnpPp5MRG")
                .put("user_id", "dKzoVqOJPD")
                .put("rf_tag", "NH_0_1-_-")
                .put("ts", "1534243659032")

        // expected: 52cb6e747f737db4582fb1a94de7da463178eee5
        signature = UrlSignature.getInstance().signUrl(context, "POST", "https://api-test.5milesapp.com/api/v2/deep_link/", params, null)

        // encoding disabled
        msg = "POST&https://api-test.5milesapp.com/api/v2/deep_link/&\$desktop_url=https://test-www.5milesapp.com/item/236ZlJGMnpPp5MRG/b-construction-fulltime&\$og_url=https://test-www.5milesapp.com/item/236ZlJGMnpPp5MRG/b-construction-fulltime&event_id=236ZlJGMnpPp5MRG&rf_tag=NH_0_1-_-&share_type=item&ts=1534243659032&user_id=dKzoVqOJPD"
        msg += SECRET
        assertEquals("52cb6e747f737db4582fb1a94de7da463178eee5", sign(msg))
        assertEquals("52cb6e747f737db4582fb1a94de7da463178eee5", signature)
    }

    /**
     * 包含emoji的情况
     */
    @Throws(Exception::class)
    fun testSignUrlWithEmoji() {
        val params = RequestParams()
                .put("text", "\uD83D\uDE00")
                .put("item_id", "Y2ZBMP16mDgn3kzj")
                .put("ts", 1468902936598L)
                .put("to_user", "dKzoVqOJPD")
                .put("msg_type", 0)
        val signature = UrlSignature.getInstance().signUrl(context, "POST", "https://api-test.5milesapp.com/api/v2/make_offer/", params, null)
        assertEquals("9765a4e91a90ea9ea6e41964abcee8e8e494d90a", signature)
    }

    /**
     * 不包含任何参数的情况
     */
    @Throws(Exception::class)
    fun testSignUrlWithoutParams() {
        val signature = UrlSignature.getInstance().signUrl(context, "GET", "https://a.c/d/", null, null)
        val msg = "GET&https://a.c/d/&" + SECRET
        assertEquals(sign(msg), signature)
    }

    /**
     * url中包含空的query参数的情况
     */
    @Throws(Exception::class)
    fun testSignUrlWithEmptyQueryParam() {
        val signature = UrlSignature.getInstance().signUrl(context, "GET", "https://a.c/d/?x=&name=呵x", null, null)
        //        String msg = "GET&https://a.c/d/&name%3D%25E5%2591%25B5x%26x%3D" + SECRET;
        //        assertEquals(sign(msg), signature);

        // encoding disabled
        val msg = "GET&https://a.c/d/&name=呵x&x=" + SECRET
        assertEquals(sign(msg), signature)
    }
    // endregion

    // region Ajax request signature testing

    /**
     * Web签名一般情况测试
     */
    @Throws(Exception::class)
    fun testSignWebUrl() {
        val data = JSONObject()
        data.put("zy", 90)
        data.put("bb", "呵:x")

        val signature = UrlSignature.getInstance().signWebUrl(context, "GET", "https://a.c/d/", data)
        val msg = "GET&https://a.c/d/&bb=呵:x&zy=90" + WEB_SECRET
        assertEquals(signWeb(msg), signature)
    }

    /**
     * Web签名测试, json body的post请求
     */
    @Throws(Exception::class)
    fun testSignWebUrlWithBody() {
        val body = JSONObject("{\n" +
                "  \"d\": 1,\n" +
                "  \"a\": \"test\",\n" +
                "  \"data\": {\n" +
                "    \"aa\": 20\n" +
                "  }\n" +
                "}")
        val data = JSONObject().put("body", body.toString())

        val signature = UrlSignature.getInstance().signWebUrl(context, "POST", "https://a.c/d/", data)
        val msg = "POST&https://a.c/d/&body=" + body.toString() + WEB_SECRET
        assertEquals(signWeb(msg), signature)
    }

    /**
     * Web签名测试, 包含emoji的情况
     */
    @Throws(Exception::class)
    fun testSignWebUrlWithEmoji() {
        val body = JSONObject("{\n" +
                "  \"text\": \"\uD83D\uDE00\",\n" +
                "  \"item_id\": \"Y2ZBMP16mDgn3kzj\",\n" +
                "  \"ts\": 1468902936598,\n" +
                "  \"to_user\": \"dKzoVqOJPD\",\n" +
                "  \"msg_type\": 0\n" +
                "}")
        val data = JSONObject().put("body", body.toString())

        val signature = UrlSignature.getInstance().signWebUrl(context, "POST", "https://m.5milesapp.com/offer/", data)
        val msg = "POST&https://m.5milesapp.com/offer/&body=" + body.toString() + WEB_SECRET
        assertEquals(signWeb(msg), signature)
    }

    /**
     * Web签名测试, 不包含任何参数的情况
     */
    @Throws(Exception::class)
    fun testSignWebUrlWithoutParams() {
        val signature = UrlSignature.getInstance().signWebUrl(context, "GET", "https://a.c/d/", null)
        val msg = "GET&https://a.c/d/&" + WEB_SECRET
        assertEquals(signWeb(msg), signature)
    }

    /**
     * Web签名测试, url中包含空的query参数的情况
     */
    @Throws(Exception::class)
    fun testSignWebUrlWithEmptyParam() {
        val data = JSONObject()
        data.put("zy", 90)
        data.put("bb", null)
        data.put("cc", "")

        val signature = UrlSignature.getInstance().signWebUrl(context, "GET", "https://a.c/d/", data)
        val msg = "GET&https://a.c/d/&cc=&zy=90" + WEB_SECRET
        assertEquals(signWeb(msg), signature)
    }
    // endregion

    companion object {

        private const val SECRET = "&Ad\$iOI34HNlK"
        private const val KEY = "Ad\$iOI34HNlK"
        private const val WEB_SECRET = "&76CP6j7*y8bm"
        private const val WEB_KEY = "76CP6j7*y8bm"
        private const val HMAC_SHA1_ALGORITHM = "HmacSHA1"

        // region signature verification helpers
        @Throws(Exception::class)
        private fun sign(msg: String): String {
            return calcHmacSha1Hex(msg, false)
        }

        @Throws(Exception::class)
        private fun signWeb(msg: String): String {
            return calcHmacSha1Hex(msg, true)
        }

        @Throws(Exception::class)
        private fun calcHmacSha1Hex(msg: String, isWeb: Boolean): String {
            return toHexString(calcHmacSha1(msg, isWeb))
        }

        @Throws(Exception::class)
        private fun calcHmacSha1Base64(msg: String): String {
            val rawHmac = calcHmacSha1(msg, false)
            return String(Base64.encode(rawHmac, Base64.DEFAULT))
        }

        @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
        private fun calcHmacSha1(msg: String, isWeb: Boolean): ByteArray {
            val key = if (isWeb) WEB_KEY else KEY
            val signingKey = SecretKeySpec(key.toByteArray(), HMAC_SHA1_ALGORITHM)
            val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
            mac.init(signingKey)
            return mac.doFinal(msg.toByteArray())
        }

        private fun toHexString(bytes: ByteArray): String {
            val formatter = Formatter()

            for (b in bytes) {
                formatter.format("%02x", b)
            }

            return formatter.toString()
        }
        // endregion
    }
}
