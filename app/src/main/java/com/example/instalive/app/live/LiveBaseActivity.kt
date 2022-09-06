package com.example.instalive.app.live

import android.os.Bundle
import android.view.SurfaceView
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
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
import com.venus.framework.util.isNeitherNullNorEmpty
import com.venus.livesdk.rtc.AgoraManager
import com.venus.livesdk.rtc.EventHandler
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.video.VideoEncoderConfiguration
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import splitties.dimensions.dp
import timber.log.Timber
import java.util.HashSet

@ExperimentalStdlibApi
abstract class LiveBaseActivity<VDB : ViewDataBinding> : InstaBaseActivity<LiveViewModel, VDB>(),
    EventHandler, LiveRelativeLayout.OnLiveVideoViewListener {

    protected val liveContainers: MutableList<LiveRelativeLayout> = mutableListOf()
    protected var liveUsers: MutableList<LiveUserInfo> = mutableListOf()
    protected var addedUidSet = HashSet<Int>()
    protected var liveStateStatus = false
    protected var isFirstLoading = true
    protected var isOpenBeauty = true
    protected var isNotStop = true//某些页面打开后，有可能禁止触发直播间进后台的逻辑
    protected var isLiving = false

    protected var liveId: String? = null

    protected var mToken: String? = null

    protected var mUid: Int = 0

    protected var isAnchor = false

    //从直播详情接口获取 主播的uid
    protected var anchorUid: String? = null
    protected var token: String? = null

    //连麦高度
    private var microphoneHeight: Int = 0
    private var microphoneHeight4: Int = 0
    private var microphoneHeight6: Int = 0
    private var microphoneHeight9: Int = 0
    var resolution: Resolution? = null

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
        registerEventHandler(this)
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

        viewModel.liveTokenInfo.observe(this, {
            agoraManager.renewToken(it.token)
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
    abstract fun setNetworkQuality(quality: Int)
    abstract fun hideLoadingCover()
    abstract fun hidePause()
    abstract fun showPause()

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
                    viewModel.liveUIPause(
                        liveId ?: "",
                        if (viewModel.isMicrophoneUser.value == true) 2 else 9
                    )
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
            viewModel.liveUIResume(
                liveId ?: "",
                if (viewModel.isMicrophoneUser.value == true) 2 else 9
            )
        }
    }

    protected fun showBlurTransformationCover(portrait: String, view: ImageView) {
        Glide.with(this)
            .load(portrait)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(this, 25, 8)))
            .into(view)
    }

    // region EventHandler
    override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats?) {
        Timber.d("sw onLeaveChannel")
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        Timber.d("sw onJoinChannelSuccess channel:$channel uid:$uid elapsed:$elapsed")
        lifecycleScope.launch(Dispatchers.Main) {
            showInteractionFragment()
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        Timber.d("sw onUserOffline uid:$uid reason:$reason")
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        Timber.d("sw onUserJoined uid:$uid elapsed:$elapsed")
    }

    override fun onLastmileQuality(quality: Int) {
        Timber.d("sw onLastmileQuality")
    }

    override fun onLastmileProbeResult(result: IRtcEngineEventHandler.LastmileProbeResult?) {
        Timber.d("sw onLastmileProbeResult")
    }

    override fun onLocalVideoStats(stats: IRtcEngineEventHandler.LocalVideoStats?) {
        Timber.d("sw onLocalVideoStats")
    }

    override fun onRtcStats(stats: IRtcEngineEventHandler.RtcStats?) {
        Timber.d("sw onRtcStats")
    }

    private val QUALITY_GOOD_LIST = listOf(
        io.agora.rtc.Constants.QUALITY_GOOD,
        io.agora.rtc.Constants.QUALITY_UNKNOWN,
        io.agora.rtc.Constants.QUALITY_EXCELLENT
    )

    private val QUALITY_POOR_LIST = listOf(
        io.agora.rtc.Constants.QUALITY_BAD,
        io.agora.rtc.Constants.QUALITY_POOR,
        io.agora.rtc.Constants.QUALITY_VBAD
    )

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        Timber.d("sw onNetworkQuality uid:$uid")
        if (uid != 0) return
        //网络比较差，但是还可以播放

        lifecycleScope.launch(Dispatchers.Main) {
//            networkData?.text = "uid: $uid \n上行网络质量: $txQuality \n下行网络质量: $rxQuality"
            if (!isAnchor) {
                setNetworkQuality(
                    if (if (viewModel.isMicrophoneUser.value == true) {
                            QUALITY_GOOD_LIST.contains(txQuality)
                                    && QUALITY_GOOD_LIST.contains(rxQuality)
                        } else {
                            QUALITY_GOOD_LIST.contains(rxQuality)
                        }
                    ) {
                        1
                    } else if (if (viewModel.isMicrophoneUser.value == true) {
                            QUALITY_POOR_LIST.contains(txQuality)
                                    || QUALITY_POOR_LIST.contains(rxQuality)
                        } else {
                            QUALITY_POOR_LIST.contains(rxQuality)
                        }
                    ) {
                        2
                    } else if (if (viewModel.isMicrophoneUser.value == true)
                            rxQuality == Constants.QUALITY_DOWN
                                    || txQuality == Constants.QUALITY_DOWN
                        else rxQuality == Constants.QUALITY_DOWN
                    ) {
                        3
                    } else {
                        4
                    }
                )
            } else {
                setNetworkQuality(
                    if (listOf(
                            Constants.QUALITY_GOOD,
                            Constants.QUALITY_EXCELLENT
                        ).contains(txQuality)
                    ) {
                        1
                    } else if (QUALITY_POOR_LIST.contains(txQuality)
                    ) {
                        2
                    } else if (txQuality == Constants.QUALITY_DOWN) {
                        3
                    } else {
                        4
                    }
                )
            }
        }
    }

    override fun onRemoteVideoStats(stats: IRtcEngineEventHandler.RemoteVideoStats?) {
        Timber.d("sw onRemoteVideoStats")
    }

    override fun onRemoteAudioStats(stats: IRtcEngineEventHandler.RemoteAudioStats?) {
        Timber.d("sw onRemoteAudioStats")
    }

    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        Timber.d("sw onRemoteVideoStateChanged uid:$uid state:$state reason:$reason elapsed:$elapsed")

        //没有连麦的逻辑

        if (viewModel.isMicrophone.value != true) {
            if (state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_DECODING
                && uid == anchorUid?.toInt()
            ) {
                Timber.d("StateChanged 1")
                if (!isAnchor) {
                    hidePause()
                    if (liveContainers.isNotEmpty()) {
                        liveContainers[0].hidePause()
                    }
                    if (!isFirstLoading) {
                        hideLoadingCover()
                    } else {
                        isFirstLoading = false
                    }
                }
            }
            //主播离开
            else if ((reason == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED)
                && uid == anchorUid?.toInt()
            ) {
                //观众端暂停页面展示
                if (!isAnchor) {
                    Timber.d("live直播暂停 3")
                    if (!isFirstLoading) showPause()
                }
            }
        } else {
            //连麦收到连麦的流
            if (state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_DECODING
                && uid != anchorUid?.toInt()
            ) {
                //连麦 主播端收到连麦人的直播流，隐藏loading

                //连麦 观众端收到流

                hideLoadingCover()
                hidePause()
                val liveContainer = liveContainers.filter {
                    it.liveUserInfo?.uid == uid
                }
                if (liveContainer.isNeitherNullNorEmpty()) {
                    liveContainer[0].hidePause()
                }
            }

            //连麦收到主播端流 对应的 观众端和连麦端的ui
            else if (state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_DECODING
                && uid == anchorUid?.toInt()
            ) {
                //连麦 连麦人收到流的情况
                if (!isAnchor && viewModel.isMicrophoneUser.value == true) {

                    hidePause()
                    hideLoadingCover()
                    val liveContainer = liveContainers.filter {
                        it.liveUserInfo?.uid == uid || it.isHost
                    }
                    if (liveContainer.isNeitherNullNorEmpty()) {
                        liveContainer[0].hidePause()
                    }
                }

                //连麦 观众收到流的情况
                if (!isAnchor && viewModel.isMicrophoneUser.value != true) {
                    hidePause()
                    hideLoadingCover()
                    val liveContainer = liveContainers.filter {
                        it.liveUserInfo?.uid == uid || it.isHost
                    }
                    if (liveContainer.isNeitherNullNorEmpty()) {
                        liveContainer[0].hidePause()
                    }
                }
            }

            //连麦状态 主播断流
            if ((state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_FROZEN
                        || state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_STOPPED)
                && uid == anchorUid?.toInt()
            ) {
                //连麦 连麦人断流的情况
                if (reason != io.agora.rtc.Constants.REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION) {
                    if (!isAnchor && viewModel.isMicrophoneUser.value == true) {
                        hideLoadingCover()
                        val liveContainer = liveContainers.filter {
                            it.liveUserInfo?.uid == uid || it.isHost
                        }
                        if (liveContainer.isNeitherNullNorEmpty()) {
                            liveContainer[0].showPause()
                        }
                    }

                    //连麦 观众断流的情况
                    if (!isAnchor && viewModel.isMicrophoneUser.value != true) {
                        val liveContainer = liveContainers.filter {
                            it.liveUserInfo?.uid == uid || it.isHost
                        }
                        if (liveContainer.isNeitherNullNorEmpty()) {
                            liveContainer[0].showPause()
                        }
                    }
                }
            }

            //连麦状态 连麦人断流
            else if ((state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_FROZEN
                        || state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_STOPPED)
                && uid != anchorUid?.toInt()
            ) {
                //主播展示ui
                if (reason != io.agora.rtc.Constants.REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION) {
                    //观众展示ui
                    if (viewModel.isMicrophoneUser.value != true) {
                        val liveContainer = liveContainers.filter {
                            it.liveUserInfo?.uid == uid || it.isHost
                        }
                        if (liveContainer.isNeitherNullNorEmpty()) {
                            liveContainer[0].showPause()
                        }
                    }
                }
            }
        }

        if (reason == io.agora.rtc.Constants.CONNECTION_CHANGED_TOKEN_EXPIRED) {  //token 过期
            liveId?.let { viewModel.getLiveToken(it) }
        }

    }

    override fun onTokenPrivilegeWillExpire(token: String?) {
        liveId?.let { viewModel.getLiveToken(it) }
    }

    override fun onRequestToken() {
        liveId?.let { viewModel.getLiveToken(it) }
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        Timber.d("sw onConnectionStateChanged state:$state reason:$reason")
    }

    override fun onAudioVolumeIndication(
        speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?,
        totalVolume: Int
    ) {
        Timber.d("sw onAudioVolumeIndication totalVolume:$totalVolume")
    }

    override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        Timber.d("sw onRemoteAudioStateChanged uid:$uid state:$state reason:$reason $elapsed")
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
        Timber.d("sw onUserMuteAudio uid:$uid muted:$muted")
    }

    override fun onError(code: Int) {
        Timber.d("sw onError $code")
    }
//endregion

}