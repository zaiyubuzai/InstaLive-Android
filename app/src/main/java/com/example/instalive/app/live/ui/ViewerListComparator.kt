package com.example.instalive.app.live.ui

import androidx.recyclerview.widget.DiffUtil
import com.example.instalive.model.LiveViewerData

class ViewerListComparator : DiffUtil.ItemCallback<LiveViewerData>() {
    override fun areItemsTheSame(
        oldItem: LiveViewerData,
        newItem: LiveViewerData,
    ): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(
        oldItem: LiveViewerData,
        newItem: LiveViewerData,
    ): Boolean {
        return oldItem.userId == newItem.userId
    }
}