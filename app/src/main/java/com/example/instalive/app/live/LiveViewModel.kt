package com.example.instalive.app.live

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.LiveDataRepository
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.app.conversation.MessageBaseViewModel
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.model.*
import com.google.gson.Gson
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.model.UserData
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.random.Random

class LiveViewModel : MessageBaseViewModel() {
    val commentLiveData = MutableLiveData<Any>()
    val liveTokenInfo = MutableLiveData<TokenInfo>()
    var inviteData = MutableLiveData<LiveWithInviteEvent>()
    var personalLiveData = MutableLiveData<UserData>()
    var liveInitInfo = MutableLiveData<LiveInitInfo>()
    val liveEndDetailData = MutableLiveData<LiveEndDetailData>()
    val liveInfoLiveData = MutableLiveData<LiveDataInfo>()
    val liveCloseLiveData = MutableLiveData<LiveCloseData>()


    val isMicrophoneUser = MutableLiveData<Boolean>()//本人是否连麦中
    val isMicrophone = MutableLiveData<Boolean>()//直播是否为连麦直播

    fun joinLive(userId: String,
                 liveId: String,
                 liveJoinData: MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>,
                 password: String? = null) {
        viewModelScope.launch {
            LiveDataRepository.joinLive(liveId, liveJoinData, object : RemoteEventEmitter{
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    this@LiveViewModel.onError(code, msg, errorType)
                }

                override fun onEvent(event: StatusEvent) {
                    this@LiveViewModel.onEvent(event)
                }
            })
        }
    }

    fun createLive(
        title: String?,
        desc: String?,
        ticketGiftId: String?,
        divideLiveIncome: Int?,
        divideIncomeRate: Int?,
        error: (String) -> Unit
    ) {
        viewModelScope.launch {
            LiveDataRepository.createLive(
                title,
                desc,
                ticketGiftId,
                divideLiveIncome,
                divideIncomeRate,
                liveInfoLiveData,
                object :
                    RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        error(msg)
                    }

                    override fun onEvent(event: StatusEvent) {
                        this@LiveViewModel.onEvent(event)
                    }

                })
        }
    }

    fun liveReport(type: Int, liveId: String, agoraChannel: String, uid: Int?) {
        viewModelScope.launch {
            val dataJson = JSONObject()
            dataJson.put("live_id", liveId)
            dataJson.put("agora_channel", agoraChannel)
            uid?.let { dataJson.put("uid", uid) }
//            DataRepository.liveReport(type, dataJson.toString(), this@LiveFragmentViewModel)
        }
    }

    fun initLive(conId: String, onError: (code: Int, msg: String, errorType: ErrorType) -> Unit) {
        viewModelScope.launch {
//            DataRepository.initLive(conId, liveInitInfo, object : RemoteEventEmitter{
//                override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                    this@LiveFragmentViewModel.onError(code, msg, errorType)
//                    onError.invoke(code, msg, errorType)
//                }
//
//                override fun onEvent(event: StatusEvent) {
//                    this@LiveFragmentViewModel.onEvent(event)
//                }
//            })
        }
    }


    fun closeLive(liveId: String?) {
        if (liveId.isNullOrEmpty()) return
        viewModelScope.launch {
            LiveDataRepository.closeLive(liveId, liveCloseLiveData, this@LiveViewModel)
        }
    }

    fun getLiveState(
        liveId: String,
        liveStateInfoLiveData: MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>,
        onError: (code: Int, msg: String) -> Unit,
        onStatusEvent: (event: StatusEvent) -> Unit
    ) {
        viewModelScope.launch {
//            DataRepository.getLiveState(liveId, liveStateInfoLiveData, object : RemoteEventEmitter {
//                override fun onError(code: Int, msg: String, errorType: ErrorType) {
////                    this@LiveFragmentViewModel.onError(code, msg, errorType)
//                    onError.invoke(code, msg)
//                }
//
//                override fun onEvent(event: StatusEvent) {
//                    this@LiveFragmentViewModel.onEvent(event)
//                    onStatusEvent.invoke(event)
//                }
//
//            })
        }
    }

    fun liveUIPause(liveId: String, userType: Int) {
        viewModelScope.launch {
//            DataRepository.liveUIPause(liveId, userType, System.currentTimeMillis())
        }
    }

    fun liveUIResume(liveId: String, userType: Int) {
        viewModelScope.launch {
//            DataRepository.liveUIResume(liveId, userType, System.currentTimeMillis())
        }
    }

    fun getLiveToken(liveId: String) {
        viewModelScope.launch {
            LiveDataRepository.getLiveToken(liveId, liveTokenInfo, this@LiveViewModel)
        }
    }

    fun goLiveWith(userId: String, liveId: String) {
        viewModelScope.launch {
//            DataRepository.goLiveWith(userId, liveId, inviteData, this@LiveFragmentViewModel)
        }
    }

    fun getLiveEndDetail(liveId: String, onError: (code: Int, msg: String) -> Unit) {
        viewModelScope.launch {
//            DataRepository.liveEndDetail(liveId, liveEndDetailData, object : RemoteEventEmitter {
//                override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                    this@LiveFragmentViewModel.onError(code, msg, errorType)
//                    onError.invoke(code, msg)
//                }
//
//                override fun onEvent(event: StatusEvent) {
//                    this@LiveFragmentViewModel.onEvent(event)
//                }
//
//            })
        }
    }

    fun getPersonalData(userId: String, event: (StatusEvent) -> Unit) {
        viewModelScope.launch {
//            DataRepository.getUserDetail(
//                userId,
//                null,
//                personalLiveData,
//                object : RemoteEventEmitter {
//                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                    }
//
//                    override fun onEvent(event: StatusEvent) {
//                        if (event == StatusEvent.SUCCESS) {
//                            event(event)
//                        }
//                    }
//                }
//            )
        }
    }

    val likeLiveData = MutableLiveData<Any>()
    val cancelLiveWithData = MutableLiveData<Any>()
    val hangUpLiveData = MutableLiveData<Any>()
    val giftListLiveData = MutableLiveData<GiftListData>()
    val raiseHandData = MutableLiveData<Any>()
    val handsDownData = MutableLiveData<Any>()
    val muteLiveData = MutableLiveData<Any>()
    val closeLiveData = MutableLiveData<Any>()
    val liveSettingUpdateData = MutableLiveData<Any>()

    var messageLoopJob: Job? = null

    fun startSendMessageLoop(liveId: String) {
        messageLoopJob?.cancel()
        messageLoopJob = viewModelScope.launch(Dispatchers.IO) {
            while (this.isActive) {
                var count = 0
                try {
                    delay(400)
                    val stringBuilder = StringBuilder()
                    for (i in 1 until Random.nextInt(10, 20)) {
                        stringBuilder.append(Char(Random.nextInt(96) + 32).toString())
                        stringBuilder.append(" ")
                        stringBuilder.append(count.toString())
                        count++
                    }
//                    sendMessage(
//                        stringBuilder.toString(),
//                        liveId,
//                        -1
//                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    fun stopSendMessageLoop() {
        messageLoopJob?.cancel()
        messageLoopJob = null
    }

    fun callClose(callId: String) {
        viewModelScope.launch {
//            LiveDataRepository.callClose(callId, closeLiveData, this@LiveInteractionViewModel)
        }
    }

    @ExperimentalStdlibApi
    fun getMessageList(
        isRefresh: Boolean,
        liveId: String,
        timeToken: Long,
        result: (List<MessageEntity>, isRefresh: Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
//            val messages = if (isRefresh) {
//                dao.getLiveMessageByConId(conId, liveId, SessionPreferences.id, timeToken)
//            } else {
//                dao.getLiveMessagedMoreByConId(conId, liveId, SessionPreferences.id, timeToken)
//            }
//            var count = 0
//            if (messages.isNotEmpty()) {
//
//                val oldMessages = mutableListOf<MessageEntityWithUser>()
//                if (isRefresh) {
//                    messages.forEachIndexed { index, messageEntityWithUser ->
//                        oldMessages.add(count, messageEntityWithUser)
//                        count++
//                        val newMessageFirstUUID = ""
//                        if (messageEntityWithUser.uuid == newMessageFirstUUID) {
//                            oldMessages.add(
//                                count,
//                                messageEntityWithUser.copy(
//                                    uuid = UUID.randomUUID().toString(),
//                                    type = 201,
//                                    timeToken = messageEntityWithUser.timeToken - 1
//                                )
//                            )
//                            count++
//                        } else {
//                            if ((messageEntityWithUser.timeToken - if (index == messages.size - 1) timeToken else messages.get(
//                                    index + 1
//                                ).timeToken) > 10 * 60 * 1000 * 10000L
//                            ) {
//                                oldMessages.add(
//                                    count,
//                                    messageEntityWithUser.copy(
//                                        uuid = UUID.randomUUID().toString(),
//                                        type = 8,
//                                        timeToken = messageEntityWithUser.timeToken - 1,
//                                        content = TimeUtils.formatMessageTime(messageEntityWithUser.timeToken),
//                                        renderType = 1
//                                    )
//                                )
//                                count++
//                            }
//                        }
//                    }
//                    withContext(Dispatchers.Main) {
//                        result.invoke(oldMessages.toList(), isRefresh)
//                    }
//                } else {
//                    messages.forEachIndexed { index, messageEntityWithUser ->
//                        oldMessages.add(messageEntityWithUser)
//                        count++
//                        val newMessageFirstUUID = ""
//                        if (messageEntityWithUser.uuid == newMessageFirstUUID) {
//                            oldMessages.add(
//                                messageEntityWithUser.copy(
//                                    uuid = UUID.randomUUID().toString(),
//                                    type = 201,
//                                    timeToken = messageEntityWithUser.timeToken - 1
//                                )
//                            )
//                            count++
//                        } else {
//                            if ((if (index == 0) timeToken else messages.get(index - 1).timeToken) - messageEntityWithUser.timeToken > 10 * 60 * 1000 * 10000L) {
//                                oldMessages.add(
//                                    messageEntityWithUser.copy(
//                                        uuid = UUID.randomUUID().toString(),
//                                        type = 8,
//                                        timeToken = messageEntityWithUser.timeToken - 1,
//                                        content = TimeUtils.formatMessageTime(messageEntityWithUser.timeToken),
//                                        renderType = 1
//                                    )
//                                )
//                                count++
//                            }
//                        }
//                    }
//                    withContext(Dispatchers.Main) {
//                        result.invoke(oldMessages.toList(), isRefresh)
//                    }
//                }
//            }
        }
    }

//    fun getMessageListBySocket(
//        liveId: String,
//        conId: String,
//        timeToken: Long,
//        timeTokenStart: Long,
//        timeTokenEnd: Long,
//    ): List<MessageEntity> {
//        return dao.getLiveMessagedMoreByConId(
//            conId,
//            liveId,
//            SessionPreferences.id,
//            timeTokenStart,
//            timeTokenEnd
//        )
//    }

    //    fun liveSettingUpdate(
//        liveId: String,
//        liveDiamondsPublic: Int,
//        onStatus: (StatusEvent) -> Unit
//    ) {
//        viewModelScope.launch {
//            DataRepository.liveSettingUpdate(
//                liveId,
//                liveDiamondsPublic,
//                liveSettingUpdateData,
//                object : RemoteEventEmitter {
//                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                        this@LiveInteractionViewModel.onError(code, msg, errorType)
//                    }
//
//                    override fun onEvent(event: StatusEvent) {
//                        this@LiveInteractionViewModel.onEvent(event)
//                        onStatus.invoke(event)
//                    }
//
//                })
//        }
//    }
//
//    fun screenshot(conversationId: String, type: Int, source: Int) {
//        viewModelScope.launch {
//            DataRepository.screenshot(conversationId, type, source)
//        }
//    }
//
//    fun getConversationsLiveMessages(conversationId: String): List<MessageEntity> {
//        return dao.getConversationsLiveMessages(conversationId, roomId, SessionPreferences.id)
//    }
//
//    fun getPersonalData(userId: String, conversationId: String, event: (StatusEvent) -> Unit) {
//        viewModelScope.launch {
//            DataRepository.getUserDetailAllowNull(
//                userId,
//                conversationId,
//                personalLiveData,
//                object : RemoteEventEmitter {
//                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                    }
//
//                    override fun onEvent(event: StatusEvent) {
//                        if (event == StatusEvent.SUCCESS) {
//                            event(event)
//                        }
//                    }
//                }
//            )
//        }
//    }
//
//    fun sendMessage(
//        msg: String,
//        liveId: String,
//        level: Int
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            var message =
//                MessageComposer.composeMessage(msg, "", 3, liveId, level = level)
//            dao.insertOwnerMessageAndShow(message)
//
//            message = dao.getMessagedByUuid(message.uuid, SessionPreferences.id) ?: return@launch
//            ConversationDataRepository.sendDm(
//                message,
//                object : RemoteEventEmitter {
//                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
//                        buildPromptMessage(
//                            "",
//                            msg,
//                            3,
//                            liveId,
//                            message.sendTime + 1
//                        )
//                    }
//
//                    override fun onEvent(event: StatusEvent) {
//                        this@LiveInteractionViewModel.onEvent(event)
//                    }
//                })
//        }
//    }
//
//    fun liveMute(liveId: String, type: Int) {
//        viewModelScope.launch {
//            DataRepository.liveMute(liveId, type, this@LiveInteractionViewModel)
//        }
//    }
//
//    fun liveHostMute(liveId: String, targetUserId: String) {
//        viewModelScope.launch {
//            DataRepository.liveHostMute(
//                liveId,
//                targetUserId,
//                muteLiveData,
//                this@LiveInteractionViewModel
//            )
//        }
//    }
//
//    fun goLiveWith(userId: String) {
//        viewModelScope.launch {
////            DataRepository.goLiveWith(userId, roomId, inviteData, this@LiveInteractionViewModel)
//        }
//    }
//
//    fun cancelLiveWith(liveId: String) {
//        viewModelScope.launch {
////            DataRepository.cancelLiveWith(liveId, cancelLiveWithData, this@LiveInteractionViewModel)
//        }
//    }
//
//    fun hangUpLiveWith(liveId: String) {
//        viewModelScope.launch {
//            DataRepository.hangUpLiveWith(
//                liveId,
//                "",
//                hangUpLiveWithData,
//                this@LiveInteractionViewModel
//            )
//        }
//    }
//
//    fun likeLive(count: Int, uuid: String, conversationId: String, liveId: String) {
//        viewModelScope.launch {
//            DataRepository.likeLive(
//                conversationId,
//                count,
//                uuid,
//                liveId,
//                likeLiveData,
//                this@LiveInteractionViewModel
//            )
//        }
//    }
//
    fun getGiftList() {
        if (InstaLivePreferences.liveGiftList != null) {
            val giftList =
                Gson().fromJson(InstaLivePreferences.liveGiftList, GiftListData::class.java)
            giftListLiveData.postValue(giftList)
            return
        }
//        viewModelScope.launch {
//            DataRepository.liveGiftList(liveGiftListData, this@LiveInteractionViewModel)
//        }
    }
//
    fun raiseHand(liveId: String, onError: (String) -> Unit, onStatus: (StatusEvent) -> Unit) {
        viewModelScope.launch {
            LiveDataRepository.raiseHandLive(liveId, raiseHandData, object : RemoteEventEmitter {
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    this@LiveViewModel.onError(code, msg, errorType)
                    onError.invoke(msg)
                }

                override fun onEvent(event: StatusEvent) {
                    this@LiveViewModel.onEvent(event)
                    onStatus.invoke(event)
                }
            })
        }
    }

    fun handsDown(liveId: String, onError: (String) -> Unit, onStatus: (StatusEvent) -> Unit) {
        viewModelScope.launch {
            LiveDataRepository.handsDownLive(liveId, handsDownData, object : RemoteEventEmitter {
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    this@LiveViewModel.onError(code, msg, errorType)
                    onError.invoke(msg)
                }

                override fun onEvent(event: StatusEvent) {
                    this@LiveViewModel.onEvent(event)
                    onStatus.invoke(event)
                }
            })
        }
    }

    fun hangUpLiveWith(liveId: String, userId: String, onError: (String) -> Unit, onStatus: (StatusEvent) -> Unit) {
        viewModelScope.launch {
            LiveDataRepository.hangUpLive(liveId, userId, hangUpLiveData, object : RemoteEventEmitter {
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    this@LiveViewModel.onError(code, msg, errorType)
                    onError.invoke(msg)
                }

                override fun onEvent(event: StatusEvent) {
                    this@LiveViewModel.onEvent(event)
                    onStatus.invoke(event)
                }
            })
        }
    }
//
//    suspend fun memberRole(
//        roomId: String,
//        userId: String,
//    ): Role? {
//        return DataRepository.memberRole(roomId, userId, this@LiveInteractionViewModel)
//    }
}