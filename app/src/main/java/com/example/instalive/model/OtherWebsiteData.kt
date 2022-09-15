package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.venus.dm.model.WebsiteData

data class OtherWebsiteData( @SerializedName("other_website_list") val otherWebsiteList: List<WebsiteData> = listOf()) {
}