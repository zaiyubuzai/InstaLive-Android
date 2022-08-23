package com.example.baselibrary.network

import android.text.TextUtils
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class BooleanTypeAdapter : TypeAdapter<Boolean?>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Boolean? {
        val peek = `in`.peek()
        return when (peek) {
            JsonToken.BOOLEAN -> `in`.nextBoolean()
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }
            JsonToken.NUMBER -> `in`.nextInt() != 0
            JsonToken.STRING -> toBoolean(`in`.nextString())
            else -> throw JsonParseException("Expected BOOLEAN or NUMBER but was $peek")
        }
    }

    companion object {
        /**
         * true  TURE 都为true
         * "0" 为 false
         *
         * @param name
         * @return
         */
        fun toBoolean(name: String): Boolean {
            return (!TextUtils.isEmpty(name)
                    &&
                    (name.equals("true", ignoreCase = true) || name != "0"))
        }
    }
}