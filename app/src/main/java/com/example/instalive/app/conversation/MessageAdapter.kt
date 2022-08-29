package com.example.instalive.app.conversation

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baselibrary.utils.HashTagHelper
import com.example.baselibrary.utils.TimeUtils
import com.example.baselibrary.utils.Utils
import com.example.baselibrary.utils.debounceClick
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.databinding.*
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.utils.DeeplinkHelper
import com.example.instalive.utils.SysUtils
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.model.event.MessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import splitties.systemservices.layoutInflater
import splitties.views.onClick
import splitties.views.onLongClick
import timber.log.Timber
import java.util.*
import kotlin.Comparator
import kotlin.math.ceil

@ExperimentalStdlibApi
class MessageAdapter(
    var messages: MutableList<MessageEntity>,
    var originalMessages: MutableList<MessageEntity>,
    var messageUUIDList: MutableList<String>,
    val conversationsEntity: ConversationsEntity,
    val onMessageActionsListener: OnMessageActionsListener,
    val unreadDividerMessageUUID: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var unreadMessageUUID: String? = null

    var isFirstUnreadDivider: Boolean = true

    var comparator: java.util.Comparator<MessageEntity> =
        Comparator { details1, details2 -> //排序规则，按照价格由大到小顺序排列("<"),按照价格由小到大顺序排列(">"),
            if (details1.uuid == details2.uuid) {
                0
            } else if (details1.sendTime < details2.sendTime) {
                1
            } else {
                -1
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_message_me_layout -> MessageMeViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_me_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_time_layout -> MessageTimeViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_time_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_prompt_layout -> MessagePromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_video_me_layout -> MessageVideoViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_video_me_layout,
                    parent,
                    false
                )
            )

            R.layout.item_message_image_me_layout -> MessageImageViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_image_me_layout,
                    parent,
                    false
                )
            )

            R.layout.item_message_reply_layout -> MessageReplyViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_reply_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_gift_prompt_layout -> MessageGiftPromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_gift_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_live_prompt_layout -> MessageLivePromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_live_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_important_prompt_layout -> MessageImportantPromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_important_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_recharge_prompt_layout -> MessageRechargePromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_recharge_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_message_new_message -> MessageNewMessagesViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_new_message,
                    parent,
                    false
                )
            )
            else -> MessageNotSupportViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_message_not_support_layout,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            1 -> R.layout.item_message_me_layout
            3 -> R.layout.item_message_image_me_layout
            4 -> R.layout.item_message_video_me_layout
            8, 9, 10 -> {
                when (messages[position].renderType) {
                    3 -> {
                        R.layout.item_message_gift_prompt_layout
                    }
                    5 -> {
                        R.layout.item_message_important_prompt_layout
                    }
                    6 -> {
                        R.layout.item_message_recharge_prompt_layout
                    }
                    else -> {
                        R.layout.item_message_prompt_layout
                    }
                }
            }
            31 -> R.layout.item_message_prompt_layout
            32 -> R.layout.item_message_reply_layout
            33 -> {
                when (messages[position].renderType) {
                    4 -> {
                        R.layout.item_message_live_prompt_layout
                    }
                    else -> {
                        R.layout.item_message_prompt_layout
                    }
                }
            }
            101 -> {
                R.layout.item_message_prompt_layout
            }
            201 -> R.layout.item_message_new_message
            else -> R.layout.item_message_not_support_layout
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = messages[position]
        when (holder) {
            is MessageMeViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)
                if (item.content == null) {
                    try {
                        val obj = JSONObject(item.payload.replace("\\", "\\\\"))
                        val content = obj.getString("content")
                        item.content = content
                    } catch (e: Exception) {
//                        CollectHelper.commonEvent(
//                            mapOf(
//                                "type" to "105",
//                                "payload" to item.payload
//                            )
//                        )
                        item.content = ""
                    }
                }
                holder.binding.messageEntity = item
                holder.binding.executePendingBindings()

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.senderId,
                        item.userRole
                    )
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }
                holder.binding.messageContent.onLongClick {
                    onMessageActionsListener.onMessageLongClicked(
                        holder.binding.messageContent,
                        item,
                        item.senderName
                    )
                }

                holder.binding.messageContent.setTag(R.id.message_tag_first, item.senderName ?: "")
                val content = item.content
                if (content != null && content.isNotEmpty()) {
                    Utils.buildTextUrlSpan(content, holder.binding.messageContent)
                }

                val helper =
                    HashTagHelper.Creator.create(Color.parseColor("#ffc835"), object :
                        HashTagHelper.OnSearchTriggerListener {
                        override fun onSearch(hashTag: String) {
                        }

                        override fun onRecommendation() {
                        }

                        override fun onSearchClosed() {
                        }
                    }, object : HashTagHelper.OnHashTagClickListener {
                        override fun onHashTagClicked(hashTag: String?) {
                            onMessageActionsListener.onTextMessageClick("", hashTag ?: "")
                        }

                        override fun onUsernameClicked(username: String?) {
                            username?.let { onMessageActionsListener.onUsernameClick(it) }
                        }
                    })
                helper.handle(holder.binding.messageContent)
            }
            is MessageTimeViewHolder -> {
                holder.binding.timestamp = item.sendTime
            }

            is MessageVideoViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)
                holder.binding.messageEntity = item
                holder.binding.executePendingBindings()

