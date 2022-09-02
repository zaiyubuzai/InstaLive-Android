package com.example.instalive.app.live

import android.annotation.SuppressLint
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.instalive.InstaLiveApp
import com.example.instalive.app.SessionPreferences
import com.example.instalive.databinding.FragmentLiveInteractionHostBinding
import com.example.instalive.model.LiveGiftEvent
import com.example.instalive.model.LiveStateInfo
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.fragment_live_interaction_host.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import splitties.alertdialog.appcompat.*
import com.example.instalive.R
import com.example.instalive.app.live.ui.GoLiveWithDialog

@ExperimentalStdlibApi
class LiveInteractionHostFragment :
    LiveInteractionBaseFragment<FragmentLiveInteractionHostBinding>() {

    var goLiveWithTicker: ReceiveChannel<Unit>? = null
    var currentDiamonds: Long = 0L
//    private var liveViewersHostDialog: LiveViewersHostDialog? = null
//    private var moreDialog: LiveMoreDialog? = null
    private var mute = 0
    private var hostGiftLiveTipJob: Job? = null

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalStdlibApi::class)
    override fun init() {
        Glide.with(activity)
            .load(SessionPreferences.portrait)
            .apply(options)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(avatar)
        SessionPreferences.nickName?.let {
            if (it.length > 12) {
                name.text = "${it.substring(0, 10)}..."
            } else {
                name.text = it
            }
        }
        giftLiveImage.isVisible =
            MarsApp.appInstance.getAppInitData()?.appFeature?.midwayPaidLiveEnabled == 1 && RecentConversation.conversationsEntity.type == 2

        onlineCountContainer.onClick {
            val c = context
            if (c != null && (liveViewersHostDialog?.isShow == false || liveViewersHostDialog == null)) {
                logFirebaseEvent("open_viewer", bundleOf("from" to "viewer_count"))

                liveViewersHostDialog = LiveViewersHostDialog(
                    c,
                    viewModel.roomId,
                    currentDiamonds,
                    isMicrophone,
                    isPaidLive,
                    currentLiveWithUser
                ) {
                    if (activity is RecordActivity) {
                        (activity as RecordActivity).tryHangUp(
                            currentLiveWithUser?.nickname ?: "",
                            currentLiveWithUser?.userId ?: ""
                        )
                    }
                }

                XPopup.Builder(c)
                    .isDestroyOnDismiss(true)
                    .enableDrag(true)
                    .asCustom(liveViewersHostDialog)
                    .show()
            }
        }

        hostDiamond.onClick {
            val c = context
            if (c != null && (liveViewersHostDialog?.isShow == false || liveViewersHostDialog == null)) {
                logFirebaseEvent("open_viewer", bundleOf("from" to "click_diamond"))

                liveViewersHostDialog =
                    LiveViewersHostDialog(
                        c,
                        viewModel.roomId,
                        currentDiamonds,
                        isMicrophone,
                        isPaidLive,
                        currentLiveWithUser,
                        cancelLiveWith = {
                            if (activity is RecordActivity) {
                                (activity as RecordActivity).tryHangUp(
                                    currentLiveWithUser?.nickname ?: "",
                                    currentLiveWithUser?.userId ?: ""
                                )
                            } else if (activity is LiveAudienceActivity) {
                                (activity as LiveAudienceActivity).tryHangUpLiveWith(
                                    currentLiveWithUser?.userId ?: ""
                                )
                            }
                        })
                XPopup.Builder(c)
                    .isDestroyOnDismiss(true)
                    .enableDrag(true)
                    .asCustom(liveViewersHostDialog)
                    .show()
            }
        }

        icMore.alphaClick {
            showMoreDialog()
        }

        nameContainer.onClick{
            ownerLiveUserInfo?.let { it1 -> showPersonBottomDialog(it1, 1) }
        }

        icDiamondView.alphaClick {
            if (FambasePreferences.isFirstGuideOpenDiamonds) {
                FambasePreferences.isFirstGuideOpenDiamonds = false
                context?.alertDialog {
                    titleResource = R.string.fb_live_open_diamonds_guide_title
                    messageResource = R.string.fb_live_open_diamonds_guide_message
                    positiveButton(R.string.fb_turn_on) {
                        viewModel.liveSettingUpdate(liveId, if (diamondPublicEnabled) 0 else 1) {
                            liveProfileLoadingView.isVisible = it == StatusEvent.LOADING
                        }
                    }
                    cancelButton()
                    setCancelable(false)
                }?.show()
            } else {
                viewModel.liveSettingUpdate(liveId, if (diamondPublicEnabled) 0 else 1) {
                    liveProfileLoadingView.isVisible = it == StatusEvent.LOADING
                }
            }
        }

        icShutDown.alphaClick {
            LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS).post(ITRCT_TYPE_LIVE_OFF)
        }

        icGoLiveWith.alphaClick {
            logFirebaseEvent("click_live_request")
            startLiveWith()
        }

        startLoop.isVisible =
            BuildConfig.DEBUG && (BuildConfig.FLAVOR == "internaltest")
        startLoop.onClick {
            if (viewModel.messageLoopJob == null) {
                viewModel.startSendMessageLoop()
                startLoop.text = "stop send"
            } else {
                viewModel.stopSendMessageLoop()
                startLoop.text = "start send"
            }
        }

        giftLiveImage.onClick {
            if (!isPaidLive) {
                val data = (activity as RecordActivity).getLiveInitData()
                if (data != null) {
                    context?.alertDialog {
                        title = data.midwayPaidLiveTitle
                        message = data.midwayPaidLiveDesc
                        positiveButton(R.string.fb_turn_on) {
                            (activity as RecordActivity).openTicketGiftDialog()
                        }
                        cancelButton()
                        setCancelable(false)
                    }?.show()
                } else {
                    context?.alertDialog {
                        titleResource = R.string.fb_midway_turn_pay_live_dialog_title
                        messageResource = R.string.fb_midway_turn_pay_live_dialog_message
                        positiveButton(R.string.fb_turn_on) {
                            (activity as RecordActivity).openTicketGiftDialog()
                        }
                        cancelButton()
                        setCancelable(false)
                    }?.show()
                }

            } else {
                if (topArrow.isVisible) {
                    hideHostGiftLiveTip()
                } else {
                    showHostGiftLiveTip()
                }
            }
        }

        icMute.alphaClick {
//            if (activity is RecordActivity && (activity as RecordActivity).microphoneState()) {
            if (mute != 1) {
                mute = 1
                icMute.setImageResource(R.drawable.live_mute_close)
                viewModel.liveMute(liveId, mute)
                (activity as RecordActivity).muteLocalAudioStream(mute == 1)
            } else {
                mute = 0
                icMute.setImageResource(R.drawable.live_mute_open)
                viewModel.liveMute(liveId, mute)
                (activity as RecordActivity).muteLocalAudioStream(mute == 1)
            }
//            }
        }

        viewModel.liveSettingUpdateData.observe(this, {
            diamondPublicEnabled = !diamondPublicEnabled
            icDiamondView.setImageResource(if (diamondPublicEnabled) R.drawable.icon_diamond_view_unlock else R.drawable.icon_diamond_view_lock)
        })

        LiveEventBus.get(Constants.EVENT_BUS_KEY_PAY_LIVE).observe(this) {
            (activity as RecordActivity).justNowGiftTicketData?.let { liveGiftData ->
                (activity as RecordActivity).giftTicketData = liveGiftData
            }
            isPaidLive = true
            giftLiveImage.setImageResource(R.drawable.icon_pay_live_on)
            showHostGiftLiveTip()
        }

        LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE).observe(this) { event ->
            when (event) {
                is LiveWithCallEvent -> {
                    when (event.event) {
                        1 -> {
                            //邀请上麦
                            lifecycleScope.launch {
                                startGoLiveWithCounting(
                                    event.endTime,
                                    event.userInfo.nickname,
                                    event.id
                                )
                            }
                        }
                        2 -> {
                            //自己取消
                            goLiveWithWaitingContainer.isVisible = false
                            goLiveWithTicker?.cancel()
                        }
                        3 -> {
                            //对方接受，开始连麦，ui变化
                            isMicrophone = true
                            currentLiveWithUser = event.userInfo
                            goLiveWithWaitingContainer.isVisible = false
                            goLiveWithTicker?.cancel()

                            icMute.setImageResource(if (mute == 0) R.drawable.live_mute_open else R.drawable.live_mute_close)
                        }
                        4 -> {
                            //对方拒绝，停止等待
                            goLiveWithWaitingContainer.isVisible = false
                            goLiveWithTicker?.cancel()
                            userDeclined.text = context?.getString(
                                R.string.lbl_live_with_user_declined,
                                event.userInfo.nickname
                            )
                            userDeclined.isVisible = true
                            lifecycleScope.launch {
                                delay(3000)
                                userDeclined.isVisible = false
                            }
                        }
                        5 -> {
                            //对方挂断，停止连麦，ui变化
                            currentLiveWithUser = null
                        }
                    }
                }
                is LiveRaiseHandEvent -> {
                    val count = event.raiseHandCount
                    raisedHandCount.isVisible = count > 0
                    raisedHandCount.text = if (count > 99) "99+" else count.toString()
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showHostGiftLiveTip() {
        hostGiftLiveTipJob?.cancel()
        hostGiftLiveTipJob = null
        hostGiftLiveTipJob = lifecycleScope.launch(Dispatchers.Main) {
            (activity as RecordActivity).giftTicketData?.let {
                hostGiftLiveTip.text =
                    "${getString(R.string.fb_host_gift_live_tip1)} ${it.name} (${it.coins}${
                        getString(
                            R.string.fb_host_gift_live_tip2
                        )
                    }"
            }
            topArrow?.isVisible = true
            hostGiftLiveTip?.isVisible = true
            delay(5000)
            topArrow?.isVisible = false
            hostGiftLiveTip?.isVisible = false
            hostGiftLiveTipJob?.cancel()
            hostGiftLiveTipJob = null
        }
    }

    private fun hideHostGiftLiveTip() {
        topArrow.isVisible = false
        hostGiftLiveTip.isVisible = false
        hostGiftLiveTipJob?.cancel()
        hostGiftLiveTipJob = null
    }

    fun showOtherProfile(userId: String, username: String, role: Int) {
        showOtherProfileDialog(userId, username, role)
    }

    override fun onNewGift(event: LiveGiftEvent, isSocketMe: Boolean) {
        super.onNewGift(event, isSocketMe)
        if (event.userInfo.userId == SessionPreferences.id && !isSocketMe) return
        currentDiamonds = event.diamonds
        updateCurrentDiamonds(currentDiamonds)
    }

    private suspend fun startGoLiveWithCounting(endTime: Long, targetUsername: String, id: String) {
        liveViewersHostDialog?.dismiss()
        goLiveWithWaitingContainer.isVisible = true
        txtGoLiveWithWaiting.text = getString(R.string.lbl_live_with_waiting_for, targetUsername)
        cancelLiveWith.onClick {
            viewModel.cancelLiveWith(id)
        }
        goLiveWithWaitingContainer.onClick {}
        goLiveWithTicker = ticker(1000, 0)
        val ticker = goLiveWithTicker
        if (ticker != null) {
            for (event in ticker) {
                if ((System.currentTimeMillis() - MarsApp.appInstance.timeDiscrepancy) / 1000 > endTime) {
                    goLiveWithWaitingContainer.isVisible = false
                    break
                }
            }
            goLiveWithTicker?.cancel()
        }
    }

    private fun showMoreDialog() {
        if (moreDialog == null || moreDialog?.isShow == false) {
            moreDialog = LiveMoreDialog(activity, activity, liveId = liveId,
                mode = 1, makeupEnable = makeUpEnabled,
                showMessage = {
                    openComment()
                }, onMakeupClick = {
                    makeUpEnabled = !makeUpEnabled
                    LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS)
                        .post(if (!makeUpEnabled) ITRCT_TYPE_MAKEUP_OFF else ITRCT_TYPE_MAKEUP_ON)
                    logFirebaseEvent(if (makeUpEnabled) "open_beauty" else "close_beauty")
                }, showFlip = {
                    LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS)
                        .post(ITRCT_TYPE_FLIP)
                }, showGifSelector = {
                    showGifDialog()
                }
            )

            XPopup.Builder(activity)
                .isDestroyOnDismiss(true)
                .asCustom(moreDialog)
                .show()
        }
    }

    @ExperimentalStdlibApi
    private fun startLiveWith() {
        val c = context ?: return
        XPopup.Builder(c)
            .isDestroyOnDismiss(true)
            .maxHeight(InstaLiveApp.appInstance.screenHeight - c.dp(300))
            .asCustom(
                GoLiveWithDialog(
                    c,
                    viewModel.roomId,
                    isMicrophone)
            )
            .show()
    }

    @SuppressLint("SetTextI18n")
    override fun onLiveStateInfoInJoined(data: LiveStateInfo) {
        currentDiamonds = data.diamonds
        updateCurrentDiamonds(currentDiamonds)
        hostDiamond?.isVisible = data.liveGiftShowEnable
        val count = data.raiseHandCount
        raisedHandCount?.isVisible = count > 0
        raisedHandCount?.text = if (count > 99) "99+" else count.toString()

        (activity as RecordActivity).giftTicketData?.let {
            hostGiftLiveTip?.text =
                "${getString(R.string.fb_host_gift_live_tip1)} ${it.name} (${it.coins}${getString(R.string.fb_host_gift_live_tip2)}"
        }
        if (data.isPaidLive) {
            giftLiveImage?.isVisible = true
            giftLiveImage?.setImageResource(R.drawable.icon_pay_live_on)
            showHostGiftLiveTip()
        }

        icDiamondView?.setImageResource(if (data.liveDiamondsPublic) R.drawable.icon_diamond_view_unlock else R.drawable.icon_diamond_view_lock)

//        You set {{gift_name}}({{count}} coins) to unlock this Live.q
//        privateSwitch.isVisible = data.privateInfo.visible
//        setPrivateInfoEnabled(data.privateInfo.enabled)
//        privateSwitch.setCompoundDrawables(end = if (data.privateInfo.checked) R.drawable.ic_live_private_on else R.drawable.ic_live_private_off)
    }

    private fun updateCurrentDiamonds(diamonds: Long) {
        currentDiamonds = diamonds
        hostDiamond?.text = VenusNumberFormatter.formatThousand(currentDiamonds)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_live_interaction_host
    }
}