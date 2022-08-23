package com.example.baselibrary.network

import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.NumberFormatException

class IntAdapter : TypeAdapter<Number?>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Number?) {
        if (value == null) {
            out.value(0)
            return
        }
        out.value(value)
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): Number? {
        if (reader.peek() == JsonToken.NULL) {
            reader.skipValue()
            return 0
        } else if (reader.peek() == JsonToken.STRING) {
            return try {
                Integer.valueOf(reader.nextString())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                0
            } catch (e: IOException) {
                e.printStackTrace()
                0
            }
        }
        return try {
            reader.nextInt()
        } catch (e: NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }
}