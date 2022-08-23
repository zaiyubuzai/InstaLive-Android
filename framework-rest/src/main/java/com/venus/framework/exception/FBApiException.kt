package com.venus.framework.exception

/**
 * Created by ywu on 2017/8/29.
 */
open class FBApiException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class FBFriendsNotFoundException : FBApiException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
