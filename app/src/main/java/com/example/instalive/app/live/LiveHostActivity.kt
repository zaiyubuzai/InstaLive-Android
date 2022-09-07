package com.example.instalive.app.live

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.utils.marsToast
import com.example.instalive.R
import com.example.instalive.app.Constants.ITRCT_TYPE_MAKEUP_OFF
import com.example.instalive.app.Constants.ITRCT_TYPE_MAKEUP_ON
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE_HOST_ACTIONS
import com.example.instalive.app.Constants.ITRCT_TYPE_FLIP
import com.example.instalive.app.Constants.ITRCT_TYPE_LIVE_OFF
import com.example.instalive.app.Constants.LIVE_END
import com.example.instalive.app.Constants.LIVE_START
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.live.ui.LiveRelativeLayout
import com.example.instalive.databinding.ActivityLiveHostBinding
import com.example.instalive.model.*
import com.example.instalive.utils.LiveSocketIO
import com.example.instalive.utils.VenusNumberFormatter
import com.example.instalive.view.CountDownProgressBar
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.framework.util.isNeitherNullNorEmpty
import com.venus.livesdk.rtc.AgoraTokenInfo
import io.agora.rtc.Constants
import io.agora.rtc.models.ClientRoleOptions
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_live_host.*
import kotlinx.coroutines.*
import splitties.alertdialog.appcompat.*
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.bundleOrNull
import splitties.bundle.withExtras
import splitties.dimensions.dp
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec
import splitties.mainhandler.mainHandler
import splitties.views.clearCompoundDrawables
import splitties.views.onClick
import splitties.views.textResource
import timber.log.Timber
import java.util.*

@ExperimentalStdlibApi
class LiveHostActivity : LiveBaseActivity<ActivityLiveHostBinding>() {
    //live end detail请求次数
    private val MAX_LOOP_COUNT = 20
    private var loopCount = 0

    //查看uid对应的surfaceView是否创建
    private var currentFragmentTag: String? = null
    private lateinit var hostFragment: LiveInteractionHostFragment
    lateinit var conversationsEntity: ConversationsEntity
    private var uuid: UUID? = null
    private var createLiveType: String? = null
    var giftTicketData: GiftData? = null
    var justNowGiftTicketData: GiftData? = null
    var eventId: String? = null

    var startLiveLaunch: Job? = null
    var liveEndJob: Job? = null

    var isLiveNeedResume = false

    //正常退出
    var isStandardClose = false

    var isDivideIncome = false

    // 3 2 1 是否倒计时结束
    var isCountTimeOver = false
    var isClickCancel = false
    var isLockedLive = false
    var isStarted = false


    //用户是否可以直播，如果用户没有直播权限则不显示
    private var isCanLive = true

    @SuppressLint("ClickableViewAccessibility")
    override fun initData(savedInstanceState: Bundle?) {
        isAnchor = true
        super.initData(savedInstanceState)
        screenName = "live_host_view"
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        withExtras(RecordExtraSpec) {
            this@LiveHostActivity.isCanLive = isCanLive
            this@LiveHostActivity.uuid = uuid
            this@LiveHostActivity.createLiveType = createLiveType
            this@LiveHostActivity.isLiveNeedResume = isLiveNeedResume
            this@LiveHostActivity.liveId = liveId ?: ""
            this@LiveHostActivity.eventId = eventId
        }
        initUI()
        initListener()
        initObserver()

        cover.setOnClickListener {}

        touchFl.setOnTouchListener { _, event ->
            event?.let {
                isLiveViewTouched(it.rawX.toInt(), it.rawY.toInt())
            }
            false
        }
        if (isLiveNeedResume) {
            showLiveResume()
        }
    }

    private fun initUI() {
        startLiveContainer.setPadding(0, BarUtils.statusBarHeight, 0, 0)
        changeNetworkUIPosition(true)
        //主播端展示的cover
        SessionPreferences.portrait?.let {
            showBlurTransformationCover(it, closeLiveCover)
            showBlurTransformationCover(it, pauseCover)
            showBlurTransformationCover(it, cover)
        }
        divideSwitchBanned.isChecked = SessionPreferences.divideIncomeState
    }

