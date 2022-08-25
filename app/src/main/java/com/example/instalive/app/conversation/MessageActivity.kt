package com.example.instalive.app.conversation

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.utils.Utils
import com.example.baselibrary.utils.hideKeyboard
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.databinding.ActivityMessageBinding
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import com.venus.framework.util.isNeitherNullNorEmpty
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Message
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.baselibrary.utils.debounceClick
import com.example.baselibrary.utils.onLinearMarsLoadMore
import com.example.instalive.app.base.TextPopupWindow
import com.example.instalive.utils.GlideEngine
import com.example.instalive.utils.aAnimatorSet
import com.example.instalive.utils.marsToast
import com.jeremyliao.liveeventbus.LiveEventBus
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.lxj.xpopup.XPopup
import com.venus.dm.app.ChatConstants
import com.venus.dm.model.event.MessageEvent
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import splitties.alertdialog.appcompat.negativeButton
import splitties.alertdialog.appcompat.positiveButton
import splitties.dimensions.dp
import splitties.mainhandler.mainHandler
import splitties.views.onClick


@ExperimentalStdlibApi
class MessageActivity : InstaBaseActivity<MessageViewModel, ActivityMessageBinding>() {

    private lateinit var conversationsEntity: ConversationsEntity
    private lateinit var screenName: String

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var messageEventTimestamp = 0L

    private var isShowToFirstMessage = false//是否正在显示toFirstMessage按钮
    private var isLoadMentionSearch = false//正在加载@群成员列表
    private var messageSwipedAway = false//消息列表没有在最底部
    private var isLiveNeedResume = false//上次直播没有关闭，是否需要恢复
    private var isShowNewMessage = false//unread message气泡正在显示
    private var isShowATMessage = false//@气泡正在显示
    private var isScrollToNew = false//点击unread message气泡后，消息列表正在滚动向
    private var isScrollToAt = false
    private var isFirstResume = true
    private var isPinDoing = false

    private var newMessageUUID: String? = null//unread message分割线下第一个消息的uuid
    private var atMessageUUID: String? = null//当前@气泡所指向的message的uuid
    private var conId = ""//当前会话id

