package com.example.instalive.model

import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveStartInfoEvent(
    val firebaseEvent: String?,
    val firebaseScreenName: String?,
    val firebasePairs: Pair<String, String?>? = null,
    val rfTag: String? = null,
    val rfSource: String? = null,
    val liveId: String,
    val conversationId: String?,
) : LiveEvent