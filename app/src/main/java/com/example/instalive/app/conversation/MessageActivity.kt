package com.example.instalive.app.conversation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.utils.hideKeyboard
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.databinding.ActivityMessageBinding
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import kotlinx.android.synthetic.main.activity_message.*
import timber.log.Timber
import java.util.*
import android.graphics.Bitmap
import android.os.Handler
import android.os.Message
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.baselibrary.utils.debounceClick
import com.example.baselibrary.utils.onLinearMarsLoadMore
import com.example.instalive.InstaLiveApp
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.base.SharedViewModel
import com.example.instalive.app.base.TextPopupWindow
import com.example.instalive.app.ui.GiftsDialog
import com.example.instalive.utils.*
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import com.venus.dm.app.ChatConstants
import com.venus.dm.db.entity.MessageEntity.Companion.SEND_STATUS_FAILED
import com.venus.dm.db.entity.MessageEntity.Companion.SEND_STATUS_SENDING
import com.venus.dm.model.event.MessageEvent
import kotlinx.android.synthetic.main.message_bottom_layout.*
import kotlinx.coroutines.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import splitties.alertdialog.appcompat.*
import splitties.dimensions.dp
import splitties.permissions.hasPermission
import splitties.systemservices.clipboardManager
import splitties.systemservices.inputMethodManager
import splitties.views.onClick
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue


