package com.venus.framework.exception;

/**
 * Network failures
 * Created by ywu on 15-3-7.
 */
public class NetworkException extends RuntimeException {

    public static final int NO_STATUS = -1;

    private int statusCode = NO_STATUS;

    public NetworkException() {
    }

    public NetworkException(int statusCode, String detailMessage) {
        super(detailMessage);
        this.statusCode = statusCode;
    }

    public NetworkException(int statusCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.statusCode = statusCode;
    }

    public NetworkException(int statusCode, Throwable throwable) {
        super(throwable);
        this.statusCode = statusCode;
    }

    public NetworkException(Throwable throwable) {
        super(throwable);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
