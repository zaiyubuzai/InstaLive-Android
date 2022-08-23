package com.venus.framework.cache

import android.util.LruCache
import javax.annotation.concurrent.Immutable
import javax.annotation.concurrent.ThreadSafe

/**
 * 根据[Contacts的源码](https://android.googlesource.com/platform/packages/apps/Contacts/+/ics-mr0/src/com/android/contacts/util/ExpirableCache.java)
 * 的源码改造, 改为按时间判定是否过期
 *
 * Created by ywu on 2017/8/4.
 */
@ThreadSafe class ExpirableCache<K, V>
/** @property cache The underlying cache used to stored the cached values. */
private constructor(val cache: LruCache<K, CachedValue<V>>) {
    /**
     * Returns the cached value for the given key, or null if no value exists.
     *
     *
     * The cached value gives access both to the value associated with the key and whether it is
     * expired or not.
     *
     *
     * If not interested in whether the value is expired, use [.getPossiblyExpired]
     * instead.
     *
     *
     * If only wants values that are not expired, use [.get] instead.
     *
     * @param key the key to look up
     */
    fun getCachedValue(key: K): CachedValue<V>? = cache.get(key)

    /**
     * Returns the value for the given key, or null if no value exists.
     *
     *
     * When using this method, it is not possible to determine whether the value is expired or not.
     * Use [.getCachedValue] to achieve that instead. However, if using
     * [.getCachedValue] to determine if an item is expired, one should use the item
     * within the [CachedValue] and not call [.getPossiblyExpired] to get the
     * value afterwards, since that is not guaranteed to return the same value or that the newly
     * returned value is in the same state.
     *
     * @param key the key to look up
     */
    fun getPossiblyExpired(key: K): V? {
        val cachedValue = getCachedValue(key)
        return cachedValue?.value
    }

    /**
     * Returns the value for the given key only if it is not expired, or null if no value exists or
     * is expired.
     *
     *
     * This method will return null if either there is no value associated with this key or if the
     * associated value is expired.
     *
     * @param key the key to look up
     */
    operator fun get(key: K): V? {
        val cachedValue = getCachedValue(key)
        return if (cachedValue == null || cachedValue.isExpired()) null else cachedValue.value
    }

    /**
     * Puts an item in the cache.
     *
     * Newly added item will not be expired until [.expireAll] is next called.
     *
     * @param key the key to look up
     * @param value the value to associate with the key
     * @param lifespan the lifespan before the item is expired
     */
    fun put(key: K, value: V, lifespan: Long) {
        cache.put(key, newCachedValue(value, lifespan))
    }

    /**
     * Evict all cached items.
     */
    fun evictAll() {
        cache.evictAll()
    }

    /**
     * Creates a new [CachedValue] instance to be stored in this cache.
     *
     * Implementation of [LruCache.create] can use this method to create a new entry.
     */
    fun newCachedValue(value: V, lifespan: Long): CachedValue<V> =
        TimedCachedValue(value = value, lifespan = lifespan)

    companion object {
        /**
         * Creates a new [ExpirableCache] that wraps the given [LruCache].
         *
         * The created cache takes ownership of the cache passed in as an argument.
         *
         * @param <K> the type of the keys
         * @param <V> the type of the values
         * @param cache the cache to store the value in
         * @return the newly created expirable cache
         * @throws IllegalArgumentException if the cache is not empty
         */
        @JvmStatic fun <K, V> create(cache: LruCache<K, CachedValue<V>>): ExpirableCache<K, V> =
            ExpirableCache(cache)

        /**
         * Creates a new [ExpirableCache] with the given maximum size.
         *
         * @param <K> the type of the keys
         * @param <V> the type of the values
         * @return the newly created expirable cache
         */
        @JvmStatic fun <K, V> create(maxSize: Int): ExpirableCache<K, V> =
            create(LruCache<K, CachedValue<V>>(maxSize))
    }

    /**
     * A cached value stored inside the cache.
     * <p>
     * It provides access to the value stored in the cache but also allows to check whether the
     * value is expired.
     *
     * @param <V> the type of value stored in the cache
     */
    interface CachedValue<V> {
        /** The value stored in the cache for a given key. */
        val value: V

        /**
         * Checks whether the value, while still being present in the cache, is expired.
         */
        fun isExpired(): Boolean
    }

    /**
     * 基于时间戳的缓存条目
     */
    @Immutable private class TimedCachedValue<V>(
        override val value: V,
        private val timestamp: Long = System.currentTimeMillis(),
        private val lifespan: Long
    ) : CachedValue<V> {

        override fun isExpired(): Boolean {
            val elapsed = System.currentTimeMillis() - timestamp
            return elapsed > lifespan
        }
    }
}
