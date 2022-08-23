package com.venus.framework.util

/**
 * 兼容简化版Optional
 *
 * Created by ywu on 2018/2/9.
 */
data class Optional<T>(private var _value: T? = null) {

    val isPresent: Boolean get() = _value != null

    val value: T get() = _value ?: throw NoSuchElementException("No value present")

    @JvmOverloads fun orElse(defaultValue: T? = null): T? = _value ?: defaultValue

    companion object {
        @JvmStatic fun <T> empty(): Optional<T> = Optional()
        @JvmStatic fun <T> of(value: T): Optional<T> = Optional(value)
    }
}
