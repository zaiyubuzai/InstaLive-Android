package com.example.instalive.app.live

import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.utils.marsToast
import com.example.instalive.R
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE_HOST_ACTIONS
import com.example.instalive.app.Constants.EXTRA_LIVE_ID
import com.example.instalive.app.Constants.ITRCT_TYPE_FLIP
import com.example.instalive.app.Constants.ITRCT_TYPE_MAKEUP_OFF
import com.example.instalive.app.Constants.ITRCT_TYPE_MAKEUP_ON
import com.example.instalive.app.Constants.LIVE_END
import com.example.instalive.app.Constants.LIVE_LEAVE
import com.example.instalive.app.Constants.LIVE_START
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.live.ui.LiveRelativeLayout
import com.example.instalive.databinding.ActivityLiveAudienceBinding
import com.example.instalive.model.*
import com.example.instalive.utils.LiveSocketIO
import com.example.instalive.utils.requestLivePermission
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.livesdk.rtc.AgoraTokenInfo
import io.agora.rtc.Constants
import io.agora.rtc.models.ClientRoleOptions
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_live_audience.*
import kotlinx.coroutines.*
import splitties.alertdialog.appcompat.*
import splitties.dimensions.dp
import splitties.views.onClick
import splitties.views.textResource
import timber.log.Timber

