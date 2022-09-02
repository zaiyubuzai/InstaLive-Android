package com.example.instalive.app.live

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.utils.Utils.processTemplate
import com.example.baselibrary.utils.liveGiftAnimatorSet
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.InstaLiveApp
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.Constants.DEFAULT_EMOJI_LIST
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.app.SESSION
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.base.SharedViewModel
import com.example.instalive.app.base.TextPopupWindow
import com.example.instalive.app.conversation.TopSmoothScroller
import com.example.instalive.app.live.ui.LiveCommentInputDialog
import com.example.instalive.mentions.Mentionable
import com.example.instalive.model.*
import com.example.instalive.utils.NoUnderlineClickableSpanBuilder
import com.example.instalive.utils.VenusNumberFormatter
import com.example.instalive.utils.marsToast
import com.example.instalive.view.KsgLikeView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.jeremyliao.liveeventbus.core.LiveEvent
import com.lxj.xpopup.XPopup
import com.opensource.svgaplayer.*
import com.venus.dm.app.ChatConstants
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.model.UserData
import com.venus.dm.model.event.MessageEvent
import kotlinx.android.synthetic.main.fragment_live_interaction_host.*
import kotlinx.android.synthetic.main.fragment_live_interaction_host.avatar
import kotlinx.android.synthetic.main.layout_live_gift_pop.view.*
import kotlinx.coroutines.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import splitties.alertdialog.appcompat.*
import splitties.dimensions.dp
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault
import splitties.systemservices.layoutInflater
import splitties.views.imageDrawable
import splitties.views.onClick
import timber.log.Timber
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalStdlibApi
abstract class LiveInteractionBaseFragment<VDB : ViewDataBinding> :
    BaseFragment<LiveViewModel, VDB>() {

    protected val sharedViewModel by lazy {
        InstaLiveApp.appInstance.getAppViewModelProvider().get(SharedViewModel::class.java)
    }

    var diamondPublicEnabled: Boolean by argOrDefault(false)
    var makeUpEnabled: Boolean by argOrDefault(false)
    var isHost: Boolean by arg()
    var liveId: String by arg()

    lateinit var liveMessageAdapter: LiveMessageAdapter
    lateinit var layoutManager: LinearLayoutManager
    lateinit var svgaParser: SVGAParser

    var giftSecondContainer: FrameLayout? = null
    var giftFirstContainer: FrameLayout? = null
    var ownerLiveUserInfo: LiveUserInfo? = null
    var liveLikesAnimView: KsgLikeView? = null
    val emojiList = mutableListOf<Bitmap>()
    var giftAnim: SVGAImageView? = null

    var isProfileLoading = false
    var isPaidLive = false

    private var topMessage: LiveEvent? = null

    var activityList = mutableListOf<LiveEvent>()
    val giftList = mutableListOf<LiveGiftEvent>()

    private var insertedMentions: MutableList<Mentionable>? = null
    private val giftUuids = mutableListOf<String>()
    private var secondGiftLot: LiveGiftEvent? = null
    private var firstGiftLot: LiveGiftEvent? = null
    private var pendingComment: String? = null

    private var commentSwipedAway = false
    private var newEventCount = 0
    private var preItemCount = -1
    private var newMessagePushTimeToken = 0L
    private var keyboardState = false
    private var subtitleFloatingView: View? = null
    private var giftListData: GiftListData? = null

    private val messageEventSyncList = ConcurrentLinkedQueue<MessageEvent>()

    private var lastActivityEventEmitJob: Job? = null
    private var secondGiftCardViewJob: Job? = null
    private var firstGiftCardViewJob: Job? = null
    private var messageEventJob: Job? = null

    private var bigAnimIsPlaying = AtomicBoolean(false)

    private var liveCommentInputDialog: LiveCommentInputDialog? = null
    private val atLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result?.resultCode == AppCompatActivity.RESULT_OK) {
                val username = result.data?.getStringExtra(Constants.EXTRA_USERNAME)
                if (username != null) {
                    liveCommentInputDialog?.onAtComplete(username)
                }
            }
        }

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalStdlibApi::class)
    override fun initData(savedInstanceState: Bundle?) {
        screenName = "live_interaction_base_view"
        container.setPadding(0, BarUtils.statusBarHeight, 0, 0)
//        logFirebaseEvent(screenName ?: "")
        DEFAULT_EMOJI_LIST.forEach {
            val bitmap =
                BitmapFactory.decodeResource(context?.resources, it, BitmapFactory.Options())
            emojiList.add(bitmap)
        }
        val options = RequestOptions.bitmapTransform(RoundedCorners(activity.dp(12)))
        svgaParser = SVGAParser(activity)

        initList()

        init()

        LiveEventBus.get(Constants.EVENT_BUS_KEY_USER).observe(this) {
            if (it is UserData) {
                showBottomProfileDialog(it)
            }
        }

//        LiveEventBus.get(Constants.EVENT_BUS_SCREENSHOT).observe(this, {
//            if (conversationsEntity.type == 2 && (otherProfileDialog == null || otherProfileDialog?.isShow == false)) {
//                if (it == 1) {
//                    viewModel.screenshot(SessionPreferences.recentConversationID, 1, 2)
//                } else if (it == 2) {
//                    viewModel.screenshot(SessionPreferences.recentConversationID, 2, 2)
//                }
//            }
//        })

        LiveEventBus.get(Constants.EVENT_BUS_PROFILE_USER_ID).observe(this) {
            if (it is String) {
                val ids = it.split("_")
                if (ids.size >= 2) {
                    if (ids[0] != SessionPreferences.id) {
                        showOtherProfileDialog(ids[0], "", 9)
                    } else {
                        SESSION.retrieveMeInfo()?.let {
                            showMeProfileDialog(it)
                        }
                    }
                }
            }
        }

        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).observe(this) {
            if (it is MessageEvent) {
                Timber.d("live MessageEvent type: ${it.type}")
                messageEventSyncList.add(it)
            }
        }

        LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE).observe(this, {
            when (it) {
                is LiveActivityEvent -> {
                    if (it.event == 6) {
                        //gift类型的activity event扔掉
                        return@observe
                    }
                    updateTotalViewerCount(it)
                    if (it.content.isNotEmpty()) {
                        handleActivityEvent(it)
                    }
                }
                is LikeEvent -> {
                    showCornerLikes(it)
                }
                is LiveGiftEvent -> {
                    if (it.userInfo.userId != SessionPreferences.id || it.isOwnerGift) {
                        onNewGift(it, false)
                    } else {
                        onNewGift(it, true)
                    }
                }
            }
        })

        sharedViewModel.liveOnlineCount.observe(this, {
            onlineCount?.text = it
        })

        sharedViewModel.liveUsersSizeData.observe(this, {
            onLiveUsersChanged(it)
        })

        sharedViewModel.liveStateInfoLiveData.observe(this, {
            it.first?.let { info ->
                Glide.with(activity)
                    .load(info.owner.portrait)
                    .apply(options)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(avatar)
                ownerLiveUserInfo = info.liveWithUserInfos.find {
                    it.userId == info.owner.userId
                }
//            val count = VenusNumberFormatter.format(info.onlineNumber)
                onlineCount.text = info.onlineNumStr
                val n = info.owner.nickname
                if (n.length > 12) {
                    name.text = n.substring(0, 10).plus("...")
                } else {
                    name.text = n
                }

                if (info.state == Constants.LIVE_START && info.needLiveWith == 1) {
                    showLiveWith()
                }

                isPaidLive = info.isPaidLive
                giftFirstContainer?.isVisible = info.liveGiftShowEnable
                giftSecondContainer?.isVisible = info.liveGiftShowEnable
                onLiveStateInfoInJoined(info)

                diamondPublicEnabled = info.liveDiamondsPublic
            }
        })

        viewModel.getGiftList()
        viewModel.giftListLiveData.observe(this, {
            //prefetch live gifts images
            giftListData = it
        })

        newMessagesCount.onClick {
            interactionsList.smoothScrollToPosition(0)
            hideNewMessageView()
        }
        avatar.onClick {
            ownerLiveUserInfo?.let { it1 -> showPersonBottomDialog(it1, 1) }
        }

        viewModel.errorInfo.observe(this, {
            marsToast(it.second)
        })

        giftAnim?.callback = object : SVGACallback {
            override fun onFinished() {
                bigAnimIsPlaying.set(false)
                giftAnim?.clear()
                popGift()
            }

            override fun onPause() {
            }

            override fun onRepeat() {
            }

            override fun onStep(frame: Int, percentage: Double) {
            }
        }

    }

    private val messageListLoadObserver: (List<MessageEntity>, isRefresh: Boolean) -> Unit =
        { messages: List<MessageEntity>, isRefresh: Boolean ->
            val oldCount = liveMessageAdapter.messages.size
            val messages2 = messages.toList().filter {
                !liveMessageAdapter.messageUUIDList.toList().contains(it.uuid)
            }
            liveMessageAdapter.messages.addAll(messages2)
            liveMessageAdapter.notifyItemRangeInserted(oldCount, messages2.size)
            scrollToNewMessage(false)
        }

    private fun startMessageEventJob() {
        messageEventJob?.cancel()
        messageEventJob = CoroutineScope(Dispatchers.IO).launch {
            while (this.isActive) {
                try {
                    val messageEvent = messageEventSyncList.poll()
                    if (messageEvent != null) {
                        buildMessageChange(messageEvent)
                    } else {
                        insertMessages()
                        delay(100)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun buildMessageChange(messageEvent: MessageEvent) {
//        when (messageEvent.type) {
//            1 -> {//单个新增
//                val message = messageEvent.messageEntity ?: return
//                val isContains = liveMessageAdapter.messageUUIDList.contains(message.uuid)
//                if (message.conId == conversationId && message.liveId == liveId && !isContains) {
//                    liveMessageAdapter.messageUUIDList.add(message.uuid)
//                    withContext(Dispatchers.Main) {
//                        liveMessageAdapter.messages.add(0, message)
//                        liveMessageAdapter.notifyItemInserted(0)
//                        scrollToNewMessage(false)
//                    }
//                }
//            }
//            2 -> {//多个新增
//                val messageList = viewModel.getMessageListBySocket(
//                    liveId,
//                    conversationId,
//                    getAdapterLastMessageTimeToken(),
//                    messageEvent.timestampStart,
//                    messageEvent.timestampEnd
//                )
//
//                val oldMessages = mutableListOf<MessageEntityWithUser>()
//                val timeToken = getAdapterLastMessageTimeToken()
//                messageList.forEachIndexed { index, messageEntityWithUser ->
//                    val isContains = liveMessageAdapter.messageUUIDList.toList().contains(messageEntityWithUser.uuid)
//                    if (!isContains && messageEntityWithUser.type != 31) {
//                        liveMessageAdapter.messageUUIDList.add(messageEntityWithUser.uuid)
//                        if (abs((if (index == 0) timeToken else messageList.get(index - 1).timeToken) - messageEntityWithUser.timeToken) > 10 * 60 * 1000 * 10000L) {
//                            val uuid = UUID.randomUUID().toString()
//                            oldMessages.add(
//                                messageEntityWithUser.copy(
//                                    uuid = uuid,
//                                    type = 8,
//                                    timeToken = messageEntityWithUser.timeToken - 1,
//                                    content = TimeUtils.formatMessageTime(messageEntityWithUser.timeToken),
//                                    renderType = 1
//                                )
//                            )
//                            liveMessageAdapter.messageUUIDList.add(uuid)
//                        }
//                        oldMessages.add(messageEntityWithUser)
//                    }
//                }
//                Timber.d("messageList size = ${messageList.size} oldMessages size = ${oldMessages.size}")
//                if (oldMessages.isNotEmpty()) {
//                    liveMessageAdapter.originalMessages.addAll(oldMessages)
//                    insertMessages()
//                }
//            }
//            4 -> {//更新消息数据内容
//                val message = messageEvent.messageEntityWithUser ?: return
//                if (message.conId == conversationId && message.liveId == liveId) {
//                    liveMessageAdapter.messages.toList()
//                        .filterIndexed { index, messageEntityWithUser ->
//                            if (messageEntityWithUser.uuid == message.uuid) {
//                                message.name = messageEntityWithUser.name
//                                message.portrait = messageEntityWithUser.portrait
//                                message.portraitIc = messageEntityWithUser.portraitIc
//                                withContext(Dispatchers.Main) {
//                                    liveMessageAdapter.messages.remove(messageEntityWithUser)
//                                    liveMessageAdapter.messages.add(index, message)
//                                    liveMessageAdapter.notifyItemChanged(index)
//                                }
//                                true
//                            } else false
//                        }
//                }
//            }
////            5 -> {
////                val newMessage = messageEvent.messageEntityWithUser ?: return
////                val targetMessage = messageEvent.targetMessage ?: return
////                if (newMessage.conId == conversationId && newMessage.liveId == liveId) {
////                    liveMessageAdapter.messages.toList()
////                        .filterIndexed { index, messageEntityWithUser ->
////                            if (messageEntityWithUser.uuid == targetMessage.uuid) {
////                                newMessage.name = messageEntityWithUser.name
////                                newMessage.portrait = messageEntityWithUser.portrait
////                                newMessage.portraitIc = messageEntityWithUser.portraitIc
////                                liveMessageAdapter.messages.remove(messageEntityWithUser)
////                                liveMessageAdapter.messages.add(index, newMessage)
////                                liveMessageAdapter.notifyItemChanged(index)
////                                true
////                            } else false
////                        }
////                }
////            }
//        }
    }

    private fun getAdapterLastMessageTimeToken(): Long{
        if (liveMessageAdapter.originalMessages.isNotEmpty()){
            return liveMessageAdapter.originalMessages.last().sendTime
        } else if (liveMessageAdapter.messages.isNotEmpty()){
            return liveMessageAdapter.messages.first().sendTime
        } else {
            return 0L
        }
    }

    @Throws(Exception::class)
    private suspend fun insertMessages() {
        if (liveMessageAdapter.originalMessages.size < 1) return
        val currentTimestamp = System.currentTimeMillis()
        if (viewModel.isMicrophone.value == true || isHost) {
            if (currentTimestamp - newMessagePushTimeToken > 1000) {
                newMessagePushTimeToken = currentTimestamp
                when (liveMessageAdapter.originalMessages.size) {
                    in 6..10 -> {
                        withContext(Dispatchers.Main) {
                            liveMessageAdapter.messages.addAll(
                                0,
                                liveMessageAdapter.originalMessages.subList(0, 5).reversed()
                            )
                            liveMessageAdapter.notifyItemRangeInserted(0, 5)
                            scrollToNewMessage(true)
                        }
                        liveMessageAdapter.originalMessages =
                            liveMessageAdapter.originalMessages.subList(
                                5,
                                liveMessageAdapter.originalMessages.size
                            )
                    }
                    in 11..50 -> {
                        withContext(Dispatchers.Main) {
                            liveMessageAdapter.messages.addAll(
                                0,
                                liveMessageAdapter.originalMessages.subList(0, 10).reversed()
                            )
                            liveMessageAdapter.notifyItemRangeInserted(0, 10)
                            scrollToNewMessage(true)
                        }
                        liveMessageAdapter.originalMessages =
                            liveMessageAdapter.originalMessages.subList(
                                10,
                                liveMessageAdapter.originalMessages.size
                            )
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            liveMessageAdapter.messages.addAll(
                                0,
                                liveMessageAdapter.originalMessages.reversed()
                            )
                            liveMessageAdapter.notifyItemRangeInserted(
                                0,
                                liveMessageAdapter.originalMessages.size
                            )
                            scrollToNewMessage(true)
                        }
                        liveMessageAdapter.originalMessages.clear()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                liveMessageAdapter.messages.addAll(
                    0,
                    liveMessageAdapter.originalMessages.reversed()
                )
                liveMessageAdapter.notifyItemRangeInserted(
                    0,
                    liveMessageAdapter.originalMessages.size
                )
                scrollToNewMessage(true)
            }
            liveMessageAdapter.originalMessages.clear()
        }
    }

    private fun onLiveUsersChanged(size: Int){
        val c = context?:return
        interactionsList?.layoutParams?.height =
            when(size){
                0, 1 -> {
                    if (isHost)c.dp(341) else c.dp(300)
                }
                2 -> {
                    val top = BarUtils.statusBarHeight + c.dp(68) + c.dp(280) + c.dp(10)
                    val bottom = interactionsList.bottom
                    bottom - top
                }
                3,4-> {
                    val top = BarUtils.statusBarHeight + c.dp(68) + c.dp(180) + c.dp(4) + c.dp(180) + c.dp(10)
                    val bottom = interactionsList.bottom
                    bottom - top
                }
                5, 6-> {
                    val top = BarUtils.statusBarHeight + c.dp(68) + c.dp(180) + c.dp(4) + c.dp(180) + c.dp(10)
                    val bottom = interactionsList.bottom
                    bottom - top
                }
                else -> {
                    val top = BarUtils.statusBarHeight + c.dp(68) + c.dp(180) + c.dp(4) + c.dp(180) + c.dp(4) + c.dp(180) + c.dp(10)
                    val bottom = interactionsList.bottom
                    Math.max(bottom - top, c.dp(100))
                }
            }
        liveMessageAdapter.notifyDataSetChanged()
    }

    private fun initList() {
        liveMessageAdapter = LiveMessageAdapter(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            true,
            object : LiveMessageAdapter.OnLiveMessageActionsListener {
                override fun onPortraitClicked(senderId: String, senderRole: Int) {
                    Timber.d("$senderId $senderRole")
                    if (senderId == SessionPreferences.id) {
                        val meData = SESSION.retrieveMeInfo()
                        meData?.let { showMeProfileDialog(it) }
                    } else {
                        showOtherProfileDialog(
                            senderId,
                            "",
                            senderRole
                        )
                    }
                }

                override fun onResendClicked(messageEntity: MessageEntity) {
                    resendMessage(messageEntity)
                }

                override fun onPlayVideo(messageEntity: MessageEntity) {
                    buildMediaData(messageEntity)
                }

                override fun onViewImage(messageEntity: MessageEntity) {
                    buildMediaData(messageEntity)
                }

                override fun onReplyViewImage(messageEntity: MessageEntity) {
                    buildMediaData(messageEntity)
                }

                override fun onReplyViewVideo(messageEntity: MessageEntity) {
                    buildMediaData(messageEntity)
                }

                override fun onReplyMessage(messageEntity: MessageEntity) {
                    val payload = MessageEntity.Payload.fromJson(messageEntity.payload)
                    payload?.targetMessage?.let {
                        if (it.type == 3) {
                            getString(R.string.il_name_image, it.senderName)
                        } else if (it.type == 4) {
                            getString(R.string.il_name_video, it.senderName)
                        } else {
                            "${it.senderName}: ${it.payload.content}"
                        }
                    }?.let {
                        val tokenPopupWindow =
                            TextPopupWindow(requireActivity(), it)
                        with(tokenPopupWindow) {
                            isClippingEnabled = false
                            animationStyle = R.style.anim_style
                            show()
                        }
                    }
                }

                override fun onUsernameClick(username: String) {
                    if (username == getString(R.string.fb_everyone_) || username == getString(R.string.fb_moderators_)) return
                    if (username == SessionPreferences.userName) {
                        SESSION.retrieveMeInfo()?.let {
                            showMeProfileDialog(it)
                        }
                    } else {
                        showOtherProfileDialog("", username, 9)
                    }
                }

                override fun onURLMessageClick(url: String) {
                    if (isHost) {
                        marsToast(R.string.fb_host_cant_leave)
                    } else if (viewModel.isMicrophoneUser.value == true) {
                        marsToast(R.string.fb_microphone_user_cant_leave)
                    } else {
                        val c = context ?: return
                        val uri = Uri.parse(url)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, c.packageName)
                        try {
                            c.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                        }
                    }
                }

                override fun onClickGift(giftId: String) {
                    if (!isHost) {
                        openGift(giftId)
                    }
                }

                override fun onViewGif(url: String?) {
//                        url?.let {
//                            val messageSimpleGifViewer = MessageSimpleGifViewer(it)
//                            messageSimpleGifViewer.show(
//                                activity.supportFragmentManager,
//                                null
//                            )
//                        }
                }

                override fun onRecharge() {}

            }
        )
        layoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = true
        interactionsList.layoutManager = layoutManager
//        val animator = interactionsList.itemAnimator
//        if (animator is SimpleItemAnimator) {
//            animator.supportsChangeAnimations = false
//        }
        interactionsList.itemAnimator = null

        interactionsList.adapter = liveMessageAdapter
        viewModel.getMessageList(true, liveId, 0L, messageListLoadObserver)
        interactionsList.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
//            Timber.d("OnLayoutChange $bottom $oldBottom $top $oldTop")
            val firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()
            Timber.d("OnLayoutChange $firstPosition $lastPosition")
            if ((bottom < oldBottom || top > oldTop) || keyboardState) {
                scrollToBottom()
            }
        }
        interactionsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                Timber.d("OnScrollListener $newState")
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                commentSwipedAway = firstPosition > 1
                if (firstPosition == 0) {
                    //位置归零
                    hideNewMessageView()
                }
            }
        })
        liveMessageAdapter.registerAdapterDataObserver(messageAdapterDataObserver)
        KeyboardVisibilityEvent.setEventListener(activity, this,
            { isOpen ->
                Timber.d("$isOpen")
                keyboardState = isOpen
            })
    }

    private fun resendMessage(messageEntity: MessageEntity) {
        activity.alertDialog {
            messageResource = R.string.fb_resend_this_message
            positiveButton(R.string.fb_resend) {
                when (messageEntity.type) {
                    1 -> {
                        scrollToBottom()
                        viewModel.resendTextMessage(
                            messageEntity,
                        )
                    }
                    3 -> {
                        //重发图片
                        val path = messageEntity.localResPath
                        val payload =
                            MessageEntity.Payload.fromJson(messageEntity.payload)
                        if (path != null) {
                            val file = File(path)
                            if (file.exists()) {
                                scrollToBottom()
                                sharedViewModel.sendImageMessage(
                                    payload?.liveId ?: "-1",
                                    messageEntity.showType,
                                    path,
                                    messageEntity.conId,
                                    payload?.width ?: 0,
                                    payload?.height ?: 0,
                                   -1
                                )
                                viewModel.deleteMessage(messageEntity)
                            }
                        }
                    }
                    4 -> {
                        //重发视频
                        val path = messageEntity.localResPath
                        if (path != null) {
                            val file = File(path)
                            if (file.exists()) {
                                val payload =
                                    MessageEntity.Payload.fromJson(messageEntity.payload)
                                scrollToBottom()
                                sharedViewModel.sendVideoMessage(
                                    path,
                                    messageEntity.conId,
                                    payload?.width ?: 100,
                                    payload?.height ?: 100,
                                    (payload?.length ?: 100).toLong(),
                                    (payload?.size ?: 100).toLong(),
                                    messageEntity.showType,
                                    payload?.liveId ?: "-1",
                                   -1
                                )
                                viewModel.deleteMessage(messageEntity)
                            }
                        }
                    }
                    32 -> {
                        scrollToBottom()
                        viewModel.resendTextMessage(
                            messageEntity,
                        )
                    }
                    34 -> {
                        val payload =
                            MessageEntity.Payload.fromJson(messageEntity.payload)
//                        sharedViewModel.sendGifMessage(
//                            RecentConversation.conversationsEntity.type,
//                            "-1",
//                            messageEntity.showType,
//                            payload?.url ?: "",
//                            messageEntity.conId,
//                            payload?.width ?: 100,
//                            payload?.height ?: 100,
//                            RecentConversation.conversationsEntity.level
//                        )
                        viewModel.deleteMessage(messageEntity)
                    }
                }
            }

            cancelButton()
        }.show()
    }

    private fun scrollToBottom() {
        if (liveMessageAdapter.itemCount > 0) interactionsList?.scrollToPosition(0)
        scrollToBottom2()
    }

    private fun scrollToBottom2() {
        val v1 = interactionsList?.computeVerticalScrollExtent() ?: 0
        val v2 = interactionsList?.computeVerticalScrollOffset() ?: 0
        val v3 = interactionsList?.computeVerticalScrollRange() ?: 0
        Timber.d("$v1 ${(v1 + v2)} $v3 ${interactionsList?.height}")
        if (v1 + v2 < v3) {
            interactionsList?.smoothScrollBy(0, v3 - v1 - v2)
        } else {
            interactionsList?.smoothScrollBy(0, 0)
        }
    }

    private val messageAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)

            if (!commentSwipedAway) {
                preItemCount = liveMessageAdapter.itemCount
            } else {
                newEventCount = liveMessageAdapter.itemCount - preItemCount
            }

            if (commentSwipedAway) {
                showNewMessageView()
            }

            Timber.d("messageAdapter.itemCount: ${liveMessageAdapter.itemCount} newEventCount: $newEventCount")
        }
    }

    private fun scrollToNewMessage(isSmoothScroll: Boolean){
        if (!commentSwipedAway){
            if (isSmoothScroll){
                context?.let {
                    val topSmoothScroller = TopSmoothScroller(it)
                    topSmoothScroller.targetPosition = 0
                    layoutManager.startSmoothScroll(topSmoothScroller)
                }
            } else {
                scrollToBottom()
            }
        }
    }

    open fun onNewGift(event: LiveGiftEvent, isSocketMe: Boolean) {
        if (!isSocketMe) {
            if (!isAdded || context == null) {
                return
            }

            if (giftUuids.contains(event.uuid)) {
                return
            }
            val weight = event.weight
            //根据权重排序，插入
            val firstSmallerWeightIndex = giftList.indexOfFirst {
                it.weight < weight
            }
            if (firstSmallerWeightIndex != -1) {
                //往第一个比这个权重小的event之前插入一个
                giftList.add(firstSmallerWeightIndex, event)
            } else {
                //直接放在队列末尾
                giftList.add(event)
            }

            giftUuids.add(event.uuid)
            popGift()
            if (event.giftInfo?.systemMessage?.show == true) {
                handleActivityEvent(event)
                if (event.userInfo.userId == SessionPreferences.id) {
                    //如果是自己发生的事件，直接展示出来
                    lastActivityEventEmitJob?.cancel()
                    lastActivityEventEmitJob = lifecycleScope.launch {
                        buildActivityMessage(event)
//                        topMessageAdapter.notifyItemChanged(0)
                        delay(2000)
                        lastActivityEventEmitJob = null
                    }
                } else {
                    //不是自己的activity，压进list
                    activityList.add(event)
                    popActivityEvent()
                }
            }
        }
    }

    private fun popGift() {
        if (giftList.isEmpty() || !isAdded || context == null) {
            return
        }
        val firstGift = giftList[0]
        if (firstGift.giftInfo?.specialEffect?.show == true && !bigAnimIsPlaying.get()) {
            //如果第一个礼物是特效礼物，就走展示特效礼物
            val gift = giftList.removeAt(0)
            val url = gift.giftInfo?.specialEffect?.img ?: ""

            bigAnimIsPlaying.set(true)
            val giftImgCachePath = InstaLivePreferences.findGiftCache(url)?:""
            try {
                val parseCompletion = object : SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        toShowGiftAnimation(videoItem, gift)
                    }

                    override fun onError() {
                    }
                }
                if (File(giftImgCachePath).exists()) {
                        val file = File(giftImgCachePath)
                        svgaParser.decodeFromInputStream(
                            file.inputStream(),
                            url,
                            parseCompletion
                        )
                } else {
                    svgaParser.decodeFromURL(URL(url), parseCompletion)
                    viewModel.cacheGift(url)
                }
            } catch (e: Exception) {
            }
        } else {
            if (firstGiftCardViewJob != null) {
                if (secondGiftCardViewJob == null && giftSecondContainer != null) {
                    val index = giftList.indexOfFirst {
                        it.giftInfo?.specialEffect?.show != true
                    }
                    if (index != -1) {
                        val gift = giftList.removeAt(index)
                        secondGiftCardViewJob = popGiftCard(gift, giftSecondContainer) {
                            secondGiftCardViewJob?.cancel()
                            secondGiftCardViewJob = null
                            if (giftList.isNotEmpty()) {
                                popGift()
                            }
                        }
                    }
                }
            } else if (!bigAnimIsPlaying.get()) {
                val index = giftList.indexOfFirst {
                    it.giftInfo?.specialEffect?.show != true
                }
                if (index != -1) {
                    val gift = giftList.removeAt(index)
                    if (giftFirstContainer != null) {
                        firstGiftCardViewJob = popGiftCard(gift, giftFirstContainer) {
                            firstGiftCardViewJob?.cancel()
                            firstGiftCardViewJob = null
                            if (giftList.isNotEmpty()) {
                                popGift()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun toShowGiftAnimation(videoItem: SVGAVideoEntity, gift: LiveGiftEvent) {
        giftAnim?.imageDrawable = SVGADrawable(videoItem)
        giftAnim?.startAnimation()
        if (firstGiftCardViewJob != null) {
            if (secondGiftCardViewJob == null && giftSecondContainer != null) {
                val length = videoItem.frames * 1000 / videoItem.FPS
                giftSecondContainer?.let {
                    secondGiftCardViewJob = popGiftCard(gift, it, length) {
                        secondGiftCardViewJob?.cancel()
                        secondGiftCardViewJob = null
                    }
                }
            } else {
                val length = videoItem.frames * 1000 / videoItem.FPS
                giftFirstContainer?.let {
                    firstGiftCardViewJob = popGiftCard(gift, it, length) {
                        firstGiftCardViewJob?.cancel()
                        firstGiftCardViewJob = null
                    }
                }
            }
        } else {
            val length = videoItem.frames * 1000 / videoItem.FPS
            giftFirstContainer?.let {
                firstGiftCardViewJob = popGiftCard(gift, it, length) {
                    firstGiftCardViewJob?.cancel()
                    firstGiftCardViewJob = null
                }
            }
        }
    }

    private fun popGiftCard(
        giftEvent: LiveGiftEvent,
        giftContainer: FrameLayout?,
        customLength: Int? = null,
        onFinish: () -> Unit,
    ): Job {
        return lifecycleScope.launch {
            val c = context ?: return@launch
            if (!isAdded) return@launch
            var duration = when {
                giftEvent.userInfo.userId == SessionPreferences.id -> {
                    giftEvent.giftInfo?.card?.durationMe ?: 2000
                }
                isHost -> {
                    giftEvent.giftInfo?.card?.durationHost ?: 2000
                }
                else -> {
                    giftEvent.giftInfo?.card?.durationViewer ?: 2000
                }
            }
            duration = if (customLength != null) {
                if (customLength > duration) customLength else duration
            } else {
                duration
            }
            val giftPop =
                c.layoutInflater.inflate(R.layout.layout_live_gift_pop, null, false)
                    ?: return@launch
            giftPop.giftAvatar.onClick {
                if (giftEvent.userInfo.userId != SessionPreferences.id) {
                    showPersonBottomDialog(giftEvent.userInfo)
                }
            }
            giftPop.onClick {
                if (!isHost) {
                    openGift()
                }
            }

            val options = RequestOptions.bitmapTransform(RoundedCorners(c.dp(12)))
            Glide.with(c)
                .load(giftEvent.giftInfo?.card?.img)
                .apply(options)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(giftPop.giftGift)
            Glide.with(c)
                .load(giftEvent.userInfo.portrait)
                .apply(options)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(giftPop.giftAvatar)
            giftPop.giftUsername.textSize = if (isHost) 15f else 13f
            giftPop.content.textSize = if (isHost) 12f else 11f
            giftPop.giftUsername.text = giftEvent.userInfo.nickname
            giftPop.content.text = giftEvent.giftInfo?.card?.content

            giftPop.giftContainer.setBackgroundResource(if (giftEvent.giftInfo?.card?.highlight == true) R.drawable.bg_live_interaction_gift_highlighted_container else R.drawable.bg_live_interaction_gift_container)
            giftContainer?.addView(giftPop)
            giftPop.liveGiftAnimatorSet(duration, {
            }, {
                giftFirstContainer?.removeView(giftPop)
                onFinish()
            })
        }
    }

    fun showMeProfileDialog(userData: UserData) {
//        if (meProfileDialog == null || meProfileDialog?.isShow == false) {
//            if (activity.isFinishing) return
//            val c = context ?: return
//            meProfileDialog =
//                MeProfileDialog(c, userData, showSettings = {
//                    if (canLeaveLive()) {
//                        activity.start<SettingsActivity> {
//                            putExtra(
//                                Constants.EXTRA_BALANCE_ENABLED,
//                                sharedViewModel.meData.value?.showBalance == true
//                            )
//                            putExtra(
//                                Constants.EXTRA_INCOME_ENABLED,
//                                sharedViewModel.meData.value?.settingsIncomeEnabled == true
//                            )
//                            putExtra(
//                                Constants.EXTRA_MEMBERSHIP_ENABLED,
//                                sharedViewModel.meData.value?.settingsMyMembership == true
//                            )
//                            putExtra(
//                                Constants.EXTRA_COINS_ENABLED,
//                                sharedViewModel.meData.value?.settingsShowCoins == true
//                            )
//                            putExtra(
//                                Constants.EXTRA_PAYMENT_CARDS_ENABLED,
//                                sharedViewModel.meData.value?.settingsPaymentCards == true
//                            )
//                        }
//                    }
//                }, showEdit = {
//                    if (isMicrophone || canLeaveLive()) {
//                        if (it.isEmpty()) {
//                            c.start<EditProfileActivity>()
//                        } else {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            c.startActivity(intent)
//                        }
//                    }
//                })
//
//            XPopup.Builder(activity)
//                .isDestroyOnDismiss(true)
//                .asCustom(
//                    meProfileDialog
//                )
//                .show()
//        }
    }

    fun showOtherProfileDialog(
        userId: String,
        username: String,
        targetUserRole: Int,
    ) {
//        if (otherProfileDialog == null || otherProfileDialog?.isShow == false) {
//            var isMicrophone = false
//            var isMute = false
//            val userInfos = if (activity is RecordActivity) {
//                (activity as RecordActivity).getLiveUser()
//            } else {
//                (activity as LiveActivity).getLiveUser()
//            }
//            userInfos.forEach {
//                if (it.userId == userId || it.userName == username) {
//                    isMicrophone = true
//                    isMute = it.mute == 1
//                }
//            }
//
//            if (activity.isFinishing) return
//            otherProfileDialog = OtherProfileDialog(
//                activity,
//                userId,
//                username,
//                conversationsEntity.role,
//                targetUserRole,
//                conversationId,
//                activity is RecordActivity,
//                isMicrophone,
//                (activity is LiveActivity) && (activity as LiveActivity).isMicrophoneUser(),
//                isMute,
//                0,
//                liveId,
//                2,
//                isSecure = conversationsEntity.type == 2,
//                moveToAudience = {
//                    (activity as RecordActivity).tryHangUp(
//                        it,
//                        userId
//                    )
//                },
//                gotoWebsite = {
//                    start(MarsWebActivity) { _, spec ->
//                        spec.url = it
//                    }
//                },
//                targetIsPerformer = userId == RecentConversation.getPerformerData()?.userInfo?.userId
//                        || username == RecentConversation.getPerformerData()?.userInfo?.userName,
//                removeFromGroup = { usId ->
//                    val userInfos = if (activity is RecordActivity) {
//                        (activity as RecordActivity).getLiveUser()
//                    } else {
//                        (activity as LiveActivity).getLiveUser()
//                    }
//                    userInfos.forEach {
//                        if (it.userId == usId) {
//                            hungUpLiveWith(usId)
//                        }
//                    }
//                },
//                onCheckLeaveLive = {
//                    canLeaveLive()
//                }
//            )
//            XPopup.Builder(activity)
//                .isDestroyOnDismiss(true)
//                .asCustom(
//                    otherProfileDialog
//                ).show()
//        }
    }

    private fun canLeaveLive(): Boolean {
        if (isHost) {
            marsToast(R.string.fb_host_cant_leave)
            return false
        } else if (viewModel.isMicrophoneUser.value == true) {
            marsToast(R.string.fb_microphone_user_cant_leave)
            return false
        } else {
            return true
        }
    }

    /**
     * 挂断连麦接口
     */
    private fun hungUpLiveWith(targetUserId: String) {
//        sharedViewModel.hangUpLiveWith(liveId, targetUserId)
//
//        MarsEventLogger.logFirebaseEvent(
//            "hang_up_live",
//            "live_view",
//            bundleOf("type" to if (activity is LiveActivity) "viewer" else "host")
//        )
    }

    open fun openGift(giftId: String? = null) {
    }

    open fun doRequestHandsUp() {
    }

    private fun handleActivityEvent(event: LiveEvent){
        if (event is LiveActivityEvent && event.userInfo?.userId == SessionPreferences.id) {
            //如果是自己发生的事件，直接展示出来
            lastActivityEventEmitJob?.cancel()
            lastActivityEventEmitJob = lifecycleScope.launch {
                topMessage = event
                activityMessage.text = event.content
                activityMessage.isVisible = true
                delay(2000)
                lastActivityEventEmitJob = null
            }

        }else if (event is LiveGiftEvent && event.userInfo.userId == SessionPreferences.id) {
            //如果是自己发生的事件，直接展示出来
            lastActivityEventEmitJob?.cancel()
            lastActivityEventEmitJob = lifecycleScope.launch {
                topMessage = event
                buildActivityMessage(event)
                delay(2000)
                lastActivityEventEmitJob = null
            }
        } else {
            //不是自己的activity，压进list
            activityList.add(event)
            popActivityEvent()
        }
    }

    private fun popActivityEvent() {
        if (activityList.isEmpty()) {
            return
        }
        if (lastActivityEventEmitJob == null) {
            lastActivityEventEmitJob = lifecycleScope.launch {
                val event = activityList.removeAt(0)
                buildActivityMessage(event)
//                topMessageAdapter.notifyItemChanged(0)
                if (activityList.size > 10) {
                    delay(1000)
                } else {
                    delay(2000)
                }
                lastActivityEventEmitJob = null
                popActivityEvent()
            }
        }
    }

    fun showPersonBottomDialog(info: LiveUserInfo, role: Int = 9) {
        if (isProfileLoading) {
            return
        }
        isProfileLoading = true
        liveProfileLoadingView.isVisible = true
        if (info.userId == SessionPreferences.id) {
            val userData = SESSION.retrieveMeInfo()
            if (userData != null) {
                showMeProfileDialog(userData)
                liveProfileLoadingView.isVisible = false
                isProfileLoading = false
            } else {
//                start(NotLoginYetActivity) { _, spec ->
//                    spec.source = "login"
//                }
            }
        } else {
            showOtherProfileDialog(info.userId, info.userName, role)
            liveProfileLoadingView.isVisible = false
            isProfileLoading = false
        }
    }

    private fun showBottomProfileDialog(userData: UserData) {
        if (userData.id == SessionPreferences.id) {
            showMeProfileDialog(userData)
        } else {
            val c = context
            if (c != null) {
//                showOtherProfileDialog(
//                    userData.id,
//                    userData.userName,
//                    userData.role ?: 9
//                )
            }
        }
    }

    @ExperimentalStdlibApi
    fun openComment() {
        val c = context
        if (c != null) {
            interactionsList.smoothScrollToPosition(0)
            liveCommentInputDialog = LiveCommentInputDialog(
                c,
                pendingComment ?: "",
                isHost,
                insertedMentions,
                object : LiveCommentInputDialog.OnLiveCommentEditListener {
                    override fun onSend(text: String) {
                        insertedMentions = null
                        pendingComment = null
//                        logFirebaseEvent(
//                            "comment_live",
//                            bundleOf("type" to if (isHost) "host" else "viewer")
//                        )
//                        viewModel.sendMessage(
//                            RecentConversation.conversationsEntity.type,
//                            text,
//                            conversationId,
//                            liveId,
//                            RecentConversation.conversationsEntity.level
//                        )
                    }

                    override fun onDismiss(text: String, mentionables: MutableList<Mentionable>?) {
                        insertedMentions = mentionables
                        pendingComment = text
                    }
                }
            )

            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .autoOpenSoftInput(true)
                .asCustom(liveCommentInputDialog)
                .show()
        }
    }

    private fun showNewMessageView() {
        val unreadMessages = liveMessageAdapter.messages.subList(0, newEventCount)
        var count = 0
        unreadMessages.forEach {
            if (!listOf(8, 9, 31, 201).contains(it.type) || it.renderType > 2){
                count++
            }
        }

        newMessagesCount.text = when {
            count > 99 -> {
                newMessagesCount.isVisible = true
                getString(
                    R.string.il_new_messages_counts,
                    "99+"
                )
            }
            count > 1 -> {
                newMessagesCount.isVisible = true
                getString(
                    R.string.il_new_messages_counts,
                    VenusNumberFormatter.format(count.toLong())
                )
            }
            count == 1 -> {
                newMessagesCount.isVisible = true
                getString(R.string.il_new_messages_counts)
            }
            else -> {
                newMessagesCount.isVisible = false
                ""
            }
        }
    }

    private fun hideNewMessageView() {
        preItemCount = liveMessageAdapter.itemCount
        newEventCount = 0
        newMessagesCount?.isVisible = false
    }

    private fun updateTotalViewerCount(event: LiveActivityEvent) {
        val num = event.onlineNumStr
        onlineCount.text = num
    }

    protected abstract fun init()

    protected abstract fun onLiveStateInfoInJoined(data: LiveStateInfo)
    protected open fun showLiveWith() {}

    open fun showCornerLikes(voteEvent: LikeEvent) {
        val c = context ?: return
        val p = voteEvent.userInfo?.portrait
        if (p != null) {
            Glide.with(c)
                .asBitmap()
                .load(p)
                .circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        liveLikesAnimView?.addFavor(resource)
                        for (i in 1..voteEvent.likeNum) {
                            lifecycleScope.launch {
                                delay(i * 100L)
                                liveLikesAnimView?.addFavor(emojiList.random())
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            for (i in 1..voteEvent.likeNum) {
                lifecycleScope.launch {
                    delay(i * 100L)
                    liveLikesAnimView?.addFavor(emojiList.random())
                }
            }
        }
    }

    open fun doLikeFavor() {
        liveLikesAnimView?.addFavor(emojiList.random())
    }

    open fun onRaiseHand() {
//        liveInteractionsAdapter.dataList.forEach {
//            if (it is LiveSystemEvent && it.isRequest == 1) {
//                it.isRequest = 2
//                liveInteractionsAdapter.notifyDataSetChanged()
//            }
//        }
    }

    open fun onHandsDown() {
    }

    protected abstract fun getLayoutId(): Int

    private fun buildActivityMessage(topMessage: LiveEvent){
        if (topMessage is LiveActivityEvent) {
            val userInfo = topMessage.userInfo
            if (userInfo != null && userInfo.userId != SessionPreferences.id) {
                val spanner = Spanner(topMessage .content)
                    .span(
                        userInfo.nickname,
                        Spans.foreground(Color.parseColor("#ffffff")),
                    )
                activityMessage.movementMethod = LinkMovementMethod()
                activityMessage.text = spanner
            } else {
                activityMessage.text = topMessage.content
            }
            activityMessage.isVisible = true
            activityMessage.onClick {
                userInfo?.let {
                    if (it.userId != SessionPreferences.id) {
//                        topMessageAdapter.goToUser(it, 9)
                    }
                }
            }
        } else if (topMessage is LiveGiftEvent) {
            //展示gift类型的message
            val userInfo = topMessage.userInfo
            val content = topMessage.giftInfo?.systemMessage?.content

            val processedContent = processTemplate(
                content ?: "",
                mapOf("user_name" to userInfo.nickname)
            )
            activityMessage.onClick {
                if (userInfo.userId != SessionPreferences.id) {

                }
            }

            giftListData?.gifts?.let { giftList ->
                val gift =
                    giftList.find { it.id == topMessage.giftId }
                if (gift != null) {
                    val nickname = userInfo.nickname
                    activityMessage.text = Spanner()
                        .append(
                            processedContent,
                            Spans.foreground(Color.parseColor("#ffffff"))
                        )
                        .span(
                            nickname,
                            Spans.custom(NoUnderlineClickableSpanBuilder {
                                if (userInfo.userId != SessionPreferences.id) {

                                }
                            }),
                            Spans.foreground(Color.parseColor("#ffffff")),
                        )
                    activityMessage.isVisible = true
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        startMessageEventJob()
    }

    override fun onPause() {
        super.onPause()
        messageEventJob?.cancel()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onDestroy() {
        messageEventJob?.cancel()
        firstGiftCardViewJob?.cancel()
        secondGiftCardViewJob?.cancel()
        viewModel.stopSendMessageLoop()
        liveMessageAdapter.unregisterAdapterDataObserver(messageAdapterDataObserver)
        giftAnim?.callback = null
        super.onDestroy()
    }

    override fun initViewModel(): LiveViewModel {
        return getActivityViewModel(LiveViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(getLayoutId(), viewModel)
    }

    private fun buildMediaData(
        msg: MessageEntity
    ) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val mediaDataList = JSONArray()
//            val message =
//                viewModel.getConversationsLiveMessages(SessionPreferences.recentConversationID)
//            var uuid = ""
//            val msgPayload = MessageEntity.Payload.fromJson(msg.payload) ?: return@launch
//            if (msg.type == 32) {
//                msgPayload.targetMessage?.let {
//                    uuid = it.uuid
//                }
//            } else {
//                uuid = msg.uuid
//            }
//
//            var msgIndex = 0
//
//            message.forEachIndexed { index, it ->
//                Timber.d("payload : ${it.payload}")
//                val payload =
//                    MessageEntity.Payload.fromJson(it.payload) ?: return@forEachIndexed
//                if (it.uuid == uuid) msgIndex = index
//                if (it.type in 3..4) {
//                    if (it.localResPath.isNeitherNullNorEmpty() && File(it.localResPath).exists()) {
//                        //缓存文件已存在无需下载
//                        mediaDataList.put(
//                            VideoMultyItem(
//                                it.uuid,
//                                payload.url ?: "",
//                                if (it.type == 4) 1 else 2,
//                                it.localResPath ?: "",
//                                if (it.type == 4) payload.cover ?: "" else payload.thumbnail
//                                    ?: ""
//                            ).toString()
//                        )
//                    } else {
//                        mediaDataList.put(
//                            VideoMultyItem(
//                                it.uuid,
//                                payload.url ?: "",
//                                if (it.type == 4) 1 else 2,
//                                it.localResPath ?: "",
//                                if (it.type == 4) payload.cover ?: "" else payload.thumbnail
//                                    ?: ""
//                            ).toString()
//                        )
//                    }
//                }
//            }
//            LiveEventBus.get(Constants.EVENT_BUS_LIVE_IMAGE_OPEN).post(Any())
//            withContext(Dispatchers.Main) {
//                start(MediaBrowserActivity) { _, spec ->
//                    spec.mediaDataList = mediaDataList.toString()
//                    spec.position = msgIndex
//                    spec.uuid = uuid
//                    spec.showVideoVolume = false
//                }
//            }
//        }
    }
}