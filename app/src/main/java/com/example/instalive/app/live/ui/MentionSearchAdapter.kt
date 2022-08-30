package com.example.instalive.app.live.ui

import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.instalive.R
import com.example.instalive.databinding.ItemMentionEveryoneBinding
import com.example.instalive.databinding.ItemMentionModeratorsBinding
import com.example.instalive.databinding.ItemMentionSearchBinding
import com.venus.dm.model.GroupMember
import splitties.systemservices.layoutInflater
import splitties.views.onClick

class MentionSearchAdapter(
    var list: List<GroupMember>,
    var isShowEveryone: Boolean,
    private val onTopItemClick: (String) -> Unit,
    private val onItemClick: (GroupMember) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_mention_everyone -> {
                MentionAllViewHolder(
                    DataBindingUtil.inflate(
                        parent.layoutInflater,
                        R.layout.item_mention_everyone,
                        parent,
                        false
                    )
                )
            }
            R.layout.item_mention_moderators -> {
                MentionModViewHolder(
                    DataBindingUtil.inflate(
                        parent.layoutInflater,
                        R.layout.item_mention_moderators,
                        parent,
                        false
                    )
                )
            }
            else -> {
                MentionSearchViewHolder(
                    DataBindingUtil.inflate(
                        parent.layoutInflater,
                        R.layout.item_mention_search,
                        parent,
                        false
                    )
                )
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MentionSearchViewHolder) {
            val data = list[if (isShowEveryone)position-2 else position]
//            holder.binding.groupMember = data
//            holder.binding.level = data.level
//            holder.binding.isNewGifter = data.portraitIc == Constants.PORTRAIT_ICON_NEW_GIFTER
            holder.bind()
            holder.itemView.onClick {
                onItemClick.invoke(data)
            }
        } else if (holder is MentionAllViewHolder) {
            holder.itemView.onClick {
                onTopItemClick.invoke(holder.itemView.context.getString(R.string.fb_everyone_))
            }
        } else if (holder is MentionModViewHolder) {
            holder.itemView.onClick {
                onTopItemClick.invoke(holder.itemView.context.getString(R.string.fb_moderators_))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowEveryone && position == 0) {
            R.layout.item_mention_everyone
        } else if (isShowEveryone && position == 1) {
            R.layout.item_mention_moderators
        } else {
            R.layout.item_mention_search
        }
    }

    override fun getItemCount(): Int {
        return list.size + if (isShowEveryone) 2 else 0
    }

    class MentionSearchViewHolder(val binding: ItemMentionSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.executePendingBindings()
        }
    }

    class MentionAllViewHolder(val binding: ItemMentionEveryoneBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.executePendingBindings()
        }
    }

    class MentionModViewHolder(val binding: ItemMentionModeratorsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.executePendingBindings()
        }
    }
}