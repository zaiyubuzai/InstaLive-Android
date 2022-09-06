package com.example.instalive.utils

import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE
import com.example.instalive.app.SessionPreferences
import com.example.instalive.model.*
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.model.VenusDirectMessage
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import timber.log.Timber

object LiveSocketIO {
    private val liveOpts = IO.Options()
    private val liveSocket = IO.socket("https://sk-app-test.halley.link", liveOpts)
    private var liveSocketJob: Job? = null
    init{

        liveOpts.path = "/halley_socket_live"
        liveOpts.transports = arrayOf("websocket", "polling")

        liveSocket.io().reconnectionAttempts(3)
        liveSocket.io().reconnectionDelay(30000)
        liveSocket.io().timeout(30000)
        liveSocket.on("activity") { objects ->//sdfsd
            try {
                val json = objects[0].toString()
                Timber.d("live activity = $json")
                val message =
                    Gson().fromJson(json, LiveActivityEvent::class.java)
                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).post(message)
            } catch (e: Exception) {
            }
        }.on("comment") { objects ->//sdfsdf
            try {
                val json = objects[0].toString()
                Timber.d("live comment = $json")

                val message =
                    Gson().fromJson(json, VenusDirectMessage::class.java)

                reportSocketReceived(message.reportUUID)
            } catch (e: Exception) {
            }
        }.on("like") { objects ->//sdf
            try {
                val json = objects[0].toString()
                Timber.d("live like = $json")
                val message =
                    Gson().fromJson(json, LikeEvent::class.java)
                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).post(message)
            } catch (e: Exception) {
            }
        }.on("system") { objects ->//sdfsd
            try {
                val json = objects[0].toString()
                Timber.d("live system = $json")
                val message =
                    Gson().fromJson(json, LiveSystemEvent::class.java)
                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).post(message)
            } catch (e: Exception) {
            }
        }.on("gift") { objects ->//sdfsd
            try {
                val json = objects[0].toString()
                Timber.d("live gift = $json")
                val message =
                    Gson().fromJson(json, LiveGiftEvent::class.java)
                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).post(message)
            } catch (e: Exception) {
            }
        }.on("remove") { objects ->//sdf
            try {
                val json = objects[0].toString()
                Timber.d("live remove = $json")
                val message =
                    Gson().fromJson(json, LiveRemoveEvent::class.java)
                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).post(message)
            } catch (e: Exception) {
            }
        }.on("live_state") { objects ->//sdfs
            try {
                val json = objects[0].toString()
                Timber.d("live live_state = $json")
                val message =
                    Gson().fromJson(json, LiveStateEvent::class.java)
                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).post(message)
            } catch (e: Exception) {
            }
        }.on("live_with_invite") { objects ->//上麦-邀请
            try {
                val json = objects[0].toString()
                Timber.d("live live_with_invite: $json")
                val message =
                    Gson().fromJson(json, LiveWithInviteEvent::class.java)
//                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).postDelay(message, 1000)
            } catch (e: Exception) {
            }
        }.on("live_with_cancel") { objects ->//上麦-取消
            try {
                val json = objects[0].toString()
                Timber.d("live live_with_cancel: $json")
                val message =
                    Gson().fromJson(json, LiveWithCancelEvent::class.java)
//                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).postDelay(message, 1000)
            } catch (e: Exception) {
            }
        }.on("live_with_reject") { objects ->//sdfsd
            try {
                val json = objects[0].toString()
                Timber.d("live live_with_reject: $json")
                val message =
                    Gson().fromJson(json, LiveWithRejectEvent::class.java)
//                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).postDelay(message, 1000)
            } catch (e: Exception) {
            }
        }.on("live_with_agree") { objects ->//sdfsd
            try {
                val json = objects[0].toString()
                Timber.d("live live_with_agree: $json")
                val message =
                    Gson().fromJson(json, LiveWithAgreeEvent::class.java)
//                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).postDelay(message, 1000)
            } catch (e: Exception) {
            }
        }.on("live_with_hang_up") { objects ->//sdfsd
            try {
                val json = objects[0].toString()
                Timber.d("live live_with_hang_up: $json")
                val message =
                    Gson().fromJson(json, LiveWithHangupEvent::class.java)
//                reportSocketReceived(message.reportUUID)
                LiveEventBus.get(EVENT_BUS_KEY_LIVE).postDelay(message, 1000)
            } catch (e: Exception) {
            }
        }.on(Socket.EVENT_CONNECT) {
            Timber.d("live socket: EVENT_CONNECT")
            stopLiveSocketReconnect()
        }.on(Socket.EVENT_DISCONNECT) {
            Timber.d("live socket: EVENT_DISCONNECT")
        }.on(Socket.EVENT_CONNECT_ERROR) {
            Timber.d("live socket: EVENT_CONNECT_ERROR")
            liveSocketReconnect()
        }
    }

    fun initLiveSocket(liveId: String) {
        Timber.d("initLiveSocket conversationId: $liveId")
        if (liveId.isEmpty()) return
        liveOpts.query =
            "ui=${SessionPreferences.id}&ut=${SessionPreferences.token}&di=${SessionPreferences.deviceId}&ch=$liveId"
        liveSocket.connect()
    }

    fun releaseLiveSocket() {
        stopLiveSocketReconnect()
        liveSocket.disconnect()
        Timber.d("releaseLiveSocket")
    }

    private fun liveSocketReconnect() {
        if (liveSocketJob == null) {
            liveSocketJob = GlobalScope.launch {
                while (isActive) {
                    delay(5000)
                    Timber.d("live socket have net")
                    if (!liveSocket.connected()) {
                        liveSocket.connect()
                    } else {
                        stopLiveSocketReconnect()
                    }
                }
            }
        }
    }

    private fun stopLiveSocketReconnect() {
        liveSocketJob?.cancel()
        liveSocketJob = null
    }

    private fun reportSocketReceived(uuid: String){

    }
}