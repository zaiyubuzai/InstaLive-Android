package com.example.instalive.app.live

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.alphaClick
import com.example.baselibrary.utils.debounceClick
import com.example.baselibrary.utils.baseToast
import com.example.baselibrary.utils.tinyMoveClickListener
import com.example.instalive.InstaLiveApp
import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.R
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE_HOST_ACTIONS
import com.example.instalive.app.Constants.ITRCT_TYPE_FLIP
import com.example.instalive.app.Constants.ITRCT_TYPE_MAKEUP_OFF
import com.example.instalive.app.Constants.ITRCT_TYPE_MAKEUP_ON
import com.example.instalive.app.Constants.LIVE_END
import com.example.instalive.app.Constants.LIVE_START
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.live.ui.GoLiveWithInviteDialog
import com.example.instalive.app.live.ui.LiveMoreDialog
import com.example.instalive.app.live.ui.LiveRaiseYourHandDialog
import com.example.instalive.app.live.ui.LiveRelativeLayout
import com.example.instalive.app.ui.GiftsDialog
import com.example.instalive.databinding.FragmentLiveInteractionBinding
import com.example.instalive.model.*
import com.example.instalive.utils.GlideEngine
import com.example.instalive.utils.VenusNumberFormatter
import com.jeremyliao.liveeventbus.LiveEventBus
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.fragment_live_interaction.*
import kotlinx.android.synthetic.main.fragment_live_interaction.hostDiamond
import kotlinx.android.synthetic.main.fragment_live_interaction.nameContainer
import kotlinx.android.synthetic.main.fragment_live_interaction.onlineCount
import kotlinx.android.synthetic.main.fragment_live_interaction.onlineCountContainer
import kotlinx.android.synthetic.main.fragment_live_interaction.startLoop
import kotlinx.coroutines.*
import splitties.dimensions.dp
import splitties.views.onClick
import timber.log.Timber
import java.util.*