@ExperimentalStdlibApi
class MessageActivity : MessageBaseActivity<ActivityMessageBinding>() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var messageEventTimestamp = 0L
    private var targetMessage: MessageEntity.TargetMessage? = null

    private val messageEventSyncList = ConcurrentLinkedQueue<MessageEvent>()//消息的队列
    private var messageEventJob: Job? = null//处理消息队列的线程

    private var isShowToFirstMessage = false//是否正在显示toFirstMessage按钮
    private var isLoadMentionSearch = false//正在加载@群成员列表
    private var messageSwipedAway = false//消息列表没有在最底部
    private var isLiveNeedResume = false//上次直播没有关闭，是否需要恢复
    private var isShowNewMessage = false//unread message气泡正在显示
    private var isShowATMessage = false//@气泡正在显示
    private var isScrollToNew = false//点击unread message气泡后，消息列表正在滚动向
    private var isFirstResume = true
    private var isScrollToAt = false
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

    private val sharedViewModel by lazy {
        InstaLiveApp.appInstance.getAppViewModelProvider().get(SharedViewModel::class.java)
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
//        conversationsEntity =
//            intent.getSerializableExtra(Constants.EXTRA_CONVERSATION_ENTITY) as ConversationsEntity
        RecentConversation.conversationsEntity ?: finish()
        conId = RecentConversation.conversationsEntity?.conversationId.toString()
        screenName = "message_view"
        viewModel.disappearMessage()
        initView()
        initList()
        initListener()
        initObserver()
        startMessageEventJob()
    }

    private fun initObserver() {
        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).observe(this) {
            if (it is MessageEvent) {
                Timber.d("MessageEvent type: ${it.type}")
                Timber.d("time test 收音: ${it.timestamp - messageEventTimestamp}")
                if (it.timestamp > messageEventTimestamp) {
                    messageEventSyncList.add(it)
                }
            }
        }

        LiveEventBus.get(Constants.EVENT_BUS_REPLY).observe(this, {
            if (it is MessageEntity) {
                reply.text =
                    "${getString(R.string.fb_message_reply_at)}${targetMessage?.senderName ?: ""}: "
                replyContainer.isVisible = true
                btnSend.isVisible = true
                ll_btn.isVisible = false
                edtChatInput.requestFocus()
                inputMethodManager.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })
    }

    private fun initListener() {
        back.onClick {
            onBackPressed()
        }
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
                    if (targetMessage == null) {
                        viewModel.sendMessage(
                            conId,
                            message,
                        )
                    } else {
                        viewModel.sendMessage(
                            conId,
                            message,
                            targetMessage!!,
                            -1
                        )
                    }

                    reply.text = ""
                    replyContainer.isVisible = false

                    targetMessage = null
                }
            }

            //选择图片或者视频的路径
            override fun pickMediaList() {
                scrollToBottom()
                requestStoragePermission({
                    openImageAndVideo()
                }, {

                })
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
                doLikeFavor()
            }

        })

        deleteReply.onClick {
            targetMessage = null
            reply.text = ""
            replyContainer.isVisible = false
            if (edtChatInput.text.toString().isEmpty()) {
                btnSend.isVisible = false
                ll_btn.isVisible = true
            }
        }
    }

    private fun initList() {
        messageEventTimestamp = System.currentTimeMillis()
        messageAdapter =
            MessageAdapter(
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                object :
                    MessageAdapter.OnMessageActionsListener {

                    override fun onPortraitClicked(senderId: String, senderRole: Int?) {
                        inputContainer.hideKeyboard()

                    }

                    override fun onResendClicked(messageEntity: MessageEntity) {
                        resendMessage(messageEntity)
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
                        lifecycleScope.launch(Dispatchers.IO) {
                            delay(100)
                            withContext(Dispatchers.Main) {
                                messageLongClicked(
                                    view,
                                    messageEntity,
                                    name,
                                    position
                                )
                            }
                        }
                    }

                    override fun onTextMessageClick(name: String, msg: String) {
                        val tokenPopupWindow =
                            TextPopupWindow(this@MessageActivity, msg)
                        with(tokenPopupWindow) {
                            isClippingEnabled = false
                            animationStyle = R.style.anim_style
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
                val list = messageAdapter.messages.toList()
                Collections.sort(list, messageAdapter.comparator)
                val time = list.lastOrNull()?.sendTime ?: 0L
                Timber.d("time test 下拉刷新: time = $time")
                if (time == 0L) return@onLinearMarsLoadMore
                LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT)
                    .post(
                        MessageEvent(
                            6,
                            null,
                            null,
                            System.currentTimeMillis(),
                            timestampStart = time
                        )
                    )
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

        Timber.d("time test 首次: $messageEventTimestamp")
//        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT)
//            .post(MessageEvent(6, null, null, 0))
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
        chatList.setOnTouchListener { _, _ ->
            inputContainer.hideKeyboard()
            false
        }
        messageAdapter.registerAdapterDataObserver(messageAdapterDataObserver)
        messageEventSyncList.add(
            MessageEvent(
                6,
                null,
                null,
                System.currentTimeMillis(),
                timestampStart = System.currentTimeMillis()
            )
        )
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
        conversationName.text = RecentConversation.conversationsEntity?.recipientName
        inputContainer.isVisible = RecentConversation.conversationsEntity?.chatState == 1
        inputContainer.conversationsEntity = RecentConversation.conversationsEntity
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
        val lastLeaveTimetoken = RecentConversation.conversationsEntity?.lastLeaveTimetoken ?: 0L
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
//                val topSmoothScroller = TopSmoothScroller(this@MessageActivity)
//                topSmoothScroller.targetPosition = 0
//                layoutManager.startSmoothScroll(topSmoothScroller)
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
                if (messageAdapter.messages.isEmpty()) 0 else {
                    val list = messageAdapter.messages.toList()
                    Collections.sort(list, messageAdapter.comparator)
                    list.first().sendTime
                }
            Timber.d("checkNewMessage time token2:${timeToken2 - (RecentConversation.conversationsEntity?.lastRead ?: 0L)}")
            if (timeToken2 - (RecentConversation.conversationsEntity?.lastRead ?: 0L) > 0) {
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
//            chatList?.smoothScrollBy(0, 0)
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
        openPictureAndVideoSelector(go = { result ->
            if (result != null) {
                when ((result[0].mimeType).lowercase(Locale.getDefault())) {
                    "video/mp4", "video/quicktime" -> {
                        scrollToBottom()
                        sharedViewModel.sendVideoMessage(
                            result[0].realPath,
                            conId,
                            result[0].width,
                            result[0].height,
                            result[0].duration,
                            result[0].size,
                            1,
                            "-1",
                            -1
                        )
                    }
                    "image/jpeg", "image/jpg", "image/png" -> {
                        scrollToBottom()
                        sharedViewModel.sendImageMessage(
                            "-1",
                            1,
                            result[0].realPath,
                            conId,
                            result[0].width,
                            result[0].height,
                            -1
                        )
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun startMessageEventJob() {
        messageEventJob?.cancel()
        messageEventJob = lifecycleScope.launch(Dispatchers.IO) {
            while (this.isActive) {
                try {
                    val messageEvent = messageEventSyncList.poll()
                    if (messageEvent != null) {
                        Timber.d("MessageEvent type 1 : ${messageEvent.type}")
                        messageAdapter.buildMessageChange(messageEvent, viewModel, conId, {
                            checkNewMessage()
                        }) {
                            if (!messageSwipedAway) {
                                if (it) {
                                    scrollToBottom()
                                } else {
                                    val topSmoothScroller = TopSmoothScroller(this@MessageActivity)
                                    topSmoothScroller.targetPosition = 0
                                    layoutManager.startSmoothScroll(topSmoothScroller)
                                }
                            }
                        }
                    } else {
                        delay(100)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun messageLongClicked(
        view: View,
        messageEntity: MessageEntity,
        name: String,
        position: Int,
    ) {

//        longClickingMessageUUID = messageEntity.uuid
        if (messageEntity.sendStatus == SEND_STATUS_SENDING) return
        val saveAction = {
            if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                if (messageEntity.type == 4) {
//                    XPopup.Builder(this)
//                        .dismissOnBackPressed(true)
//                        .dismissOnTouchOutside(false)
//                        .asCustom(progressDialog)
//                        .show()
//                }

//                viewModel.saveCheck(
//                    messageEntity.conId,
//                    messageEntity.type,
//                    onSuccess = {
//                        viewModel.saveMessageContent(messageEntity)
//                    })

            } else {
//                requestStoragePermission({}, {})
            }
        }
        val strings = when (messageEntity.type) {
            1, 32, 21 -> {//text
                if (messageEntity.senderId == SessionPreferences.id) {
                    arrayOf(
                        getString(R.string.fb_message_bubbles_copy),
                        getString(R.string.fb_message_bubbles_reply),
                        getString(R.string.delete)
                    )
                } else {
                    arrayOf(
                        getString(R.string.fb_message_bubbles_copy),
                        getString(R.string.fb_message_bubbles_reply)
                    )
                }
            }
            3 -> {//image
                if (messageEntity.senderId == SessionPreferences.id) {
                    arrayOf(
                        getString(R.string.fb_save),
                        getString(R.string.fb_message_bubbles_reply),
                        getString(R.string.delete)
                    )
                } else {
                    arrayOf(
                        getString(R.string.fb_save),
                        getString(R.string.fb_message_bubbles_reply)
                    )
                }
            }
            4 -> {//video
                if (messageEntity.senderId == SessionPreferences.id) {
                    arrayOf(
                        getString(R.string.fb_save),
                        getString(R.string.fb_message_bubbles_reply),
                        getString(R.string.delete)
                    )
                } else {
                    arrayOf(
                        getString(R.string.fb_save),
                        getString(R.string.fb_message_bubbles_reply)
                    )
                }
            }
            else -> {
                null
            }
        }
        if (strings != null) {

            Timber.d("view top:${view.top} bottom:${view.bottom} x:${view.x} y:${view.y}")

            XPopup.Builder(this)
                .atView(view)
                .popupPosition(PopupPosition.Right)
                .autoDismiss(true)
                .hasShadowBg(false)
                .asAttachList(
                    strings, null
                ) { _, text ->
                    when (text) {
                        getString(R.string.fb_message_bubbles_copy) -> {
                            val clip = ClipData.newPlainText(
                                getString(R.string.app_name),
                                messageEntity.content
                            )
                            clipboardManager.setPrimaryClip(clip)
                            marsToast(R.string.fb_coppied)
                        }

                        getString(R.string.fb_message_bubbles_reply) -> {
                            val payload =
                                MessageEntity.Payload.fromJson(messageEntity.payload)
                                    ?: return@asAttachList
                            payload.targetMessage = null
                            targetMessage = MessageEntity.TargetMessage(
                                messageEntity.uuid,
                                payload,
                                name,
                                messageEntity.type
                            )
                            LiveEventBus.get(Constants.EVENT_BUS_REPLY)
                                .postDelay(messageEntity, 300)
                        }
                        getString(R.string.delete) -> {
                            if (messageEntity.sendStatus == SEND_STATUS_FAILED) {
                                viewModel.deleteMessage(messageEntity)
                            } else {
                                deleteMessageDialog(messageEntity)
                            }
                        }
                        getString(R.string.fb_save) -> {
                            saveAction.invoke()
                        }
//                        getString(R.string.fb_mention) -> {
//                            lifecycleScope.launch(Dispatchers.IO) {
//                                val userData =
//                                    viewModel.getUserDataFromDB(messageEntity.senderId)
//                                if (userData == null) {
//                                    viewModel.getPersonalData(userId = messageEntity.senderId) {}
//                                    viewModel.personalLiveData.observe(this@MessageFragment) {
//                                        val message = "@${it.userName} "
//                                        insertAtFocusedPosition(edtChatInput, message)
//                                    }
//                                } else {
//                                    withContext(Dispatchers.Main) {
//                                        val message = "@${userData.username} "
//                                        insertAtFocusedPosition(edtChatInput, message)
//                                    }
//                                }
//                            }
//                        }
                    }
                }.show()
        }
    }

    private fun resendMessage(messageEntity: MessageEntity) {
        alertDialog {
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
                                    1,
                                    path,
                                    messageEntity.conId,
                                    payload?.width ?: 0,
                                    payload?.height ?: 0,
                                    -1
                                )
                                messageAdapter.removeItem(messageEntity.uuid)
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
                                    1,
                                    payload?.liveId ?: "-1",
                                    -1
                                )
                                messageAdapter.removeItem(messageEntity.uuid)
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
                }
            }
            cancelButton()
        }.show()
    }

    fun popupOpenGift() {
        if (giftsDialog == null || giftsDialog?.isShow == false) {
            giftsDialog = GiftsDialog(
                this,
                viewModel.giftListLiveData.value,
                conId,
                "",
                null,
                1,
                -1,
                onGiftSent = { gift, dialog ->
                    //insert gift event to sequence
                    gift.isOwnerGift = true
                    LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE).post(gift)
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

            XPopup.Builder(this)
                .isDestroyOnDismiss(true)
                .hasShadowBg(false)
                .asCustom(giftsDialog).show()

            val set = ConstraintSet()
            set.clone(container)
            set.setMargin(R.id.giftSecondContainer, ConstraintSet.BOTTOM, dp(420))
            set.applyTo(container)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        sharedViewModel.updateConversationLastLeaveTimeToken(conId)
        messageEventJob?.cancel()
        messageEventJob = null
    }

    override fun showLikeFavor(resource: Bitmap) {
        messageLikesAnimView?.addFavor(resource)
    }

}