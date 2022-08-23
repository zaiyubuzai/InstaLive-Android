package com.venus.framework.cache

import kotlin.text.Charsets.UTF_8

fun DiskLruCache.getString(key: String): String? =
    get(key)?.getInputStream(0)?.use {
        String(it.readBytes(), UTF_8)
    }

fun DiskLruCache.put(key: String, data: String) {
    val editor = edit(key) ?: return
    editor.newOutputStream(0).use {
        val writer = it.bufferedWriter()
        writer.write(data)
        writer.flush()
        editor.commit()
    }
}
