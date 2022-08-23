/*
 * Extension functions for Rest framework 
 */
@file:JvmName("RestExt")
package com.venus.framework.rest

/**
 * 分页参数接口
 */
interface Pagination {
    val hasNext: Boolean
    val offset: Int
    val limit: Int
}

/**
 * 统一为请求参数添加分页参数.
 *
 * 这是一个中缀函数, 所以可以这样写:
 * ```
 * mutableMapOf("a", 1) paging meta
 * RequestParams("a", 1) paging meta
 * ```
 */
infix fun MutableMap<String, Any?>.paging(meta: Pagination?): MutableMap<String, Any?> {
    if (meta?.hasNext == true) {
        put("offset", meta.offset)
        put("limit", meta.limit)
    }
    return this
}