    private fun initListener() {
        recordFlip.onClick {
            agoraManager.switchCamera()
        }

        startLive.onClick {
            startLive()
        }
        cancel.onClick {
            closeLivePrompt?.isVisible = false
            showInteractionContainer()
            isClickCancel = true
        }
        beauty.onClick {
            toggleBeauty()
        }
        close.onClick {
            //有三种情况，在没有点击 1、start点击之前直接关闭  2、start之后停止停止3 2 1  3、倒计时结束调用
            if (isStarted) {
                isStarted = false
                if (!isCountTimeOver) {
                    goBackLiveSetting()
                }
                viewModel.closeLive(liveId)
            } else {
                finish()
            }
        }
        done.onClick {
            finish()
        }

        //空的点击事件，防止下一层ui获取点击事件。 620
        giftTicketContainer.onClick {
//            popupOpenGift(2)
        }
        startGiftLiveAbout.onClick {
            alertDialog {
                title = viewModel.liveInitInfo.value?.paidLiveTipsTitle ?: ""
                message = viewModel.liveInitInfo.value?.paidLiveTipsDesc ?: ""
                okButton()
            }.show()
        }
        startLiveContainer.onClick {}
        closeLiveContainer.onClick {}
        closeLivePrompt.onClick {}
        divideLiveAbout.onClick {
            alertDialog {
                title = viewModel.liveInitInfo.value?.dividePopupTitle ?: ""
                message = viewModel.liveInitInfo.value?.dividePopupDesc ?: ""
                okButton()
            }.show()
        }
        container.onClick {}

        switchBanned.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                giftTicketContainer?.isVisible = isChecked
                giftTicketName?.isVisible = isChecked
                mainHandler.postDelayed({
                    if (isChecked) {
//                        popupOpenGift(2)
                    }
                }, 250)
            }
        }

        divideSwitchBanned.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                lifecycleScope.launch(Dispatchers.IO) {
                    openDivideIncome(isChecked)
                }
            }
            isDivideIncome = isChecked
        }
    }

    /**
     * 返回直播设置页面
     */
    private fun goBackLiveSetting() {
        startLiveLaunch?.cancel()
        performerDesc?.isVisible =
            viewModel.liveInitInfo.value?.performerDesc.isNeitherNullNorEmpty()
        giftLiveContainer?.isVisible =
            viewModel.liveInitInfo.value?.paidLiveFunctionEnable == true
        startLiveContainer?.setBackgroundResource(R.color.translate_black33)
        startLiveLoadingContainer?.isVisible = false
        divideLiveContainer?.isVisible = false
        recordFlip?.isVisible = true
        startLive?.isVisible = true
        beauty?.isVisible = true
        count?.isVisible = false
        isStarted = false
    }

    override fun changeNetworkUIPosition(isSingleLive: Boolean) {
        val layoutParams = networkQualityContainer.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.setMargins(
            0,
            if (!isSingleLive) dp(47) + BarUtils.statusBarHeight else dp(68) + BarUtils.statusBarHeight,
            dp(16),
            0
        )
        networkQualityContainer.layoutParams = layoutParams
    }

    private suspend fun openDivideIncome(isChecked: Boolean) {
        withContext(Dispatchers.Main) {
//            if (isChecked && FambasePreferences.isFirstOpenDivideIncome) {
//                FambasePreferences.isFirstOpenDivideIncome = false
//                showFirstOpenDivideDialog(
//                    viewModel.liveInitInfo.value?.dividePopupTitle ?: "",
//                    viewModel.liveInitInfo.value?.dividePopupDesc ?: ""
//                )
//            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun toggleBeauty() {
//        logFirebaseEvent("start_live_view", if (isOpenBeauty) "open_beauty" else "close_beauty")
        if (isOpenBeauty) {  //guan bi mei yan
            beauty.setImageResource(R.mipmap.live_beauty_no)
        } else {  //da kai mei yan
            beauty.setImageResource(R.mipmap.live_beauty_yes)
        }
        isOpenBeauty = !isOpenBeauty
        LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS)
            .post(if (!isOpenBeauty) ITRCT_TYPE_MAKEUP_OFF else ITRCT_TYPE_MAKEUP_ON)
    }

    private fun showLiveResume() {
        // 创建构建器
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        // 设置参数
        builder.setTitle(getString(R.string.fb_live_stream_resume_desc))
            .setMessage(getString(R.string.fb_live_stream_resume_desc))
            .setPositiveButton(getString(R.string.fb_resume)) { _, _ ->
                startLive(true)
            }.setNegativeButton(getString(R.string.fb_cancel)) { _, _ ->
                if (!isClickCancel) {
                    isStandardClose = true
                    isCountTimeOver = true
                    viewModel.closeLive(liveId)
                }
            }
        builder.create().show()
    }

    private fun startLive(isResumeLive: Boolean = false) {
        isStarted = true
        startLive.isEnabled = false
        viewModel.createLive(
            " ",
            null,
            justNowGiftTicketData?.id,
            null,
            null,
        ) {
            isStarted = false
            alertDialog {
                titleResource = R.string.fb_live_cannot_live_title
                message = it
                okButton {
                    if (isResumeLive) {
                        if (!isClickCancel) {
                            isStandardClose = true
                            isCountTimeOver = true
                            viewModel.closeLive(liveId)
                        }
                    } else {
                        finish()
                    }
                }
                isClickCancel = false
            }.show()
        }
    }

    private fun joinChannel(
        token: String,
        mUid: Int,
    ) {
        mToken = token
//        this.mUid = mUid

        isLiving = true
        val clientRoleOptions = ClientRoleOptions()
//        if (MarsApp.appInstance.appInitData.value?.appFeature?.liveLatencyLevel == 0) {
        clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
//        } else {
//            clientRoleOptions.audienceLatencyLevel =
//                AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY
//        }

        //如果是连麦则是连麦的分辨率
        val width = resolution?.liveWidth ?: 720
        val height = resolution?.liveHigh ?: 1280
        agoraManager.setVideoEncoderResolution(
            width,
            height,
            VideoEncoderConfiguration.STANDARD_BITRATE
        )
        agoraManager.enableLocalAudio = true
        agoraManager.joinChannel(this, true, AgoraTokenInfo(token, liveId ?: "", null, mUid))
    }

    private fun initObserver() {
        //开启直播后的回调
        viewModel.liveInfoLiveData.observe(this, {
            resolution = it.resolution
            liveId = it.id
            LiveSocketIO.initLiveSocket(liveId!!)
            startLive?.isEnabled = true
            if (!isStarted) return@observe
            startLiveLaunch = CoroutineScope(Dispatchers.IO).launch {
                //倒计时3秒进入直播间
                var i = 4
                while (i >= 2) {
                    i--
                    withContext(Dispatchers.Main) {
                        if (i == 3) {
                            startLiveLoadingContainer?.isVisible = true
                            divideLiveContainer?.isVisible = false
                            giftLiveContainer?.isVisible = false
                            performerDesc?.isVisible = false
                            recordFlip?.isVisible = false
                            startLive?.isVisible = false
                            beauty?.isVisible = false
                            count?.isVisible = true
                            startLiveContainer?.setBackgroundResource(R.color.translate_black88)
                        }
                        count?.text = i.toString()
                    }
                    delay(1000)
                }
//                viewModel.liveReport(101, liveId ?: "", liveId ?: "", null)
                withContext(Dispatchers.Main) {
                    isCountTimeOver = true

                    startLiveContainer?.setBackgroundResource(R.color.translate_black33)
                    startLiveLoadingContainer?.isVisible = false
                    startLiveContainer?.isVisible = false
                    count?.isVisible = false
                    count?.text = "3"

                    liveId = it.id
                    anchorUid = it.uid.toString()
                    if (switchBanned.isChecked) {
                        justNowGiftTicketData?.let { liveGiftData ->
                            giftTicketData = liveGiftData
                        }
                    }

                    joinChannel(it.token, it.uid)
                    SessionPreferences.divideIncomeState = divideSwitchBanned?.isChecked ?: false
                }
            }
        })

        //直播间关闭回调
        viewModel.liveCloseLiveData.observe(this, {

            if (isCountTimeOver) {
                closeLiveContainer?.isVisible = true

                hideInteractionContainer()

                if (it != null) {
                    youGotDiamonds.text = VenusNumberFormatter.format(it.gotDiamonds)
                    liveDuration.text = it.liveDuration.toString()
                    diamonds.text = VenusNumberFormatter.format(it.diamonds)
                    viewers.text = VenusNumberFormatter.format(it.viewerCount.toLong())
                    likes.text = VenusNumberFormatter.format(it.likeCount.toLong())

//                    if (it.unlockLiveDiamonds == null) {
//                        unlockLiveDiamonds.text = getString(R.string.un_know_views)
//                    } else {
//                        unlockLiveDiamonds.text = it.unlockLiveDiamonds.toString()
//                    }
//                    youGotDiamonds.isVisible = !it.loadingData
//                    youGotProgress.isVisible = it.loadingData
//                    moreProgress.isVisible = it.loadingData
//                    if (it.loadingData) {
//                        startLiveEndJob()
//                    }
                } else {
                    unlockLiveDiamonds.text = getString(R.string.un_know_views)
                    liveDuration.text = getString(R.string.un_know_time)
                    diamonds.text = getString(R.string.un_know_views)
                    viewers.text = getString(R.string.un_know_views)
                    likes.text = getString(R.string.un_know_likes)
                }

                closeLivePrompt?.isVisible = false
                agoraManager.leaveLiveRoom()
                agoraManager.mRtcEngine = null
            } else {
                agoraManager.mRtcEngine?.enableVideo()
                agoraManager.mRtcEngine?.enableAudio()
            }
        })

        viewModel.errorMessageLiveData.observe(this, {
            startLive?.isEnabled = true
            marsToast(it)
        })

        viewModel.errorTypeLiveData.observe(this, {
            if (it == ErrorType.TIMEOUT || it == ErrorType.NETWORK) {
                startLive.isEnabled = true
            }
        })

        viewModel.errorInfo.observe(this, {
            if (it.first == 7998 || it.first == 1215) {
                showErrorPrompt(it.second)
            } else if (it.first == 6208) {
                marsToast(it.second)
            }
        })

        viewModel.liveEndDetailData.observe(this) {
            if (it == null || loopCount >= MAX_LOOP_COUNT) return@observe
            liveEndJob?.cancel()
            youGotProgress?.isVisible = false
            moreProgress?.isVisible = false

            youGotDiamonds?.isVisible = true
            youGotDiamonds?.text = VenusNumberFormatter.format(it.gotDiamonds)
            moreDesc?.isVisible = true
            moreLL?.isVisible = true
            it.more?.forEachIndexed { index, liveEndMoreData ->
                if (index == 0) {
                    firstMoreValue?.text =
                        VenusNumberFormatter.format(liveEndMoreData.diamonds)
                    firstMoreKey?.text = liveEndMoreData.text
                } else if (index == 1) {
                    secondMoreValue?.text =
                        VenusNumberFormatter.format(liveEndMoreData.diamonds)
                    secondMoreKey?.text = liveEndMoreData.text
                }
            }
        }

        viewModel.liveInitInfo.observe(this) {
            loading?.isVisible = false
            startGiftLiveTitle?.text = it.paidLiveTipsTitle
            divideLiveTitle?.text = it.dividePopupTitle
            switchBanned?.isChecked = it.paidLiveEnable
            performerDesc?.isVisible = it.performerDesc.isNeitherNullNorEmpty()
            performerDesc?.text = it.performerDesc
            giftLiveContainer?.isVisible = it.paidLiveFunctionEnable
            divideLiveContainer?.isVisible = true
            if (it.paidLiveFunctionEnable && InstaLivePreferences.liveGiftList != null) {
                giftTicketName.isVisible = it.paidLiveEnable
                giftTicketContainer.isVisible = it.paidLiveEnable
                val giftList =
                    Gson().fromJson(
                        InstaLivePreferences.liveGiftList,
                        GiftListData::class.java
                    )
                val giftData = giftList.gifts.find { gift ->
                    gift.id == it.defaultTicketGiftId
                }
                if (giftData != null) {
                    giftTicketName.text = getString(
                        R.string.lbl_live_gift_coin_cost,
                        giftData.coins.toString()
                    )
                    Glide.with(this@LiveHostActivity).load(giftData.image)
                        .skipMemoryCache(true).diskCacheStrategy(
                            DiskCacheStrategy.ALL
                        ).into(giftImage)
                    giftTicketData = giftData
                    justNowGiftTicketData = giftData
                }
            }

            recordFlip?.isVisible = true
            startLive?.isVisible = true
            beauty?.isVisible = true

            if (isLiveNeedResume) {
                showLiveResume()
            }
        }

        LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS).observe(this, {
            when (it) {
                ITRCT_TYPE_FLIP -> {  //摄像头翻转
                    agoraManager.switchCamera()
                }
                ITRCT_TYPE_MAKEUP_ON -> {  // 美颜开
                    agoraManager.setBeautyEffectOptions(true)
                    isOpenBeauty = true
                }
                ITRCT_TYPE_MAKEUP_OFF -> {  // 美颜关
                    agoraManager.setBeautyEffectOptions(false)
                    isOpenBeauty = false
                }
                ITRCT_TYPE_LIVE_OFF -> { //直播结束
                    if (viewModel.isMicrophone.value == true && liveUserWithUids.size > 1) {
                        marsToast(R.string.please_hang_up_your_invited_user_live_video)
                    } else {
                        hideInteractionContainer()
                        isClickCancel = false
                        closeLivePrompt?.isVisible = true
                        val countDownProgressBarListener =
                            CountDownProgressBar.OnFinishListener {
                                if (!isClickCancel) {
                                    cancel.isEnabled = false
                                    isStandardClose = true
                                    viewModel.closeLive(liveId)
                                }
                            }
                        countDownProgressBar.setDuration(3000, countDownProgressBarListener)
                    }
                }
            }
        })

        LiveEventBus.get(EVENT_BUS_KEY_LIVE).observe(this, {
            when (it) {
                is LiveStartInfoEvent -> {
                    // 主播
                    alertDialog {
                        messageResource =
                            R.string.you_are_Live_now_can_go_to_another_live_or_radio
                        okButton()
                    }.show()
                }
                is LivePublisherStateEvent -> {
                    if (liveUserWithUids.size > 1) {
                        liveUserWithUids.toMutableList().forEach { liveUserInfo ->
                            if (liveUserInfo.userInfo.userId == it.targetUserInfo.userId) {
                                val index = liveUserWithUids.indexOf(liveUserInfo)
                                val liveC = liveContainers[index]
                                // 1 静音 2 取消静音 3 摄像头关闭 4 摄像头开启
                                if (it.event == 1) {
                                    liveUserWithUids[index].userInfo.mute = 1
                                    liveC.changeMuteState(1)
                                } else if (it.event == 2) {
                                    liveUserWithUids[index].userInfo.mute = 0
                                    liveC.changeMuteState(0)
                                }
                            }
                        }
                    } else {
                        liveUserWithUids.toMutableList().forEach { liveUserInfo ->
                            if (liveUserInfo.userInfo.userId == it.targetUserInfo.userId) {
                                if (it.event == 1) {
                                    liveUserWithUids[0].userInfo.mute = 1
                                    val index = liveUserWithUids.indexOf(liveUserInfo)
                                    val liveC = liveContainers[index]
                                    liveC.changeMuteState(1, false)
                                } else if (it.event == 2) {
                                    liveUserWithUids[0].userInfo.mute = 0
                                    val index = liveUserWithUids.indexOf(liveUserInfo)
                                    val liveC = liveContainers[index]
                                    liveC.changeMuteState(0, false)
                                }
                            }
                        }
                    }
                }
            }
        })

        //获取直播状态监听（每次从后台进入都会走此处）
        sharedViewModel.liveStateInfoLiveData.observe(this, {
            it.first?.let { info ->
                when (info.state) {
                    LIVE_START -> {
                        agoraManager.mRtcEngine?.stopPreview()
                        agoraManager.mRtcEngine?.disableAudio()
                        agoraManager.mRtcEngine?.disableVideo()
                        isLiving = true
                        anchorUid = info.liveWithUserInfos[0].uid.toString()
                        //说明连麦（默认是不连麦模式）
                        if (info.liveWithUserInfos.size > 1) {
                            viewModel.isMicrophoneUser.postValue(false)
                            info.liveWithUserInfos.toList().forEach { liveUsers ->
                                if (liveUsers.userInfo.userId == SessionPreferences.id) {
                                    viewModel.isMicrophoneUser.postValue(true)
                                }
                            }

                            liveUIController(info.liveWithUserInfos)
                            viewModel.isMicrophone.postValue(true)
                        } else {
                            viewModel.isMicrophone.postValue(false)
                            liveUIController(info.liveWithUserInfos)
                        }

                        //直播开播才开始可以推流
                        agoraManager.mRtcEngine?.enableVideo()
                        if (isLockedLive) agoraManager.mRtcEngine?.enableAudio()
                        agoraManager.mRtcEngine?.startPreview()
                        sharedViewModel.liveOnlineCount.postValue(info.onlineStr)
                    }
                    LIVE_END -> {
                        isLiving = false
                        // 展示直播结束画面
                        if (isCountTimeOver) {
                            viewers.text = info.onlineStr
                            diamonds.text = info.diamonds.toString()
                            closeLiveContainer?.isVisible = true
                            agoraManager.mRtcEngine?.disableAudio()
                            agoraManager.mRtcEngine?.disableVideo()
                            agoraManager.mRtcEngine?.leaveChannel()
                            agoraManager.mRtcEngine = null
                        } else {
                            agoraManager.mRtcEngine?.enableVideo()
                            agoraManager.mRtcEngine?.enableAudio()
                        }
                    }
                }
            }
        })
    }

    override fun onLiveStateError(code: Int, msg: String) {
        lifecycleScope.launch {
            delay(2000)
            liveStateStatus = false
            startGetLiveState()
        }
    }

    private fun startLiveEndJob() {
        liveEndJob?.cancel()
        liveEndJob = lifecycleScope.launch {
            while (this.isActive) {
                if (loopCount < MAX_LOOP_COUNT) {
                    liveId?.let { it1 ->
                        viewModel.getLiveEndDetail(it1) { _, _ -> }
                    }
                    loopCount++
                    delay(2000)
                } else {
                    youGotProgress?.isVisible = false
                    moreProgress?.isVisible = false
                    youGotDiamonds?.isVisible = true
                    youGotDiamonds?.clearCompoundDrawables()
                    youGotDiamonds?.textResource = R.string.un_know_likes
                    showEndDetailFailedDialog()
                    liveEndJob?.cancel()
                    this.cancel()
                }
            }
        }
    }

    private fun showEndDetailFailedDialog() {
        alertDialog {
            titleResource = R.string.fb_live_end_detail_failed_title
            messageResource = R.string.fb_live_end_detail_failed_message
            negativeButton(R.string.fb_live_end_detail_failed_left) {
                it.dismiss()
            }
            positiveButton(R.string.fb_live_end_detail_failed_right) {
//                showOtherProfileDialog(if (BuildConfig.FLAVOR == "official")"YpRzJZbnJMPB" else "4Z9jKryVWg", "", 9)
//                showOtherProfileDialog("YpRzJZbnJMPB", "", 9)
            }
        }.show()
    }

    fun muteLocalAudioStream(isMute: Boolean){
        agoraManager.muteLocalAudioStream(isMute)
    }

    private fun liveUIController(liveUserWithUidInfos: List<LiveUserWithUidData>) {
        //Fix 需要先处理断开连麦的人
        checkMicrophoneUser(liveUserWithUidInfos)
        liveUserWithUidInfos.forEach {
            val contains = liveUserWithUids.any { it1 ->
                it1.userInfo.userId == it.userInfo.userId
            }
            if (!contains) {
                liveUserWithUids.add(it)
                liveWithController(it, liveUserWithUids.size)
            }
        }
    }

    private fun checkMicrophoneUser(liveUserWithUidInfos: List<LiveUserWithUidData>) {
        liveUserWithUids.toMutableList().forEach {
            val have = liveUserWithUidInfos.any { it1 ->
                it1.userInfo.userId == it.userInfo.userId
            }
            if (!have) {
                hungUpLiveWithUI(it.userInfo)
            }
        }
    }

    private fun liveWithController(
        liveWithInfoWithUid: LiveUserWithUidData,
        number: Int,
    ) {
        viewModel.isMicrophone.postValue(liveUserWithUids.size > 1)
        setLiveResolution()
        //主播端连麦ui
        if (liveWithInfoWithUid.userInfo.userId == SessionPreferences.id) {
            localVideo.liveUserInfo = liveWithInfoWithUid.userInfo
            localVideo.liveUserInfo?.uid = liveWithInfoWithUid.uid
            addedUidSet.add(liveWithInfoWithUid.uid)
        }
        microphoneAnchor()

        if (liveWithInfoWithUid.userInfo.userId != SessionPreferences.id) {
            setRemoteVideoView(liveWithInfoWithUid)
        }
    }

    private fun setRemoteVideoView(userWithUidInfo: LiveUserWithUidData) {
        Timber.d("${userWithUidInfo.userInfo.nickname} ${userWithUidInfo.userInfo.userId} ${userWithUidInfo.uid} ${addedUidSet.size - 1}")

        userWithUidInfo.uid.let {
            //如果已经添加则不再添加
            if (!addedUidSet.add(it)) {
                return
            }
            val surfaceView = agoraManager.getVideoUI(this, false, it)
            if (addedUidSet.size >= 1 && liveContainers.size >= addedUidSet.size) {
                liveContainers[addedUidSet.size - 1].setLiveVideoSurfaceView(surfaceView)
                liveContainers[addedUidSet.size - 1].liveUserInfo = userWithUidInfo.userInfo
                liveContainers[addedUidSet.size - 1].liveUserInfo?.uid = userWithUidInfo.uid
                liveContainers[addedUidSet.size - 1].onLiveVideoViewListener = this
                liveContainers[addedUidSet.size - 1].initUserView(true)
                liveContainers[addedUidSet.size - 1].loading(true)
                liveContainers[addedUidSet.size - 1].changeMuteState(userWithUidInfo.userInfo.mute ?: 0)
            }
        }
    }

    private fun hungUpLiveWithUI(userInfo: LiveUserInfo) {
        if (viewModel.isMicrophoneUser.value == true && userInfo.userId == SessionPreferences.id) {
            //设置为观众角色
            agoraManager.setClientRole(false)
            agoraManager.mRtcEngine?.enableLocalAudio(false)
            agoraManager.mRtcEngine?.enableLocalVideo(false)
            localVideo?.isVisible = false
            setLocalVideo(0)
            viewModel.isMicrophoneUser.postValue(false)
        }
        liveUserWithUids.toMutableList().forEach {
            if (it.userInfo.userId == userInfo.userId) {
                val b = addedUidSet.remove(it.uid)
                Timber.d("$b ${it.uid} ${userInfo.uid}")
                liveUserWithUids.remove(it)
                removeRemoteView(it.userInfo.userId)
            }
        }

        viewModel.isMicrophone.postValue(liveUserWithUids.size > 1)

        //连麦结束设置分辨率
        setLiveResolution()
    }

    private fun setLocalVideo(mute: Int) {
        localVideo?.changeMuteState(mute)
    }

    override fun showInteractionFragment() {
        BarUtils.setStatusBarLightMode(this, false)
        val hostInteractionFragment = LiveInteractionHostFragment()
        hostInteractionFragment.liveId = liveId ?: ""
        hostInteractionFragment.isHost = true
        hostInteractionFragment.makeUpEnabled = isOpenBeauty
        hostInteractionFragment.giftSecondContainer = giftSecondContainer
        hostInteractionFragment.giftFirstContainer = giftFirstContainer
        hostInteractionFragment.liveLikesAnimView = liveLikesAnimView
        hostInteractionFragment.giftAnim = giftAnim
        hostFragment = hostInteractionFragment
        giftContainer.isVisible = true
        pager?.isVisible = true
        pager?.adapter = LiveHostPagerAdapter(hostInteractionFragment, this)
        pager?.offscreenPageLimit = 3
        pager?.setCurrentItem(1, false)
    }

    fun hideInteractionContainer() {
        pager?.isVisible = false
    }

    fun showInteractionContainer() {
        pager?.isVisible = true
    }

    fun showErrorPrompt(desc: String) {
    }

    companion object :
        ActivityIntentSpec<LiveHostActivity, RecordExtraSpec> by activitySpec(RecordExtraSpec)

    object RecordExtraSpec : BundleSpec() {
        var hashtag: String? by bundleOrNull()
        var source: String? by bundleOrNull()
        var uuid: UUID? by bundleOrNull()
        var commentData: String? by bundleOrNull()
        var isCanLive: Boolean = true
        var isLiveNeedResume: Boolean = false
        var createLiveType: String? by bundleOrNull()
        var conversationsEntity: ConversationsEntity by bundle()
        var liveId: String? by bundleOrNull()
        var eventId: String? by bundleOrNull()
    }

    override fun onBackPressed() {
        if (currentFragmentTag != null) {
//            val fragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isNotStop) {
            agoraManager.mRtcEngine?.enableLocalAudio(true)
            agoraManager.mRtcEngine?.enableLocalVideo(true)
            agoraManager.mRtcEngine?.enableVideo()
            agoraManager.mRtcEngine?.enableAudio()
            agoraManager.mRtcEngine?.startPreview()
            resumeReport()
        } else {
            isNotStop = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isNotStop) {
                // 如果 3 2 1 此时倒计时突然退到后台则重新返回设置页面
                if (!isCountTimeOver) {
                    if (isStarted) {
                        viewModel.closeLive(liveId)
                    }
                    goBackLiveSetting()
                }
            pauseReport()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.isMicrophoneUser.value == true) {
            liveId?.let {
                liveUserWithUids.toMutableList().forEach { userInfo ->
//                        sharedViewModel.hangUpLiveWith(it, userInfo.userId)
                }
            }
        }
        LiveSocketIO.releaseLiveSocket()
        startLiveLaunch?.cancel()
        liveEndJob?.cancel()
        sharedViewModel.liveStateInfoLiveData = MutableLiveData()
        sharedViewModel.liveUsersSizeData = MutableLiveData()
        sharedViewModel.liveOnlineCount = MutableLiveData()
    }

    private inner class LiveHostPagerAdapter(
        val liveInteractionFragment: LiveInteractionHostFragment,
        act: AppCompatActivity,
    ) : FragmentStateAdapter(act) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val fragment = LiveEmptyFragment()
                    fragment.showClose = false
                    fragment.isHost = true
                    fragment
                }
                1 -> liveInteractionFragment
                else -> LiveEmptyFragment().apply {
                    isHost = true
                }
            }
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.activity_live_host
    }

    override fun initLocalVideoView(surfaceView: SurfaceView) {
        localVideo.hideAllChildView()
        liveContainers.add(localVideo)
        localVideo.setLiveVideoSurfaceView(surfaceView)
        localVideo.isVisible = true
    }

    override fun addLiveVideoContainer(liveRl: LiveRelativeLayout) {
        rlContainer.addView(liveRl)
    }

    override fun removeLiveVideoContainer(liveRl: LiveRelativeLayout) {
        rlContainer.removeView(liveRl)
    }

    //region LiveRelativeLayout
    override fun onHangUPClick(liveUserInfo: LiveUserInfo) {
        Timber.d("onHangUPClick ${liveUserInfo.userId} ${liveUserInfo.nickname} ${liveUserInfo.uid}")
        alertDialog {
            message = getString(R.string.hang_up_live_video, liveUserInfo.userName)
            positiveButton(R.string.fb_confirm) {
                // FIXME: 2022/9/2 断开连麦接口
                it.dismiss()
            }
            neutralButton(R.string.fb_cancel) {
                it.dismiss()
            }
        }.show()
    }

    override fun onProfileClick(liveUserInfo: LiveUserInfo) {
//        showProfile(
//            liveUserInfo.userId,
//            liveUserInfo.userName,
//            9
//        )
    }

    override fun onRaiseHandClick() {}
    //endregion

    override fun setNetworkQuality(quality: Int) {
        when (quality) {
            1 -> {
                networkQualityContainer.isVisible = true
                networkQualityIcon.setBackgroundResource(R.drawable.icon_network_quality_green)
                networkQuality.textResource = R.string.fb_network_is_good
            }
            2 -> {
                networkQualityContainer.isVisible = true
                networkQualityIcon.setBackgroundResource(R.drawable.icon_network_quality_orange)
                networkQuality.textResource = R.string.fb_network_is_poor
            }
            3 -> {
                networkQualityContainer.isVisible = true
                networkQualityIcon.setBackgroundResource(R.drawable.icon_network_quality_red)
                networkQuality.textResource = R.string.fb_disconnected
            }
        }
    }

    override fun hideLoadingCover() {
    }

    override fun hidePause() {
    }

    override fun showPause() {
    }
}