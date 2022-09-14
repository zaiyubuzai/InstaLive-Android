package com.example.instalive.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.instalive.R
import kotlinx.android.synthetic.main.layout_discover_load_state.view.*

class FooterLoadStateAdapter: LoadStateAdapter<FooterLoadStateAdapter.FooterLoadStateViewHolder>() {

    var isShowNoMore = false

    override fun onBindViewHolder(holder: FooterLoadStateViewHolder, loadState: LoadState) {
        holder.itemView.loadingAnim.isVisible = loadState is LoadState.Loading
        holder.itemView.noMore.isVisible = isShowNoMore
    }

    override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        return loadState is LoadState.Loading || loadState is LoadState.Error || loadState is LoadState.NotLoading
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): FooterLoadStateViewHolder {
        return FooterLoadStateViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_discover_load_state, parent, false)
        )
    }

    class FooterLoadStateViewHolder(view: View) : RecyclerView.ViewHolder(view)
}

