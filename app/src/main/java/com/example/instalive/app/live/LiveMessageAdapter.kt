package com.example.instalive.app.live

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baselibrary.utils.HashTagHelper
import com.example.baselibrary.utils.Utils
import com.example.baselibrary.utils.debounceClick
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.databinding.*
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.model.InstaLiveStringTemplate
import com.example.instalive.utils.DeeplinkHelper
import com.example.instalive.utils.SysUtils
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import kotlinx.coroutines.*
import org.json.JSONObject
import splitties.systemservices.layoutInflater
import splitties.views.onClick
import timber.log.Timber

@ExperimentalStdlibApi
class LiveMessageAdapter(
    @Volatile
    var messages: MutableList<MessageEntity>,
    var originalMessages: MutableList<MessageEntity>,
    var messageUUIDList: MutableList<String>,
    var isGroupChat: Boolean,
    val onMessageActionsListener: OnLiveMessageActionsListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "UseCompatLoadingForDrawables")
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
                holder.binding.isGroupChat = isGroupChat
                holder.binding.level = payload?.level ?: -1
                holder.binding.executePendingBindings()

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.senderId,
                        item.userRole ?: 9
                    )
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }

                val content = item.content
                if (content != null && content.isNotEmpty()) {
                    Utils.buildTextUrlSpan(content, holder.binding.messageContent) {
                        onMessageActionsListener.onURLMessageClick(it)
                    }
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
                        }

                        override fun onUsernameClicked(username: String?) {
                            username?.let { onMessageActionsListener.onUsernameClick(it) }
                        }
                    })
                helper.handle(holder.binding.messageContent)
            }
            is MessageVideoViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)
                holder.binding.messageEntity = item
                holder.binding.level = payload?.level ?: -1
                holder.binding.isGroupChat = isGroupChat
                holder.binding.executePendingBindings()

                Utils.changeImageViewWH(
                    holder.binding.videoCover,
                    payload?.width ?: 100,
                    payload?.height ?: 100
                )

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.senderId,
                        item.userRole ?: 9
                    )
                }
                holder.binding.messageContainer.onClick {
                    onMessageActionsListener.onPlayVideo(item)
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }
            }
//            is MessageGifViewHolder -> {
//                val payload = MessageEntity.Payload.fromJson(item.payload)
//                item.localResPath = payload?.url
//                val height = payload?.height ?: 100
//                val width = payload?.width ?: 100
//                Utils.changeImageViewWH(holder.binding.gifView, width, height)
//                Utils.changeImageViewWH(holder.binding.gifImageView, width, height)
//                var isMedia = false
//                gifList.toList().forEach {
//                    if (it.images.original?.gifUrl == payload?.url && payload?.url.isNeitherNullNorEmpty()) {
//                        if (holder.binding.gifView.tag != payload?.url) {
//                            holder.binding.gifView.isVisible = true
//                            holder.binding.gifImageView.isVisible = false
//                            holder.binding.gifView.setMedia(
//                                it,
//                                null,
//                                holder.itemView.context?.getDrawable(R.drawable.common_video_feed_placeholder)
//                            )
//                            holder.binding.gifView.tag = payload?.url
//                        }
//                        isMedia = true
//                    }
//                }
//                holder.binding.messageEntity = item
//                holder.binding.level = payload?.level ?: -1
//                holder.binding.isGroupChat = isGroupChat
//                val performerStr = RecentConversation.conversationsEntity.performer
//                holder.binding.isPerformer =
//                    if (performerStr == null) false else PerformerData.fromJson(performerStr).userInfo.userId == item.senderId
//                holder.binding.isNewGifter =
//                    item.portraitIc == Constants.PORTRAIT_ICON_NEW_GIFTER
//                holder.binding.executePendingBindings()
//
//                if (!isMedia) {
//                    holder.binding.gifView.isVisible = false
//                    holder.binding.gifImageView.isVisible = true
//                }
//
//                holder.binding.avatar.onClick {
//                    onMessageActionsListener.onPortraitClicked(
//                        item.senderId,
//                        item.userRole ?: 9
//                    )
//                }
//                holder.binding.gifView.onClick {
//                    onMessageActionsListener.onViewGif(payload?.url)
//                }
//                holder.binding.gifView.onLongClick {}
//                holder.binding.gifImageView.onClick {
//                    onMessageActionsListener.onViewGif(payload?.url)
//                }
//                holder.binding.icSentError.onClick {
//                    onMessageActionsListener.onResendClicked(item.toMessageEntity())
//                }
//            }
            is MessageImageViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)
                holder.binding.messageEntity = item
                holder.binding.isGroupChat = isGroupChat
                holder.binding.level = payload?.level ?: -1
                holder.binding.executePendingBindings()

                val width = payload?.width ?: 0
                val height = payload?.height ?: 0
                Utils.changeImageViewWH(holder.binding.imageCover, width, height)

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.senderId,
                        item.userRole ?: 9
                    )
                }
                holder.binding.imageCover.onClick {
                    onMessageActionsListener.onViewImage(item)
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }
            }
            is MessageGiftPromptViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)
