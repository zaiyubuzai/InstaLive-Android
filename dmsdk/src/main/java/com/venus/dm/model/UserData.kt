package com.venus.dm.model

import com.google.gson.Gson
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserData(
    val id: String,
    @SerializedName("user_name") var username: String,
    var nickname: String,
    var portrait: String,
    val email: String?,
    var bio: String?,
    var gender: Int,
    var identity: Int,
    @SerializedName("mobile_phone") var mobile: String?,
    val location: UserLocation?,
    val stat: UserStats?,
    @UserRelation var relationship: Int,
    @SerializedName("user_role") var role: Int? = 9,
    @SerializedName("chat_state") val chatState: Int,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("show_balance") val showBalance: Boolean? = false,
    @SerializedName("website_list") val websiteList: List<WebsiteData>? = null,
    @SerializedName("add_new_link_button") val addNewLinkButton: Boolean? = false,
    @SerializedName("name_change_use") val nameChangeUse: Int? = null,
    @SerializedName("name_change_total") val nameChangeTotal: Int? = null,
    @SerializedName("portrait_icon") val portraitIc: String?,
    @SerializedName("mute_timestamp") var muteTimestamp: Long?,
    @SerializedName("name_change_tips") val nameChangeTips: String? = null,
    @SerializedName("nickname_change_tips") val nicknameChangeTips: String? = null,
) : Serializable {

    fun setFollowed() {
        if (relationship == USER_RELATION_DEFAULT) {
            relationship = USER_RELATION_FOLLOWING
        } else if (relationship == USER_RELATION_BEING_FOLLOWED) {
            relationship = USER_RELATION_MUTUAL_FOLLOWED
        }
    }

    fun setUnfollowed() {
        if (relationship == USER_RELATION_MUTUAL_FOLLOWED) {
            relationship = USER_RELATION_BEING_FOLLOWED
        } else if (relationship == USER_RELATION_FOLLOWING) {
            relationship = USER_RELATION_DEFAULT
        }
    }

    fun canFollow(): Boolean {
        return relationship == USER_RELATION_DEFAULT || relationship == USER_RELATION_BEING_FOLLOWED
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {

        @UserRelation
        const val USER_RELATION_EACH_BLOCKED = -4

        @UserRelation
        const val USER_RELATION_BEING_BLOCKED = -3

        @UserRelation
        const val USER_RELATION_BLOCKED = -2

        @UserRelation
        const val USER_RELATION_SELF = -1

        @UserRelation
        const val USER_RELATION_DEFAULT = 0

        @UserRelation
        const val USER_RELATION_FOLLOWING = 1

        @UserRelation
        const val USER_RELATION_BEING_FOLLOWED = 2

        @UserRelation
        const val USER_RELATION_MUTUAL_FOLLOWED = 3

        fun canFollow(relationship: Int): Boolean {
            return relationship == USER_RELATION_DEFAULT || relationship == USER_RELATION_BEING_FOLLOWED
        }
    }
}

data class WebsiteData(@SerializedName("website_link") val websiteLink: String,
                       @SerializedName("website_icon") val websiteIcon: String,
                       @SerializedName("website_img") val websiteImg: String,
                       @SerializedName("website_name") val websiteName: String,
                       @SerializedName("website_base_url") val websiteBaseUrl: String,
                       @SerializedName("website_field_name") val websiteFieldName: String,
                       @SerializedName("max_len") val max_len: Int,
                       val tips: String): Serializable {

    var isSelected:Boolean? = false
        get() = field ?: false

    override fun toString(): String {
        return Gson().toJson(this)
    }

    fun fromJson(json: String): WebsiteData{
        return Gson().fromJson(json, WebsiteData::class.java)
    }
}

data class UserLocation(
    val country: String?,
    val region: String?,
    val city: String?,
    val lat: String,
    val lon: String
): Serializable

data class UserStats(
    @SerializedName("followers_count") val followers: Long,
    @SerializedName("following_count") val following: Long,
    @SerializedName("likes_count") val like: Long,
    val balance: Double,
    @SerializedName("video_count") val videoCount: Long?,
    @SerializedName("comment_count") val commentCount: Long?,
    @SerializedName("like_video_count") val likeVideoCount: Long?
): Serializable

data class GroupInfo(
    val id: String,
    @SerializedName("group_name") val groupName: String,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_member") val isMember: Boolean,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("notify_new_followers") val notifyNewFollowers: Boolean,
): Serializable

data class LiveInfo(
    val id: String,
    val name: String,
    val title: String,
    val desc: String,
    val state: Int,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("live_enabled") val liveEnabled: Boolean,
): Serializable


@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD
)
annotation class UserRelation