//                Utils.changeImageViewWH(
//                    holder.binding.videoCover,
//                    payload?.width ?: 100,
//                    payload?.height ?: 100
//                )

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.senderId,
                        item.userRole
                    )
                }
                holder.binding.messageContainer.onClick {
                    onMessageActionsListener.onPlayVideo(item)
                }
                holder.binding.messageContainer.onLongClick {
                    onMessageActionsListener.onMessageLongClicked(
                        holder.binding.messageContainer,
                        item,
                        item.senderName
                    )
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }
            }
            is MessageImageViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)
                holder.binding.messageEntity = item
                holder.binding.executePendingBindings()

                val width = payload?.width ?: 100
                val height = payload?.height ?: 100
                Utils.changeImageViewWH(holder.binding.imageCover, width, height)

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.senderId,
                        item.userRole
                    )
                }
                holder.binding.imageCover.onClick {
                    onMessageActionsListener.onViewImage(item)
                }
                holder.binding.imageCover.onLongClick {
                    onMessageActionsListener.onMessageLongClicked(
                        holder.binding.imageCover,
                        item,
                        item.senderName
                    )
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }
            }
            is MessageReplyViewHolder -> {
                holder.binding.messageEntity = item
                holder.binding.executePendingBindings()
                try {
                    val payload = MessageEntity.Payload.fromJson(item.payload)

                    var replyMsg: MessageEntity? = null

                    payload?.targetMessage?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            val replyMessage = InstaLiveDBProvider.db.directMessagingDao()
                                .getMessagedByUuid(it.uuid, SessionPreferences.id)
                            withContext(Dispatchers.Main) {
                                holder.binding.replyContent.text =
                                    if (replyMessage != null && replyMessage.state != 2) {
                                        replyMsg = replyMessage
                                        if (it.type == 3) {
                                            "${it.senderName}: [Image]"
                                        } else if (it.type == 4) {
                                            "${it.senderName}: [Video]"
                                        } else if (it.type == 34) {
                                            "${it.senderName}: [Gif]"
                                        } else {
                                            "${it.senderName}: ${it.payload.content}"
                                        }
                                    } else {
                                        "${it.senderName}: ${holder.itemView.context.getString(R.string.fb_reply_message_deleted_content)}"
                                    }
                            }
                        }
                    }
                    holder.binding.replyContent.onClick { _ ->
                        if (payload?.targetMessage?.state == 2 || replyMsg == null) return@onClick
                        if (payload?.targetMessage?.type == 3) {
                            onMessageActionsListener.onReplyViewImage(item)
                        } else if (payload?.targetMessage?.type == 4) {
                            onMessageActionsListener.onReplyViewVideo(item)
                        } else if (payload?.targetMessage?.type == 1) {
                            onMessageActionsListener.onReplyMessage(item)
                        } else {
                            payload?.targetMessage?.payload?.content.let {
                                onMessageActionsListener.onReplyMessage(item)
                            }
                        }
                    }
                } catch (e: Exception) {
//                    CollectHelper.commonEvent(
//                        mapOf(
//                            "type" to "105",
//                            "payload" to item.payload
//                        )
//                    )
                    holder.binding.replyContent.text = ""
                }

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.senderId,
                        item.userRole
                    )
                }

                holder.binding.messageContent.onLongClick {
                    onMessageActionsListener.onMessageLongClicked(
                        holder.binding.messageContent,
                        item,
                        item.senderName
                    )
                }
                holder.binding.messageContent.setTag(R.id.message_tag_first, item.senderName)

                val content = item.content
                if (content != null && content.isNotEmpty()) {
                    Utils.buildTextUrlSpan(content, holder.binding.messageContent)
                }

                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }
                val helper =
                    HashTagHelper.Creator.create(Color.parseColor("#ffc835"), object :

                        HashTagHelper.OnSearchTriggerListener {
                        override fun onSearch(hashTag: String) {
                        }

                        override fun onRecommendation() {
                        }

                        override fun onSearchClosed() {
                        }
                    }, object : HashTagHelper.OnHashTagClickListener {
                        override fun onHashTagClicked(hashTag: String?) {
                            onMessageActionsListener.onTextMessageClick("", hashTag ?: "")
                        }

                        override fun onUsernameClicked(username: String?) {
                            username?.let { onMessageActionsListener.onUsernameClick(it) }
                        }
                    })
                helper.handle(holder.binding.messageContent)
            }
            is MessagePromptViewHolder -> {
                holder.binding.messageEntity = item
                holder.binding.contentView.onClick {
                    try {
                        val payload = MessageEntity.Payload.fromJson(item.payload)
                        if (payload?.renderType == 2) {
                            DeeplinkHelper.handleDeeplink(
                                Uri.parse(payload.deeplink ?: ""),
                                holder.itemView.context
                            )
                        }
                    } catch (e: Exception) {
                    }
                }
            }

            is MessageGiftPromptViewHolder -> {

                val payload = MessageEntity.Payload.fromJson(item.payload)


                payload?.let {
                    holder.binding.contentStr =
                        holder.itemView.context.getString(R.string.fb_send_a_gift, it.giftName)

                    holder.binding.highlight = it.highlight
//                        if (item.senderId.isNullOrEmpty()){
                    if (item.senderPortrait.isEmpty()) item.senderPortrait =
                        payload.userInfo?.portrait ?: ""
                    if (item.senderName.isEmpty()) item.senderName =
                        payload.userInfo?.nickname ?: ""
                    item.userRole = payload.userInfo?.userRole ?: 9
//                        }
                    holder.binding.messageEntity = item
                }
                holder.binding.executePendingBindings()

                Glide.with(holder.itemView.context)
                    .load(payload?.giftImg)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.binding.giftIcon)
                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        payload?.userInfo?.userId ?: "",
                        payload?.userInfo?.userRole
                    )
                }
                holder.binding.giftMessageContainer.onClick {
                    payload?.giftId?.let { it1 ->
                        onMessageActionsListener.onClickGift(it1)
                    }
                }
            }
            is MessageLivePromptViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)

                payload?.let {

                    holder.binding.contentStr = it.content

                    holder.binding.highlight = it.highlight
//                        if (item.senderId.isNullOrEmpty()){
                    if (item.senderPortrait.isNullOrEmpty()) item.senderPortrait =
                        payload.userInfo?.portrait ?: ""
                    if (item.senderName.isNullOrEmpty()) item.senderName =
                        payload.userInfo?.nickname ?: ""
                    item.userRole = payload.userInfo?.userRole ?: 9
//                        }
                    holder.binding.messageEntity = item
                }
                holder.binding.executePendingBindings()

