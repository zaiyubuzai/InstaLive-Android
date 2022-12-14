package com.example.instalive.app.live

import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
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
import com.example.baselibrary.utils.baseToast
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.InstaLiveApp.Companion.appInstance
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
class LiveAudienceActivity : LiveBaseActivity<LiveAudienceViewModel, ActivityLiveAudienceBinding>(),
    LiveEmptyFragment.OnEventListener {

    private var isGiftCardViewUp = false//???????????????????????????

    private var liveOwner: Owner? = null
    private val liveInteractionFragment: LiveInteractionFragment by lazy {
        LiveInteractionFragment()
    }

    private var pauseJob: Job? = null

    override fun initData(savedInstanceState: Bundle?) {
        isAnchor = false
        super.initData(savedInstanceState)
        screenName = "live_audience_view"
        closeActivity.setPadding(0, BarUtils.statusBarHeight, 0, 0)
        networkQualityContainer.setPadding(0, dp(68) + BarUtils.statusBarHeight, dp(16), 0)
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
//        val clientRoleOptions = ClientRoleOptions()
//        if (MarsApp.appInstance.appInitData.value?.appFeature?.liveLatencyLevel == 0) {
//        clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
//        } else {
//            clientRoleOptions.audienceLatencyLevel =
//                AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY
//        }

        //???????????????????????????????????????
        val width = resolution?.liveWidth ?: 720
        val height = resolution?.liveHigh ?: 1280
        agoraManager.setVideoEncoderResolution(
            width,
            height,
            VideoEncoderConfiguration.STANDARD_BITRATE
        )
        appInstance.rtcEngine()?.leaveChannel()
        agoraManager.enableLocalAudio = false
        agoraManager.joinChannel(this, false, AgoraTokenInfo(token, liveId ?: "", null, mUid))
    }

    private fun setLoadingCover(portrait: String?) {
        Timber.d("live init setLoadingCover ${System.currentTimeMillis()}")
        if (portrait == null) return
        showBlurTransformationCover(portrait, leaveBgCover)
        showBlurTransformationCover(portrait, pauseBgCover)
    }

    private fun initObserver() {

        viewModel.errorCodeLiveData.observe(this) {
            baseToast(viewModel.errorMessageLiveData.value.toString())
        }

        sharedViewModel.liveUsersSizeData.observe(this, {
            onLiveUsersChanged(it)
        })

        //??????????????????????????????????????????????????????????????????
        sharedViewModel.liveStateInfoLiveData.observe(this, {
            if (it.first != null) {
                val info = it.first ?: return@observe
                setLoadingCover(info.owner.portrait)
                when (info.state) {
                    LIVE_START -> {
//                        agoraManager.mRtcEngine?.stopPreview()
//                        agoraManager.mRtcEngine?.disableAudio()
//                        agoraManager.mRtcEngine?.disableVideo()
                        isLiving = true
                        anchorUid = info.liveWithUserInfos[0].uid.toString()
                        //??????????????????????????????????????????
                        if (info.liveWithUserInfos.size > 1) {
                            sharedViewModel.isMicrophoneUser = false
                            info.liveWithUserInfos.toList().forEach { liveUsers ->
                                if (liveUsers.userInfo.userId == SessionPreferences.id) {
                                    sharedViewModel.isMicrophoneUser = true
                                }
                            }
//                            (activity as LiveActivity).allowInAppNotify = !isMicrophoneUser


                            liveUIController(info.liveWithUserInfos)
                            sharedViewModel.isMicrophone = true
                        } else {
                            sharedViewModel.isMicrophone = false
                            liveUIController(info.liveWithUserInfos)
                        }

                        //?????????????????????????????????
//                        agoraManager.mRtcEngine?.enableVideo()
//                        agoraManager.mRtcEngine?.enableAudio()
//                        agoraManager.mRtcEngine?.startPreview()
                        sharedViewModel.liveOnlineCount.postValue(info.onlineStr)
                    }
                    LIVE_END -> {
                        isLiving = false
                        // ????????????????????????

                        sharedViewModel.isMicrophoneUser = false
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
            val errorInfo = it.second
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
            } else if (errorInfo != null) {
                if (errorInfo.error.code == 6001) { //Room Has Ended.
                    showLeavePrompt()
                }
                hideLoadingCover()
                if (liveOwner == null) {
                    liveOwner = it.second?.extData?.owner
                    setCoverImageOrUsername()
                }
            }
        }
        LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS).observe(this, {
            when (it) {
                ITRCT_TYPE_FLIP -> {  //???????????????
                    agoraManager.switchCamera()
                }
                ITRCT_TYPE_MAKEUP_ON -> {  // ?????????
                    agoraManager.setBeautyEffectOptions(true)
                    isOpenBeauty = true
                }
                ITRCT_TYPE_MAKEUP_OFF -> {  // ?????????
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
                    if (!sharedViewModel.isMicrophoneUser) {
                        it.liveUserWithUidInfos.any { it1 ->
                            if (it1.userInfo.userId == SessionPreferences.id) {
                                sharedViewModel.isMicrophoneUser = true
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
                        if (!sharedViewModel.isMicrophoneUser) {
                            liveUIController(it.liveUserWithUidInfos)
                        }
                    } else {
                        liveUIController(it.liveUserWithUidInfos)
                    }
                }
                is LiveWithHangupEvent -> {
                    val user = liveUserWithUids.firstOrNull { it1 ->
                        it1.userInfo.userId == it.targetUserId
                    }
                    if (user != null) {
                        hungUpLiveWithUI(user)
                    }
                }
                is LiveWithCancelEvent -> {
                    val user = liveUserWithUids.firstOrNull { it1 ->
                        it1.userInfo.userId == it.targetUserId
                    }
                    if (user != null) {
                        hungUpLiveWithUI(user)
                    } else if (it.targetUserId == SessionPreferences.id) {
                        agoraManager.mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
                        agoraManager.mRtcEngine?.enableLocalAudio(false)
                        agoraManager.mRtcEngine?.enableLocalVideo(false)
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
                    if (sharedViewModel.isMicrophoneUser == true) {
                        //?????????
                        alertDialog {
                            messageResource =
                                R.string.you_are_Live_now_can_go_to_another_live_or_radio
                            okButton()
                        }.show()
//                    } else {
                        //??????
//                        LiveEventBus.get(EVENT_BUS_CLOSE_ALL_LIVE).post(it)
                    }
                }

                is PublisherStateEvent -> {
                    if (liveUserWithUids.size > 1) {
                        liveUserWithUids.toMutableList().forEach { liveUserInfo ->
                            if (liveUserInfo.userInfo.userId == it.targetUserInfo.userId) {
                                val index = liveUserWithUids.indexOf(liveUserInfo)
                                val liveC = liveContainers[index]
                                // 1 ?????? 2 ???????????? 3 ??????????????? 4 ???????????????
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
        liveInteractionFragment.makeUpEnabled = isOpenBeauty
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
        val layoutParams = networkQualityContainer.layoutParams as RelativeLayout.LayoutParams
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
        agoraManager.mRtcEngine?.enableAudio()
    }

    override fun setNetworkQuality(quality: Int) {
        when (quality) {
            1 -> {
                if (sharedViewModel.isMicrophoneUser) {
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
                    if (!sharedViewModel.isMicrophoneUser) R.string.fb_your_network_is_poor else R.string.fb_network_is_poor
            }
            3 -> {
                networkQualityContainer.isVisible = true
                networkQualityIcon.setBackgroundResource(R.drawable.icon_network_quality_red)
                networkQuality.textResource =
                    if (!sharedViewModel.isMicrophoneUser) R.string.fb_you_are_disconnected else R.string.fb_disconnected
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
        if (sharedViewModel.isMicrophoneUser == true) {
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

    private fun showLeavePrompt() {
        leavePrompt?.isVisible = true
        hideLoadingCover()
        destroyAppRTCEngine()
        sharedViewModel.isMicrophoneUser = false
        LiveSocketIO.releaseLiveSocket()
        liveInteractionFragment.hideLiveWithInvite()
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
     * ????????????
     */
    private fun hungUpLiveWithUI(userWithUidInfo: LiveUserWithUidData) {
        if (sharedViewModel.isMicrophoneUser == true && userWithUidInfo.userInfo.userId == SessionPreferences.id) {
            //?????????????????????
            agoraManager.mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
            agoraManager.mRtcEngine?.enableLocalAudio(false)
            agoraManager.mRtcEngine?.enableLocalVideo(false)
            localVideo?.isVisible = false
//            setLocalVideo(0)
            sharedViewModel.isMicrophoneUser = false
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

        sharedViewModel.isMicrophone = liveUserWithUids.size > 1

        //???????????????????????????
        setLiveResolution()
    }

    private fun liveUIController(liveUserWithUidInfos: List<LiveUserWithUidData>) {
        //Fix ?????????????????????????????????
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
        sharedViewModel.isMicrophone = liveUserWithUids.size > 1
        setLiveResolution()
        //???????????????ui
        if (sharedViewModel.isMicrophoneUser) { // ???????????????ui??????

            //?????????????????????
            if (liveWithInfoWithUid.userInfo.userId == SessionPreferences.id) {
//                viewModel.liveReport(101, liveId, liveId, null)
//                liveUserInfos.forEach {
//                    if (liveWithInfo.userId == SessionPreferences.id) {
                localVideo?.isVisible = true
                liveWithInfoWithUid.userInfo.uid = liveWithInfoWithUid.uid
                localVideo?.liveUserInfo = liveWithInfoWithUid.userInfo
                localVideo?.initUserView(true)
                if (number == 4 || number == 6 || number == 8 || number == 9) {//???????????????????????????
                    rlContainer.removeView(liveContainers[number - 1])
                    liveContainers.removeLast()
                    liveContainers.add(number - 1, localVideo)
                } else {
                    liveContainers.add(localVideo)
                }
                localVideo?.onLiveVideoViewListener = this
                addedUidSet.add(liveWithInfoWithUid.uid)
//                    }
//                }
            }

            microphoneAnchor()

            //?????????????????????
            agoraManager.mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            agoraManager.mRtcEngine?.enableLocalAudio(true)
            agoraManager.mRtcEngine?.enableLocalVideo(true)
            agoraManager.mRtcEngine?.enableVideo()
            agoraManager.mRtcEngine?.enableAudio()
            agoraManager.mRtcEngine?.startPreview()
            if (liveWithInfoWithUid.userInfo.userId == SessionPreferences.id) {
                muteLocalAudioStream(false)// FIXME: 2022/3/16 ????????????????????????????????????????????????????????????????????????
            }
        } else { // ?????????UI??????
            localVideo?.isVisible = false
            microphoneAnchor()
//            setRemoteVideoView(liveWithInfo)
        }
        if (liveWithInfoWithUid.userInfo.userId != SessionPreferences.id) {
            setRemoteVideoView(liveWithInfoWithUid)
        }
    }

    private fun setRemoteVideoView(userWithUidInfo: LiveUserWithUidData) {
        //??????loading

        hideLoadingCover()
        Timber.d("${userWithUidInfo.userInfo.nickname} ${userWithUidInfo.userInfo.userId} ${userWithUidInfo.uid} ${addedUidSet.size - 1}")

        userWithUidInfo.uid.let {
            //?????????????????????????????????
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
//                liveContainers[addedUidSet.size - 1].loading(true)
                if (addedUidSet.size > 1) {
                    liveContainers[addedUidSet.size - 1].changeMuteState(
                        userWithUidInfo.userInfo.mute ?: 0
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        if (sharedViewModel.isMicrophoneUser) {

        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        liveId?.let { sharedViewModel.leaveLive(it) }
        agoraManager.leaveLiveRoom()
        super.onDestroy()
        LiveSocketIO.releaseLiveSocket()
    }

    override fun initViewModel(): LiveAudienceViewModel {
        return getActivityViewModel(LiveAudienceViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_live_audience, viewModel)
    }

}