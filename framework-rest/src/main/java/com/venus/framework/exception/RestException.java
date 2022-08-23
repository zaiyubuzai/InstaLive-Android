package com.venus.framework.exception;

import com.venus.framework.rest.HttpConstants;

/**
 * Restful api error
 * Created by ywu on 14-9-27.
 */
public class RestException extends NetworkException implements HttpConstants {
    public static final int UNKNOWN_ERROR = -1;

    private int errorCode = UNKNOWN_ERROR;

    public RestException() {
    }

    public RestException(int statusCode, String detailMessage) {
        super(statusCode, detailMessage);
    }

    public RestException(int statusCode, String detailMessage, Throwable throwable) {
        super(statusCode, detailMessage, throwable);
    }

    public RestException(int statusCode, Throwable throwable) {
        super(statusCode, throwable);
    }

    public RestException(int statusCode, int errorCode, String detailMessage) {
        super(statusCode, detailMessage);
        this.errorCode = errorCode;
    }

    public RestException(int statusCode, int errorCode, String detailMessage, Throwable throwable) {
        super(statusCode, detailMessage, throwable);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

//    @Override
//    public String getMessage() {
//        String msg = super.getMessage();
//        return shouldShowErrorCode() ? msg + " Error code: [" + errorCode + "]" : msg;
//    }
//
//    // fix FMA-632 只显示某些错误码, 现在没有理想的方案, 先根据范围判断
//    private boolean shouldShowErrorCode() {
//        return errorCode > 0 &&
//                (errorCode <= FM_ERR_MISSING_EMAIL || errorCode >= FM_ERR_INTERNAL_ERROR);
//    }
}
