package com.example.instalive.utils

import com.example.instalive.app.SessionPreferences
import com.google.gson.Gson
import com.venus.dm.model.VenusDirectMessageWrapper
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


object DMSocketIO : BaseInstaSocket() {
    private var lastTimeStampReported: Boolean = false
    private val opts = IO.Options()
    private val socket: Socket =
        IO.socket("https://sk-app-test.halley.link", opts)

    init {
        GlobalScope.launch {
            lastTimeStamp = dao.getLatestRecievedTimetoken(SessionPreferences.id) ?: 0L
        }

        opts.path = "/halley_socket_app"
        opts.transports = arrayOf("websocket", "polling")

        socket.io().reconnectionAttempts(3)
        socket.io().reconnectionDelay(30000)
        socket.io().timeout(30000)
        socket.on("dm") { objects ->
            handleDMMessage(objects)
        }.on("notify") { objects ->
            try {
                val json = objects[0].toString()
                Timber.d("notify: $json")
//                if (json.contains("new_friend_request_count")) {
//                    val message = Gson().fromJson(json, FriendRequestMessage::class.java)
//                    reportSocketReceived(message.reportUUID)
//                    if (message != null) {
//                        LiveEventBus.get(Constants.EVENT_BUS_KEY_PUB_MESSAGE).post(message)
//                    }
//                } else {
//                    val message =
//                        Gson().fromJson(json, MarsSocketMessage::class.java)
//                    reportSocketReceived(message.reportUUID)
//                    if (message != null) {
//                        LiveEventBus.get(Constants.EVENT_BUS_KEY_PUB_MESSAGE).post(message)
//                    }
//                }
            } catch (e: Exception) {
            }
        }.on("unread_pull") { objects ->
            try {
                val json = objects[0].toString()
                Timber.d("unread_pull: $json")
//                val message =
//                    Gson().fromJson(json, UnreadPullMessage::class.java)
//                onUnreadPullMessage(message)
//                reportSocketReceived(message.reportUUID)
            } catch (e: Exception) {
            }

        }.on("ping") { objects ->
            Timber.d("ping: ")
            try {
                val json = objects[0].toString()
                Timber.d("ping: $json")
//                val message =
//                    Gson().fromJson(json, UnreadPullMessage::class.java)
//                onUnreadPullMessage(message)
//                reportSocketReceived(message.reportUUID)
            } catch (e: Exception) {
            }
        }.on("action") { objects ->
            try {
                val json = objects[0].toString()
//                val message =
//                    Gson().fromJson(json, ConversationUpdatingData::class.java)
//                reportSocketReceived(message.reportUUID)
//                onUpdateConversation(message)
            } catch (e: Exception) {
            }
        }.on("realtime_notify") { objects ->
            try {
                val json = objects[0].toString()
//                val message =
//                    Gson().fromJson(json, InAppNotificationData::class.java)
//                reportSocketReceived(message.reportUUID)
                Timber.d("realtime_notify: $json")
//                message.action = 1
//                pendingExecutionInAppNotificationData.add(message)
//                dealWithPendingInAppNotification()
            } catch (e: Exception) {
            }
        }.on("signout") { objects ->
            try {
                val json = objects[0].toString()
                Timber.d("signout: $json")
            } catch (e: Exception) {
            }
        }.on(Socket.EVENT_CONNECT) {
            Timber.d("LOADING: EVENT_CONNECT")
        }.on(Socket.EVENT_DISCONNECT) {
            Timber.d("LOADING: EVENT_DISCONNECT")
        }.on(Socket.EVENT_CONNECT_ERROR) {
            Timber.d("LOADING: EVENT_CONNECT_ERROR")
        }
    }

    private fun handleDMMessage(objects: Array<Any>){
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val json = objects[0].toString()
                Timber.d("dm msg = $json")
                val message =
                    Gson().fromJson(json, VenusDirectMessageWrapper::class.java)
                message.isUnreadMessage
                onDirectMessage(message)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initSocket(initDM: Boolean) {
        Timber.d("Alert: init dm...")
        initSocket()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun initSocket() {
        if (socket.connected()) return

        releaseSocket()
        startMessageJob()
        CoroutineScope(Dispatchers.IO).launch {
            val lastReceivedTimeToken = if (lastTimeStampReported && lastTimeStamp > 0L) {
                dao.getLatestRecievedTimetoken(SessionPreferences.id) ?: 0L
            } else {
                lastTimeStampReported = true
                lastTimeStamp
            }
            opts.query =
                "ui=${SessionPreferences.id}&ut=${SessionPreferences.token}&di=${SessionPreferences.deviceId}&lst=${lastReceivedTimeToken}"
            socket.connect()
        }
    }

    override fun releaseSocket() {
        Timber.d("Alert: release dm socket...")
        socket.disconnect()
        stopMessageJob()
    }

}