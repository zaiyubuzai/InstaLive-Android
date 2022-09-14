package com.example.instalive.app.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.MediaPathUtil
import com.example.baselibrary.utils.MediaPathUtil.coverPath
import com.example.baselibrary.utils.MediaPathUtil.getVideoWH
import com.example.baselibrary.utils.ZipUtils
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.InstaLiveApp
import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.api.ConversationDataRepository
import com.example.instalive.api.DataRepository
import com.example.instalive.api.LiveDataRepository
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.db.MessageComposer
import com.example.instalive.model.AccountBalanceData
import com.example.instalive.model.JoinLiveError
import com.example.instalive.model.LiveStateInfo
import com.example.instalive.model.TokenInfo
import com.google.gson.Gson
import com.hw.videoprocessor.VideoProcessor
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageEntity.Companion.SEND_STATUS_FAILED
import com.venus.dm.db.entity.MessageEntity.Companion.SEND_STATUS_SUCCESS
import com.venus.framework.util.isNeitherNullNorEmpty
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class SharedViewModel : BaseViewModel() {

    val dao = InstaLiveDBProvider.db.directMessagingDao()

    val verifyCodePhoneLeft = MutableLiveData<Int>()
    var verifyCodePhoneJob: Job? = null
    val phonePasscodeMap = mutableMapOf<String, Long>()

    val currentConversationData = MutableLiveData<ConversationsEntity>()
    val accountBalanceData = MutableLiveData<AccountBalanceData>()
    val videoMessageQueue = ConcurrentLinkedQueue<MessageEntity>()
    val videoMessageLiveIdQueue = ConcurrentLinkedQueue<String>()
    val videoMessageDoingQueue = ConcurrentLinkedQueue<MessageEntity>()

    val cloudinaryUploadJob = mutableMapOf<String, Job>()

    val liveLeaveData = MutableLiveData<Any>()

    fun getAccountBalance() {
        viewModelScope.launch {
            DataRepository.getAccountBalance(accountBalanceData, this@SharedViewModel)
        }
    }

    fun leaveLive(liveId: String) {
        viewModelScope.launch {
            LiveDataRepository.leaveLive(liveId, liveLeaveData, this@SharedViewModel)
        }
    }

    fun updateConversationLastLeaveTimeToken(conId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val con = dao.getConversationByConId(conId, SessionPreferences.id)
            con?.let {
                it.lastLeaveTimetoken = it.lastMsgTimetoken
                dao.updateConversation(it)
            }
        }
    }

    fun startGlobalVerifyCodePhoneTicking(timestamp: Long) {
        verifyCodePhoneJob?.cancel()
        verifyCodePhoneJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                if (System.currentTimeMillis() - timestamp < 60 * 1000) {
                    val count = (timestamp + 60 * 1000 - System.currentTimeMillis()).toInt() / 1000
                    for (i in count downTo 0) {
                        verifyCodePhoneLeft.postValue(i)
                        delay(1000)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun sendImageMessage(
        liveId: String,
        showType: Int,
        imagePath: String,
        conId: String,
        width: Int,
        height: Int,
        level: Int
    ) {
        GlobalScope.launch {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)
            options.outWidth
            options.outHeight
            val orientation = ZipUtils.pictureDegree(imagePath)

            var outputImagePath = imagePath
            if (width * height > 1000 * 1000) {
                var bmp = BitmapFactory.decodeFile(imagePath)
                if (orientation > 0) {
                    bmp = rotateBitmap(bmp, orientation.toFloat())
                }
                if (bmp != null) {
                    val current = (System.currentTimeMillis() / 1000).toString()
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
                    val time = sdf.format(Date(java.lang.Long.valueOf(current + "000")))
                    val saveFileName = String.format("fambase_image_%s.jpg", time)
                    outputImagePath = MediaPathUtil.getCustomImageOutputPath(
                        context = InstaLiveApp.appInstance,
                        saveFileName
                    )
                    ZipUtils.qualityCompress(bmp, File(outputImagePath))
                    delay(100)
                }
            }
            val message = MessageComposer.composeImageMessage(
                outputImagePath,
                conId,
                showType,
                liveId,
                level = level
            )
            dao.insertOwnerMessageAndShow(message)
            initiateMessagePhotoUpload(liveId, message)
        }
    }

    private fun rotateBitmap(image: Bitmap?, degress: Float): Bitmap? {
        if (image != null) {
            val m = Matrix()
            m.postRotate(degress)
            return Bitmap.createBitmap(
                image, 0, 0, image.width, image.height, m,
                true
            )
        }
        return image
    }

    private fun initiateMessagePhotoUpload(
        liveId: String,
        messageEntity: MessageEntity
    ) {
        val timeoutJob = GlobalScope.launch {
            delay(30000)
            val message = dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
            if (message != null) {
                message.sendStatus = SEND_STATUS_FAILED
                dao.updateMessageAndUpdateConversation(message, true)
                cloudinaryUploadJob.remove(messageEntity.uuid)
            }
        }
        cloudinaryUploadJob[messageEntity.uuid] = timeoutJob
        viewModelScope.launch {
            DataRepository.uploadDMImage(messageEntity.localResPath ?: "",
                messageEntity.conId,
                object : RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val message =
                                dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
                            cloudinaryUploadJob[messageEntity.uuid]?.cancel()
                            if (message != null) {
                                message.sendStatus = SEND_STATUS_FAILED
                                dao.updateMessageAndUpdateConversation(message)
                            }
                        }
                    }

                    override fun onEvent(event: StatusEvent) {
                    }
                }) {
                viewModelScope.launch(Dispatchers.IO) {
                    val message = dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
                    cloudinaryUploadJob[messageEntity.uuid]?.cancel()
                    if (message != null) {
                        val payload = Gson().fromJson(
                            messageEntity.payload,
                            MessageEntity.Payload::class.java
                        )
                        payload.url = it
                        message.payload = payload.toString()
                        Timber.d("1 send image ：${message}")
                        ConversationDataRepository.sendDm(
                            message,
                            object : RemoteEventEmitter {
                                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                                    this@SharedViewModel.onError(code, msg, errorType)
                                    buildPromptMessage(
                                        messageEntity.conId,
                                        msg,
                                        payload.showType,
                                        liveId,
                                        message.sendTime + 1
                                    )
                                }

                                override fun onEvent(event: StatusEvent) {
                                    this@SharedViewModel.onEvent(event)
                                }

                            }
                        )
                    }
                }
            }
        }
    }

    fun sendVideoMessage(
        videoPath: String,
        conId: String,
        w: Int,
        h: Int,
        duration: Long,
        size: Long,
        showType: Int,
        liveId: String,
        level: Int,
    ) {
        //先用本地视频的信息插入数据库，让ui上有信息展示出来
        GlobalScope.launch {
            var width = w
            var height = h
            var messageEntity: MessageEntity? = null
            val cover = coverPath(videoPath, appInstance.applicationContext)
            val wh = getVideoWH(videoPath)
            if (wh != null) {
                width = wh[0]
                height = wh[1]
            }
            if (width > height && width > 1280) {
                val scale = width / 1280f
                height = (height / scale).toInt()
                width = 1280
            } else if (height >= width && height > 1280) {
                val scale = height / 1280f
                height = 1280
                width = (width / scale).toInt()
            }
            messageEntity = MessageComposer.composeVideoMessage(
                videoPath,
                if (cover?.isNeitherNullNorEmpty() == true) cover else "",
                conId,
                width.toString(),
                height.toString(),
                duration.toString(),
                size.toString(),
                showType,
                liveId,
                level = level
            )

            dao.insertOwnerMessageAndShow(messageEntity)
            videoMessageQueue.add(messageEntity)
            videoMessageLiveIdQueue.add(liveId)
            videoMessageStart(appInstance)
        }
    }

    private suspend fun videoMessageStart(context: Context) {
        GlobalScope.launch {
            //再本地上传
            if (videoMessageQueue.isEmpty()) return@launch

            if (videoMessageDoingQueue.isEmpty()) {
                val pollMessage = videoMessageQueue.poll()
                val liveId = videoMessageLiveIdQueue.poll()
                videoMessageDoingQueue.add(pollMessage)

                val outputPath = MediaPathUtil.getGenerateVideoOutputPath(context)

                val payload = MessageEntity.Payload.fromJson(pollMessage.payload) ?: return@launch

                VideoProcessor.processor(context)
                    .input(pollMessage.localResPath) // .input(inputVideoUri)
                    .output(outputPath)
                    //以下参数全部为可选
                    .outWidth(payload.width ?: 100)
                    .outHeight(payload.height ?: 100)
                    .progressListener {
                        Timber.d("VideoProcessor progress: $it")
                        if (it >= 1.0f) {
                            viewModelScope.launch(Dispatchers.IO) {
//                            val cover = coverPath(
//                                outputPath,
//                                context
//                            )

                                if ((payload.width ?: 100) * (payload.height
                                        ?: 100) > 1000 * 1000
                                ) {
                                    val bmp = BitmapFactory.decodeFile(pollMessage.localThumbnail)

                                    if (bmp != null) {
                                        val current = (System.currentTimeMillis() / 1000).toString()
                                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
                                        val time =
                                            sdf.format(Date(java.lang.Long.valueOf(current + "000")))
                                        val saveFileName =
                                            String.format("fambase_image_%s.jpg", time)
                                        val outputImagePath =
                                            MediaPathUtil.getCustomImageOutputPath(
                                                context = InstaLiveApp.appInstance,
                                                saveFileName
                                            )
                                        ZipUtils.qualityCompress(bmp, File(outputImagePath))
                                        pollMessage.localThumbnail = outputImagePath
                                    }
                                }
                                val videoFile = File(outputPath)
                                pollMessage?.let { m ->
                                    dao.updateMessageAndUpdateConversation(m)
                                    initiateVideoCoverS3Upload(
                                        m,
                                        outputPath,
                                        m.localThumbnail ?: "",
                                        liveId,
                                        if (videoFile.exists()) videoFile.length() else 10
                                    )
                                }
                            }
                        }
                    }
                    .process()
            }
        }
    }

    private fun initiateVideoCoverS3Upload(
        messageEntity: MessageEntity,
        compressVideoPath: String,
        compressVideoCoverPath: String,
        liveId: String,
        size: Long
    ) {
        val timeoutJob = GlobalScope.launch {
            delay(30000)
            val message = dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
            if (message != null) {
                val coverFile = File(compressVideoCoverPath)
                if (coverFile.exists()) coverFile.delete()

                val videoFile = File(compressVideoPath)
                if (videoFile.exists()) videoFile.delete()
                message.sendStatus = SEND_STATUS_FAILED
                dao.updateMessageAndUpdateConversation(message, true)
                cloudinaryUploadJob.remove(messageEntity.uuid)
                videoMessageDoingQueue.clear()
                videoMessageStart(appInstance)
            }
        }
        cloudinaryUploadJob[messageEntity.uuid] = timeoutJob
        viewModelScope.launch {
            //这里传图片
            DataRepository.uploadDMImage(compressVideoCoverPath,
                messageEntity.conId,
                object : RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        viewModelScope.launch(Dispatchers.IO) {
                            cloudinaryUploadJob[messageEntity.uuid]?.cancel()
                            cloudinaryUploadJob.remove(messageEntity.uuid)

                            val coverFile = File(compressVideoCoverPath)
                            if (coverFile.exists()) coverFile.delete()

                            val videoFile = File(compressVideoPath)
                            if (videoFile.exists()) videoFile.delete()

                            val message =
                                dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
                            if (message != null) {
                                message.sendStatus = SEND_STATUS_FAILED
                                dao.updateMessageAndUpdateConversation(message)
                            }
                            videoMessageDoingQueue.clear()
                            videoMessageStart(appInstance)
                        }
                    }

                    override fun onEvent(event: StatusEvent) {
                    }
                }) {
                viewModelScope.launch(Dispatchers.IO) {
                    val message = dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
                    cloudinaryUploadJob[messageEntity.uuid]?.cancel()
                    cloudinaryUploadJob.remove(messageEntity.uuid)
                    if (message != null) {
                        Timber.d("1 send video cover ：$message")
//                        val coverFile = File(compressVideoCoverPath)
//                        if (coverFile.exists()) coverFile.delete()
                        initiateVideoS3Upload(
                            messageEntity,
                            compressVideoPath,
                            it,
                            liveId,
                            size
                        )
                    }
                }
            }
        }
    }

    private fun initiateVideoS3Upload(
        messageEntity: MessageEntity,
        compressVideoPath: String,
        compressCoverPathUrl: String,
        liveId: String,
        size: Long
    ) {
        val timeoutJob = GlobalScope.launch {
            delay(30000)
            val message = dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
            if (message != null) {
                val videoFile = File(compressVideoPath)
                if (videoFile.exists()) videoFile.delete()
                message.sendStatus = SEND_STATUS_FAILED
                dao.updateMessageAndUpdateConversation(message, true)
                cloudinaryUploadJob.remove(messageEntity.uuid)
                videoMessageDoingQueue.clear()
                videoMessageStart(appInstance)
            }
        }
        cloudinaryUploadJob[messageEntity.uuid] = timeoutJob
        viewModelScope.launch {
            //这里传图片
            DataRepository.uploadDMVideo(
                compressVideoPath,
                messageEntity.conId,
                object : RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val message =
                                dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
                            cloudinaryUploadJob[messageEntity.uuid]?.cancel()
                            cloudinaryUploadJob.remove(messageEntity.uuid)
                            if (message != null) {
                                val videoFile = File(compressVideoPath)
                                if (videoFile.exists()) videoFile.delete()
                                message.sendStatus = SEND_STATUS_FAILED
                                dao.updateMessageAndUpdateConversation(message)
                            }
                            videoMessageDoingQueue.clear()
                            videoMessageStart(appInstance)
                        }
                    }

                    override fun onEvent(event: StatusEvent) {
                    }
                }) {
                viewModelScope.launch(Dispatchers.IO) {
                    val message = dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
                    cloudinaryUploadJob[messageEntity.uuid]?.cancel()
                    cloudinaryUploadJob.remove(messageEntity.uuid)
                    if (message != null) {
                        val videoFile = File(compressVideoPath)
                        if (videoFile.exists()) videoFile.delete()
                        Timber.d("2 send video ：$message")
                        val conversation = dao.getConversationByConId(message.conId, message.userId)
                        if (conversation != null && SessionPreferences.id == message.userId) {
                            message.sendStatus = SEND_STATUS_SUCCESS
                            val payload =
                                MessageEntity.Payload.fromJson(message.payload) ?: return@launch
                            payload.url = it
                            payload.size = size
                            payload.cover = compressCoverPathUrl
                            message.payload = payload.toString()
                            dao.updateMessageAndUpdateConversation(message)
                            ConversationDataRepository.sendDm(
                                message,
                                object : RemoteEventEmitter {
                                    override fun onError(
                                        code: Int,
                                        msg: String,
                                        errorType: ErrorType
                                    ) {
                                        this@SharedViewModel.onError(code, msg, errorType)
                                        buildPromptMessage(
                                            messageEntity.conId,
                                            msg,
                                            payload.showType,
                                            liveId,
                                            message.sendTime + 1
                                        )
                                    }

                                    override fun onEvent(event: StatusEvent) {
                                        this@SharedViewModel.onEvent(event)
                                    }

                                })
                            videoMessageDoingQueue.clear()
                            videoMessageStart(appInstance)
                        }
                    }
                }
            }
        }
    }

    fun buildPromptMessage(
        conversationId: String,
        msg: String,
        showType: Int,
        liveId: String,
        timeToken: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (msg.isNotEmpty()) {
                val message = MessageComposer.composePromptMessage(
                    msg,
                    conversationId,
                    showType,
                    liveId,
                    timeToken = timeToken
                )
                dao.insertOwnerMessageAndShow(message)
            }
        }
    }

    //region 群直播间
    var liveStateInfoLiveData = MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>()
    var liveJoinData = MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>()
    var liveUsersSizeData = MutableLiveData<Int>()
    var liveOnlineCount = MutableLiveData<String>()
    var isMicrophoneUser = false//本人是否连麦中
    var isMicrophone = false//直播是否为连麦直播
    var liveTokenInfo = MutableLiveData<TokenInfo>()
    fun getLiveToken(liveId: String) {
        viewModelScope.launch {
            LiveDataRepository.getLiveToken(liveId, liveTokenInfo, this@SharedViewModel)
        }
    }
    fun liveRefresh(
        liveId: String,
        liveStateInfoLiveData: MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>,
        onError: (code: Int, msg: String) -> Unit,
        onStatusEvent: (event: StatusEvent) -> Unit
    ) {
        viewModelScope.launch {
            LiveDataRepository.liveRefresh(liveId, liveStateInfoLiveData, object : RemoteEventEmitter {
                override fun onError(code: Int, msg: String, errorType: ErrorType) {
                    onError.invoke(code, msg)
                }

                override fun onEvent(event: StatusEvent) {
                    onStatusEvent.invoke(event)
                }

            })
        }
    }
    //endregion
    fun liveReset(){
        liveStateInfoLiveData = MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>()
        liveJoinData = MutableLiveData<Pair<LiveStateInfo?, JoinLiveError?>>()
        liveTokenInfo = MutableLiveData<TokenInfo>()
        liveUsersSizeData = MutableLiveData<Int>()
        liveOnlineCount = MutableLiveData<String>()
        isMicrophoneUser = false//本人是否连麦中
        isMicrophone = false//直播是否为连麦直播
    }
}