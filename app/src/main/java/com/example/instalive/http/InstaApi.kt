package com.example.instalive.http

import com.example.baselibrary.api.*
import com.example.baselibrary.model.CountryCodeListData
import com.example.baselibrary.model.PresignData
import com.example.instalive.model.*
import com.venus.dm.model.UserData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface InstaApi : BaseApi {

    @GET("api/core/system/init/")
    suspend fun init(@Query("start_type") startType: Int): BaseResponse<AppInitData>

    @GET
    suspend fun <T> getAnyData(@Url url: String): BaseResponse<T>

    @GET
    suspend fun getCountryCode(@Url url: String): BaseResponse<CountryCodeListData>

    @GET
    suspend fun getGifts(@Url url: String): BaseListResponse<List<GiftData>>

    @GET("api/core/system/current_time/")
    suspend fun calibrationTime(): BaseResponse<CalibrationTimeData>

    @FormUrlEncoded
    @POST("api/core/appauth/passcode/send/")
    suspend fun sendPasscode(
        @Field("phone") phone: String,
        @Field("source") source: String,
        @Field("dial_code") dialCode: String,
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/core/appauth/login/mobile/")
    suspend fun loginByPhone(
        @Field("phone") phone: String,
        @Field("passcode") passcode: String,
        @Field("user_name") userName: String?,
        @Field("portrait") portrait: String?,
        @Field("birth") birth: String?,
        @Field("gender") gender: String?,
        @Field("identity") identity: String?,
        @Field("fullname") nickname: String?,
        @Field("timezone") timezone: String?,
        @Field("s") s: String?
    ): BaseResponse<LoginData>

    @FormUrlEncoded
    @POST("api/core/appauth/logout/")
    suspend fun logout(@Field("id") id: String? = null): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/core/appuser/check_username/")
    suspend fun checkUsernameAvailability(@Field("username") username: String): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/core/appuser/check_fullname/")
    suspend fun checkFullNameAvailability(
        @Field("fullname") fullName: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/core/appuser/check_birth/")
    suspend fun checkBirthday(
        @Field("birth") birthday: String,
    ): BaseResponse<Any>

    @GET("api/core/appuser/me/")
    suspend fun me(): BaseResponse<UserData>

    @GET("api/core/appuser/user/detail/")
    suspend fun getUserDetail(
        @Query("user_id") userId: String?,
        @Query("user_name") userName: String?,
    ): BaseResponse<UserData>

    @GET("api/core/appuser/level/me/")
    suspend fun getMyLevel(
        @Query("target_user_id") targetUserId: String,
    ): BaseResponse<MyLevelData>

    @FormUrlEncoded
    @POST("api/core/appuser/me/profile/update/")
    suspend fun updateProfile(
        @Field("user_name") user_name: String?,
        @Field("nickname") nickname: String?,
        @Field("portrait") portrait: String?,
        @Field("bio") bio: String?,
    ): BaseResponse<UserData>

    @GET("api/core/appuser/website/")
    suspend fun getOtherWebsite(): BaseResponse<OtherWebsiteData>

    @FormUrlEncoded
    @POST("api/core/appuser/website/update/")
    suspend fun updateWebsite(@FieldMap fields: Map<String, String>): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/core/appuser/delete_my_account/")
    suspend fun deleteAccount(@FieldMap fields: Map<String, String>): BaseResponse<DeleteAccountData>

    @FormUrlEncoded
    @POST("api/core/media/presign/portrait/")
    suspend fun preSignPortrait(
        @Field("id") userId: String? = null,
    ): BaseResponse<PresignData>

    @FormUrlEncoded
    @POST("api/core/media/presign/dm_img/")
    suspend fun preSignDMImage(
        @Field("conversation_id") conId: String,
    ): BaseResponse<PresignData>

    @FormUrlEncoded
    @POST("api/core/media/presign/dm_video/")
    suspend fun preSignDMVideo(
        @Field("conversation_id") conId: String,
    ): BaseResponse<PresignData>

    @Multipart
    @POST
    suspend fun uploadResourceToS3(
        @Url url: String,
        @Part("key") key: RequestBody,
        @Part("AWSAccessKeyId") keyId: RequestBody,
        @Part("policy") policy: RequestBody,
        @Part("signature") sig: RequestBody,
        @Part("acl") acl: RequestBody?,
        @Part("x-amz-security-token") token: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<Unit>

    //????????????
    @FormUrlEncoded
    @POST("api/core/notification/register_device/")
    suspend fun registerPushToken(@FieldMap fields: Map<String, String>): BaseResponse<Any>

    @GET("api/core/notification/setting/")
    suspend fun getNotificationSettings(
    ): BaseResponse<NotificationSettingsData>

    @FormUrlEncoded
    @POST("api/core/notification/setting/update/")
    suspend fun updateNotification(
        @Field("pause_type") pauseType: Int?,//1???1??????  2???????????? 3:1??? 4:??????
        @Field("state") state: Int,//1??????????????? 0???????????????  ?????? 0 ?????????
    ): BaseResponse<NotificationSettingsData>

    @FormUrlEncoded
    @POST("api/dm/conversation/create/")
    suspend fun createConversation(
        @Field("target_user_id") userId: String
    ): BaseResponse<Any>

    @GET("api/dm/conversation/detail/")
    suspend fun conversationDetail(
        @Query("id") id: String,
    ): BaseResponse<ConversationResponseData>

    @GET("api/dm/conversation/list/")
    suspend fun getConversationList(): BaseResponse<ConversationListData>

    @FormUrlEncoded
    @POST("/api/dm/conversation/mute/")
    suspend fun muteConversation(@Field("id") conId: String): BaseResponse<Any>

    @FormUrlEncoded
    @POST("/api/dm/conversation/un_mute/")
    suspend fun unMuteConversation(@Field("id") conId: String): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/dm/conversation/pin/")
    suspend fun pinConversation(
        @Field("conversation_id") conversationId: String,
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/dm/conversation/unpin/")
    suspend fun unpinConversation(
        @Field("conversation_id") conversationId: String,
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/dm/chat/send/")
    suspend fun sendDm(
        @Field("conversation_id") conversationId: String,
        @Field("msg_type") type: Int,
        @Field("payload") payload: String,
        @Field("uuid") uuid: String,
        @Field("conversation_type") conversationType: Int,
        @Field("retry") retry: Int,//1?????? 0 ????????????
    ): BaseResponse<DmResponseData>

    //????????????
    @FormUrlEncoded
    @POST("api/dm/chat/recall/")
    suspend fun dmRecall(
        @Field("conversation_id") conId: String,
        @Field("uuid") uuid: String,
        @Field("msg_uuid") msgUUID: String
    ): BaseResponse<DmRecallResponseData>

    @FormUrlEncoded
    @POST("api/dm/chat/pull_unread/")
    suspend fun pullUnread(
        @Field("timetoken") timeToken: Long,
        @Field("pull_uuid") pullUUID: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("/api/dm/chat/report_read/")
    suspend fun reportConversationHaveRead(
        @Field("conversation_id") conversationId: String,
        @Field("timetoken") timeToken: Long
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("/api/dm/chat/report_ack/")
    suspend fun messageReportACK(
        @Field("uuids") uuids: String,
    ): BaseResponse<Any>

    //"title": "xxx", # ??????
    //    "desc": "xxxxxx", # ??????
    //    "ticket_gift_id": "xxxxxx", # ????????????id ???????????? ?????????
    //    "divide_income": 1, # ??????
    //    "divide_income_rate": 50, # ???????????? 0-60 0:turn-off
    @FormUrlEncoded
    @POST("api/live/live/start/")
    suspend fun createLive(
        @Field("title") title: String?,
        @Field("desc") desc: String?,
        @Field("ticket_gift_id") ticketGiftId: String?,
        @Field("divide_income") divideIncome: Int?,//0 or 1
        @Field("divide_income_rate") divideIncomeRate: Int?,//???????????? 0-60 0:turn-off
    ): BaseResponse<LiveDataInfo>

    @FormUrlEncoded
    @POST("api/live/live/close/")
    suspend fun closeLive(
        @Field("live_id") liveId: String
    ): BaseResponse<LiveCloseData>

    @FormUrlEncoded
    @POST("api/live/live/join/")
    suspend fun joinLive(
        @Field("live_id") liveId: String
    ): BaseResponse<LiveStateInfo>

    @FormUrlEncoded
    @POST("api/live/live/raise_hand/")
    suspend fun raiseHandLive(
        @Field("live_id") liveId: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/hand_down/")
    suspend fun handDownLive(
        @Field("live_id") liveId: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/live_with/hang_up/")
    suspend fun hangUpLive(
        @Field("live_id") liveId: String,
        @Field("target_user_id") targetUserId: String,
    ): BaseResponse<Any>

    @GET("api/live/live/list/")
    suspend fun getLiveList(
        @Query("offset") offset: Int
    ): BaseListResponseWithExt<List<LiveData>, Meta>

    @GET("api/live/live/token/")
    suspend fun getLiveToken(
        @Query("live_id") liveId: String
    ): BaseResponse<TokenInfo>

    @FormUrlEncoded
    @POST("api/live/live/leave/")
    suspend fun leaveLive(
        @Field("live_id") liveId: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/live_with/cancel/")
    suspend fun cancelLiveWith(
        @Field("live_id") liveId: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/live_with/agree/")
    suspend fun agreeLiveWith(@Field("live_id") liveId: String): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/live_with/reject/")
    suspend fun rejectLiveWith(@Field("live_id") liveId: String): BaseResponse<Any>

    @GET("api/live/live/raised_hands/")
    suspend fun liveWithViewer(
        @Query("live_id") liveId: String,
        @Query("offset") offset: Int,
    ): BaseListResponse<List<LiveViewerData>>

    @GET("api/live/live/online/members/")
    suspend fun liveViewer(
        @Query("live_id") roomId: String,
        @Query("offset") offset: Int,
    ): BaseListResponseWithExt<List<LiveViewerData>, LiveViewerExtData>

    @FormUrlEncoded
    @POST("api/live/live/live_with/invite/")
    suspend fun goLiveWith(
        @Field("live_id") liveId: String,
        @Field("target_user_id") userId: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/mute/")
    suspend fun liveMute(
        @Field("live_id") liveId: String,
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/unmute/")
    suspend fun liveUnmute(
        @Field("live_id") liveId: String,
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/host/mute/")
    suspend fun liveHostMute(
        @Field("live_id") liveId: String,
        @Field("target_user_id") userId: String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/share/")
    suspend fun liveShare(
        @Field("live_id") liveId: String,
    ): BaseResponse<LiveShareData>

    @GET("api/live/live/detail/refresh/")
    suspend fun liveRefresh(
        @Query("live_id")liveId: String,
    ): BaseResponse<LiveStateInfo>

    @FormUrlEncoded
    @POST("api/live/live/comment/")
    suspend fun sendLiveComment(
        @Field("live_id") liveId: String,
        @Field("content") content: String,
        @Field("uuid") uuid:String
    ): BaseResponse<Any>

    @FormUrlEncoded
    @POST("api/live/live/send_gift/")
    suspend fun sendLiveGift(
        @Field("live_id") liveId: String,
        @Field("gift_id") content: String,
        @Field("uuid") uuid:String
    ): BaseResponse<LiveSendGiftResponse>

    @GET("api/pay/account/accounts/")
    suspend fun accountBalance(): BaseResponse<AccountBalanceData>
}