@ExperimentalStdlibApi
class LiveAudienceActivity : LiveBaseActivity<ActivityLiveAudienceBinding>(),
    LiveEmptyFragment.OnEventListener {

    private var isGiftCardViewUp = false//礼物卡片被弹窗顶起

    private var liveOwner: Owner? = null
    private val liveInteractionFragment: LiveInteractionFragment by lazy {
        LiveInteractionFragment()
    }

    private var pauseJob: Job? = null

    override fun initData(savedInstanceState: Bundle?) {
        isAnchor = false
        super.initData(savedInstanceState)
        screenName = "live_audience_view"
        val set = ConstraintSet()
        set.clone(container)
        set.setMargin(R.id.closeActivity, ConstraintSet.TOP, BarUtils.statusBarHeight)
        set.setMargin(
            R.id.networkQualityContainer,
            ConstraintSet.TOP,
            dp(68) + BarUtils.statusBarHeight
        )
        set.applyTo(container)
        BarUtils.setStatusBarLightMode(this, false)
        liveId = intent.getStringExtra(EXTRA_LIVE_ID).toString()

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        viewModel.joinLive(SessionPreferences.id, liveId ?: "", sharedViewModel.liveJoinData)

        initListener()
        initObserver()
        showLoadingCover()
    }

    private fun initListener() {
        done.onClick {
            agoraManager.leaveLiveRoom()
            finish()
        }
        leavePrompt.onClick {}
        closeActivity.onClick { onBackPressed() }
        loadingBgCover.onClick {}
        profileAvatar.onClick {
            viewModel.getPersonalData(liveOwner?.userId ?: "") {}
        }

        username.onClick {
            viewModel.getPersonalData(liveOwner?.userId ?: "") {}
        }
        touchFl.setOnTouchListener { _, event ->
            event?.let {
                isLiveViewTouched(it.rawX.toInt(), it.rawY.toInt())
            }
            false
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
        agoraManager.enableLocalAudio = false
        agoraManager.joinChannel(this, true, AgoraTokenInfo(token, liveId ?: "", null, mUid))
    }

    private fun setLoadingCover(portrait: String?) {
        Timber.d("live init setLoadingCover ${System.currentTimeMillis()}")
        if (portrait == null) return
        showBlurTransformationCover(portrait, leaveBgCover)
        showBlurTransformationCover(portrait, pauseBgCover)
    }

    private fun initObserver() {

        viewModel.errorCodeLiveData.observe(this) {
            marsToast(viewModel.errorMessageLiveData.value.toString())
        }

        sharedViewModel.liveUsersSizeData.observe(this, {
            onLiveUsersChanged(it)
        })

        //获取直播状态监听（每次从后台进入都会走此处）
        sharedViewModel.liveStateInfoLiveData.observe(this, {
            if (it.first != null) {
                val info = it.first ?: return@observe
                setLoadingCover(info.owner.portrait)
                when (info.state) {
                    LIVE_START -> {
                        agoraManager.mRtcEngine?.stopPreview()
                        agoraManager.mRtcEngine?.disableAudio()
                        agoraManager.mRtcEngine?.disableVideo()
                        isLiving = true
                        anchorUid = info.liveWithUserInfos[0].uid.toString()
                        //说明连麦（默认是不连麦模式）
                        if (info.liveWithUserInfos.size > 1) {
                            viewModel.isMicrophoneUser.value = false
                            info.liveWithUserInfos.toList().forEach { liveUsers ->
                                if (liveUsers.userInfo.userId == SessionPreferences.id) {
                                    viewModel.isMicrophoneUser.value = true
                                }
                            }
//                            (activity as LiveActivity).allowInAppNotify = !isMicrophoneUser


                            liveUIController(info.liveWithUserInfos)
                            viewModel.isMicrophone.value = true
                        } else {
                            viewModel.isMicrophone.value = false
                            liveUIController(info.liveWithUserInfos)
                        }

                        //直播开播才开始可以推流
                        agoraManager.mRtcEngine?.enableVideo()
                        agoraManager.mRtcEngine?.enableAudio()
                        agoraManager.mRtcEngine?.startPreview()
                        sharedViewModel.liveOnlineCount.postValue(info.onlineStr)
                    }
                    LIVE_END -> {
                        isLiving = false
                        // 展示直播结束画面

                        viewModel.isMicrophoneUser.value = false
//                        (activity as LiveActivity).allowInAppNotify = !isMicrophoneUser
                        showLeavePrompt()
                        hideLoadingCover()
                    }
                    LIVE_LEAVE -> {
                    }
                }
            } else if (it.second != null) {
                it.second?.extData?.owner?.let {
                    setLoadingCover(it.portrait)
                }
            }
        })

        sharedViewModel.liveJoinData.observe(this) {
            sharedViewModel.liveStateInfoLiveData.postValue(it)
            val liveInfo = it.first
            if (liveInfo != null) {
                LiveSocketIO.initLiveSocket(liveInfo.id)
                if (liveOwner == null) {
                    liveOwner = liveInfo.owner
                    setCoverImageOrUsername()
                }
                anchorUid = try {
                    liveInfo.liveWithUserInfos.get(0).uid.toString()
                } catch (e: Exception) {
                    "0"
                }

                mUid = try {
                    liveInfo.uid
                } catch (e: Exception) {
                    0
                }
                token = liveInfo.token
                resolution = liveInfo.resolution

                initLiveFragment()
                joinChannel(liveInfo.token, liveInfo.uid)
            } else {
                hideLoadingCover()
                if (liveOwner == null) {
                    liveOwner = it.second?.extData?.owner
                    setCoverImageOrUsername()
                }
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
            }
        })

        LiveEventBus.get(EVENT_BUS_KEY_LIVE).observe(this, {
            when (it) {
                is LiveStateEvent -> {
                    if (it.liveState == LIVE_END) {
                        isLiving = false
                        showLeavePrompt()
                    }
                }
                is LiveWithAgreeEvent -> {
                    if (viewModel.isMicrophoneUser.value != true) {
                        it.liveUserWithUidInfos.any { it1 ->
                            if (it1.userInfo.userId == SessionPreferences.id) {
                                viewModel.isMicrophoneUser.value = true
                                requestLivePermission(go = {
                                    liveUIController(it.liveUserWithUidInfos)
                                }) {
                                    viewModel.hangUpLiveWith(
                                        liveId ?: "",
                                        it1.userInfo.userId,
                                        {},
                                        {})
                                }
                                onHandCanDownOrUp(LiveRelativeLayout.Raise_Hand_Cannot)
                            }
                            it1.userInfo.userId == SessionPreferences.id
                        }
                        if (viewModel.isMicrophoneUser.value != true) {
                            liveUIController(it.liveUserWithUidInfos)
                        }
                    } else {
                        liveUIController(it.liveUserWithUidInfos)
                    }
                }
                is LiveWithHangupEvent -> {
                    val user = liveUserWithUids.firstOrNull() { it1 ->
                        it1.userInfo.userId == it.targetUserId
                    }
                    if (user != null) {
                        hungUpLiveWithUI(user)
                    }
                }
//                is LiveLiveRemoveEvent -> {
//                    if (it.removeType == 1) { //remove user
//                        if (it.targetUserIds?.contains(SessionPreferences.id) == true) {
//                            if (isMicrophoneUser()) {
//                                liveFragment.hungUpLiveWith(SessionPreferences.id)
//                            }
//                            liveFragment.isMicrophoneUser = false
//                            sharedViewModel.liveLeave(SessionPreferences.id, liveId)
//                            liveFragment.finishRtcEngine()
//                            onBackPressed()
//                        }
//                    } else if (it.removeType == 2) {
//                        isMidwayPayLive = true
//                        liveInteractionFragment.isPaidLive = true
//                        if (it.ignoreUserIds?.contains(SessionPreferences.id) == false
//                            || it.targetUserIds?.contains(SessionPreferences.id) == true
//                        ) {
//                            sharedViewModel.liveLeave(SessionPreferences.id, liveId)
//                            liveFragment.isLockedLive = it.extData?.shadowDisplay ?: false
//                            liveFragment.setRtcEngineAudio(liveFragment.isLockedLive)
//                            liveInteractionFragment.isPaidLive = true
//                            showUnlockLivePrompt(it.extData?.ticketGiftInfo)
//                        }
//                    }
//                }

                is LiveStartInfoEvent -> {
                    if (viewModel.isMicrophoneUser.value == true) {
                        //连麦人
                        alertDialog {
                            messageResource =
                                R.string.you_are_Live_now_can_go_to_another_live_or_radio
                            okButton()
                        }.show()
                    } else {
                        //观众
//                        LiveEventBus.get(EVENT_BUS_CLOSE_ALL_LIVE).post(it)
                    }
                }

                is PublisherStateEvent -> {
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
    }

    private fun changeMarginGiftCard(up: Boolean) {
        isGiftCardViewUp = up
        if (up) {
            setMarginGiftCard(dp(420))
        } else {
            val size = sharedViewModel.liveUsersSizeData.value ?: 1
            onLiveUsersChanged(size)
        }
    }

    private fun onLiveUsersChanged(size: Int) {
        if (isGiftCardViewUp || isFinishing) return
        val b = when (size) {
            0, 1 -> {
                dp(300) + dp(82) + dp(8)
            }
            2 -> {
                val top = BarUtils.statusBarHeight + dp(68) + dp(280) + dp(10)
                val bottom = container?.height ?: 2000
                bottom - top - dp(8)
            }
            3, 4 -> {
                val top = BarUtils.statusBarHeight + dp(68) + dp(180) + dp(4) + dp(180) + dp(10)
                val bottom = container?.height ?: 2000
                bottom - top - dp(8)
            }
            5, 6 -> {
                val top = BarUtils.statusBarHeight + dp(68) + dp(180) + dp(4) + dp(180) + dp(10)
                val bottom = container?.height ?: 2000
                bottom - top - dp(8)
            }
            else -> {
                val top =
                    BarUtils.statusBarHeight + dp(68) + dp(180) + dp(4) + dp(180) + dp(4) + dp(180) + dp(
                        10
                    )
                val bottom = container?.height ?: 2000
                bottom - top - dp(8)
            }
        }
        setMarginGiftCard(b)
    }

    private fun setMarginGiftCard(bottomDP: Int) {
        val set = ConstraintSet()
        set.clone(giftContainer)
        set.setMargin(
            R.id.giftSecondContainer,
            ConstraintSet.BOTTOM,
            bottomDP
//            dip(if (up) 420 else 308)
        )
        set.applyTo(giftContainer)
    }

    private fun setCoverImageOrUsername() {
        Glide.with(this)
            .load(liveOwner?.portrait)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(dp(12))))
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(profileAvatar)

        username.text = liveOwner?.nickname ?: ""
        liveOwner?.portrait?.let {
            showBlurTransformationCover(it, loadingBgCover)
            showBlurTransformationCover(it, leaveBgCover)
        }
    }


    private fun showLoadingCover() {
        loadingAnimContainer?.isVisible = true
    }

    override fun hideLoadingCover() {
        loadingAnimContainer?.isVisible = false
    }

    private fun initLiveFragment() {
        giftContainer.isVisible = true
        liveInteractionFragment.giftSecondContainer = giftSecondContainer
        liveInteractionFragment.giftFirstContainer = giftFirstContainer
        liveInteractionFragment.liveLikesAnimView = liveLikesAnimView
        liveInteractionFragment.giftAnim = giftAnim
        liveInteractionFragment.liveId = liveId ?: ""
        liveInteractionFragment.isHost = false
        pager.adapter = LivePagerAdapter(liveInteractionFragment, this)
        pager.setCurrentItem(1, false)
        pager.offscreenPageLimit = 3
    }

    fun muteLocalAudioStream(b: Boolean) {
        agoraManager.muteLocalAudioStream(b)
    }

    fun onHandCanDownOrUp(canRaise: String) {
        liveContainers.forEach {
            if (it.liveUserInfo == null) {
                it.changeRaiseHandUI(canRaise)
            }
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.activity_live_audience
    }

    //region base activity
    override fun initLocalVideoView(surfaceView: SurfaceView) {
        localVideo.hideAllChildView()
        localVideo.setLiveVideoSurfaceView(surfaceView)
    }

    override fun onLiveStateError(code: Int, msg: String) {
        if (code == 6501) {
            liveStateStatus = false
        } else {
            lifecycleScope.launch {
                delay(2000)
                liveStateStatus = false
                startGetLiveState()
            }
        }
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

    override fun addLiveVideoContainer(liveRl: LiveRelativeLayout) {
        rlContainer.addView(liveRl)
    }

    override fun removeLiveVideoContainer(liveRl: LiveRelativeLayout) {
        rlContainer.removeView(liveRl)
    }

    override fun showInteractionFragment() {

    }

    override fun setNetworkQuality(quality: Int) {
        when (quality) {
            1 -> {
                if (viewModel.isMicrophoneUser.value == true) {
                    networkQualityContainer.isVisible = true
                    networkQualityIcon.setBackgroundResource(R.drawable.icon_network_quality_green)
                    networkQuality.textResource = R.string.fb_network_is_good
                } else {
                    networkQualityContainer.isVisible = false
                }
            }
            2 -> {
                networkQualityContainer.isVisible = true
                networkQualityIcon.setBackgroundResource(R.drawable.icon_network_quality_orange)
                networkQuality.textResource =
                    if (viewModel.isMicrophoneUser.value != true) R.string.fb_your_network_is_poor else R.string.fb_network_is_poor
            }
            3 -> {
                networkQualityContainer.isVisible = true
                networkQualityIcon.setBackgroundResource(R.drawable.icon_network_quality_red)
                networkQuality.textResource =
                    if (viewModel.isMicrophoneUser.value != true) R.string.fb_you_are_disconnected else R.string.fb_disconnected
            }
        }
    }

    override fun onHangUPClick(liveUserInfo: LiveUserInfo) {
        tryHangUpLiveWith(liveUserInfo.userId)
    }

    override fun onProfileClick(liveUserInfo: LiveUserInfo) {
    }

    override fun onRaiseHandClick() {
        liveInteractionFragment.onRaiseHandClick()
    }
    //endregion

    fun tryHangUpLiveWith(userId: String) {
        if (isFinishing) return
        alertDialog {
            messageResource = R.string.fb_hang_up_your_live_video
            positiveButton(R.string.fb_confirm) {
                viewModel.hangUpLiveWith(liveId ?: "", userId, {

                }, {

                })
                it.dismiss()
            }
            neutralButton(R.string.fb_cancel) {
                it.dismiss()
            }
        }.show()
    }

    override fun hidePause() {
        pauseJob?.cancel()
        pauseJob = null
        pauseContainer?.isVisible = false
        if (viewModel.isMicrophoneUser.value == true) {
            localVideo?.isVisible = true
        }
    }

    override fun showPause() {
        if (pauseJob == null) {
            pauseJob = lifecycleScope.launch(Dispatchers.IO) {
                delay(8000)
                withContext(Dispatchers.Main) {
                    pauseContainer?.isVisible = true
                    localVideo?.isVisible = false
                }
                pauseJob?.cancel()
                pauseJob = null
            }
        }
    }

    private inner class LivePagerAdapter(
        val liveInteractionFragment: LiveInteractionFragment,
        act: AppCompatActivity,
    ) : FragmentStateAdapter(act) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val fragment = LiveEmptyFragment()
                    fragment.showClose = true
                    fragment.isHost = false
                    fragment.onEventListener = this@LiveAudienceActivity
                    fragment
                }
                1 -> liveInteractionFragment
                else -> LiveEmptyFragment().apply {
                    isHost = false
                }
            }
        }
    }

    override fun onClickGift() {

    }

    fun showLeavePrompt() {
        agoraManager.leaveLiveRoom()
        viewModel.isMicrophoneUser.value = false
        LiveSocketIO.releaseLiveSocket()
        liveInteractionFragment.hideLiveWithInvite()
        leavePrompt?.isVisible = true
        hideLoadingCover()
    }

    private fun checkMicrophoneUser(liveUserWithUidInfos: List<LiveUserWithUidData>) {
        liveUserWithUids.toMutableList().forEach {
            val have = liveUserWithUidInfos.any { it1 ->
                it1.userInfo.userId == it.userInfo.userId
            }
            if (!have) {
                hungUpLiveWithUI(it)
            }
        }
    }

    /**
     * 挂断连麦
     */
    private fun hungUpLiveWithUI(userWithUidInfo: LiveUserWithUidData) {
        if (viewModel.isMicrophoneUser.value == true && userWithUidInfo.userInfo.userId == SessionPreferences.id) {
            //设置为观众角色
            agoraManager.mRtcEngine?.setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE)
            agoraManager.mRtcEngine?.enableLocalAudio(false)
            agoraManager.mRtcEngine?.enableLocalVideo(false)
            localVideo?.isVisible = false
//            setLocalVideo(0)
            viewModel.isMicrophoneUser.value = false
//            (activity as LiveActivity).allowInAppNotify = !isMicrophoneUser
        }
        liveUserWithUids.toMutableList().forEach {
            if (it.userInfo.userId == userWithUidInfo.userInfo.userId) {
                val b = addedUidSet.remove(it.uid)
                Timber.d("$b ${it.uid} ${userWithUidInfo.uid}")
                liveUserWithUids.remove(it)
                removeRemoteView(it.userInfo.userId)
            }
        }

        viewModel.isMicrophone.value = liveUserWithUids.size > 1

        //连麦结束设置分辨率
        setLiveResolution()
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

    private fun liveWithController(
        liveWithInfoWithUid: LiveUserWithUidData,
        number: Int,
    ) {
        viewModel.isMicrophone.value = liveUserWithUids.size > 1
        setLiveResolution()
        //主播端连麦ui
        if (viewModel.isMicrophoneUser.value == true) { // 连麦观众端ui展示

            //当前观众要连麦
            if (liveWithInfoWithUid.userInfo.userId == SessionPreferences.id) {
//                viewModel.liveReport(101, liveId, liveId, null)
//                liveUserInfos.forEach {
//                    if (liveWithInfo.userId == SessionPreferences.id) {
                localVideo?.isVisible = true
                liveWithInfoWithUid.userInfo.uid = liveWithInfoWithUid.uid
                localVideo?.liveUserInfo = liveWithInfoWithUid.userInfo
                localVideo?.initUserView(true)
                if (number == 4 || number == 6 || number == 8 || number == 9) {//当是这个位置的时候
                    rlContainer.removeView(liveContainers[number - 1])
                    liveContainers.removeLast()
                    liveContainers.add(number - 1, localVideo)
                } else {
                    liveContainers.add(localVideo)
                }
                localVideo?.onLiveVideoViewListener = this
                addedUidSet.add(liveWithInfoWithUid.uid ?: 0)
//                    }
//                }
            }

            microphoneAnchor()

            //设置为主播角色
            agoraManager.mRtcEngine?.setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER)
            agoraManager.mRtcEngine?.enableLocalAudio(true)
            agoraManager.mRtcEngine?.enableLocalVideo(true)
            agoraManager.mRtcEngine?.enableVideo()
            agoraManager.mRtcEngine?.enableAudio()
            agoraManager.mRtcEngine?.startPreview()
            if (liveWithInfoWithUid.userInfo.userId == SessionPreferences.id) {
                muteLocalAudioStream(false)// FIXME: 2022/3/16 处理连麦上去没有声音的问题，具体原因还需要再查找
            }
        } else { // 观众端UI展示
            localVideo?.isVisible = false
            microphoneAnchor()
//            setRemoteVideoView(liveWithInfo)
        }
        if (liveWithInfoWithUid.userInfo.userId != SessionPreferences.id) {
            setRemoteVideoView(liveWithInfoWithUid)
        }
    }

    private fun setRemoteVideoView(userWithUidInfo: LiveUserWithUidData) {
        //隐藏loading

        hideLoadingCover()
        Timber.d("${userWithUidInfo.userInfo.nickname} ${userWithUidInfo.userInfo.userId} ${userWithUidInfo.uid} ${addedUidSet.size - 1}")

        userWithUidInfo.uid.let {
            //如果已经添加则不再添加
            if (!addedUidSet.add(it)) {
                return
            }

            val surfaceView = agoraManager.getVideoUI(this, false, it)
            surfaceView.tag = it

            if (addedUidSet.size >= 1 && liveContainers.size >= addedUidSet.size) {
                liveContainers[addedUidSet.size - 1].setLiveVideoSurfaceView(surfaceView)
                liveContainers[addedUidSet.size - 1].liveUserInfo = userWithUidInfo.userInfo
                liveContainers[addedUidSet.size - 1].liveUserInfo?.uid = it
                liveContainers[addedUidSet.size - 1].onLiveVideoViewListener = this
                if (addedUidSet.size != 1) {
                    liveContainers[addedUidSet.size - 1].initUserView(false)
                }
                liveContainers[addedUidSet.size - 1].loading(true)
                if (addedUidSet.size > 1) {
                    liveContainers[addedUidSet.size - 1].changeMuteState(
                        userWithUidInfo.userInfo.mute ?: 0
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        liveId?.let { sharedViewModel.leaveLive(it) }
        agoraManager.leaveLiveRoom()
        super.onDestroy()
        LiveSocketIO.releaseLiveSocket()
    }

}