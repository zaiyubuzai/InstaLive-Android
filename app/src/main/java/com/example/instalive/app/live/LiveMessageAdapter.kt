package com.example.instalive.app.live

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.utils.HashTagHelper
import com.example.baselibrary.utils.Utils
import com.example.instalive.R
import com.example.instalive.databinding.*
import com.example.instalive.model.InstaLiveStringTemplate
import com.example.instalive.model.LiveCommentEvent
import com.example.instalive.utils.SysUtils
import splitties.systemservices.layoutInflater
import splitties.views.onClick

@ExperimentalStdlibApi
class LiveMessageAdapter(
    @Volatile
    var messages: MutableList<LiveCommentEvent>,
    var originalMessages: MutableList<LiveCommentEvent>,
    var messageUUIDList: MutableList<String>,
    var isGroupChat: Boolean,
    val onMessageActionsListener: OnLiveMessageActionsListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = messages[position]
        when (holder) {
            is MessageMeViewHolder -> {
                holder.binding.liveCommentEvent = item
                holder.binding.level = -1
                holder.binding.executePendingBindings()

                holder.binding.avatar.onClick {
                    onMessageActionsListener.onPortraitClicked(
                        item.userInfo.userId
                    )
                }
                holder.binding.icSentError.onClick {
                    onMessageActionsListener.onResendClicked(item)
                }

                val content = item.content
                if (content.isNotEmpty()) {
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
            is MessageNotSupportViewHolder -> {
                holder.binding.liveCommentEvent = item
                holder.binding.messageContent.text =
                    InstaLiveStringTemplate.template?.unsupportedMsgTypeTips

                holder.binding.isGroup = isGroupChat
                holder.binding.level = -1
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
            0 -> R.layout.item_live_message_me_layout
//            3 -> R.layout.item_live_message_image_me_layout
//            4 -> R.layout.item_live_message_video_me_layout
//            8, 9, 10 -> {
//                when (messages[position].renderType) {
//                    3 -> {
//                        R.layout.item_live_message_gift_prompt_layout
//                    }
//                    5 -> {
//                        R.layout.item_live_message_important_prompt_layout
//                    }
//                    6 -> {
//                        R.layout.item_live_message_recharge_prompt_layout
//                    }
//                    else -> {
//                        R.layout.item_live_message_prompt_layout
//                    }
//                }
//            }
//            31 -> R.layout.item_live_message_prompt_layout
//            32 -> R.layout.item_live_message_reply_layout
//            33 -> {
//                when (messages[position].renderType) {
//                    4 -> {
//                        R.layout.item_live_message_live_prompt_layout
//                    }
//                    else -> {
//                        R.layout.item_live_message_prompt_layout
//                    }
//                }
//            }
//            34 -> R.layout.item_live_message_gif_layout
//            101 -> R.layout.item_live_message_prompt_layout
            else -> R.layout.item_live_message_not_support_layout
        }
    }

    interface OnLiveMessageActionsListener {
        fun onPortraitClicked(senderId: String)
        fun onResendClicked(commentEvent: LiveCommentEvent)
        fun onUsernameClick(username: String)
        fun onURLMessageClick(url: String)
        fun onClickGift(giftId: String)
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