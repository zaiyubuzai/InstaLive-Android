package com.example.instalive.app.live.ui

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.utils.marsToast
import com.example.baselibrary.views.BaseBottomPopup
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.ui.FooterLoadStateAdapter
import com.example.instalive.databinding.ItemGoLiveWithFeedBinding
import com.example.instalive.model.LiveActivityEvent
import com.example.instalive.model.LiveUserInfo
import com.example.instalive.model.LiveViewerData
import com.example.instalive.model.LiveWithInviteEvent
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.dialog_go_live_with.view.*
import kotlinx.android.synthetic.main.item_go_live_with_feed.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import splitties.systemservices.layoutInflater
import splitties.views.onClick

class GoLiveWithDialog(
    context: Context,
    val liveId: String,
    val isMicrophone: Boolean,
) :
    BaseBottomPopup<GoLiveWithViewModel>(context) {

    private lateinit var listAdapter: GoLiveWithListAdapter

    override fun initData() {
        viewModel.initPagerFlow(liveId)
        listAdapter =
            GoLiveWithListAdapter(ViewerListComparator(), null, isMicrophone) {
                btnSend.isEnabled = listAdapter.selectedId != null
                listAdapter.notifyDataSetChanged()
            }
        lifecycleScope.launch {
            viewModel.viewerFlow.collectLatest {
                listAdapter.submitData(it)
            }
        }
        viewerList.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = listAdapter.withLoadStateFooter(FooterLoadStateAdapter())
        }
        lifecycleScope.launch {
            listAdapter.loadStateFlow.collectLatest {
                loadingAnimViewer.isVisible = it.refresh is LoadState.Loading
                emptyWrapper.isVisible = it.source.refresh is LoadState.NotLoading &&
                        it.append.endOfPaginationReached &&
                        listAdapter.itemCount < 1
                if (listAdapter.itemCount < 20) {
                    viewerList.scrollToPosition(0)
                }
                btnSend.isVisible = listAdapter.itemCount != 0
            }
        }

        LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE).observeForever {
            if (it is LiveActivityEvent) {
                val membersCount = it.membersNum ?: 0
                if (membersCount > 0 && listAdapter.itemCount == 0) {
                    //如果进人了，就刷新一下子
                    listAdapter.refresh()
                }
            }
        }

        close.onClick {
            dismiss()
        }

        btnSend.onClick {
//            if (isMicrophone) {
//                context?.alertDialog {
//                    title = context.getString(R.string.title_already_live_with, userInfo?.userName ?: "")
//                    messageResource = R.string.desc_already_live_with
//                    okButton()
//                }?.show()
//            } else {
                if (listAdapter.selectedId != null) {
//                    MarsEventLogger.logFirebaseEvent("send_live_request", "live_view")
                    viewModel.goLiveWith(listAdapter.selectedId ?: "")
                }
//            }
        }

        viewModel.inviteData.observeForever {
//            LiveEventBus.get(Constants.EVENT_BUS_KEY_LIVE).post(it)
            dismiss()
        }
        viewModel.errorMessageLiveData.observeForever {
            marsToast(it)
        }
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_go_live_with

    override fun initViewModel(): GoLiveWithViewModel =
        GoLiveWithViewModel()

    class GoLiveWithListAdapter(
        diffCallback: ViewerListComparator,
        var selectedId: String? = null,
        val isMicrophone: Boolean,
        val onSelect: () -> Unit,
    ) : PagingDataAdapter<LiveViewerData, ViewerListViewHolder>(diffCallback) {
        override fun onBindViewHolder(
            holder: ViewerListViewHolder,
            position: Int,
        ) {
            val data = getItem(position) ?: return
            holder.binding.userData = data
            holder.binding.level = data.level
            holder.binding.executePendingBindings()
            holder.itemView.checkbox.isChecked = data.userId == selectedId

            holder.itemView.checkbox.onClick {
                selectedId = if (holder.itemView.checkbox.isChecked) {
                    data.userId
                } else {
                    null
                }
                onSelect()
            }
            holder.itemView.onClick {
                if (holder.itemView.checkbox.isChecked) {
                    holder.itemView.checkbox.isChecked = false
                    selectedId = null
                } else {
                    holder.itemView.checkbox.isChecked = true
                    selectedId = data.userId
                }
                onSelect()
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ViewerListViewHolder {
            return ViewerListViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_go_live_with_feed,
                    parent,
                    false
                )
            )
        }
    }

    class ViewerListViewHolder(val binding: ItemGoLiveWithFeedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onDismiss() {
        viewModel.inviteData = MutableLiveData<LiveWithInviteEvent>()
        viewModel.errorMessageLiveData = MutableLiveData()
    }
}