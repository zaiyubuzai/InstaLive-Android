package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LoginData(
    val id: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("nickname") val nickName: String?,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    val portrait: String?,
    val gender: Int,
    val identity: Int,
    val lat: String?,
    val lon: String?,
    val country: String?,
    val region: String?,
    val city: String?,
    val token: String,
    val state: Int,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_anonymous") val isAnonymous: Boolean,
    @SerializedName("invite_info") val inviteInfo: Any?,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_create") val isCreate: Boolean,
) :Serializable {

    companion object {
        fun emptyLoginInfo(): LoginData {
            return LoginData("",
                "",
                null,
                null,
                null,
                null,
                1,
                1,
                null,
                null,
                null,
                null,
                null,
                "",
                1,
                false,
                0,
                isCreate = false,
            )
        }
    }
}