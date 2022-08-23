package com.venus.framework.util

import android.content.Context
import android.os.Bundle

/**
 * 简化并链条化Bundle构建
 */
fun buildBundle(builder: Bundle.() -> Unit): Bundle {
    val bundle = Bundle()
    bundle.builder()
    return bundle
}

/**
 * 构造fragment参数的方便方法
 */
fun <T : androidx.fragment.app.Fragment> T.withArguments(init: Bundle.() -> Unit): T {
    val args = buildBundle(init)
    arguments = args
    return this
}

/** 此CharSequence既不为null也不为空 */
fun CharSequence?.isNeitherNullNorEmpty(): Boolean = this?.isNotEmpty() == true

/**
 * 将一个list分割为指定大小为`chunkSize`的块
 */
fun <T> Iterable<T>.toChunks(chunkSize: Int): Iterable<Iterable<T>> =
        mapIndexed { i, t -> i to t }
                .groupBy { it.first / chunkSize }
                .map { it.value.map { it.second } }

/** 此Collection既不为null也不为空 */
fun <T> Collection<T>?.isNeitherNullNorEmpty(): Boolean = this?.isNotEmpty() == true

/** 此Collection为null或空 */
fun <T> Collection<T>?.isNullOrEmpty(): Boolean = this == null || isEmpty()

/** 判断两个List的内容是否相等，即长度及每一个元素都相等 */
infix fun <T> List<T>?.eq(other: List<*>?): Boolean {
    if (this == other) return true
    if (this?.size != other?.size) return false

    // 此时this/other都不为null
    return (0 until this!!.size).all { i ->
        this[i] == other!![i]
    }
}

/** 安全的`>=`判断 */
infix fun Int?.ge(b: Int): Boolean = this != null && this >= b

/** 安全的`>`判断 */
infix fun Int?.gt(b: Int): Boolean = this != null && this > b

/** 安全的`<=`判断 */
infix fun Int?.le(b: Int): Boolean = this != null && this <= b

/** 安全的`<`判断 */
infix fun Int?.lt(b: Int): Boolean = this != null && this < b

/** 安全的`>=`判断 */
infix fun Long?.ge(b: Long): Boolean = this != null && this >= b

/** 安全的`>`判断 */
infix fun Long?.gt(b: Long): Boolean = this != null && this > b

/** 安全的`<=`判断 */
infix fun Long?.le(b: Long): Boolean = this != null && this <= b

/** 安全的`<`判断 */
infix fun Long?.lt(b: Long): Boolean = this != null && this < b

/** 是否偶数 */
val Int.isEven: Boolean get() = and(1) == 0

/** 是否奇数 */
val Int.isOdd: Boolean get() = and(1) == 1

/** 获得字符串的md5 */
val String.md5: String get() = MD5Encoder.encode(this)

fun Context.getStatusHeight(): Int {
    val resourceId = resources.getIdentifier(
        "status_bar_height",
        "dimen", "android"
    )
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}
