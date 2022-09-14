package com.example.instalive.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.dialog_gifts.view.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baselibrary.utils.baseToast
import com.example.baselibrary.views.BaseBottomPopup
import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.base.SharedViewModel
import com.example.instalive.model.*
import com.example.instalive.utils.VenusNumberFormatter
import com.venus.framework.util.isNeitherNullNorEmpty
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import kotlinx.android.synthetic.main.fragment_live_gift_list_page.view.*
import kotlinx.coroutines.*
import splitties.collections.forEachWithIndex
import splitties.dimensions.dip
import splitties.systemservices.layoutInflater
import splitties.views.onClick
import splitties.views.textResource
import timber.log.Timber
import java.util.*

@SuppressLint("ViewConstructor")
class GiftsDialog(
    context: Context,
    private val giftDatas: List<GiftData>?,
    private val conversationId: String,
    private val liveId: String,
    private var checkedGiftId: String?,
    private val liveGiftMode: Int,//1:send gift; 2:check ticket; 3:check ticket and open pay live
    private var level: Int,
    val onGiftSent: (LiveGiftEvent, GiftsDialog) -> Unit,
    val onDismiss: () -> Unit,
    val onSelectedGift: (GiftData) -> Unit,
    var gotoFirstRecharge: (Long, isPaymentByCard: Boolean) -> Unit,
) :
    BaseBottomPopup<GiftsViewModel>(context) {
    val sharedViewModel = appInstance.getAppViewModelProvider()
        .get(SharedViewModel::class.java)
    var giftSent: LiveGiftEvent? = null
    var currentBalance: Long = 0
    var goRecharging = false
    var isSending = false

    private var giftAdapter: GiftListAdapter? = null
    private var bonusTimeJob: Job? = null
    private var prevLevel = 0

    private var currentLevelIcon: String? = null

    private val MAX_PROGRESS = 505
    private var pageFlips = 0
    private var checkedGift: GiftData? = null
    private var paperPosition: Int = 0
    private var position: Int = 0
    private var isVertical = false
    private val giftListObserver = Observer<List<GiftData>> {
        if (it.isNotEmpty()) {
            if (isVertical) {
                val gridLayoutManager = GridLayoutManager(
                    context,
                    3,
                    GridLayoutManager.VERTICAL,
                    false
                )

                if (checkedGiftId != null) {
                    checkedGift = it.find { liveGift ->
                        liveGift.id == checkedGiftId
                    }
                } else {
                    checkedGiftId = it[0].id
                    checkedGift = it[0]
                }

                giftsRecyclerView.layoutManager = gridLayoutManager
                giftAdapter =
                    GiftListAdapter(
                        it,
                        checkedGiftId,
                        3,
                        false,
                        onReset = { _, giftData ->
                            val oldIndex = giftAdapter?.viewList?.indexOfLast {it1 ->
                                it1.id == checkedGiftId
                            }
                            checkedGiftId = giftData.id
                            checkedGift = giftData
                            if (oldIndex != null) {
                                giftAdapter?.giftId = giftData.id
                                giftAdapter?.notifyItemChanged(oldIndex)
                            }
                        },
                        showGlobal = {})
                giftsRecyclerView.adapter = giftAdapter

                if (checkedGiftId.isNeitherNullNorEmpty()) {
                    val index = it.indexOfFirst { lgd ->
                        lgd.id == checkedGiftId
                    }
                    giftsRecyclerView.scrollToPosition(index)
                }
            } else {
                val size = it.size
                val count = if (size % 6 > 0) size / 6 + 1 else size / 6

                if (it.isNotEmpty()) checkedGift = it[0]
                var ininin = 0
                val pageListView = mutableListOf<GiftListPageView>()
                for (i in 0 until count) {
                    val sublist = if (it.size > ((i + 1) * 6)) {
                        it.subList(i * 6, i * 6 + 6)
                    } else {
                        it.subList(i * 6, it.size)
                    }

                    if (checkedGiftId != null) {
                        val giftIdIndex = sublist.filter { liveGift ->
                            liveGift.id == checkedGiftId
                        }
                        if (giftIdIndex.isNeitherNullNorEmpty()) {
                            checkedGift = giftIdIndex[0]
                            ininin = i
                        }
                    }
                    Timber.d("gift time 6 ${System.currentTimeMillis()}")
                    val giftListPageView =
                        GiftListPageView(
                            context,
                            gifts = sublist,
                            isFirstPage = i == 0,
                            paperPosition = i,
                            onReset = {
                                pageListView.forEach {
                                    it.reset()
                                }
                            },
                            giftId = checkedGiftId
                        )
                    giftListPageView.onChecked = checkedGiftBack
                    giftListPageView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dip(256))
                    pageListView.add(giftListPageView)
                    Timber.d("gift time 7 ${System.currentTimeMillis()}")
                }

                giftsPager.adapter =
                    GiftListPagerAdapter(pageListView) { liveG ->
                        sendGift(liveG)
                    }
                giftsPager.offscreenPageLimit = 3

                if (pageListView.size > 1) {
                    pagerIndicator.isVisible = true
                    pagerIndicator.apply {
                        setSliderColor(Color.parseColor("#59FFFFFF"), Color.parseColor("#ffffff"))
                        setSliderWidth(dip(8).toFloat())
                        setSliderHeight(dip(8).toFloat())
                        setSliderGap(dip(8).toFloat())
                        setSlideMode(IndicatorSlideMode.SMOOTH)
                        setIndicatorStyle(IndicatorStyle.CIRCLE)
                    }
                    pagerIndicator.setupWithViewPager(giftsPager)
                }
                giftsPager.currentItem = ininin
                giftsPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                    }

                    override fun onPageSelected(position: Int) {
                        Timber.d("position = $position $pageFlips")
                        pageFlips++
                    }

                    override fun onPageScrollStateChanged(state: Int) {}
                })
                Timber.d("gift time 8 ${System.currentTimeMillis()}")
            }
        }
    }

    private fun sendGift(it: GiftData) {
        if (isSending) {
            return
        }

        if (liveGiftMode == MODE_CHECK_TICKET) {
            isSending = true
            onSelectedGift.invoke(it)
            dismiss()
            return
        }

        if (liveGiftMode == MODE_CHECK_TICKET_AND_OPEN_LIVE) {
            onSelectedGift.invoke(it)
            isSending = true
//            viewModel.turnOnPayLive(liveId, it.id, onError = { _, msg ->
//                marsToast(msg)
//            }, onEvent = { event ->
//                sendGiftText.isVisible = event != StatusEvent.LOADING
//                sendGiftProgress.isVisible = event == StatusEvent.LOADING
//            })
            return
        }

        if (it.giftInfo?.specialEffect?.show == true) {
            isSending = true
        }
        val uuid = UUID.randomUUID().toString()
        giftSent = LiveGiftEvent(
            uuid,
            it.id,
            1,
            0,
            1,
            LiveUserInfo(
                SessionPreferences.id,
                SessionPreferences.userName ?: "",
                SessionPreferences.nickName ?: "",
                SessionPreferences.portrait ?: "",
                SessionPreferences.isVerified,
                SessionPreferences.bio,
                SessionPreferences.portraitIc,
                uid = 0,
                mute = 0,
            ),
            it.giftInfo
        )

        viewModel.sendLiveGift(
            liveId,
            it.id,
            uuid,
        ) { _, msg ->
            isSending = false
            baseToast(msg)
        }
        pageFlips = 0
    }

    private fun onGiftData(giftDatas: List<GiftData>?) {
        if (giftDatas?.isNotEmpty() == true) {
            loadingAnim.isVisible = false
            viewModel.giftListLiveData.postValue(giftDatas)
        } else {
            viewModel.getGiftList { _: Int, msg: String ->
                loadingAnim.isVisible = false
                baseToast(msg)
            }
        }
    }

    @ExperimentalStdlibApi
    private fun showLevelDialog() {
//        val c = context ?: return
//        XPopup.Builder(c)
//            .maxHeight(c.dip(480))
//            .isDestroyOnDismiss(true)
//            .asCustom(
//                LiveWebUrlDialog(
//                    c,
//                    BuildConfig.MARS_WEB_URL + "account/level/main?id=" + RecentConversation.conversationsEntity.ownerId
//                )
//            )
//            .show()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun initData() {
        screenName = "live_view"
        if (liveGiftMode != MODE_SEND_GIFT) {
            selected.isVisible = true
            rechargeContainer.isVisible = false
            sendGiftText.textResource = R.string.fb_confirm
        } else {
            updateLevel()
            titleContainer.isVisible = false
            levelTextView?.text = level.toString()
            selected.isVisible = false
            rechargeContainer.isVisible = true
            sendGiftText.textResource = R.string.send
            prevLevel = 0
        }
        viewModel.reset()
//        viewModel.showBonusTips()
//        viewModel.getRechargePackages(refer = 0, null) { it1, it2 ->
//            packageData = it1
//            packageExtData = it2
//        }
        onGiftData(giftDatas)
//        marketingRecharge.onClick {
//            CollectHelper.collectUV(1004)
//            viewModel.getRechargePackages(2, null) { it1, it2 ->
//                onRecharge(
//                    2,
//                    currentBalance,
//                    "card_bonus",
//                    viewModel.rechargeBonusData.value?.show == 1,
//                    it1,
//                    it2,
//                )
//                dismiss()
//            }
//        }
        rechargeContainer.onClick {
            gotoDefaultRecharge("giftpop_coinbalance")
            dismiss()
        }

        sendGiftText.onClick {
            checkedGift?.let {
                sendGift(it)
            }
        }
        titleContainer.onClick {
            if (liveGiftMode == MODE_SEND_GIFT) {
                showLevelDialog()
            }
        }

//        sharedViewModel.giftBackpackListData.observeForever(giftBackpackListObserver)

        viewModel.giftListLiveData.observe(this, giftListObserver)
        viewModel.sendGiftLiveData.observe(this) {
            isSending = false
            level = it.level

            if (it.giftInfo != null && giftSent != null) {
                giftSent?.giftInfo = it.giftInfo
            }
            giftSent?.let { liveGiftEvent ->
                onGiftSent(liveGiftEvent, this)
            }
            currentBalance = it.balance
            if (currentBalance <= 0L) {
                txtRechargeAmount.textResource = R.string.fb_recharge
            } else {
                txtRechargeAmount.text = currentBalance.toInt().toString()
            }
//            val backpackListData = sharedViewModel.giftBackpackListData.value
//            val backpackGiftIndex =
//                backpackListData?.backpackList?.indexOfFirst { backpackGiftData ->
//                    backpackGiftData.giftId == giftSent?.giftId && giftSent != null
//                }
//            if (backpackListData != null && backpackGiftIndex != null && backpackGiftIndex != -1) {
//                backpackListData.backpackList[backpackGiftIndex].num--
//                sharedViewModel.giftBackpackListData.postValue(backpackListData)
//                (giftsPager.adapter as GiftListPagerAdapter).rebuildItemUi(
//                    paperPosition,
//                    position,
//                    backpackListData
//                )
//            }
//            updateLevel()
            if (liveGiftMode == MODE_SEND_GIFT) {
                setLevelProgress(it.toMyLevelData(prevLevel), true)
            }
        }
//        viewModel.myLevelLiveData.observe(this, myLevelObserver)
        viewModel.errorCodeLiveData.observe(this) {
            if (it == 6301) {
                baseToast(viewModel.errorMessageLiveData.value ?: "")
                gotoDefaultRecharge("send_gift")
            }
        }
//        if (liveGiftMode == MODE_SEND_GIFT) {
            sharedViewModel.getAccountBalance()
            sharedViewModel.accountBalanceData.observe(this) {
                val balance = it.coinBalance.balance.toInt()
                currentBalance = balance.toLong()
                if (balance <= 0) {
                    txtRechargeAmount.textResource = R.string.fb_recharge
                } else {
                    txtRechargeAmount.text = balance.toString()
                }
            }
//        }
//        viewModel.rechargeBonusData.observe(this, rechargeBonusObserver)
//        viewModel.turnOnPayLiveData.observe(this, turnOnPayLiveObserver)
//        LiveEventBus.get(EVENT_BUS_KEY_LIVE).observe(this) {
//            if (it is LiveStateEvent) {
//                if (it.liveState == Constants.LIVE_END) {
//                    dismiss()
//                }
//            }
//        }
//        Timber.d("LiveGiftDialog: initData, popupHeight: $popupHeight, height: $height, measuredHeight: $measuredHeight")

//        if (!isGroupOwner && liveGiftMode == MODE_SEND_GIFT) {
//            if (RecentConversation.myLevelData == null) {
//                RecentConversation.conversationsEntity.ownerId?.let { it1 ->
//                    viewModel.getMyLevel(it1)
//                }
//            } else {
//                RecentConversation.myLevelData?.let { it1 ->
//                    levelTextView?.text = it1.level.toString()
//                    setLevelProgress(it1, false)
//                }
//            }
//        }
    }

    private fun gotoDefaultRecharge(from: String) {
        
        dismiss()
    }

    override fun dismiss() {
        onDismiss.invoke()
        super.dismiss()
    }

    override fun initViewModel() = GiftsViewModel()

    override fun getImplLayoutId(): Int = R.layout.dialog_gifts

    override fun onDismiss() {
//        stopBonusTimeJob()
        super.onDismiss()
    }

    private val checkedGiftBack = { gift: GiftData, paperPosition: Int, position: Int ->
        checkedGift = gift
        this.paperPosition = paperPosition
        this.position = position
    }

    private class GiftListPagerAdapter(
        val viewList: List<GiftListPageView>,
        val onSend: (GiftData) -> Unit,
    ) : PagerAdapter() {
        override fun getCount(): Int = viewList.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
//            super.destroyItem(container, position, `object`)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = viewList[position]
            view.onSendGift = {
                onSend(it)
            }
            container.addView(view)
            return view
        }