    private val MSG_TO_FIRST_MESSAGE = 1
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_TO_FIRST_MESSAGE) {
                toFirstMessage?.isVisible = isShowToFirstMessage
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        conversationsEntity = intent.getSerializableExtra(Constants.EXTRA_CONVERSATION_ENTITY) as ConversationsEntity
        screenName = "message_view"
        viewModel.disappearMessage()
        initView()
        initList()
        initListener()
        initObserver()
    }

    private fun initObserver() {
        TODO("Not yet implemented")
    }

    private fun initListener() {
        inputContainer.setBottomLayoutActionListener(object :
            MessageBottomLayout.BottomLayoutActionListener {

            //发送 文本相关操作
            override fun sendMessage(message: String?) {
//                if (conversationsEntity.disableSend == 1) {
//                    marsToast(R.string.fb_disable_send_message_warning)
//                    return
//                }
//                logFirebaseEvent("send_message")
                if (message != null) {
//                    if (targetMessage == null) {
                        viewModel.sendMessage(
                            conversationsEntity.conversationId,
                            message,
                        )
//                    } else {
//                        viewModel.sendMessage(
//                            RecentConversation.conversationsEntity.type,
//                            message,
//                            targetMessage!!,
//                            RecentConversation.conversationsEntity.level
//                        )
//                    }

//                    reply.text = ""
//                    constraintLayout.isVisible = false
//
//                    targetMessage = null
                }
            }

            //选择图片或者视频的路径
            override fun pickMediaList() {
                scrollToBottom()
                openImageAndVideo()
            }

            override fun onClickGift(conversationsEntity: ConversationsEntity) {
//                if (RecentConversation.conversationsEntity.disableSend == 1) {
//                    marsToast(R.string.fb_disable_send_message_warning)
//                    return
//                }
                if (conversationsEntity.chatState != 1) return
                scrollToBottom()
//                popupOpenGift()
            }

            override fun onClickLike(conversationsEntity: ConversationsEntity) {
                scrollToBottom()
//                doLikeFavor()
            }

        })
    }

    private fun initList() {
        messageAdapter =
            MessageAdapter(
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                conversationsEntity,
                object :
                    MessageAdapter.OnMessageActionsListener {

                    override fun onPortraitClicked(senderId: String, senderRole: Int?) {
                        inputContainer.hideKeyboard()

                    }

                    override fun onResendClicked(messageEntity: MessageEntity) {
//                        resendMessage(messageEntity)
                    }

                    override fun onPlayVideo(messageEntity: MessageEntity) {
                        inputContainer.hideKeyboard()
//                        buildMediaData(messageEntity)
                    }

                    override fun onViewImage(messageEntity: MessageEntity) {
                        inputContainer.hideKeyboard()
//                        buildMediaData(messageEntity)
                    }

                    override fun onReplyViewImage(messageEntity: MessageEntity) {
                        inputContainer.hideKeyboard()
//                        buildMediaData(messageEntity)
                    }

                    override fun onReplyViewVideo(messageEntity: MessageEntity) {
                        inputContainer.hideKeyboard()
//                        buildMediaData(messageEntity)
                    }

                    override fun onReplyMessage(messageEntity: MessageEntity) {

                    }

                    override fun onUsernameClick(username: String) {

                    }

                    override fun onURLMessageClick(url: String) {

                    }

                    override fun onClickGift(giftId: String) {

                    }

                    override fun onRecharge() {

                    }

                    override fun onMessageLongClicked(
                        view: View,
                        messageEntity: MessageEntity,
                        name: String,
                        position: Int,
                    ) {
                        inputContainer.hideKeyboard()

                    }

                    override fun onTextMessageClick(name: String, msg: String) {
                        val tokenPopupWindow =
                            TextPopupWindow(this@MessageActivity, msg)
                        with(tokenPopupWindow) {
                            isClippingEnabled = false
                            show()
                        }

                    }
                }, UUID.randomUUID().toString()
            )
        layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
//        layoutManager.stackFromEnd = true
        chatList.layoutManager = layoutManager
//        chatList?.itemAnimator?.changeDuration = 0
//        chatList?.itemAnimator?.removeDuration = 0
//        chatList?.itemAnimator?.addDuration = 0
//        val animator = chatList.itemAnimator
//        if (animator is SimpleItemAnimator) {
//            animator.supportsChangeAnimations = false
//        }
        chatList?.itemAnimator = null
        chatList.onLinearMarsLoadMore {
            if (messageAdapter.messages.isNotEmpty()) {
                val time = messageAdapter.messages.lastOrNull()?.sendTime ?: 0L
                Timber.d("time test 下拉刷新: time = $time")
                if (time == 0L) return@onLinearMarsLoadMore
                LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).post(MessageEvent(6, null, null, time))
            }
        }

        chatList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val position = parent.getChildAdapterPosition(view)
                if (position == messageAdapter.itemCount - 1) {
                    outRect.top = dp(20)
                } else {
                    outRect.top = 0
                }
            }
        })
        chatList.setHasFixedSize(true)
        messageEventTimestamp = System.currentTimeMillis()
        Timber.d("time test 首次: $messageEventTimestamp")
        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).post(MessageEvent(6, null, null, 0))
        chatList.adapter = messageAdapter
        chatList.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            currentScreenMessages { message ->
                checkScrollToUnreadUI(message)
                checkScrollToAt(message)
            }
        }
        chatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == 0) {
                    val bottomOffset = getChatListBottomOffset()
                    val height = recyclerView.height
//                    toFirstMessage.isVisible = bottomOffset > height / 10
                    messageSwipedAway = bottomOffset > height
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val bottomOffset = getChatListBottomOffset()
                val height = recyclerView.height
                isShowToFirstMessage = bottomOffset > height / 10
                showToFirstMessage()
                messageSwipedAway = bottomOffset > height

                if (isScrollToNew || isScrollToAt || isShowNewMessage || isShowATMessage) {
                    currentScreenMessages { message ->
                        checkScrollToUnreadUI(message)
                        checkScrollToAt(message)
                    }
                }
            }
        })

        messageAdapter.registerAdapterDataObserver(messageAdapterDataObserver)

        KeyboardVisibilityEvent.setEventListener(this, this,
            { isOpen ->
                Timber.d("$isOpen")
//                inputContainer.keyboardVisibilityEvent(isOpen)
                lifecycleScope.launch(Dispatchers.Main) {
                    if (isOpen) scrollToBottom()
                }
            })
        newMessages.debounceClick {
            isScrollToNew = true
            isShowNewMessage = false
            newMessageUUID?.let { scrollToMessage(false, it) }
            newMessages.isVisible = false
        }
        atMessages.debounceClick {
            isScrollToAt = true
            atMessageUUID?.let { scrollToMessage(true, it) }
            isShowATMessage = false
            atMessages.isVisible = false
        }
        toFirstMessage.onClick {
            scrollToBottom()
        }
    }

    private fun currentScreenMessages(messageResult: (MessageEntity) -> Unit) {
        try {
            val lastCompletelyPosition =
                layoutManager.findLastCompletelyVisibleItemPosition()
            val firstPosition =
                layoutManager.findFirstVisibleItemPosition().coerceAtLeast(0)
            val lastPosition = layoutManager.findLastVisibleItemPosition()
            val endPosition =
                if (lastCompletelyPosition != -1) lastCompletelyPosition else 0.coerceAtLeast(
                    lastPosition
                )
            for (index in firstPosition..endPosition) {
                val message = messageAdapter.messages[index]
                messageResult.invoke(message)
            }
        } catch (e: Exception) {
        }
    }

    private fun initView() {
        conversationName.text = conversationsEntity.recipientName
        inputContainer.isVisible = conversationsEntity.chatState == 1
        inputContainer.conversationsEntity = conversationsEntity
    }

    private fun showToFirstMessage() {
        if (mHandler.hasMessages(MSG_TO_FIRST_MESSAGE)) {
            mHandler.removeMessages(MSG_TO_FIRST_MESSAGE)
        }
        mHandler.sendEmptyMessageDelayed(MSG_TO_FIRST_MESSAGE, 300) //删除
    }

    private fun showUnreadMessageTip(timeToken: Long) {
        viewModel.getConversationUnreadCount(conId, timeToken) { count, uuid ->
            showNewMessage(count)
            newMessageUUID = uuid
            Timber.d("newMessage onclick unread: $count uuid:$newMessageUUID")
        }
    }

    private fun checkNewMessage() {
        val lastLeaveTimetoken = conversationsEntity.lastLeaveTimetoken
        val lastPosition = layoutManager.findLastVisibleItemPosition()
        Timber.d("checkNewMessage lastPosition:$lastPosition")

        if (lastPosition == -1 || messageAdapter.messages.size <= lastPosition) return

        val timeToken =
            if (messageAdapter.messages.isEmpty()) 0 else if (messageAdapter.messages.get(
                    lastPosition
                ).read == 0
            ) messageAdapter.messages.get(lastPosition).sendTime else 0
        Timber.d("checkNewMessage time token:${timeToken - lastLeaveTimetoken}")
        if (timeToken > lastLeaveTimetoken) {//离线未读消息超过一屏幕
            showUnreadMessageTip(lastLeaveTimetoken)
            viewModel.checkConversationBeingAt(
                conId
            ) { isAtMe, messageUUID ->
                Timber.d("checkNewMessage isAtMe: $isAtMe")
                if (isAtMe) {
                    var isOnScreen = false
                    currentScreenMessages {
                        if (messageUUID == it.uuid) {
                            isOnScreen = true
                        }
                    }
                    if (!isOnScreen) {
                        isShowATMessage = true
                        atMessages.isVisible = true
                        this.atMessageUUID = messageUUID
                    }
                }
                Timber.d("checkNewMessage clear 1")
//                viewModel.updateConversationBeingAt(conId, SessionPreferences.id)
            }
        } else {
            if (messageAdapter.itemCount > 0 && messageSwipedAway) {
//                showAtMe()
            } else {
                Timber.d("checkNewMessage clear 2")
//                viewModel.removeConversationAtState(conId, SessionPreferences.id)
            }
        }
    }

    private fun checkScrollToAt(messageEntityWithUser: MessageEntity) {
        if (this.atMessageUUID == messageEntityWithUser.uuid) {
            isScrollToAt = false
            isShowATMessage = false
            atMessages.isVisible = false
        }
    }

    private fun checkScrollToUnreadUI(messageEntityWithUser: MessageEntity) {
        if (messageEntityWithUser.type == 201) {
            isScrollToNew = false
            isShowNewMessage = false
            newMessages.isVisible = false
        }
    }

    var isFirst = true
    private val messageAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
