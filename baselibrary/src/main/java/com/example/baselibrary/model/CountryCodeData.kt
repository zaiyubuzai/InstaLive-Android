package com.example.baselibrary.model

import com.google.gson.annotations.SerializedName

data class CountryCodeData (
    val name: String,
    @SerializedName("dial_code") val dialCode: String,
    val code: String
)

data class CountryCodeListData(
    @SerializedName("country_code") val countryCode: List<List<CountryCodeData>>,
    val version: Int,
)