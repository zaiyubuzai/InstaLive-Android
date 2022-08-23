package com.example.baselibrary.network

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class StringAdapter : TypeAdapter<String?>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.value("")
            return
        }
        out.value(value)
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): String? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return ""
        }
        return `in`.nextString()
    }
}