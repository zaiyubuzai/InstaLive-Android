package com.example.instalive.app.live

import android.os.Bundle
import android.view.SurfaceView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.utils.ScreenUtils
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.app.Constants.EVENT_BUS_KEY_APP_STOP
import com.example.instalive.app.Constants.EVENT_BUS_KEY_NOT_GO_BACK
import com.example.instalive.app.Constants.EVENT_BUS_KEY_TELEPHONY
import com.example.instalive.app.Constants.EVENT_BUS_TELEPHONY_IDLE
import com.example.instalive.app.Constants.EVENT_BUS_TELEPHONY_OFFHOOK
import com.example.instalive.app.Constants.EVENT_BUS_TELEPHONY_RINGING
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.base.AppBackgroundObserver
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.app.base.SharedViewModel
import com.example.instalive.app.live.ui.LiveRelativeLayout
import com.example.instalive.model.LiveUserInfo
import com.example.instalive.model.Resolution
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.livesdk.rtc.AgoraManager
import com.venus.livesdk.rtc.EventHandler
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.dimensions.dp
import timber.log.Timber

@ExperimentalStdlibApi
abstract class LiveBaseActivity<VDB : ViewDataBinding> : InstaBaseActivity<LiveViewModel, VDB>(),
    EventHandler, LiveRelativeLayout.OnLiveVideoViewListener {

    protected val liveContainers: MutableList<LiveRelativeLayout> = mutableListOf()
    protected var liveUsers: MutableList<LiveUserInfo> = mutableListOf()

    protected var liveStateStatus = false
    protected var isOpenBeauty = true
    protected var isNotStop = true//某些页面打开后，有可能禁止触发直播间进后台的逻辑
    protected var isLiving = false

    protected var liveId: String? = null

    protected var mToken: String? = null

    protected var mUid: Int = 0

    //从直播详情接口获取 主播的uid
    protected var anchorUid: String? = null
    protected var token: String? = null

    //连麦高度
    private var microphoneHeight: Int = 0
    private var microphoneHeight4: Int = 0
    private var microphoneHeight6: Int = 0
    private var microphoneHeight9: Int = 0
    private var resolution: Resolution? = null

    private var pauseReportJob: Job? = null

    protected val agoraManager by lazy {
        AgoraManager()
    }

    protected val sharedViewModel by lazy {
        appInstance.getAppViewModelProvider()[SharedViewModel::class.java]
    }

    protected val liveCommonViewModel by lazy {
        getViewModel(LiveCommonViewModel::class.java)
    }

    override fun initData(savedInstanceState: Bundle?) {
        removeEventHandler(this)
        appInstance.mRtcEngine?.let {
            agoraManager.initAgora(it)
            agoraManager.initComponents(this, true, 400)
            val surfaceView = agoraManager.getVideoUI(this, true, mUid)
            initLocalVideoView(surfaceView)
        }

        microphoneHeight = dp(280)
        microphoneHeight4 = dp(180)
        microphoneHeight6 = dp(180)
        microphoneHeight9 = dp(180)

        LiveEventBus.get(EVENT_BUS_KEY_TELEPHONY).observe(this, {
            when (it) {
                EVENT_BUS_TELEPHONY_RINGING -> {
                    agoraManager.mRtcEngine?.stopPreview()
                    agoraManager.mRtcEngine?.disableAudio()
                    agoraManager.mRtcEngine?.disableVideo()
                }
                EVENT_BUS_TELEPHONY_OFFHOOK -> {
                    agoraManager.mRtcEngine?.stopPreview()
                    agoraManager.mRtcEngine?.disableAudio()
                    agoraManager.mRtcEngine?.disableVideo()
                }
                EVENT_BUS_TELEPHONY_IDLE -> {
                    if (!AppBackgroundObserver.isAppBackground) {
                        startGetLiveState()
                    }
                }
            }
        })

        LiveEventBus.get(EVENT_BUS_KEY_NOT_GO_BACK).observe(this, {
            isNotStop = true
        })

        LiveEventBus.get(EVENT_BUS_KEY_APP_STOP).observe(this, {
            agoraManager.mRtcEngine?.enableLocalVideo(it == 0)
        })
    }

    abstract fun initLocalVideoView(surfaceView: SurfaceView)
    abstract fun onLiveStateError(code: Int, msg: String)

    fun startGetLiveState() {
        if (liveStateStatus) return
        liveStateStatus = true
        if (liveId != null) {
            //查看直播状态
            viewModel.getLiveState(liveId ?: "",
                sharedViewModel.liveStateInfoLiveData,
                onError = { code, msg ->
                    liveStateStatus = false
                    onLiveStateError(code, msg)
                }, onStatusEvent = { event ->
                    if (event == StatusEvent.SUCCESS) {
                        liveStateStatus = false
                    }
                })
        } else {
            liveStateStatus = false
        }
    }

    protected fun registerEventHandler(handler: EventHandler?) {
        appInstance.registerEventHandler(handler)
    }

    protected fun removeEventHandler(handler: EventHandler?) {
        appInstance.removeEventHandler(handler)
    }

    override fun initViewModel(): LiveViewModel {
        return getActivityViewModel(LiveViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(getImplLayoutId(), viewModel)
    }

    abstract fun getImplLayoutId(): Int

    protected fun microphoneAnchor() {
        Timber.d("${liveContainers.size} ${liveUsers.size}")

        userAndLayout()

        val topMargin = BarUtils.statusBarHeight + dp(68)
        if (liveContainers.size == 1) {
            val viewParams0 = liveContainers[0].layoutParams as RelativeLayout.LayoutParams
            viewParams0.width = ScreenUtils.getScreenWidth(this)
            viewParams0.height = ScreenUtils.getScreenHeightActual(this)
            viewParams0.topMargin = 0
            viewParams0.marginStart = 0
            liveContainers[0].hideAllChildView()
            liveContainers[0].viewRebuild(false)
            liveContainers[0].layoutParams = viewParams0
        } else if (liveContainers.size == 2) {
            val viewParams0 = liveContainers[0].layoutParams as RelativeLayout.LayoutParams
            viewParams0.width = ScreenUtils.getScreenWidth(this) / 2 - dp(7.5f).toInt()
            viewParams0.height = microphoneHeight
            viewParams0.topMargin = topMargin
            viewParams0.marginStart = dp(5)
            liveContainers[0].viewRebuild(true)
            liveContainers[0].layoutParams = viewParams0

            val viewParams1 = liveContainers[1].layoutParams as RelativeLayout.LayoutParams
            viewParams1.width = ScreenUtils.getScreenWidth(this) / 2 - dp(7.5f).toInt()
            viewParams1.height = microphoneHeight
            viewParams1.topMargin = topMargin
            viewParams1.marginStart =
                ScreenUtils.getScreenWidth(this) / 2 + dp(2.5f).toInt()
            liveContainers[1].layoutParams = viewParams1

        } else if (liveContainers.size in 3..4) {
            val viewParams0 = liveContainers[0].layoutParams as RelativeLayout.LayoutParams
            viewParams0.width = ScreenUtils.getScreenWidth(this) / 2 - dp(7.5f).toInt()
            viewParams0.height = microphoneHeight4
            viewParams0.topMargin = topMargin
            viewParams0.marginStart = dp(5)
            liveContainers[0].showHost(true)
            liveContainers[0].layoutParams = viewParams0

            val viewParams1 = liveContainers[1].layoutParams as RelativeLayout.LayoutParams
            viewParams1.width = ScreenUtils.getScreenWidth(this) / 2 - dp(7.5f).toInt()
            viewParams1.height = microphoneHeight4
            viewParams1.topMargin = topMargin
            viewParams1.marginStart =
                ScreenUtils.getScreenWidth(this) / 2 + dp(2.5f).toInt()
            liveContainers[1].layoutParams = viewParams1

            val viewParams2 = liveContainers[2].layoutParams as RelativeLayout.LayoutParams
            viewParams2.width = ScreenUtils.getScreenWidth(this) / 2 - dp(7.5f).toInt()
            viewParams2.height = microphoneHeight4
            viewParams2.topMargin = topMargin + microphoneHeight4 + dp(5)
            viewParams2.marginStart = dp(5)
            liveContainers[2].layoutParams = viewParams2

            val viewParams3 = liveContainers[3].layoutParams as RelativeLayout.LayoutParams
            viewParams3.width = ScreenUtils.getScreenWidth(this) / 2 - dp(7.5f).toInt()
            viewParams3.height = microphoneHeight4
            viewParams3.topMargin = topMargin + microphoneHeight4 + dp(5)
            viewParams3.marginStart =
                ScreenUtils.getScreenWidth(this) / 2 + dp(2.5f).toInt()
            liveContainers[3].layoutParams = viewParams3
        } else if (liveContainers.size in 5..6) {

            val viewWith = ScreenUtils.getScreenWidth(this) / 3 - dp(18) / 3

            val viewParams0 = liveContainers[0].layoutParams as RelativeLayout.LayoutParams
            viewParams0.width = viewWith
            viewParams0.height = microphoneHeight6
            viewParams0.topMargin = topMargin
            viewParams0.marginStart = dp(5)
            liveContainers[0].viewRebuild(true)
            liveContainers[0].layoutParams = viewParams0

            val viewParams1 = liveContainers[1].layoutParams as RelativeLayout.LayoutParams
            viewParams1.width = viewWith
            viewParams1.height = microphoneHeight6
            viewParams1.topMargin = topMargin
            viewParams1.marginStart = dp(5) + viewWith + dp(4)
            liveContainers[1].layoutParams = viewParams1

            val viewParams2 = liveContainers[2].layoutParams as RelativeLayout.LayoutParams
            viewParams2.width = viewWith
            viewParams2.height = microphoneHeight6
            viewParams2.topMargin = topMargin
            viewParams2.marginStart = dp(5) + (viewWith + dp(4)) * 2
            liveContainers[2].layoutParams = viewParams2

            val viewParams3 = liveContainers[3].layoutParams as RelativeLayout.LayoutParams
            viewParams3.width = viewWith
            viewParams3.height = microphoneHeight6
            viewParams3.topMargin = topMargin + microphoneHeight6 + dp(4)
            viewParams3.marginStart = dp(5)
            liveContainers[3].layoutParams = viewParams3

            val viewParams4 = liveContainers[4].layoutParams as RelativeLayout.LayoutParams
            viewParams4.width = viewWith
            viewParams4.height = microphoneHeight6
            viewParams4.topMargin = topMargin + microphoneHeight6 + dp(4)
            viewParams4.marginStart = dp(5) + viewWith + dp(4)
            liveContainers[4].layoutParams = viewParams4

            val viewParams5 = liveContainers[5].layoutParams as RelativeLayout.LayoutParams
            viewParams5.width = viewWith
            viewParams5.height = microphoneHeight6
            viewParams5.topMargin = topMargin + microphoneHeight6 + dp(4)
            viewParams5.marginStart = dp(5) + (viewWith + dp(4)) * 2
            liveContainers[5].layoutParams = viewParams5

        } else if (liveContainers.size in 7..9) {
            val viewWith = ScreenUtils.getScreenWidth(this) / 3 - dp(18) / 3

            val viewParams0 = liveContainers[0].layoutParams as RelativeLayout.LayoutParams
            viewParams0.width = viewWith
            viewParams0.height = microphoneHeight6
            viewParams0.topMargin = topMargin
            viewParams0.marginStart = dp(5)
            liveContainers[0].viewRebuild(true)
            liveContainers[0].layoutParams = viewParams0

            val viewParams1 = liveContainers[1].layoutParams as RelativeLayout.LayoutParams
            viewParams1.width = viewWith
            viewParams1.height = microphoneHeight6
            viewParams1.topMargin = topMargin
            viewParams1.marginStart = dp(5) + viewWith + dp(4)
            liveContainers[1].layoutParams = viewParams1

            val viewParams2 = liveContainers[2].layoutParams as RelativeLayout.LayoutParams
            viewParams2.width = viewWith
            viewParams2.height = microphoneHeight6
            viewParams2.topMargin = topMargin
            viewParams2.marginStart = dp(5) + (viewWith + dp(4)) * 2
            liveContainers[2].layoutParams = viewParams2

            val viewParams3 = liveContainers[3].layoutParams as RelativeLayout.LayoutParams
            viewParams3.width = viewWith
            viewParams3.height = microphoneHeight6
            viewParams3.topMargin = topMargin + microphoneHeight6 + dp(4)
            viewParams3.marginStart = dp(5)
            liveContainers[3].layoutParams = viewParams3

            val viewParams4 = liveContainers[4].layoutParams as RelativeLayout.LayoutParams
            viewParams4.width = viewWith
            viewParams4.height = microphoneHeight6
            viewParams4.topMargin = topMargin + microphoneHeight6 + dp(4)
            viewParams4.marginStart = dp(5) + viewWith + dp(4)
            liveContainers[4].layoutParams = viewParams4

            val viewParams5 = liveContainers[5].layoutParams as RelativeLayout.LayoutParams
            viewParams5.width = viewWith
            viewParams5.height = microphoneHeight6
            viewParams5.topMargin = topMargin + microphoneHeight6 + dp(4)
            viewParams5.marginStart = dp(5) + (viewWith + dp(4)) * 2
            liveContainers[5].layoutParams = viewParams5

            val viewParams6 = liveContainers[6].layoutParams as RelativeLayout.LayoutParams
            viewParams6.width = viewWith
            viewParams6.height = microphoneHeight6
            viewParams6.topMargin = topMargin + (microphoneHeight6 + dp(4)) * 2
            viewParams6.marginStart = dp(5)
            liveContainers[6].layoutParams = viewParams6

            val viewParams7 = liveContainers[7].layoutParams as RelativeLayout.LayoutParams
            viewParams7.width = viewWith
            viewParams7.height = microphoneHeight6
            viewParams7.topMargin = topMargin + (microphoneHeight6 + dp(4)) * 2
            viewParams7.marginStart = dp(5) + viewWith + dp(4)
            liveContainers[7].layoutParams = viewParams7

            val viewParams8 = liveContainers[8].layoutParams as RelativeLayout.LayoutParams
            viewParams8.width = viewWith
            viewParams8.height = microphoneHeight6
            viewParams8.topMargin = topMargin + (microphoneHeight6 + dp(4)) * 2
            viewParams8.marginStart = dp(5) + (viewWith + dp(4)) * 2
            liveContainers[8].layoutParams = viewParams8
        }
        changeNetworkUIPosition(liveContainers.size <= 1)
        sharedViewModel.liveUsersSizeData.postValue(liveContainers.size)
//        microphoneAudienceCover?.isVisible = true
//        screenCover?.isVisible = true
    }

    abstract fun changeNetworkUIPosition(isSingleLive: Boolean)
    abstract fun addLiveVideoContainer(liveRl: LiveRelativeLayout)
    abstract fun removeLiveVideoContainer(liveRl: LiveRelativeLayout)
    abstract fun showInteractionFragment()

    private fun userAndLayout() {
        var needLayoutCount = 1
        if (liveUsers.size == 2) {
            needLayoutCount = 2
        } else if (liveUsers.size in 3..4) {
            needLayoutCount = 4
        } else if (liveUsers.size in 5..6) {
            needLayoutCount = 6
        } else if (liveUsers.size in 7..9) {
            needLayoutCount = 9
        }

        if (liveContainers.size > needLayoutCount) {
            for (i in 1..(liveContainers.size - needLayoutCount)) {
                removeLiveVideoContainer(liveContainers.last())
                liveContainers.removeLast()
            }
        } else if (liveContainers.size < needLayoutCount) {
            for (i in 1..(needLayoutCount - liveContainers.size)) {
                val liveRl = LiveRelativeLayout(this)
                val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                liveRl.layoutParams = layoutParams
                liveRl.showRaiseHandUI(true)
                liveRl.changeRaiseHandUI(LiveRelativeLayout.Raise_Hand_Cannot)
                addLiveVideoContainer(liveRl)
                liveContainers.add(liveRl)
            }
        }
    }

    protected fun setLiveResolution() {
        var width = resolution?.liveWidth ?: 720
        var height = resolution?.liveHigh ?: 1280
        val size = liveUsers.size
        if (size in 1..2) {
            width /= size
            height /= size
        } else if (size in 3..4) {
            width /= 2
            height /= 2
        } else if (size in 5..6) {
            width /= 3
            height /= 2
        } else {
            width /= 3
            height /= 3
        }
        agoraManager.setVideoEncoderResolution(
            width,
            height,
            VideoEncoderConfiguration.STANDARD_BITRATE
        )
    }

    protected fun isLiveViewTouched(x: Int, y: Int) {
        liveContainers.toMutableList().forEach {
            Timber.d("x = $x y = $y")
            it.getHangUpGlobalRect(x, y)
            it.getAvatarGlobalRect(x, y)
            it.getRaiseHandGlobalRect(x, y)
        }
    }

    protected fun removeRemoteView(userId: String) {
        liveContainers.toMutableList().forEach {
            it.liveUserInfo?.let { lui ->
                if (lui.userId == userId) {
                    if (userId == SessionPreferences.id) {
                        it.isVisible = false
                    } else {
                        it.removeLiveVideoSurfaceView()
                        removeLiveVideoContainer(it)
                    }
                    liveContainers.remove(it)
                }
            }
        }

        microphoneAnchor()
    }

    protected fun pauseReport() {
        if (pauseReportJob == null) {
            pauseReportJob = lifecycleScope.launch(Dispatchers.IO) {
                delay(3000)
                if (isLiving) {//看播角度有些情况不算是在live中，
                    viewModel.liveUIPause(liveId?:"", if (viewModel.isMicrophoneUser.value == true) 2 else 9)
                }
            }
        }
    }

    protected fun resumeReport() {
        if (pauseReportJob != null) {
            pauseReportJob?.cancel()
            pauseReportJob = null
        }
        if (isLiving) {
            viewModel.liveUIResume(liveId?:"", if (viewModel.isMicrophoneUser.value == true) 2 else 9)
        }
    }

    // region EventHandler
    override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats?) {
        TODO("Not yet implemented")
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            showInteractionFragment()
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        TODO("Not yet implemented")
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        TODO("Not yet implemented")
    }

    override fun onLastmileQuality(quality: Int) {
        TODO("Not yet implemented")
    }

    override fun onLastmileProbeResult(result: IRtcEngineEventHandler.LastmileProbeResult?) {
        TODO("Not yet implemented")
    }

    override fun onLocalVideoStats(stats: IRtcEngineEventHandler.LocalVideoStats?) {
        TODO("Not yet implemented")
    }

    override fun onRtcStats(stats: IRtcEngineEventHandler.RtcStats?) {
        TODO("Not yet implemented")
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        TODO("Not yet implemented")
    }

    override fun onRemoteVideoStats(stats: IRtcEngineEventHandler.RemoteVideoStats?) {
        TODO("Not yet implemented")
    }

    override fun onRemoteAudioStats(stats: IRtcEngineEventHandler.RemoteAudioStats?) {
        TODO("Not yet implemented")
    }

    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        TODO("Not yet implemented")
    }

    override fun onTokenPrivilegeWillExpire(token: String?) {
        TODO("Not yet implemented")
    }

    override fun onRequestToken() {
        TODO("Not yet implemented")
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        TODO("Not yet implemented")
    }

    override fun onAudioVolumeIndication(
        speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?,
        totalVolume: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        TODO("Not yet implemented")
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onError(code: Int) {
        TODO("Not yet implemented")
    }
    //endregion

}