//        fun rebuildItemUi(
//            paperPosition: Int,
//            position: Int,
//            giftBackpackData: GiftBackpackListData?,
//        ) {
//            val view = viewList[paperPosition]
//            val liveGiftDetail = view.gifts[position]
//            val backpackGiftData = giftBackpackData?.backpackList?.find {
//                it.giftId == liveGiftDetail.id
//            }
//            view.initRewardsCountView(position, giftBackpackData, backpackGiftData)
//        }
    }

    private fun updateLevel() {
//        if (isGroup) {
//            VenusLevelData.levelData?.let {
//
//                progressBarContainer?.isVisible = level > -1 && !isGroupOwner
//                levelIconContainer?.isVisible = level > -1 && !isGroupOwner
//                progressBarLevel?.isVisible = level > 0 && !isGroupOwner
//                gotoLevel?.isVisible = level > -1 && !isGroupOwner
//
//                var preLevelIcon: LevelIconData? = null
//                it.levelIcons.forEach { levelIcon ->
//                    if (level == levelIcon.level) {
//                        updateLevelIcon(levelIcon.icon)
//                        return@let
//                    } else if (level > levelIcon.level) {
//                        preLevelIcon = levelIcon
//                    } else if (level < levelIcon.level) {
//                        preLevelIcon?.icon?.let { it1 -> updateLevelIcon(it1) }
//                        return@let
//                    }
//                }
//            }
//        }
    }

    private fun updateLevelIcon(icon: String) {
        if (currentLevelIcon != icon) {
            currentLevelIcon = icon
            context?.let {
                Glide.with(it)
                    .load(icon)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(levelIconView)
            }
        }
    }

    private fun setLevelProgress(myLevelData: MyLevelData, animation: Boolean) {
        if (myLevelData.level > 0) {
            if (!progressBarLevel.isVisible) progressBarLevel.isVisible = true
            goLevelProgress(myLevelData.prevLevel?:0 < myLevelData.level, animation, myLevelData)
        }
        level = myLevelData.level
        updateLevel()
//        RecentConversation.myLevelData = myLevelData
        prevLevel = myLevelData.level
    }

    private fun updateLevelProgress(
        targetProgress: Int,
        isUpLevel: Boolean,
        desc: String = "",
        mLevel: Int
    ) {
        var isUpdating = true
        var isUpdated = false
        lifecycleScope.launch(Dispatchers.IO) {
            while (this.isActive && isUpdating) {
                withContext(Dispatchers.Main) {
                    val progress = progressBarLevel.progress
                    if (isUpLevel) {
                        if (progress < MAX_PROGRESS && !isUpdated) {
                            progressBarLevel?.progress = progress + 1
                        } else if (progress == MAX_PROGRESS && !isUpdated) {
                            isUpdated = true
                            progressBarLevel?.progress = 0
                            levelDesc?.text = desc
                            levelDesc?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
                            levelTextView?.text = mLevel.toString()
                        } else {
                            if (progress < targetProgress) {
                                progressBarLevel?.progress = progress + 1
                            } else {
                                isUpdating = false
                            }
                        }
                    } else {
                        if (progress < targetProgress) {
                            progressBarLevel?.progress = progress + 1
                        } else {
                            isUpdating = false
                        }
                    }
                }
                delay(1)
            }
        }
    }

    private fun goLevelProgress(isUpLevel: Boolean, animation: Boolean, myLevelData: MyLevelData) {
        if (myLevelData.nextLevelAmount == 0L) {
            //已经满级
            levelDesc?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13F)
            levelDesc?.textResource = R.string.fb_full_level_tip
            progressBarLevel?.isVisible = false
        } else if (myLevelData.nextLevelAmount != -1L) {
            if (animation) {
                updateLevelProgress(
                    (((myLevelData.validAmount - myLevelData.levelAmount).toFloat() / (myLevelData.nextLevelAmount - myLevelData.levelAmount).toFloat()) * 500 + 5).toInt(),
                    isUpLevel,
                    context?.getString(
                        R.string.fb_send_coins_to_level_up,
                        VenusNumberFormatter.format(myLevelData.nextLevelAmount - myLevelData.validAmount)
                    ) ?: "",
                    myLevelData.level
                )
                if (!isUpLevel) {
                    levelDesc?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
                    levelDesc?.text = context?.getString(
                        R.string.fb_send_coins_to_level_up,
                        VenusNumberFormatter.format(myLevelData.nextLevelAmount - myLevelData.validAmount)
                    ) ?: ""
                    levelTextView?.text = myLevelData.level.toString()
                }
            } else {
                progressBarLevel?.progress =
                    (((myLevelData.validAmount - myLevelData.levelAmount).toFloat() / (myLevelData.nextLevelAmount - myLevelData.levelAmount).toFloat()) * 500 + 5).toInt()
                levelDesc?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
                levelDesc?.text = context?.getString(
                    R.string.fb_send_coins_to_level_up,
                    VenusNumberFormatter.format(myLevelData.nextLevelAmount - myLevelData.validAmount)
                ) ?: ""
            }
        }
    }

    class GiftListPageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        val isFirstPage: Boolean,
        var paperPosition: Int,
        var gifts: List<GiftData>,
        var onReset: (() -> Unit),
        val giftId: String?
    ) : FrameLayout(context, attrs, defStyleAttr) {
        var onSendGift: ((GiftData) -> Unit)? = null
        var onChecked: ((GiftData, paperPosition: Int, position: Int) -> Unit)? = null
        private var checkIndex = -1

        init {
            Timber.d("gift time 1 ${System.currentTimeMillis()}")
            layoutInflater.inflate(R.layout.fragment_live_gift_list_page, this)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.dip(256))
            firstContainer.isVisible = gifts.isNotEmpty()
            secondContainer.isVisible = gifts.size > 1
            thirdContainer.isVisible = gifts.size > 2
            fourthContainer.isVisible = gifts.size > 3
            fifthContainer.isVisible = gifts.size > 4
            sixthContainer.isVisible = gifts.size > 5
            Timber.d("gift time 2 ${System.currentTimeMillis()}")
            if (giftId != null) {
                val defaultGift = gifts.filter {
                    it.id == giftId
                }
                if (defaultGift.isNeitherNullNorEmpty()) {
                    val index = gifts.indexOf(defaultGift[0])
                    checkIndex = index
                    onChecked?.invoke(gifts[index], paperPosition, index)
                    when (index) {
                        0 -> {
                            firstContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
                        }
                        1 -> {
                            secondContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
                        }
                        2 -> {
                            thirdContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
                        }
                        3 -> {
                            fourthContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
                        }
                        4 -> {
                            fifthContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
                        }
                        5 -> {
                            sixthContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
                        }
                    }
                }
            } else {
                if (isFirstPage) {
                    checkIndex = 0
                    onChecked?.invoke(gifts[0], paperPosition, 0)
                    firstContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
                }
            }

            Timber.d("gift time 3 ${System.currentTimeMillis()}")
            firstContainer.onClick {
//                if (checkIndex == 0) {
////                    if (isSendGift) onSendGift?.invoke(gifts[0])
//                } else {
                checkIndex = 0
                onReset.invoke()
                onChecked?.invoke(gifts[0], paperPosition, 0)
                firstContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
//                }
            }
            secondContainer.onClick {
//                if (checkIndex == 1) {
////                    if (isSendGift) onSendGift?.invoke(gifts[1])
//                } else {
                checkIndex = 1
                onReset.invoke()
                onChecked?.invoke(gifts[1], paperPosition, 1)
                secondContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
//                }
            }
            thirdContainer.onClick {
//                if (checkIndex == 2) {
////                    if (isSendGift) onSendGift?.invoke(gifts[2])
//                } else {
                checkIndex = 2
                onReset.invoke()
                onChecked?.invoke(gifts[2], paperPosition, 2)
                thirdContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
//                }
            }
            fourthContainer.onClick {
//                if (checkIndex == 3) {
////                    if (isSendGift) onSendGift?.invoke(gifts[3])
//                } else {
                checkIndex = 3
                onReset.invoke()
                onChecked?.invoke(gifts[3], paperPosition, 3)
                fourthContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
//                }
            }
            fifthContainer.onClick {
//                if (checkIndex == 4) {
////                    if (isSendGift) onSendGift?.invoke(gifts[4])
//
//                } else {
                checkIndex = 4
                onReset.invoke()
                onChecked?.invoke(gifts[4], paperPosition, 4)
                fifthContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
//                }
            }
            sixthContainer.onClick {
//                if (checkIndex == 5) {
////                    if (isSendGift) onSendGift?.invoke(gifts[5])
//                    sixthContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
//                } else {
                checkIndex = 5
                onReset.invoke()
                onChecked?.invoke(gifts[5], paperPosition, 5)
                sixthContainer.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
//                }
            }
            Timber.d("gift time 4 ${System.currentTimeMillis()}")
            gifts.forEachWithIndex { i, liveGiftDetail ->
//                val gift = giftBackpackData?.backpackList?.find {
//                    it.giftId == liveGiftDetail.id
//                }
                val gift = null
                when (i) {
                    0 -> {
                        initItemUI(
                            firstGiftName,
                            firstGiftImage,
                            firstRewardsCount,
                            liveGiftDetail,
//                            gift
                        )
                    }
                    1 -> {
                        initItemUI(
                            secondGiftName,
                            secondGiftImage,
                            secondRewardsCount,
                            liveGiftDetail,
//                            gift
                        )
                    }
                    2 -> {
                        initItemUI(
                            thirdGiftName,
                            thirdGiftImage,
                            thirdRewardsCount,
                            liveGiftDetail,
//                            gift
                        )
                    }
                    3 -> {
                        initItemUI(
                            fourthGiftName,
                            fourthGiftImage,
                            fourthRewardsCount,
                            liveGiftDetail,
//                            gift
                        )
                    }
                    4 -> {
                        initItemUI(
                            fifthGiftName,
                            fifthGiftImage,
                            fifthRewardsCount,
                            liveGiftDetail,
//                            gift
                        )
                    }
                    5 -> {
                        initItemUI(
                            sixthGiftName,
                            sixthGiftImage,
                            sixthRewardsCount,
                            liveGiftDetail,
//                            gift
                        )
                    }
                }
            }
        }

        private fun initItemUI(
            giftName: TextView,
            image: ImageView,
            rewardsCount: TextView,
            liveGiftDetail: GiftData,
//            gift: BackpackGiftData?
        ) {
            giftName.text = context.getString(
                R.string.lbl_live_gift_coin_cost,
                liveGiftDetail.coins.toString()
            )
            Glide.with(giftName.context).load(liveGiftDetail.image)
                .skipMemoryCache(true).diskCacheStrategy(
                    DiskCacheStrategy.ALL
                ).placeholder(R.mipmap.ic_live_gift_default).fitCenter()
                .into(image)
//            if (gift != null && gift.num > 0) {
//                rewardsCount.isVisible = true
//                rewardsCount.text =
//                    giftName.context.getString(R.string.fb_x_count, gift.num.toString())
//            } else {
//                rewardsCount.isVisible = false
//            }
        }

