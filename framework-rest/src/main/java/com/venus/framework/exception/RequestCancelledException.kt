package com.venus.framework.exception

/**
 * 请求被Cancel
 *
 * Created by ywu on 2018/1/16.
 */
class RequestCancelledException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