//            if (!messageSwipedAway) {
//                context?.let {
//                    val topSmoothScroller = TopSmoothScroller(it)
//                    topSmoothScroller.targetPosition = 0
//                    layoutManager.startSmoothScroll(topSmoothScroller)
//                }
//            }
            insertMessage(positionStart)
        }
    }

    private fun insertMessage(positionStart: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(300)
            withContext(Dispatchers.Main) {
                if (isFirst) {
                    isFirst = false
                    layoutManager.scrollToPositionWithOffset(0, 12)

                    checkNewMessage()
                }
            }
            val timeToken2 =
                if (messageAdapter.messages.isEmpty()) 0 else messageAdapter.messages.first().sendTime
            Timber.d("checkNewMessage time token2:${timeToken2 - conversationsEntity.lastRead}")
            if (timeToken2 - conversationsEntity.lastRead > 0) {
                haveRead(2)
            }
        }
        lifecycleScope.launch {
            if (positionStart == 0 && toFirstMessage.isVisible) toFirstMessage.aAnimatorSet(
                toFirstMessage
            )
            if (isScrollToNew) newMessageUUID?.let { scrollToMessage(false, it) }
            if (isScrollToAt) atMessageUUID?.let { scrollToMessage(true, it) }
        }
    }

    /**
     * new message 只有向上滑动的状态
     */
    @SuppressLint("SetTextI18n")
    private fun showNewMessage(unreadCount: Int) {
        if (!isFullScreen(chatList)) return
        if (unreadCount < 1) return
        newMessages.text = "${
            if (unreadCount > 99) {
                "99+"
            } else {
                "$unreadCount"
            }
        } ${getString(R.string.fb_new_messages)}"
        isShowNewMessage = true
        newMessages.isVisible = true
    }

    private fun scrollToMessage(isAt: Boolean, uuid: String?) {
        uuid?.let {
            val index = messageAdapter.messages.toList().indexOfFirst { messageEntityWithUser ->
                if (isAt) messageEntityWithUser.uuid == it else messageEntityWithUser.type == 201
            }
            if (index == -1) {
                layoutManager.scrollToPosition(messageAdapter.messages.size - 1)
                chatList?.smoothScrollBy(0, -1)
            } else {
                if (isAt) {
                    layoutManager.scrollToPositionWithOffset(index, layoutManager.height / 3)
                } else {
                    layoutManager.scrollToPosition(index)
                    chatList?.smoothScrollBy(0, -1)
                }
            }
        }
    }

    private fun scrollToBottom() {
        if (messageAdapter.itemCount > 0) {
            Timber.d("scrollToBottom")
            chatList?.scrollToPosition(0)
            chatList?.smoothScrollBy(0, 0)
        }
    }

    private fun getChatListBottomOffset(): Int {
        val v1 = chatList?.computeVerticalScrollExtent() ?: 0
        val v2 = chatList?.computeVerticalScrollOffset() ?: 0
        val v3 = chatList?.computeVerticalScrollRange() ?: 0
        if (v2 < 0) return 0
        return v3 - v1 - v2
    }

    private fun deleteMessageDialog(message: MessageEntity) {
//        val c = context ?: return
//        c.alertDialog {
//            titleResource = R.string.fb_dialog_delete_message_title
//            messageResource =
//                if (isOwner() || RecentConversation.conversationsEntity.type == 1)
//                    R.string.fb_dialog_delete_message_content_owner
//                else
//                    R.string.fb_dialog_delete_message_content_other
//            positiveButton(R.string.fb_delete) {
//                if (isOwner()) {
//                    viewModel.chatDelete(message)
//                } else {
//                    viewModel.chatRecall(message)
//                }
//                it.dismiss()
//            }
//            negativeButton(R.string.fb_cancel) {
//                it.dismiss()
//            }
//        }.show()
    }

    private fun openImageAndVideo() {
//        if (RecentConversation.conversationsEntity.disableSend == 1) {
//            marsToast(R.string.fb_disable_send_message_warning)
//            return
//        }
//        logFirebaseEvent("click_img")

        val filterMimeType = ArrayList<String>()
        filterMimeType.add("video/mp4")
        filterMimeType.add("video/quicktime")
        filterMimeType.add("image/jpeg")
        filterMimeType.add("image/jpg")
        filterMimeType.add("image/png")
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofVideo())
            .isMaxSelectEnabledMask(true)
            .isCanPreView(false)
            .isWeChatStyle(true)
            .theme(R.style.picture_WeChat_style)
            .imageEngine(GlideEngine.createGlideEngine())
            .isPreviewVideo(false)
            .selectionMode(PictureConfig.SINGLE)
            .maxSelectNum(1)
            .maxVideoSelectNum(1)
            .selectCountText(getString(R.string.fb_send))
            .setLanguage(LanguageConfig.ENGLISH)
            .isOnlyVideo(false)
            .isWithVideoImage(true)
            .isSelectedLocalMedia(false)
            .selectMaxPrompt(resources.getString(R.string.fb_send))
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .isCamera(false)
            .isShowPreView(false)
            .setFilterMimeType(filterMimeType)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: MutableList<LocalMedia>?) {
                    if (result != null) {
                        when ((result[0].mimeType).lowercase(Locale.getDefault())) {
                            "video/mp4", "video/quicktime" -> {
                                scrollToBottom()
//                                sharedViewModel.sendVideoMessage(
//                                    result[0].realPath,
//                                    RecentConversation.conversationsEntity.conversationId,
//                                    RecentConversation.conversationsEntity.type,
//                                    result[0].width,
//                                    result[0].height,
//                                    result[0].duration,
//                                    result[0].size,
//                                    1,
//                                    "-1",
//                                    RecentConversation.conversationsEntity.level
//                                )
                            }
                            "image/jpeg", "image/jpg", "image/png" -> {
                                scrollToBottom()
//                                sharedViewModel.sendImageMessage(
//                                    RecentConversation.conversationsEntity.type,
//                                    "-1",
//                                    1,
//                                    result[0].realPath,
//                                    SessionPreferences.recentConversationID,
//                                    result[0].width,
//                                    result[0].height,
//                                    RecentConversation.conversationsEntity.level
//                                )
                            }
                            else -> {
                            }
                        }
                    }
                }

                override fun onCancel() {}

            })
    }

    private fun isFullScreen(recyclerView: RecyclerView?): Boolean {
        if (recyclerView == null) return false
        return recyclerView.computeVerticalScrollExtent() >= recyclerView.height
    }

    private fun haveRead(type: Int) {
        if (messageAdapter.itemCount > 0) {
            viewModel.updateConversationLastRead(conId, type)
        }
    }

    override fun initViewModel(): MessageViewModel {
        return getActivityViewModel(MessageViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_message, viewModel)
    }

}