package com.example.instalive.app.live

import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.utils.marsToast
import com.example.instalive.R
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LIVE
import com.example.instalive.app.Constants.EXTRA_LIVE_ID
import com.example.instalive.app.Constants.LIVE_END
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.live.ui.LiveRelativeLayout
import com.example.instalive.databinding.ActivityLiveAudienceBinding
import com.example.instalive.model.GiftData
import com.example.instalive.model.LiveStateEvent
import com.example.instalive.model.LiveUserInfo
import com.example.instalive.model.Owner
import com.jeremyliao.liveeventbus.LiveEventBus
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_live_audience.*
import splitties.dimensions.dp
import splitties.views.onClick
import timber.log.Timber

@ExperimentalStdlibApi
class LiveAudienceActivity : LiveBaseActivity<ActivityLiveAudienceBinding>(),
    LiveEmptyFragment.OnEventListener {

    private var isGiftCardViewUp = false//礼物卡片被弹窗顶起

    private var liveOwner: Owner? = null
    private val liveInteractionFragment: LiveInteractionFragment by lazy {
        LiveInteractionFragment()
    }

    override fun initData(savedInstanceState: Bundle?) {
        screenName = "live_audience_view"
        val set = ConstraintSet()
        set.clone(container)
        set.setMargin(R.id.closeActivity, ConstraintSet.TOP, BarUtils.statusBarHeight)
        set.setMargin(R.id.closeActivity, ConstraintSet.TOP, dp(68) + BarUtils.statusBarHeight)
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
        removeDone.onClick {onBackPressed()}
        leavePrompt.onClick {}
        removePrompt.onClick {}
        closeActivity.onClick { onBackPressed()}
        loadingBgCover.onClick {}
        nonGifterPrompt.onClick {}
        unlockLivePrompt.onClick {}

        profileAvatar.onClick {
            viewModel.getPersonalData(liveOwner?.userId ?: "") {}
        }

        username.onClick {
            viewModel.getPersonalData(liveOwner?.userId ?: "") {}
        }
        touchFl.setOnTouchListener { _, event ->
            event?.let {
//                liveFragment.isLiveViewTouched(it.rawX.toInt(), it.rawY.toInt())
            }
            false
        }

        unlockLiveBtn.onClick {
//            if (!isSending) {
//                isSending = true
//                viewModel.getAccountBalance {
//                    progress?.isVisible = it == StatusEvent.LOADING
//                }
//            }
        }

        sendGiftBtn.onClick {
//            isNonGifter = true
//            popupOpenGift()
        }

        LiveEventBus.get(EVENT_BUS_KEY_LIVE).observe(this, {
            when (it) {
                is LiveStateEvent -> {
                    if (it.liveState == LIVE_END) {
//                        showLeavePrompt()
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
            }
        })

//        viewModel.accountBalanceData.observeForever {
//            val balance = it.coinBalance.balance.toInt()
//            currentBalance = balance.toLong()
//            sendTicketGift()
//        }
        viewModel.personalLiveData.observe(this, {})
//        viewModel.sendTicketDate.observe(this) {
//            isSending = false
//            hideUnlockLivePrompt()
//            showLoadingCover()
//            isLockCover = false
//            viewModel.joinLive(SessionPreferences.id, liveId, sharedViewModel.liveJoinData)
//            initLiveFragment(rid)

//            val backpackListData = sharedViewModel.giftBackpackListData.value
//            val backpackGiftIndex = backpackListData?.backpackList?.indexOfFirst { backpackGiftData ->
//                backpackGiftData.giftId == giftSent?.giftId && giftSent != null
//            }
//            if (backpackListData != null && backpackGiftIndex != null && backpackGiftIndex != -1) {
//                backpackListData.backpackList[backpackGiftIndex].num--
//                sharedViewModel.giftBackpackListData.postValue(backpackListData)
//            }
//            RecentConversation.myLevelData = it.toMyLevelData(RecentConversation.conversationsEntity.level)
//            currentBalance = it.balance
//            sharedViewModel.getBackpackList()
//        }

//        viewModel.rechargeBonusData.observe(this) {
//            if (it.recharged) {
//                showLiveRechargeDialog(0, currentBalance, "live_ticket", !it.recharged, true, true, null, null)
//            } else {
//                showWelcomeRechargeBonusDialog(currentBalance, 0, true, true, true, false, null, null)
//            }
//        }

        viewModel.errorCodeLiveData.observe(this) {
//            isSending = false
            marsToast(viewModel.errorMessageLiveData.value.toString())
        }

        sharedViewModel.liveUsersSizeData.observe(this, {
//            onLiveUsersChanged(it)
        })

        sharedViewModel.liveJoinData.observe(this) {
            sharedViewModel.liveStateInfoLiveData.postValue(it)
            if (it.first != null) {
                if (liveOwner == null) {
                    liveOwner = it.first?.owner
                    setCoverImageOrUsername()
                }
                anchorUid = try {
                    it.first?.liveWithUserInfos?.get(0)?.uid.toString()
                } catch (e: Exception) {
                    "0"
                }

                mUid = try {
                    it.first?.tokenInfo?.uid?.toInt() ?: 0
                } catch (e: Exception) {
                    0
                }
                token = it.first?.tokenInfo?.token
                resolution = it.first?.resolution

//                if (isMidwayPayLive || liveInteractionFragment.isPaidLive) {
//                    Timber.d("live text22222222222")
//                    hideLoadingCover()
//                    liveFragment.setRtcEngineAudio(true)
//                    liveFragment.isLockedLive = true
//                    if (liveFragment.viewModelInit()) liveFragment.startGetLiveState()
//                } else {
                Timber.d("live text33333333333")
                initLiveFragment()
//                }
            } else {
                hideLoadingCover()
                if (liveOwner == null) {
                    liveOwner = it.second?.extData?.owner
                    setCoverImageOrUsername()
                }
                when (it.second?.error?.code) {
                    6200 -> {
                        removeUser()
                    }
                    6201, 6206 -> {
                        removeUser(
                            it.second?.error?.message
                                ?: getString(R.string.you_have_been_removed_from_the_live_video)
                        )
                    }
                    6102 -> {
                        showLeavePrompt()
                    }
                    6501 -> {
                        Timber.d("join 3")
//                        liveFragment.isLockedLive = it.second?.extData?.shadowDisplay ?: false
//                        liveFragment.setRtcEngineAudio(liveFragment.isLockedLive)
                        liveInteractionFragment.isPaidLive = true
                        showUnlockLivePrompt(it.second?.extData?.giftInfo)
//                        if (!isMidwayPayLive) {
//                            val uid = it.second?.extData?.tokenInfo?.uid ?: "0"
//                            setAnchorUid(
//                                uid,
//                                it.second?.extData?.tokenInfo?.token,
//                                uid.toInt(),
//                                it.first?.resolution
//                            )
//                            initLiveFragment()
//                        }
                    }
                    6502 -> {
                        it.second?.let { marsError ->
                            marsError.error?.message?.let { message ->
                                joinLiveNonGiftError(message)
                            }
                        }
                    }
                    else -> {
                        it.second?.let { marsError ->
                            marsError.error?.message?.let { message ->
                                joinLiveErrorMessage(message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initObserver() {}

    private fun changeMarginGiftCard(up: Boolean) {
        isGiftCardViewUp = up
        if (up){
            setMarginGiftCard(dp(420))
        } else {
            val size =  sharedViewModel.liveUsersSizeData.value?:1
            onLiveUsersChanged(size)
        }
    }

    private fun onLiveUsersChanged(size: Int){
        if (isGiftCardViewUp || isFinishing) return
        val b = when(size){
            0,1 -> {
                dp(300) + dp(82) + dp(8)
            }
            2 -> {
                val top = BarUtils.statusBarHeight + dp(68) + dp(280) + dp(10)
                val bottom = container?.height?:2000
                bottom - top - dp(8)
            }
            3,4-> {
                val top = BarUtils.statusBarHeight + dp(68) + dp(180) + dp(4) + dp(180) + dp(10)
                val bottom = container?.height?:2000
                bottom - top - dp(8)
            }
            5, 6-> {
                val top = BarUtils.statusBarHeight + dp(68) + dp(180) + dp(4) + dp(180) + dp(10)
                val bottom = container?.height?:2000
                bottom - top - dp(8)
            }
            else -> {
                val top = BarUtils.statusBarHeight + dp(68) + dp(180) + dp(4) + dp(180) + dp(4) + dp(180) + dp(10)
                val bottom = container?.height?:2000
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

        Glide.with(this)
            .load(liveOwner?.portrait)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(dp(12))))
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(unlockLiveAvatar)

        username.text = liveOwner?.nickname ?: ""
        liveOwner?.portrait?.let {
            showBlurTransformationCover(it, loadingBgCover)
            showBlurTransformationCover(it, nonGifterBgCover)
            showBlurTransformationCover(it, leaveBgCover)
            showBlurTransformationCover(it, removeBgCover)
            showBlurTransformationCover(it, unlockBgCover)
            showBlurTransformationCover(it, nonGifterBgCover)
        }

//        liveFragment.setLoadingCover(liveOwner?.portrait ?: "")

    }

    private fun showBlurTransformationCover(portrait: String, view: ImageView) {
        Glide.with(this)
            .load(portrait)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(this, 25, 8)))
            .into(view)
    }

    private fun showLoadingCover() {
        loadingAnimContainer?.isVisible = true
    }

    private fun hideLoadingCover() {
        loadingAnimContainer?.isVisible = false
    }

    private fun initLiveFragment() {
        giftContainer.isVisible = true
        liveInteractionFragment.giftSecondContainer = giftSecondContainer
        liveInteractionFragment.giftFirstContainer = giftFirstContainer
        liveInteractionFragment.liveLikesAnimView = liveLikesAnimView
        liveInteractionFragment.giftAnim = giftAnim
        liveInteractionFragment.liveId = liveId?:""
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
        TODO("Not yet implemented")
    }

    override fun onLiveStateError(code: Int, msg: String) {
        TODO("Not yet implemented")
    }

    override fun changeNetworkUIPosition(isSingleLive: Boolean) {
        TODO("Not yet implemented")
    }

    override fun addLiveVideoContainer(liveRl: LiveRelativeLayout) {
        TODO("Not yet implemented")
    }

    override fun removeLiveVideoContainer(liveRl: LiveRelativeLayout) {
        TODO("Not yet implemented")
    }

    override fun showInteractionFragment() {
        TODO("Not yet implemented")
    }

    override fun onHangUPClick(liveUserInfo: LiveUserInfo) {
        TODO("Not yet implemented")
    }

    override fun onProfileClick(liveUserInfo: LiveUserInfo) {
        TODO("Not yet implemented")
    }

    override fun onRaiseHandClick() {
        TODO("Not yet implemented")
    }
    //endregion

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
//        allowInAppNotify = true
//        isLockCover = true
//        if (isRemovedFromGroup) return //Has been removed from group chats

        viewModel.isMicrophoneUser.value = false
        liveInteractionFragment.hideLiveWithInvite()

        leavePrompt?.isVisible = true

        hideLoadingCover()
        unlockLivePrompt?.isVisible = false
        nonGifterPrompt?.isVisible = false
        removePrompt?.isVisible = false

//        liveGiftDialog?.dismiss()
    }

    fun reJoin() {
//        isMidwayPayLive = true
//        sharedViewModel.liveLeave(SessionPreferences.id, liveId)
//        viewModel.joinLive(SessionPreferences.id, liveId)
    }

    private fun showUnlockLivePrompt(gift: GiftData?) {
        if (leavePrompt?.isVisible == true) return
//        isLockCover = true
        gift?.let {
//            this.gift = it
            Glide.with(this)
                .load(it.image)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.ic_live_gift_default)
                .into(unlockLiveGift)
            Glide.with(this)
                .load(liveOwner?.portrait)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(dp(33))))
                .skipMemoryCache(false)
                .into(unlockLiveAvatar)
//            onLiveNow.text = if (!isMidwayPayLive) {
//                "${getString(R.string.fb_hi)} ${SessionPreferences.nickName}, ${getString(R.string.fb_unlock_live_i_on_live_now)}"
//            } else {
//                "${getString(R.string.fb_hi)} ${SessionPreferences.nickName} ${getString(R.string.fb_midway_live_i_on_live_now)}"
//            }
//
//            unlockLiveGiftCoin.text = getString(R.string.fb_any_lower_coins, it.coins.toString())

            unlockLivePrompt?.isVisible = true
            closeActivity?.isVisible = true

            nonGifterPrompt?.isVisible = false
            removePrompt?.isVisible = false

//            liveGiftDialog?.dismiss()
        }
    }

    private fun hideUnlockLivePrompt() {
//        liveFragment.isLockedLive = false
        unlockLivePrompt?.isVisible = false
        closeActivity?.isVisible = false
    }

    /**
     * 用户被移除的ui
     */
    fun removeUser(desc: String = getString(R.string.you_have_been_removed_from_the_live_video)) {
        if (leavePrompt?.isVisible == true) return

        viewModel.isMicrophoneUser.value = false
//        isLockCover = true
        activityDesc?.text = desc
        unlockLivePrompt?.isVisible = false
        closeActivity?.isVisible = true
        removePrompt?.isVisible = true
    }

    private fun joinLiveErrorMessage(desc: String) {
        closeActivity?.isVisible = true
        removePrompt?.isVisible = true
        activityDesc?.text = desc
    }

    fun joinLiveNonGiftError(desc: String) {
        nonGifterPrompt?.isVisible = true
        closeActivity?.isVisible = true
        if (desc.isNotEmpty()) nonGifterDesc?.text = desc
    }

    private fun hideNonGiftError() {
        nonGifterPrompt?.isVisible = false
        closeActivity?.isVisible = false
    }
}