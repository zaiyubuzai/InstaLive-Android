package com.venus.framework.rest;

import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;

/**
 * Constant definitions for HTTP
 *
 * Created by ywu on 15/11/27.
 */
public interface HttpConstants {
    //region HTTP methods
    String HTTP_GET = "GET";
    String HTTP_POST = "POST";
    //endregion

    //region HTTP headers
    String HTTP_HEADER_IF_NONE_MATCH = "If-None-Match";
    String HTTP_HEADER_ETAG = "ETag";
    String HTTP_HEADER_CONTENT_TYPE = "content-type";
    //endregion

    String HTML_CONTENT_TYPE_PREFIX = "text/html";

    MediaType CONTENT_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded");
    MediaType CONTENT_TYPE_MULTIPART_FORM = MultipartBody.FORM;
    MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    //region HTTP status codes
    int HTTP_STATUS_OK = HttpURLConnection.HTTP_OK;
    int HTTP_STATUS_NOT_MODIFIED = HttpURLConnection.HTTP_NOT_MODIFIED;
    int HTTP_STATUS_BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST;
    int HTTP_STATUS_UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED;
    int HTTP_STATUS_UNPROCESSABLE = 422;
    int HTTP_STATUS_SERVER_ERROR = HttpURLConnection.HTTP_INTERNAL_ERROR;
    //endregion

    //region HTTP Client defaults
    int TIMEOUT_MS = 60000;  // read/write timeout, in ms
    int TIMEOUT_MS_LONG = 120000;  // a longer read/write timeout, in ms
    int CONNECT_TIMEOUT_MS = 15000;  // connection timeout, in ms
    int MAX_RETRIES = 2;
    int RETRIES_INTERVAL_MS = 2000;  // interval between retries, in ms
    int MAX_CACHE_SIZE = 50 * 1024 * 1024;  // cache size 50M
    int WEB_VIEW_CUSTOM_CACHE_SIZE = 100 * 1024 * 1024;  // cache size 100M
    //endregion

    //region product specified restful params
    String PARAM_SIGN = "sign";
    String PARAM_RF_TAG = "rf_tag";
    String PARAM_TIMESTAMP = "ts";

    String HEADER_FM_CMD = "X-FIVEMILES-COMMAND";
    String HEADER_FM_CODE_COMPAT = "HTTP_X_FIVEMILES_CODE";
    String HEADER_FM_CODE = "X-FIVEMILES-CODE";
    /** 记录签名校验结果的header */
    String HEADER_FM_SIGN_CODE = "HTTP_X_FIVEMILES_SIGN_CODE";

    /** referer */
    String HTTP_X_FIVEMILES_REFERER = "X-FIVEMILES-REFERER";

    int FM_CODE_SUCCESS = 0;
    int FM_ERR_UNKNOWN = -1;
    int FM_ERR_INVALID_CLIENT_TIME = 9003;
    String FM_RESP_CODE_SUCCESS = String.valueOf(FM_CODE_SUCCESS);
    String FM_RESP_CODE_INVALID_CLIENT_TIME = String.valueOf(FM_ERR_INVALID_CLIENT_TIME);
    //endregion

    // region product specified error codes
    int FM_ERR_INTERNAL_ERROR = 1999;

    int FM_ERR_TOKEN_EXPIRED = 1000;
    int FM_ERR_UNAUTHENTICATED = 1001;
    int FM_ERR_ZIPCODE_NOT_EXISTS = 1018;
    int FM_ERR_DUPLICATE_EMAIL = 1023;
    int FM_ERR_NO_EMAIL_PERMISSION = 1025;
    int FM_ERR_INVALID_INVATATION = 1050;

    /** Too many keywords subscribed */
    int ERR_KEYWORD_SUBSCRIBE_TOO_MANY = 1031;

    /** 汽车VIN码查询服务失败 */
    int FM_ERR_VIN_SVC_FAILURE = 1042;

    /** 汽车的VIN码无效或不存在 */
    int FM_ERR_VIN_UNAVAILABLE = 1043;
    // endregion
}
