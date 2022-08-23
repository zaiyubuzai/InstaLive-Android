package com.example.baselibrary.api

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    val meta: Meta,
    val result: String?,
    val data: T
) {
    fun resultOk(): Boolean {
        return result == "ok"
    }
}

data class BaseResponseWithExt<T, E>(
    val meta: Meta,
    val result: String?,
    val data: T,
    @SerializedName("data_ext") val dataExt: E?
) {
    fun resultOk(): Boolean {
        return result == "ok"
    }
}

data class Meta(
    @SerializedName("has_next") val hasNext: Boolean = true,
    @SerializedName("next_offset") val nextOffset: Int = 0,
    val limit: Int = 20,
    val offset: Int = 0,
    @SerializedName("total_count") val total: Long = 0
)