@ExperimentalStdlibApi
class LiveInteractionFragment :
    LiveInteractionBaseFragment<FragmentLiveInteractionBinding>() {

    private val likedUuid = mutableSetOf<String>()

    private var goLiveWithJob: Job? = null
    private var likeJob: Job? = null

    private var goWithInviteDialog: GoLiveWithInviteDialog? = null
    private var giftsDialog: GiftsDialog? = null
    private var moreDialog: LiveMoreDialog? = null

    private var lastLikeTimeStamp = 0L
    private var likeCount = 0
    private var mute = 0

    private var isRaiseClicking = false
    private var isRaiseHanded = false

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    @OptIn(ExperimentalStdlibApi::class)
    override fun init() {
        Timber.d("live init interaction init ${System.currentTimeMillis()}")
        icLiveWithGiftReddot.isVisible = !InstaLivePreferences.liveSendGiftClicked
        icGiftReddot.isVisible = !InstaLivePreferences.liveSendGiftClicked

        txtComment.onClick {
            openComment()
        }

        liveThumb.onClick {}
        liveThumb.tinyMoveClickListener {
            doLikeFavor()
        }

        icGift.alphaClick {
            popupOpenGift()
        }

        icLiveWithGift.onClick {popupOpenGift()}

        nameContainer.onClick {
            ownerLiveUserInfo?.let { it1 -> showPersonBottomDialog(it1, 1) }
        }

//        startLoop.isVisible =
//            BuildConfig.DEBUG
        startLoop.onClick {
            if (viewModel.messageLoopJob == null) {
                viewModel.startSendMessageLoop(liveId)
                startLoop.text = "stop send"
            } else {
                viewModel.stopSendMessageLoop()
                startLoop.text = "start send"
            }
        }

        onlineCountContainer.onClick {
            val c = context
            if (c != null) {
//                XPopup.Builder(c)
//                    .isDestroyOnDismiss(true)
//                    .enableDrag(true)
//                    .asCustom(
//                        LiveViewersDialog(
//                            c,
//                            viewModel.roomId,
//                            isMicrophone,
//                            isPaidLive,
//                            if (diamondPublicEnabled) 1 else 0,
//                            currentLiveWithUser
//                        )
//                    )
//                    .show()
            }
        }

        icRaiseHand.debounceClick {
            onRaiseHandClick()
        }

        close.onClick {
            activity.onBackPressed()
        }

        icLiveWithMute.alphaClick {
            if (mute != 1) {
                mute = 1
                icLiveWithMute.setImageResource(if (mute == 0) R.mipmap.live_mute_open else R.mipmap.live_mute_close)
//                viewModel.liveMute(liveId, 1)
            } else {
                mute = 0
                icLiveWithMute.setImageResource(if (mute == 0) R.mipmap.live_mute_open else R.mipmap.live_mute_close)
//                viewModel.liveMute(liveId, 0)
            }
            (activity as LiveAudienceActivity).muteLocalAudioStream(mute == 1)
        }

        sendPicture.alphaClick {
//            MarsEventLogger.logFirebaseEvent("click_img")
            val filterMimeType = ArrayList<String>()
            filterMimeType.add("video/mp4")
            filterMimeType.add("video/quicktime")
            filterMimeType.add("image/jpeg")
            filterMimeType.add("image/jpg")
            filterMimeType.add("image/png")
            PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofVideo())
                .isMaxSelectEnabledMask(true)
                .isCanPreView(false)
                .isWeChatStyle(true)
                .theme(R.style.picture_WeChat_style)
                .imageEngine(GlideEngine.createGlideEngine())
                .isPreviewVideo(false)
                .selectionMode(PictureConfig.SINGLE)
                .maxSelectNum(1)
                .selectCountText(getString(R.string.fb_send))
                .maxVideoSelectNum(1)
                .setLanguage(LanguageConfig.ENGLISH)
                .isOnlyVideo(false)
                .isWithVideoImage(true)
                .selectMaxPrompt(resources.getString(R.string.send))
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .isCamera(false)
                .isShowPreView(false)
                .setFilterMimeType(filterMimeType)
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: MutableList<LocalMedia>?) {
                        if (result != null) {
                            when ((result[0].mimeType).lowercase(Locale.getDefault())) {
                                "video/mp4", "video/quicktime" -> {
                                    val width = result[0].width
                                    val height = result[0].height
                                    sharedViewModel.sendVideoMessage(
                                        result[0].realPath,
                                        "",
                                        width,
                                        height,
                                        result[0].duration,
                                        result[0].size,
                                        3,
                                        liveId,
                                        -1
                                    )
                                }
                                "image/jpeg", "image/jpg", "image/png" -> {
                                    sharedViewModel.sendImageMessage(
                                        liveId,
                                        3,
                                        result[0].realPath,
                                        "",
                                        result[0].width,
                                        result[0].height,
                                        -1
                                    )
                                }
                                else -> {
                                }
                            }
                        }
                    }

                    override fun onCancel() {}

                })
        }

        sendGif.alphaClick {
//            showGifDialog()
        }

        LiveEventBus.get(EVENT_BUS_KEY_LIVE).observe(this) { event ->
            when (event) {
                is LiveWithInviteEvent -> {
                    if (event.targetUserId == SessionPreferences.id) {
                        showLiveWithInvite(liveId, event.timeoutTS)
                    }
                }
                is LiveWithCancelEvent -> {
                    if (event.targetUserId == SessionPreferences.id) {
                        hideLiveWithInvite()
                        onHandsDown()
                    }
                }
                is LiveWithAgreeEvent -> {
                    val have = event.liveUserWithUidInfos.any {
                        it.userInfo.userId == SessionPreferences.id
                    }
                    if (have) {
                        //只有我自己是连麦对象的时候才对以下view做操作
                        //          liveWithHangUp.isVisible = true
                        isShowLiveWithContainer(true)
                        onHaveMicrophone()
                    }
                }
                is LiveWithRejectEvent -> {
                    isShowLiveWithContainer(false)
                    onHandsDown()
                }
                is LiveWithHangupEvent -> {
                    if (event.targetUserId == SessionPreferences.id) {
                        if (mute == 1) {
                            mute = 0
                            icLiveWithMute.setImageResource(if (mute == 0) R.mipmap.live_mute_open else R.mipmap.live_mute_close)
                            (activity as LiveAudienceActivity).muteLocalAudioStream(mute == 1)
                        }
                        isShowLiveWithContainer(false)
                        onHandsDown()
                    }
                }
//                is PublisherStateEvent -> {
//                    if (SessionPreferences.id == event.targetUserInfo.userId) {
//                        // 1 静音 2 取消静音 3 摄像头关闭 4 摄像头开启
//                        if (event.event == 1) {
//                            mute = 1
//                            icLiveWithMute.setImageResource(if (mute == 0) R.drawable.live_mute_open else R.drawable.live_mute_close)
//                            (activity as LiveActivity).muteLocalAudioStream(mute == 1)
//                        } else if (event.event == 2) {
//                            mute = 0
//                            icLiveWithMute.setImageResource(if (mute == 0) R.drawable.live_mute_open else R.drawable.live_mute_close)
//                            (activity as LiveActivity).muteLocalAudioStream(mute == 1)
//                        }
//                    }
//                    if (event.targetUserInfo.userId == ownerLiveUserInfo?.userId && !(activity as LiveActivity).isMicrophone()){
//                        hostMute.isVisible = event.event == 1
//                    }
//                }
//                is LiveDiamondsPublicEvent -> {
//                    diamondPublicEnabled = event.liveDiamondsPublic == 1
//                    showDiamonds(event)
//                }
            }
        }

        icLiveWithMakeup.alphaClick {
            makeUpEnabled = !makeUpEnabled
            LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS)
                .post(if (!makeUpEnabled) ITRCT_TYPE_MAKEUP_OFF else ITRCT_TYPE_MAKEUP_ON)
            icLiveWithMakeup.setImageResource(if (makeUpEnabled) R.mipmap.live_beauty_yes else R.mipmap.live_beauty_no)
        }

        icLiveWithMore.alphaClick {
            showMoreDialog()
        }

        icLikeLiveWith.alphaClick {
            doLikeFavor()
        }

        viewModel.raiseHandData.observe(this) {
            isRaiseClicking = false
            onRaiseHand()
        }
        viewModel.handsDownData.observe(this) {
            isRaiseClicking = false
            onHandsDown()
        }

        sharedViewModel.liveOnlineCount.observe(this, {
            if (it == null) return@observe
            onlineCount?.text = it
        })

    }

    fun refreshHostMute(isMute: Boolean) {
        hostMute?.isVisible = isMute
    }

    private fun showMoreDialog() {
        val c = context ?: return
        if (moreDialog == null || moreDialog?.isShow == false) {
            moreDialog = LiveMoreDialog(activity, activity, liveId = liveId, 2, false,
                showMessage = {
                    openComment()
                }, onMakeupClick = {

                }, showFlip = {
                    LiveEventBus.get(EVENT_BUS_KEY_LIVE_HOST_ACTIONS)
                        .post(ITRCT_TYPE_FLIP)
                }, showGifSelector = {
//                    showGifDialog()
                }
            )

            XPopup.Builder(c)
                .isDestroyOnDismiss(true)
                .asCustom(moreDialog)
                .show()
        }
    }

    fun showOtherProfile(
        userId: String,
        username: String,
        role: Int
    ) {
        showOtherProfileDialog(userId, username, role)
    }

    fun showMeProfile(
    ) {
//        val userData = SESSION.retrieveMeInfo()
//        userData?.let {
//            showMeProfileDialog(it)
//        }
    }

    private fun isShowLiveWithContainer(isShowLiveWith: Boolean) {
        if (liveWithInteractions == null) return
        liveWithInteractions.isVisible = isShowLiveWith
//        sendPicture.isVisible = !isShowLiveWith
        txtComment.isVisible = !isShowLiveWith
        liveThumb.isVisible = !isShowLiveWith
//        sendGif.isVisible = !isShowLiveWith
        icGift.isVisible = !isShowLiveWith
    }

    override fun doLikeFavor() {
        super.doLikeFavor()
        if (likeCount == 0) {
            val c = context
            if (c != null) {
                Glide.with(c)
                    .asBitmap()
                    .load(SessionPreferences.portrait)
                    .circleCrop()
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?,
                        ) {
                            liveLikesAnimView?.addFavor(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            }
        }

        likeCount++
        lastLikeTimeStamp = System.currentTimeMillis()

        likeJob?.cancel()
        likeJob = lifecycleScope.launch {
            delay(500)
            if (likeCount > 0) {
//                logFirebaseEvent("like_live")
                val uuid = UUID.randomUUID().toString()
                likedUuid.add(uuid)
//                viewModel.likeLive(likeCount, uuid, conversationId, liveId)
            }
            likeCount = 0
        }
    }

    private fun showLiveWithInvite(id: String?, endTime: Long) {
        if (goWithInviteDialog?.isShow == true) return
        val liveInfo = this.sharedViewModel.liveStateInfoLiveData.value?.first ?: return
        val c = context ?: return
        goWithInviteDialog =
            GoLiveWithInviteDialog(
                c,
                id ?: liveId,
                liveInfo.owner.portrait,
                liveInfo.owner.nickname,
                false
            )
        XPopup.Builder(c)
            .isDestroyOnDismiss(true)
            .dismissOnTouchOutside(false)
            .enableDrag(false)
            .asCustom(goWithInviteDialog)
            .show()

        goLiveWithJob?.cancel()
        goLiveWithJob = lifecycleScope.launch(Dispatchers.IO) {
            var time = (System.currentTimeMillis() - appInstance.timeDiscrepancy) / 1000 - endTime
            if (time > -10) time = -60
            while (time < 0) {
                time++
                delay(1000)
            }
            withContext(Dispatchers.Main) {
                hideLiveWithInvite()
            }
        }
    }

    fun hideLiveWithInvite() {
        if (goWithInviteDialog?.isShow == true) {
            Timber.d("hideLiveWithInvite")
            goWithInviteDialog?.dismiss()
        }

        goLiveWithJob?.cancel()
        goLiveWithJob = null
    }

    fun onRaiseHandClick() {
        if (sharedViewModel.isMicrophoneUser) {
            baseToast(R.string.fb_live_you_on_live_now)
            return
        }

        if (!isRaiseClicking) {
            isRaiseClicking = true
            if (isRaiseHanded) {
//                logFirebaseEvent("hands_down")
                viewModel.handsDown(liveId, {
                    isRaiseClicking = false
                }) {
                    loadingAnimContainer?.isVisible = it == StatusEvent.LOADING
                }
            } else {
                doRequestHandsUp()
            }
        }

    }

    override fun openGift(giftId: String?) {
//        popupOpenGift(giftId= giftId)
    }

    override fun onRaiseHand() {
        super.onRaiseHand()
        if (!isAdded) return
        isRaiseHanded = true
        icRaiseHand.setImageResource(R.mipmap.live_raise_hand_upping)
        (activity as LiveAudienceActivity).onHandCanDownOrUp(LiveRelativeLayout.Raise_Hand_Doing)
    }

    /**
     * 看播端连麦隐藏情况
     * 1、直播结束
     * 2、连麦结束
     */
    override fun onHandsDown() {
        if (!isAdded) return
        isRaiseHanded = false
        close?.isVisible = true
        icRaiseHand?.setImageResource(R.mipmap.live_raise_hand_can)
        try {
            (activity as LiveAudienceActivity).onHandCanDownOrUp(LiveRelativeLayout.Raise_Hand_Can)
        } catch (e: Exception) {
        }
    }

    private fun onHaveMicrophone() {
        isRaiseHanded = false
        icRaiseHand?.setImageResource(R.mipmap.live_raise_hand_no)
        close?.isVisible = false
        try {
            (activity as LiveAudienceActivity).onHandCanDownOrUp(LiveRelativeLayout.Raise_Hand_Cannot)
        } catch (e: Exception) {
        }
    }

    override fun doRequestHandsUp() {
        if (!InstaLivePreferences.liveRaiseHandDialogShowed) {
            val c = context ?: return
            XPopup.Builder(c)
                .asCustom(LiveRaiseYourHandDialog(c) {
//                    logFirebaseEvent("raised_hand")
                    viewModel.raiseHand(liveId, { isRaiseClicking = false }) {
                        loadingAnimContainer?.isVisible = it == StatusEvent.LOADING
                    }
                })
                .show()
            InstaLivePreferences.liveRaiseHandDialogShowed = true
        } else {
//            logFirebaseEvent("raised_hand")
            viewModel.raiseHand(liveId, { isRaiseClicking = false }) {
                loadingAnimContainer?.isVisible = it == StatusEvent.LOADING
            }
        }
    }

    private fun popupOpenGift(giftId: String? = null) {
        if (!isAdded) return
        val c = context?:return
        if (giftsDialog == null || giftsDialog?.isShow == false) {
            giftsDialog = GiftsDialog(
                c,
                viewModel.giftListLiveData.value,
                "",
                liveId,
                giftId,
                1,
                -1,
                onGiftSent = { gift, dialog ->
                    //insert gift event to sequence
                    gift.isOwnerGift = true
                    LiveEventBus.get(EVENT_BUS_KEY_LIVE).post(gift)
                    if (gift.giftInfo?.specialEffect?.show == true) {
                        dialog.dismiss()
                    }
                },
                onDismiss = {

                },
                onSelectedGift = {},
                gotoFirstRecharge = { it1, isPaymentByCard ->

                }
            )

            XPopup.Builder(c)
                .isDestroyOnDismiss(true)
                .hasShadowBg(false)
                .asCustom(giftsDialog).show()

            icGiftReddot.isVisible = false
            icLiveWithGiftReddot.isVisible = false
        }
    }

    override fun showCornerLikes(voteEvent: LikeEvent) {
        if (!likedUuid.contains(voteEvent.uuid)) {
            super.showCornerLikes(voteEvent)
        }
    }

    override fun showLiveWith() {
        showLiveWithInvite(
            null,
            (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 + 60
        )
    }

    override fun onNewGift(event: LiveGiftEvent, isSocketMe: Boolean) {
        super.onNewGift(event, isSocketMe)
        if (event.userInfo.userId == SessionPreferences.id && !isSocketMe) return
        hostDiamond?.text = VenusNumberFormatter.formatThousand(event.diamonds)
    }

    override fun onLiveStateInfoInJoined(data: LiveStateInfo) {
//        icLiveWithGift?.isVisible = data.liveGiftShowEnable
//        icGift?.isVisible = data.liveGiftShowEnable && viewModel.isMicrophoneUser.value != true
//        icGiftReddot?.isVisible = data.liveGiftShowEnable && viewModel.isMicrophoneUser.value != true
        showDiamonds(
            LiveDiamondsPublicEvent(
                if (data.liveDiamondsPublic == 1) 1 else 0,
                data.diamonds
            )
        )
        if (data.liveWithUserInfos.size in 1..1) {
            hostMute?.isVisible = data.liveWithUserInfos[0].mute == 1
        }

        when (data.state) {
            LIVE_START -> {
                if (data.liveWithUserInfos.size > 1) {
                    sharedViewModel.isMicrophone = true
                } else {
                    onHandsDown()
                    sharedViewModel.isMicrophone = false
                }
            }
            LIVE_END -> {
                sharedViewModel.isMicrophone = false
                onHandsDown()
            }
        }
    }

    private fun showDiamonds(liveDiamondsPublicEvent: LiveDiamondsPublicEvent) {
        hostDiamond?.text = VenusNumberFormatter.formatThousand(liveDiamondsPublicEvent.diamonds)
        diamondsContainer?.isVisible = liveDiamondsPublicEvent.liveDiamondsPublic == 1
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_live_interaction
    }

}