//        fun initRewardsCountView(
//            position: Int,
//            giftBackpackData: GiftBackpackListData?,
//            backpackGiftData: BackpackGiftData?
//        ) {
//            this.giftBackpackData = giftBackpackData
//            when (position) {
//                0 -> {
//                    firstRewardsCount.isVisible =
//                        backpackGiftData != null && backpackGiftData.num > 0
//                    firstRewardsCount.text = MarsApp.appInstance.getString(
//                        R.string.fb_x_count,
//                        backpackGiftData?.num.toString()
//                    )
//                }
//                1 -> {
//                    secondRewardsCount.isVisible =
//                        backpackGiftData != null && backpackGiftData.num > 0
//                    secondRewardsCount.text = MarsApp.appInstance.getString(
//                        R.string.fb_x_count,
//                        backpackGiftData?.num.toString()
//                    )
//                }
//                2 -> {
//                    thirdRewardsCount.isVisible =
//                        backpackGiftData != null && backpackGiftData.num > 0
//                    thirdRewardsCount.text = MarsApp.appInstance.getString(
//                        R.string.fb_x_count,
//                        backpackGiftData?.num.toString()
//                    )
//                }
//                3 -> {
//                    fourthRewardsCount.isVisible =
//                        backpackGiftData != null && backpackGiftData.num > 0
//                    fourthRewardsCount.text = MarsApp.appInstance.getString(
//                        R.string.fb_x_count,
//                        backpackGiftData?.num.toString()
//                    )
//                }
//                4 -> {
//                    fifthRewardsCount.isVisible =
//                        backpackGiftData != null && backpackGiftData.num > 0
//                    fifthRewardsCount.text = MarsApp.appInstance.getString(
//                        R.string.fb_x_count,
//                        backpackGiftData?.num.toString()
//                    )
//                }
//                5 -> {
//                    sixthRewardsCount.isVisible =
//                        backpackGiftData != null && backpackGiftData.num > 0
//                    sixthRewardsCount.text = MarsApp.appInstance.getString(
//                        R.string.fb_x_count,
//                        backpackGiftData?.num.toString()
//                    )
//                }
//            }
//        }

        fun reset() {
            firstContainer.background = null
            secondContainer.background = null
            thirdContainer.background = null
            fourthContainer.background = null
            fifthContainer.background = null
            sixthContainer.background = null
        }
    }

    companion object {
        const val MODE_SEND_GIFT = 1
        const val MODE_CHECK_TICKET = 2
        const val MODE_CHECK_TICKET_AND_OPEN_LIVE = 3
    }

}