//                val performerStr = RecentConversation.conversationsEntity.performer

                holder.binding.isGroup = isGroupChat
                payload?.let {
//                    holder.binding.isPerformer =
//                        if (performerStr == null) false else PerformerData.fromJson(performerStr).userInfo.userId == it.userInfo?.userId
                    holder.binding.contentStr =
                        holder.itemView.context.getString(R.string.fb_send_a_gift, it.giftName)
//                    holder.binding.isNewGifter =
//                        it.userInfo?.portraitIc == Constants.PORTRAIT_ICON_NEW_GIFTER
                    holder.binding.highlight = it.highlight

//                        if (item.messageEntity.senderId.isNullOrEmpty()){
                    if (item.senderPortrait.isNullOrEmpty()) item.senderPortrait =
                        payload.userInfo?.portrait ?: ""
                    if (item.senderName.isNullOrEmpty()) item.senderName = payload.userInfo?.nickname ?: ""
                    item.userRole = payload.userInfo?.userRole ?: 9
//                        }
                    holder.binding.messageEntity = item
                    holder.binding.level = it.level
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
                        payload?.userInfo?.userRole ?: 9
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
//                val performerStr = RecentConversation.conversationsEntity.performer

                holder.binding.isGroup = isGroupChat
                payload?.let {
//                    holder.binding.isPerformer =
//                        if (performerStr == null) false else PerformerData.fromJson(performerStr).userInfo.userId == it.userInfo?.userId
                    holder.binding.contentStr = it.content
//                    holder.binding.isNewGifter =
//                        it.userInfo?.portraitIc == Constants.PORTRAIT_ICON_NEW_GIFTER
                    holder.binding.highlight = it.highlight
//                        if (item.senderId.isNullOrEmpty()){
                    if (item.senderPortrait.isNullOrEmpty()) item.senderPortrait =
                        payload.userInfo?.portrait ?: ""
                    if (item.senderName.isNullOrEmpty()) item.senderName = payload.userInfo?.nickname ?: ""
                    item.userRole = payload.userInfo?.userRole ?: 9
//                        }
                    holder.binding.messageEntity = item
                    holder.binding.level = it.level
                }
//                holder.binding.messageIcon.imageResource =
//                    if (isGroupChat) R.drawable.ic_live_message else R.drawable.icon_message_private_video_call
                holder.binding.executePendingBindings()

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        payload?.userInfo?.userId ?: "",
                        payload?.userInfo?.userRole ?: 9
                    )
                }
            }
            is MessageReplyViewHolder -> {
                holder.binding.messageEntity = item
                holder.binding.isGroupChat = isGroupChat
                try {
                    val payload = MessageEntity.Payload.fromJson(item.payload)
                    holder.binding.level = payload?.level ?: -1
                    holder.binding.executePendingBindings()
                    var replyMsg: MessageEntity? = null

                    payload?.targetMessage?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            Timber.d("ReplyView 2 ${System.currentTimeMillis()}")
                            val replyMessage = InstaLiveDBProvider.db.directMessagingDao()
                                .getMessagedByUuid(it.uuid, SessionPreferences.id)
                            withContext(Dispatchers.Main) {
                                holder.binding.replyContent.text =
                                    if (replyMessage !=null && replyMessage.state != 2) {
                                        if (it.type == 3) {
                                            "${it.senderName}: [Image]"
                                        } else if (it.type == 4) {
                                            "${it.senderName}: [Video]"
                                        } else {
                                            "${it.senderName}: ${it.payload.content}"
                                        }
                                    } else {
                                        "${it.senderName}: ${holder.itemView.context.getString(R.string.fb_reply_message_deleted_content)}"
                                    }
                                Timber.d("ReplyView 3 ${System.currentTimeMillis()}")
                            }
                        }
                    }
                    holder.binding.replyContent.onClick {
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
                        item.userRole ?: 9
                    )
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }
                val content = item.content
                if (content != null && content.isNotEmpty()) {
                    Utils.buildTextUrlSpan(content, holder.binding.messageContent) {
                        onMessageActionsListener.onURLMessageClick(it)
                    }
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
            is MessageNotSupportViewHolder -> {
                val payload = MessageEntity.Payload.fromJson(item.payload)
                holder.binding.messageEntity = item
                holder.binding.messageContent.text =
                    InstaLiveStringTemplate.template?.unsupportedMsgTypeTips

                holder.binding.isGroup = isGroupChat
                holder.binding.level = payload?.level ?: -1
                holder.binding.executePendingBindings()
                holder.itemView.onClick {
                    SysUtils.showPlayReview(holder.itemView.context as Activity)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_live_message_me_layout -> MessageMeViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_me_layout,
                    parent,
                    false
                )
            )
            R.layout.item_live_message_prompt_layout -> MessagePromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_live_message_gift_prompt_layout -> MessageGiftPromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_gift_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_live_message_live_prompt_layout -> MessageLivePromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_live_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_live_message_important_prompt_layout -> MessageImportantPromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_important_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_live_message_recharge_prompt_layout -> MessageRechargePromptViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_recharge_prompt_layout,
                    parent,
                    false
                )
            )
            R.layout.item_live_message_video_me_layout -> MessageVideoViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_video_me_layout,
                    parent,
                    false
                )
            )
