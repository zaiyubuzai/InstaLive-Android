package com.example.instalive.app.home

import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.utils.debounceClick
import com.example.instalive.R
import com.example.instalive.databinding.ItemLiveListLayoutBinding
import com.example.instalive.model.LiveData
import splitties.systemservices.layoutInflater

class LiveAdapter(
    var liveList: List<LiveData>,
    private val onLiveClicked: (liveId: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LiveViewHolder(
            DataBindingUtil.inflate(
                parent.layoutInflater,
                R.layout.item_live_list_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = liveList[position]
        if (holder is LiveViewHolder) {
            holder.binding.title.text =
                holder.itemView.context.getString(R.string.fb_is_live, data.liveOwner.nickname)

            holder.itemView.debounceClick {
                onLiveClicked.invoke(data.id)
            }
        }
    }

    override fun getItemCount(): Int {
        return liveList.size
    }

    class LiveViewHolder(val binding: ItemLiveListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}