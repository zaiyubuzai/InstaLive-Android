package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class PresignData(
    val url: String,
    @SerializedName("resource_url") val resourceUrl: String,
    val fields: PresignFields,
)

data class PresignFields(
    val key: String,
    val AWSAccessKeyId: String,
    val policy: String,
    val signature: String,
    val acl: String?,
    @SerializedName("x-amz-security-token") val token: String,
)