//                holder.binding.messageIcon.imageResource =
//                    if (isGroupChat) R.drawable.ic_live_message else R.drawable.icon_message_private_video_call

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        payload?.userInfo?.userId ?: "",
                        payload?.userInfo?.userRole
                    )
                }
            }
            is MessageImportantPromptViewHolder -> {
                holder.binding.messageEntity = item
                holder.binding.executePendingBindings()
            }
            is MessageRechargePromptViewHolder -> {
                holder.binding.messageEntity = item
                holder.binding.executePendingBindings()
                holder.binding.rechargeBtn.debounceClick {
                    onMessageActionsListener.onRecharge()
                }
            }
            is MessageNewMessagesViewHolder -> {}
            is MessageNotSupportViewHolder -> {

                holder.binding.messageEntity = item



                holder.binding.executePendingBindings()
                holder.itemView.onClick {
                    SysUtils.showPlayReview(holder.itemView.context as Activity)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun removeItem(uuid: String) {
        val index = messages.indexOfLast {
            it.uuid == uuid
        }
        messageUUIDList.remove(uuid)
        messages.removeAt(index)
        notifyItemRemoved(index)
    }

    class MessageMeViewHolder(val binding: ItemMessageMeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageReplyViewHolder(val binding: ItemMessageReplyLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageTimeViewHolder(val binding: ItemMessageTimeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessagePromptViewHolder(val binding: ItemMessagePromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageVideoViewHolder(val binding: ItemMessageVideoMeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageImageViewHolder(val binding: ItemMessageImageMeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageNotSupportViewHolder(val binding: ItemMessageNotSupportLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageGiftPromptViewHolder(val binding: ItemMessageGiftPromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageLivePromptViewHolder(val binding: ItemMessageLivePromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageImportantPromptViewHolder(val binding: ItemMessageImportantPromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageRechargePromptViewHolder(val binding: ItemMessageRechargePromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageNewMessagesViewHolder(val binding: ItemMessageNewMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    suspend fun buildMessageChange(
        messageEvent: MessageEvent,
        viewModel: MessageViewModel,
        conId: String,
        checkBubbles: () -> Unit,
        scrollToBottom: (noAnimation: Boolean) -> Unit,
    ) {
        when (messageEvent.type) {
            1 -> {//自己发送新增
                val it = messageEvent.messageEntity ?: return
                withContext(Dispatchers.Main){

                        messageUUIDList.add(it.uuid)
                        val insertMessage = checkInsertMessage(
                            it,
                            if (messages.isEmpty()) null else messages.first()
                        )
                        if (insertMessage != null) {
                            messageUUIDList.add(insertMessage.uuid)
                            messages.add(0, insertMessage)
                            notifyItemInserted(0)
                            scrollToBottom.invoke(true)
                        }

                        messages.add(0, it)
                        notifyItemInserted(0)
                        scrollToBottom.invoke(true)
                }
            }
            2 -> {//socket新增
                viewModel.getMessageInTimeZone(
                    conId,
                    messageEvent.timestampStart,
                    messageEvent.timestampEnd
                ) {
                    val mutableList = mutableListOf<MessageEntity>()
                    it.forEachIndexed { index, messageEntity ->
                        if (!messageUUIDList.contains(messageEntity.uuid)) {
                            messageUUIDList.add(messageEntity.uuid)
                            val insertMessage = checkInsertMessage(
                                messageEntity, if (index == 0) {
                                    if (messages.isEmpty()) null else messages.first()
                                } else it[index - 1]
                            )
                            if (insertMessage != null) {
                                mutableList.add(insertMessage)
                            }
                            mutableList.add(messageEntity)
                        }

                        if (mutableList.isNotEmpty()) {
                            if (messageEvent.isLoading) {
                                originalMessages.addAll(mutableList)
                            } else {
                                if (originalMessages.isEmpty()) {
                                    val old = messages.toList()
                                    val new = messages.toMutableList()
                                    new.addAll(0, mutableList.reversed())
                                    Collections.sort(new, comparator)

                                    withContext(Dispatchers.Main) {
                                        val result = DiffUtil.calculateDiff(
                                            MessageListComparator(
                                                new,
                                                old,
                                            ), false
                                        )
                                        messages = new
                                        Timber.d("message size 2: ${messages.size}, ${originalMessages.size}")
                                        result.dispatchUpdatesTo(this@MessageAdapter)
                                        scrollToBottom.invoke(false)
                                    }
                                } else {
                                    originalMessages.addAll(mutableList)
                                    val old = messages.toList()
                                    val new = messages.toMutableList()
                                    new.addAll(0, originalMessages.reversed())
                                    Collections.sort(new, comparator)
//                                    var index = -1
//                                    val unreadDividerMessage =
//                                        checkAddUnreadMessage(new, viewModel, true) {
//                                            index = it
//                                        }
//                                    if (unreadDividerMessage != null) {
//                                        if (messageUUIDList.contains(unreadDividerMessageUUID)) {
//                                            new.removeAll { it.uuid == unreadDividerMessageUUID }
//                                        } else {
//                                            messageUUIDList.add(unreadDividerMessageUUID)
//                                        }
//                                        new.add(index + 1, unreadDividerMessage)
//                                    }
                                    withContext(Dispatchers.Main) {
                                        val result = DiffUtil.calculateDiff(
                                            MessageListComparator(
                                                new,
                                                old,
                                            ), false
                                        )
                                        messages = new
                                        Timber.d("message size 3: ${messages.size}, ${originalMessages.size}")
                                        result.dispatchUpdatesTo(this@MessageAdapter)
                                        checkBubbles.invoke()
                                        scrollToBottom.invoke(true)
                                    }
                                    originalMessages.clear()
                                }
                            }
                        } else {
                            if (originalMessages.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    val old = messages.toList()
                                    val new = messages.toMutableList()
                                    new.addAll(
                                        0,
                                        originalMessages.reversed()
                                    )
                                    Collections.sort(new, comparator)
//                                    var index = -1
//                                    val unreadDividerMessage =
//                                        checkAddUnreadMessage(new, viewModel, true) {
//                                            index = it
//                                        }
//                                    if (unreadDividerMessage != null) {
//                                        if (messageUUIDList.contains(unreadDividerMessageUUID)) {
//                                            new.removeAll { it.uuid == unreadDividerMessageUUID }
//                                        } else {
//                                            messageUUIDList.add(unreadDividerMessageUUID)
//                                        }
//                                        new.add(index + 1, unreadDividerMessage)
//                                    }
                                    val result = DiffUtil.calculateDiff(
                                        MessageListComparator(
                                            new,
                                            old,
                                        ), false
                                    )
                                    messages = new
                                    Timber.d("message size 4: ${messages.size}, ${originalMessages.size}")
                                    result.dispatchUpdatesTo(this@MessageAdapter)
                                    checkBubbles.invoke()
                                    scrollToBottom.invoke(true)
                                }
                                originalMessages.clear()
                            }
                        }

                    }
                }
            }
            3 -> {
                if (originalMessages.isNotEmpty()) {
                    val old = messages.toList()
                    val new = messages.toMutableList()
                    new.addAll(
                        0,
                        originalMessages.reversed()
                    )
                    Collections.sort(new, comparator)
//                    var index = -1
//                    val unreadDividerMessage = checkAddUnreadMessage(new, viewModel, true) {
//                        index = it
//                    }
//                    if (unreadDividerMessage != null) {
//                        if (messageUUIDList.contains(unreadDividerMessageUUID)) {
//                            new.removeAll { it.uuid == unreadDividerMessageUUID }
//                        } else {
//                            messageUUIDList.add(unreadDividerMessageUUID)
//                        }
//                        new.add(index + 1, unreadDividerMessage)
//                    }
                    withContext(Dispatchers.Main) {
                        val result = DiffUtil.calculateDiff(
                            MessageListComparator(
                                new,
                                old,
                            ), false
                        )
                        messages = new
                        Timber.d("message size 5: ${messages.size}, ${originalMessages.size}")
                        result.dispatchUpdatesTo(this@MessageAdapter)
                        checkBubbles.invoke()
                        scrollToBottom.invoke(true)
                    }
                    originalMessages.clear()
                }
            }
            4 -> {//更新消息数据内容
                val message = messageEvent.messageEntity ?: return
                if (message.conId == conId) {
                    messages.toList().filterIndexed { index, messageEntityWithUser ->
                        if (messageEntityWithUser.uuid == message.uuid) {
                            message.senderName = messageEntityWithUser.senderName
                            message.senderPortrait = messageEntityWithUser.senderPortrait
                            if (message.portraitIc == null) {
                                message.portraitIc = messageEntityWithUser.portraitIc
                            }
                            withContext(Dispatchers.Main) {
                                messages.remove(messageEntityWithUser)
                                messages.add(index, message)
                                notifyItemChanged(index)
                            }
                            true
                        } else false
                    }
                }
            }
            5 -> {
                val newMessage = messageEvent.messageEntity ?: return
                val targetMessage = messageEvent.targetMessageEntity ?: return
                if (newMessage.conId == conId) {
                    messages.toList().filterIndexed { index, messageEntityWithUser ->
                        if (messageEntityWithUser.uuid == targetMessage.uuid) {
                            newMessage.senderName = messageEntityWithUser.senderName
                            newMessage.senderPortrait = messageEntityWithUser.senderPortrait
                            newMessage.portraitIc = messageEntityWithUser.portraitIc

                            withContext(Dispatchers.Main) {
                                messages.remove(messageEntityWithUser)
                                messages.add(index, newMessage)
                                notifyItemChanged(index)
                            }
                            true
                        } else false
                    }
                }
            }
            6 -> {
                viewModel.getMessagesByConIdDesc(conId, messageEvent.timestampStart){ messageList ->
                    try {
                        val temporaryList = messages.toMutableList()
                        messageList.toList().forEachIndexed { index, it ->
                            if (!messageUUIDList.contains(it.uuid)) {
                                messageUUIDList.add(it.uuid)
                                val insertMessage = checkInsertMessage(
                                    it,
                                    if (index == messageList.size - 1) {
                                        if (messages.isEmpty()) null else messages.first()
                                    } else messageList[index + 1]
                                )
                                if (insertMessage != null) {
                                    temporaryList.add(insertMessage)
                                }
                                temporaryList.add(it)
                            }
                        }

                        Collections.sort(temporaryList, comparator)
//                    var index = -1
//                    val unreadDividerMessage =
//                        checkAddUnreadMessage(temporaryList.toMutableList(), viewModel, true) {
//                            index = it
//                        }
//                val maxMessageTimeToken = messages1.firstOrNull()?.timeToken?:0
//                    if (unreadDividerMessage != null) {
//                        if (messageUUIDList.contains(unreadDividerMessageUUID)) {
//                            originalMessages.removeAll { it.uuid == unreadDividerMessageUUID }
//                            temporaryList.removeAll { it.uuid == unreadDividerMessageUUID }
//                        } else {
//                            messageUUIDList.add(unreadDividerMessageUUID)
//                        }
//                        temporaryList.add(index + 1, unreadDividerMessage)
//                    }

                        withContext(Dispatchers.Main) {
                            val noMessage = messages.isEmpty()
                            val result = DiffUtil.calculateDiff(
                                MessageListComparator(
                                    temporaryList,
                                    messages.toList(),
                                ), false
                            )
                            messages = temporaryList
                            Timber.d("message size 6: ${messages.size}, ${originalMessages.size}")
                            result.dispatchUpdatesTo(this@MessageAdapter)
                            if (noMessage) {
                                scrollToBottom.invoke(true)
                            }
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun checkInsertMessage(
        message: MessageEntity?,
        beforeMessage: MessageEntity?
    ): MessageEntity? {
        if (beforeMessage != null && message != null && message.sendTime - beforeMessage.sendTime > 10 * 60 * 1000 * 10000L) { //10 min
            return message.copy(
                uuid = UUID.randomUUID().toString(),
                type = 8,
                sendTime = message.sendTime - 1,
                content = TimeUtils.formatMessageTime(message.sendTime),
                renderType = 1
            )
        }
        return null
    }

//    private suspend fun checkAddUnreadMessage(
//        newList: MutableList<MessageEntity>,
//        viewModel: MessageViewModel,
//        isLast: Boolean,
//        result: (Int) -> Unit
//    ): MessageEntity? {
//        if (isFirstUnreadDivider) {
//            Timber.d("checkAddUnreadMessage newList.size: ${newList.size}")
//            val messageL = viewModel.getConversationUnreadFirstMessage(
//                conversationsEntity.conversationId,
//                conversationsEntity.lastLeaveTimetoken
//            )
//            val time = messageL?.timeToken ?: 0L
//            Timber.d("checkAddUnreadMessage time: $time last leave time: ${RecentConversation.conversationsEntity.lastLeaveTimetoken}")
//            val index = newList.indexOfLast {
//                it.timeToken > RecentConversation.conversationsEntity.lastLeaveTimetoken
//                        && it.senderId != SessionPreferences.id
//                        && it.read == 0
//                        && it.state == 1
//                        && it.showType != 2
//                        && (it.timeToken <= time || time == 0L)
//                        && (it.type !in listOf(8, 9, 31) || it.renderType > 2)
//            }
//            if (index != -1) {
//                Timber.d("checkAddUnreadMessage index: $index")
//                result.invoke(index)
//                isFirstUnreadDivider = false
//                val unreadMsg = newList[index]
//                unreadMessageUUID = unreadMsg.uuid
//                return unreadMsg.copy(
//                    uuid = unreadDividerMessageUUID,
//                    type = 201,
//                    showType = 1,
//                    timeToken = unreadMsg.timeToken - 1,
//                    renderType = 1
//                )
//            }
//        }
//        return null
//    }

    interface OnMessageActionsListener {
        fun onPortraitClicked(senderId: String, senderRole: Int? = 9)
        fun onResendClicked(messageEntity: MessageEntity)
        fun onPlayVideo(messageEntity: MessageEntity)
        fun onViewImage(messageEntity: MessageEntity)
        fun onReplyViewImage(messageEntity: MessageEntity)
        fun onReplyViewVideo(messageEntity: MessageEntity)
        fun onReplyMessage(messageEntity: MessageEntity)
        fun onMessageLongClicked(
            view: View,
            messageEntity: MessageEntity,
            name: String,
            position: Int = 0
        )

        fun onTextMessageClick(name: String, msg: String)
        fun onUsernameClick(username: String)
        fun onURLMessageClick(url: String)
        fun onClickGift(giftId: String)
        fun onRecharge()
    }
}

class MessageListComparator(
    val newList: List<MessageEntity>,
    val oldList: List<MessageEntity>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uuid == newList[newItemPosition].uuid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].portraitIc == newList[newItemPosition].portraitIc
                && oldList[oldItemPosition].state == newList[newItemPosition].state
                && oldList[oldItemPosition].userRole == newList[newItemPosition].userRole
                && oldList[oldItemPosition].sendStatus == newList[newItemPosition].sendStatus
                && oldList[oldItemPosition].sendTime == newList[newItemPosition].sendTime
                && oldList[oldItemPosition].uuid == newList[newItemPosition].uuid
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}

class TopSmoothScroller constructor(context: Context) : LinearSmoothScroller(context) {
    //    LinearSmoothScroller scroller = new LinearSmoothScroller(getContext()) {
//        @Override
//        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
//            return 2000 / displayMetrics.densityDpi;
//        }
//
//        @Override
//        protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
//            final int dx = calculateDxToMakeVisible(targetView, getHorizontalSnapPreference());
//            final int dy = calculateDyToMakeVisible(targetView, getVerticalSnapPreference());
//            final int distance = (int) Math.sqrt(dx * dx + dy * dy);
//            final int time = calculateTimeForDeceleration(distance);
//            if (time > 0) {
//                action.update(-dx, -dy, time, mLinearInterpolator);
//            }
//        }
//
//        @Override
//        protected int calculateTimeForDeceleration(int dx) {
//            return (int) Math.ceil(calculateTimeForScrolling(dx));
//        }
//    };
//    scroller.setTargetPosition(mRecyclerView.getAdapter().getItemCount() - 1);
//    mRecyclerView.getLayoutManager().startSmoothScroll(scroller);
    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return 250.0f / displayMetrics.densityDpi
    }

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val dx = calculateDxToMakeVisible(targetView, horizontalSnapPreference)
        val dy = calculateDyToMakeVisible(targetView, verticalSnapPreference)
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
        val time = calculateTimeForDeceleration(distance)
        if (time > 0) {
            action.update(-dx, -dy, time, mLinearInterpolator)
        }
    }

    override fun calculateTimeForDeceleration(dx: Int): Int {
        return ceil(calculateTimeForScrolling(dx).toDouble()).toInt()
    }

    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_END
    }

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        return boxStart - viewStart
    }
}