package com.venus.framework.exception;

/**
 * 记录第三方服务异常，便于分析、debug
 *
 * Created by ywu on 2017/3/13.
 */
public class VendorServiceException extends RuntimeException {
    public VendorServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public VendorServiceException(Throwable cause) {
        super(cause);
    }
}
