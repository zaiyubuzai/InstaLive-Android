package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class LiveShareData(
    @SerializedName("share_link")val shareLink: String,
)