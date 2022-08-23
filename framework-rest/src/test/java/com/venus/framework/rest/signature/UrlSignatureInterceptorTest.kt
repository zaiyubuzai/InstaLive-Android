package com.venus.framework.rest.signature

import com.venus.framework.rest.HttpConstants
import com.venus.framework.rest.UrlSignatureTestHelper
import com.venus.framework.util.L
import com.venus.framework.util.Utils.encodeUrl
import com.venus.framework.util.rx.RxSchedulers
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor.Chain
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.mockito.Matchers.isA
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.stubbing.Answer

/**
 * Created by ywu on 2017/2/24.
 */
@RunWith(MockitoJUnitRunner::class)
class UrlSignatureInterceptorTest : HttpConstants {

    @Mock
    lateinit var chainMock: Chain

    @Mock
    lateinit var config: UrlSignatureConfig

    private var nowMillis: Long = 0
    private lateinit var signatureInterceptor: UrlSignatureInterceptor
    private lateinit var responseAnswer: Answer<Response>

    @Before
    fun setup() {
        L.disabled = true
        RxSchedulers.testing = true

        nowMillis = System.currentTimeMillis()
        signatureInterceptor = object : UrlSignatureInterceptor(config) {
            override fun nowMillis(): Long {
                return nowMillis
            }

            override fun signUrl(method: String, url: String, params: Map<String, *>?): String? {
                // 由于单元测试没有加载签名动态库，此处调用Java的实现
                try {
                    return UrlSignatureTestHelper.signUrl(method, url, params)
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
        }

        `when`(config.isSignatureRequired(any(HttpUrl::class.java))).thenReturn(true)

        responseAnswer = Answer { invocation ->
            Response.Builder()
                    .request(invocation.getArgument(0))
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpConstants.HTTP_STATUS_OK)
                    .message("")
                    .build()
        }
    }

    private fun getBaseUrl(path: String): String {
        return urlBuilder(path).build().toString()
    }

    private fun urlBuilder(path: String): HttpUrl.Builder {
        return HttpUrl.Builder().scheme("https").host(HOST).encodedPath(path)
    }

    private fun requestBuilder(path: String): Request.Builder {
        return Request.Builder()
                .url(urlBuilder(path)
                        .scheme("https")
                        .host(HOST)
                        .build()
                )
    }

    private fun getSignature(req: Request): String? {
        val formBody = req.body() as FormBody
        for (i in 0 until formBody.size()) {
            val name = formBody.encodedName(i)
            val value = formBody.encodedValue(i)
            if (HttpConstants.PARAM_SIGN.equals(name, ignoreCase = true)) {
                return value
            }
        }

        return null
    }

    @Test @Throws(Exception::class)
    fun interceptHttpGetWithoutParams() {
        val originReq = requestBuilder("/a/b/").build()

        `when`(chainMock.request()).thenReturn(originReq)
        `when`(chainMock.proceed(isA(Request::class.java))).thenAnswer(responseAnswer)

        val rewrittenReq = signatureInterceptor.intercept(chainMock).request()
        val signature = rewrittenReq.url().queryParameter(HttpConstants.PARAM_SIGN)

        val expectedSignature = UrlSignatureTestHelper.signUrl(
            HttpConstants.HTTP_GET,
                getBaseUrl("/a/b/"),
                mapOf(HttpConstants.PARAM_TIMESTAMP to nowMillis))
        assertEquals(expectedSignature, signature)
    }

    @Test @Throws(Exception::class)
    fun interceptHttpGetWithEmoji() {
        val url = urlBuilder("/a/b/")
                .setEncodedQueryParameter("text", encodeUrl("\uD83D\uDE00"))
                .setEncodedQueryParameter("item_id", "Y2ZBMP16mDgn3kzj")
                .setEncodedQueryParameter("msg_type", "0")
                .setEncodedQueryParameter("to_user", "dKzoVqOJPD")
                .build()
        val originReq = Request.Builder()
                .url(url)
                .build()

        `when`(chainMock.request()).thenReturn(originReq)
        `when`(chainMock.proceed(isA(Request::class.java))).thenAnswer(responseAnswer)

        val rewrittenReq = signatureInterceptor.intercept(chainMock).request()
        val signature = rewrittenReq.url().queryParameter(HttpConstants.PARAM_SIGN)

        val expectedSignature = UrlSignatureTestHelper.signUrl(
            HttpConstants.HTTP_GET,
                getBaseUrl("/a/b/"),
                mapOf(
                "text" to "\uD83D\uDE00",
                "item_id" to "Y2ZBMP16mDgn3kzj",
                "msg_type" to "0",
                "to_user" to "dKzoVqOJPD",
                HttpConstants.PARAM_TIMESTAMP to nowMillis
        ))
        assertEquals(expectedSignature, signature)
    }

    @Test @Throws(Exception::class)
    fun interceptHttpGetWithEscapedChars() {
        val url = urlBuilder("/a/b/")
                .setEncodedQueryParameter("q", encodeUrl("children&#x27;s courtyard"))
                .setEncodedQueryParameter("rf_tag", "P685")
                .setEncodedQueryParameter("lon", "-97.0100021")
                .setEncodedQueryParameter("lat", "32.6899986")
                .build()
        val originReq = Request.Builder()
                .url(url)
                .build()

        `when`(chainMock.request()).thenReturn(originReq)
        `when`(chainMock.proceed(isA(Request::class.java))).thenAnswer(responseAnswer)

        val rewrittenReq = signatureInterceptor.intercept(chainMock).request()
        val signature = rewrittenReq.url().queryParameter(HttpConstants.PARAM_SIGN)

        val expectedSignature = UrlSignatureTestHelper.signUrl(
            HttpConstants.HTTP_GET,
                getBaseUrl("/a/b/"),
                mapOf(
                "q" to "children&#x27;s courtyard",
                "rf_tag" to "P685",
                "lon" to "-97.0100021",
                "lat" to "32.6899986",
                HttpConstants.PARAM_TIMESTAMP to nowMillis
        ))
        assertEquals(expectedSignature, signature)
    }

    @Test @Throws(Exception::class)
    fun interceptHttpPostWithEmoji() {
        val originReq = requestBuilder("/a/b/")
                .post(FormBody.Builder()
                        .addEncoded("text", encodeUrl("\uD83D\uDE00"))
                        .addEncoded("item_id", "Y2ZBMP16mDgn3kzj")
                        .addEncoded("msg_type", "0")
                        .addEncoded("to_user", "dKzoVqOJPD")
                        .build()
                )
                .build()

        `when`(chainMock.request()).thenReturn(originReq)
        `when`(chainMock.proceed(isA(Request::class.java))).thenAnswer(responseAnswer)

        val rewrittenReq = signatureInterceptor.intercept(chainMock).request()
        val signature = getSignature(rewrittenReq)

        val expectedSignature = UrlSignatureTestHelper.signUrl(
            HttpConstants.HTTP_POST,
                getBaseUrl("/a/b/"),
                mapOf(
                "text" to "\uD83D\uDE00",
                "item_id" to "Y2ZBMP16mDgn3kzj",
                "msg_type" to "0",
                "to_user" to "dKzoVqOJPD",
                HttpConstants.PARAM_TIMESTAMP to nowMillis
        ))
        assertEquals(expectedSignature, signature)
    }

    companion object {
        private val HOST = "api.5milesapp.com"
    }
}
