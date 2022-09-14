package com.example.instalive.app.conversation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.instalive.R
import com.example.instalive.databinding.ItemConversationBinding
import com.venus.dm.db.entity.ConversationsEntity
import kotlinx.android.synthetic.main.item_conversation.view.*
import splitties.dimensions.dp
import splitties.systemservices.layoutInflater
import splitties.views.onClick
import splitties.views.onLongClick
import timber.log.Timber

@ExperimentalStdlibApi
class ConversationListAdapter(
    val context: Context,
    var conversationList: List<ConversationsEntity>,
    private val onConversationClickListener: OnConversationClickListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var oldPosition = ""
    var currentID = ""

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ConversationViewHolder -> {
                val conversationEntity = conversationList[position]
                Timber.d("ispin:${conversationEntity.isPin}")
                holder.binding.conversationEntity = conversationEntity
                holder.binding.executePendingBindings()
                val options = RequestOptions.bitmapTransform(RoundedCorners(context.dp(16)))
                Glide.with(context).load(conversationEntity.recipientPortrait) //图片地址
                    .apply(options)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.binding.avatar)
                if (currentID == conversationEntity.conversationId) {
                    holder.binding.constraint.setBackgroundColor(context.resources.getColor(R.color.purple_100))
                } else {
                    holder.binding.constraint.setBackgroundColor(context.resources.getColor(R.color.transparent))
                }

                holder.itemView.name.text =
                    if (conversationEntity.type == 1) "@ ${conversationEntity.recipientName}" else conversationEntity.recipientName

                holder.itemView.onClick {
                    conversationEntity.let {
                        onConversationClickListener.onConversationClicked(it, position)
                    }
                }
                holder.itemView.onLongClick {
                    val con = holder.binding.conversationEntity
                    if (con != null) {
                        onConversationClickListener.onConversationLongClicked(con)
                    }
                }
                holder.itemView.avatar.onClick {
                    conversationEntity.let {
                        onConversationClickListener.onAvatarClicked(it, position)
                    }
                }
            }
            is BottomRefreshViewHolder -> {
                holder.itemView.onClick {
                    onConversationClickListener.onCreateGroup()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_CONVERSATION -> ConversationViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_conversation,
                    parent,
                    false
                )
            )

            else -> ConversationViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_conversation,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < conversationList.size -> ITEM_TYPE_CONVERSATION
            else -> ITEM_TYPE_CONVERSATION
        }
    }

    class ConversationViewHolder(var binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root)

    class BottomRefreshViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnConversationClickListener {
        fun onConversationClicked(conversationsEntity: ConversationsEntity, position: Int)
        fun onConversationLongClicked(conversationsEntity: ConversationsEntity)
        fun onAvatarClicked(conversationsEntity: ConversationsEntity, position: Int)
        fun onCreateGroup()
    }

    companion object {
        const val ITEM_TYPE_CONVERSATION = 0
    }
}

class ConversationListComparator(
    val newList: List<ConversationsEntity>,
    val oldList: List<ConversationsEntity>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].conversationId == newList[newItemPosition].conversationId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].conversationId == newList[newItemPosition].conversationId
                && oldList[oldItemPosition].isPin == newList[newItemPosition].isPin
                && oldList[oldItemPosition].unreadCount == newList[newItemPosition].unreadCount
                && oldList[oldItemPosition].lastMsgContent == newList[newItemPosition].lastMsgContent
                && oldList[oldItemPosition].lastRead == newList[newItemPosition].lastRead
                && oldList[oldItemPosition].beingAt == newList[newItemPosition].beingAt
                && oldList[oldItemPosition].living == newList[newItemPosition].living
                && oldList[oldItemPosition].recipientPortrait == newList[newItemPosition].recipientPortrait
                && oldList[oldItemPosition].mute == newList[newItemPosition].mute
    }
}