//            R.layout.item_live_message_gif_layout -> {
//                MessageGifViewHolder(
//                    DataBindingUtil.inflate(
//                        parent.layoutInflater,
//                        R.layout.item_live_message_gif_layout,
//                        parent,
//                        false
//                    )
//                )
//            }
            R.layout.item_live_message_image_me_layout -> MessageImageViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_image_me_layout,
                    parent,
                    false
                )
            )
            R.layout.item_live_message_reply_layout -> MessageReplyViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_reply_layout,
                    parent,
                    false
                )
            )
            else -> MessageNotSupportViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_message_not_support_layout,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            1 -> R.layout.item_live_message_me_layout
            3 -> R.layout.item_live_message_image_me_layout
            4 -> R.layout.item_live_message_video_me_layout
            8, 9, 10 -> {
                when (messages[position].renderType) {
                    3 -> {
                        R.layout.item_live_message_gift_prompt_layout
                    }
                    5 -> {
                        R.layout.item_live_message_important_prompt_layout
                    }
                    6 -> {
                        R.layout.item_live_message_recharge_prompt_layout
                    }
                    else -> {
                        R.layout.item_live_message_prompt_layout
                    }
                }
            }
            31 -> R.layout.item_live_message_prompt_layout
            32 -> R.layout.item_live_message_reply_layout
            33 -> {
                when (messages[position].renderType) {
                    4 -> {
                        R.layout.item_live_message_live_prompt_layout
                    }
                    else -> {
                        R.layout.item_live_message_prompt_layout
                    }
                }
            }
            34 -> R.layout.item_live_message_gif_layout
            101 -> R.layout.item_live_message_prompt_layout
            else -> R.layout.item_live_message_not_support_layout
        }
    }

    interface OnLiveMessageActionsListener {
        fun onPortraitClicked(senderId: String, senderRole: Int)
        fun onResendClicked(messageEntity: MessageEntity)
        fun onPlayVideo(messageEntity: MessageEntity)
        fun onViewImage(messageEntity: MessageEntity)
        fun onReplyViewImage(messageEntity: MessageEntity)
        fun onReplyViewVideo(messageEntity: MessageEntity)
        fun onReplyMessage(messageEntity: MessageEntity)
        fun onUsernameClick(username: String)
        fun onURLMessageClick(url: String)
        fun onClickGift(giftId: String)
        fun onViewGif(url: String?)
        fun onRecharge()
    }

    class MessageMeViewHolder(val binding: ItemLiveMessageMeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageReplyViewHolder(val binding: ItemLiveMessageReplyLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessagePromptViewHolder(val binding: ItemLiveMessagePromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageVideoViewHolder(val binding: ItemLiveMessageVideoMeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

//    class MessageGifViewHolder(val binding: ItemLiveMessageGifLayoutBinding) :
//        RecyclerView.ViewHolder(binding.root)

    class MessageImageViewHolder(val binding: ItemLiveMessageImageMeLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageNotSupportViewHolder(val binding: ItemLiveMessageNotSupportLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageGiftPromptViewHolder(val binding: ItemLiveMessageGiftPromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageLivePromptViewHolder(val binding: ItemLiveMessageLivePromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageImportantPromptViewHolder(val binding: ItemLiveMessageImportantPromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class MessageRechargePromptViewHolder(val binding: ItemLiveMessageRechargePromptLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return messages.size
    }

}