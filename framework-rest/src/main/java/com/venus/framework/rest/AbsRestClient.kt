package com.venus.framework.rest

/* ktlint-disable no-wildcard-imports */
import androidx.annotation.WorkerThread
import com.venus.framework.exception.RequestCancelledException
import com.venus.framework.rest.HttpConstants.*
import com.venus.framework.rest.util.cancelTag
import com.venus.framework.util.L
import com.venus.framework.util.Utils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.net.MalformedURLException
import java.net.URL

/**
 * Base repository for accessing backend data through network
 * Created by ywu on 14-9-27.
 */
@WorkerThread
abstract class AbsRestClient(
    private val httpClient: OkHttpClient,
    val requestHelper: RequestHelper,
    val bodyParserFactory: HttpBodyParserFactory
) : HttpConstants {

    /**
     * 如果已经是完整URL,则直接返回.
     * update: 如果是绝对地址,则直接返回.
     */
    protected fun toAbsoluteUrl(path: String) = try {
        URL(toAbsoluteUrlAsString(path))
    } catch (e: MalformedURLException) {
        throw IllegalArgumentException(e)
    }

    protected fun toAbsoluteUrlAsString(path: String): String {
        if (path.startsWith("http")) return path // 指定的完整url不作修改

        var base = requestHelper.baseUrl
        if (!base.endsWith("/") && !path.startsWith("/")) base += '/'

        return base + path
    }

    // 目前每次重新构建header，避免注销后无法清除session信息
    protected open fun initHeaders(requestBuilder: Request.Builder): Request.Builder =
            requestBuilder.headers(Headers.of(requestHelper.buildHeaders()))

    protected fun makeRequestBuilder(path: String): Request.Builder =
            initHeaders(Request.Builder().url(toAbsoluteUrl(path)))

    protected fun makeRequestBuilder(url: HttpUrl): Request.Builder =
            initHeaders(Request.Builder().url(url))

    protected open fun makeHttpGetRequest(path: String, params: Map<String, *>?): Request {
        L.d("rftag: %s, path: %s", requestHelper.rfTag, path)
        val requestTag = "${path}_$params"
        val urlBuilder = HttpUrl.parse(toAbsoluteUrlAsString(path))!!.newBuilder() // 暂时无法很好的处理parse为null的情况

        // TODO 禁止在path中包含query
        if (!path.startsWith("http") && path.contains("?")) {
            L.w("DO NOT concat query string in path, use params instead: %s", path)
        }

        // 自己编码,避免OkHttp编码方法标准不一致导致的问题
        params?.forEach { (name, value) -> if (value != null)
            urlBuilder.setEncodedQueryParameter(name, encode(value))
        }

        val rfTag = requestHelper.rfTag
        if (!rfTag.isNullOrEmpty()) {
            urlBuilder.setEncodedQueryParameter(PARAM_RF_TAG, rfTag)
        }

        return makeRequestBuilder(urlBuilder.build()).tag(requestTag).build()
    }

    // 自己编码,避免OkHttp编码方法标准不一致导致的问题
    private fun encode(value: Any?): String =
            if (value == null) "" else Utils.encodeUrl(value.toString())

    protected fun makeHttpPostRequest(path: String, params: Map<String, *>?): Request {
        val formBuilder = FormBody.Builder()

        // 自己编码,避免OkHttp编码方法标准不一致导致的问题
        params?.forEach { (name, value) -> if (value != null)
            formBuilder.addEncoded(name, encode(value))
        }

        val rfTag = requestHelper.rfTag
        if (!rfTag.isNullOrEmpty()) {
            formBuilder.addEncoded(PARAM_RF_TAG, rfTag)
        }

        val body = formBuilder.build()
        return makeRequestBuilder(path).post(body).build()
    }

    // TODO 借助jsonHelper实现通用接口
    protected fun makeJsonPost(path: String, json: String): Request =
            makeRequestBuilder(path)
                    .post(RequestBody.create(CONTENT_TYPE_JSON, json))
                    .build()

    protected fun makeMultipartRequest(
        path: String,
        filename: String,
        outputStream: ByteArrayOutputStream
    ): Request {
        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(filename,
                        filename,
                        RequestBody.create(CONTENT_TYPE_MULTIPART_FORM, outputStream.toByteArray()))
                .addFormDataPart(PARAM_TIMESTAMP, System.currentTimeMillis().toString())
                .build()

        return makeRequestBuilder(path).post(requestBody).build()
    }

    protected fun makeMultipartRequest(
        path: String,
        filename: String,
        bytes: ByteArray,
        params: Map<String, *>?
    ): Request {
        val formBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(filename,
                        filename,
                        RequestBody.create(CONTENT_TYPE_MULTIPART_FORM, bytes))

        params?.forEach { (name, value) -> if (value != null)
            formBuilder.addFormDataPart(name, value.toString())
        }

        val theParams = params?.toMutableMap() ?: mutableMapOf()
        if (PARAM_TIMESTAMP !in theParams) {
            // 增加ts参数,用于url签名
            val ts = System.currentTimeMillis()
            formBuilder.addFormDataPart(PARAM_TIMESTAMP, ts.toString())
            theParams[PARAM_TIMESTAMP] = ts
        }

        if (requestHelper.isUrlSignatureEnabled) {
            val signature = UrlSignature.getInstance().signUrl(requestHelper.getContext(),
                    HTTP_POST, toAbsoluteUrl(path).toString(), theParams, null)
            if (!signature.isNullOrEmpty()) formBuilder.addFormDataPart(PARAM_SIGN, signature!!) // signature is not null here
        }

        return makeRequestBuilder(path).post(formBuilder.build()).build()
    }

    @JvmOverloads protected fun <T> get(
        url: String,
        params: Map<String, *>? = null,
        respParser: HttpBodyParser<T>
    ): Observable<T> = Observable.fromCallable {
        handleRequest(makeHttpGetRequest(url, params), respParser)
    }

    @JvmOverloads public fun <T> getSingle(
        url: String,
        params: Map<String, *>? = null,
        type: Class<T>
    ): Single<T> = getSingle(url, params, bodyParserFactory.createParser(type))

    @JvmOverloads protected fun <T> getSingle(
        url: String,
        params: Map<String, *>? = null,
        type: Type
    ): Single<T> = getSingle(url, params, bodyParserFactory.createParser(type))

    @JvmOverloads protected fun get(
        url: String,
        params: Map<String, *>? = null
    ): Completable = getSingle(url, params, Unit::class.java).toCompletable()

    @JvmOverloads fun <T> get(
        url: String,
        params: Map<String, *>? = null,
        type: Class<T>
    ): Observable<T> {
        val parser = bodyParserFactory.createParser(type)
        return get(url, params, parser)
    }

    @JvmOverloads fun <T> get(
        url: String,
        params: Map<String, *>? = null,
        type: Type
    ): Observable<T> {
        val parser = bodyParserFactory.createParser<T>(type)
        return get(url, params, parser)
    }

    fun <T> get(request: Request, type: Class<T>): Observable<T> {
        val parser = bodyParserFactory.createParser(type)
        return Observable.fromCallable { handleRequest(request, parser) }
    }

    protected fun <T> getSingle(request: Request, type: Class<T>): Single<T> =
        Single.fromCallable {
            handleRequest(request, bodyParserFactory.createParser(type))
        }

    @JvmOverloads protected fun <T> getSingle(
        url: String,
        params: Map<String, *>? = null,
        respParser: HttpBodyParser<T>
    ): Single<T> = Single.fromCallable {
        handleRequest(makeHttpGetRequest(url, params), respParser)
    }

    @JvmOverloads public fun <T> postSingle(
        url: String,
        params: Map<String, *>? = null,
        type: Class<T>
    ): Single<T> = postSingle(url, params, bodyParserFactory.createParser(type))

    @JvmOverloads protected fun <T> postSingle(
        url: String,
        params: Map<String, *>? = null,
        respParser: HttpBodyParser<T>
    ): Single<T> = Single.fromCallable {
        handleRequest(makeHttpPostRequest(url, params), respParser)
    }

    @JvmOverloads protected fun <T> post(
        url: String,
        params: Map<String, *>? = null,
        respParser: HttpBodyParser<T>
    ): Observable<T> = Observable.fromCallable {
        handleRequest(makeHttpPostRequest(url, params), respParser)
    }

    @JvmOverloads fun <T> post(
        url: String,
        params: Map<String, *>? = null,
        type: Class<T>
    ): Observable<T> {
        val parser = bodyParserFactory.createParser(type)
        return post(url, params, parser)
    }

    @JvmOverloads fun <T> post(
        url: String,
        params: Map<String, *>? = null,
        type: Type
    ): Observable<T> {
        val parser = bodyParserFactory.createParser<T>(type)
        return post(url, params, parser)
    }

    @JvmOverloads protected fun post(
        url: String,
        params: Map<String, *>? = null
    ): Completable = postSingle(url, params, Unit::class.java).toCompletable()

    protected fun postMultipart(
        url: String,
        filename: String,
        stream: ByteArrayOutputStream
    ): Completable {
        val parser = bodyParserFactory.createParser(Unit::class.java)
        return postMultipartSingle(url, filename, stream, parser).toCompletable()
    }

    protected fun <T> postMultipart(
        url: String,
        filename: String,
        stream: ByteArrayOutputStream,
        type: Class<T>
    ): Observable<T> {
        val parser = bodyParserFactory.createParser(type)
        return postMultipart(url, filename, stream, parser)
    }

    protected fun <T> postMultipart(
        url: String,
        filename: String,
        bytes: ByteArray,
        params: Map<String, *>?,
        type: Class<T>
    ): Observable<T> {
        val parser = bodyParserFactory.createParser(type)
        return Observable.fromCallable { handleRequest(makeMultipartRequest(url, filename, bytes, params), parser) }
    }

    protected fun <T> postMultipart(
        url: String,
        filename: String,
        stream: ByteArrayOutputStream,
        respParser: HttpBodyParser<T>
    ): Observable<T> {
        return Observable.fromCallable { handleRequest(makeMultipartRequest(url, filename, stream), respParser) }
    }

    protected fun <T> postMultipartSingle(
        url: String,
        filename: String,
        stream: ByteArrayOutputStream,
        respParser: HttpBodyParser<T>
    ): Single<T> = Single.fromCallable {
        handleRequest(makeMultipartRequest(url, filename, stream), respParser)
    }

    /**
     * TODO 借助jsonHelper实现通用接口
     */
    protected fun <T> postJson(
        url: String,
        json: String,
        type: Class<T>
    ): Observable<T> {
        val parser = bodyParserFactory.createParser(type)
        return Observable.fromCallable { handleRequest(makeJsonPost(url, json), parser) }
    }

    /**
     * TODO 借助jsonHelper实现通用接口
     */
    protected fun <T> postJson(
        url: String,
        json: String,
        respParser: HttpBodyParser<T>
    ): Observable<T> =
        Observable.fromCallable { handleRequest(makeJsonPost(url, json), respParser) }

    protected fun postJson(url: String, json: String): Completable =
            Single.fromCallable {
                handleRequest(makeJsonPost(url, json),
                        bodyParserFactory.createParser(Unit::class.java))
            }.toCompletable()

    @WorkerThread @Throws(Exception::class)
    private fun <T> handleRequest(request: Request, respParser: HttpBodyParser<T>): T {
        val startedAt = System.currentTimeMillis()
        var respBody: ResponseBody? = null
        var call: Call? = null
        try {
            L.v("http %s <%s>", request.method(), request.url())
            val tag = request.tag()
            if (tag != null) {
                // 取消掉队列中的相同请求
                httpClient.cancelTag(tag)
            }

            call = httpClient.newCall(request)
            val resp = call!!.execute()
            respBody = resp.body()
            val bodyText = if (respBody != null) respBody.string() else ""

            if (respParser.isSuccessful(resp)) {
                return respParser.parse(resp, bodyText)
            }

            val endedAt = System.currentTimeMillis()
            L.v("http response: [%dms] [%d] %s", endedAt - startedAt, resp.code(), bodyText)
            emitDiagnosis(resp)
            throw respParser.parseError(resp, bodyText)
        } catch (e: IOException) {
            val endedAt = System.currentTimeMillis()
            L.e("request failed [%s] %s: %s, delay: %d (ms)",
                    request.method(), request.url(), e.message, endedAt - startedAt) // 此处不打印堆栈,以免重复
            if (call?.isCanceled == true || "Canceled" == e.message) {
                // 通过tag计算被排除的重复请求
                throw RequestCancelledException("Task cancelled", e)
            }
            throw respParser.parseError(e)
        } finally {
            if (respBody != null) {
                respBody.close()
            }
        }
    }

    // 暂时通过fabric记录一些诊断信息, 排查签名错误
    private fun emitDiagnosis(resp: Response) {
        val code = resp.header(HEADER_FM_SIGN_CODE)
        if (!code.isNullOrEmpty() && "0" != code) {
            requestHelper.logException(RuntimeException("invalid signature: " + code))
        }
    }
}
