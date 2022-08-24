package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class AppInitData(
    @SerializedName("server_information") val serverInformation: ServerInformation,
    @SerializedName("operation_information") val operationInformation: Any,
    @SerializedName("feature_information") val appFeature: AppFeature,
    @SerializedName("search_default_tab") val searchDefaultTab: Int,
    @SerializedName("profile_share_enabled") val profileShareEnabled: Int,
    @SerializedName("profile_share_prefix") val profileSharePrefix: String,
    @SerializedName("app_init_data") val appInitData: Any,
    @SerializedName("cache_configs") val cacheConfig: CacheConfig,
    @SerializedName("apips") val apips: APIPSData,
)

data class ServerInformation(
    @SerializedName("api_version") val apiVersion: String,
)

data class AppFeature(
    @SerializedName("app_update_title") val appUpdateTitle: String,
    @SerializedName("app_update_description") val appUpdateDescription: String,
    @SerializedName("app_update_buttons") val appUpdateButtons: List<String>,
    @SerializedName("app_update_type") val appUpdateType: Int,// 0:not update; 1:update; 2:power update
)

data class APIPSData(
    val aiap: String,
    val cardr: String,
)

data class CacheConfig(
    @SerializedName("live_gifts") val liveGiftsCache: Cache?,
    @SerializedName("string_template") val stringTemplate: Cache?,
    @SerializedName("level_icons") val levelIcons: Cache?,
    @SerializedName("country_code") val countryCode: Cache?,
) {
    data class Cache(
        val version: Int,
        @SerializedName("api_path") val apiPath: String,
    )
}

data class CalibrationTimeData(
    val timestamp: Long
)
