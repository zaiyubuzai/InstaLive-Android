package com.example.instalive.app.live.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.baseToast
import com.example.baselibrary.views.BaseBottomPopup
import com.example.instalive.model.LiveUserInfo
import com.example.instalive.model.LiveViewerData
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.dialog_live_viewer_list.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import splitties.alertdialog.appcompat.*
import splitties.systemservices.layoutInflater
import splitties.views.onClick
import timber.log.Timber
import com.example.instalive.R
import com.example.instalive.app.Constants.EVENT_BUS_KEY_REMOVE_FROM_LIVE
import com.example.instalive.app.ui.FooterLoadStateAdapter
import com.example.instalive.databinding.ItemLiveViewerDividerBinding
import com.example.instalive.databinding.ItemLiveViewerFeedBinding
import com.venus.dm.model.UserData
import splitties.dimensions.dp

class LiveViewersDialog(context: Context,
                        val liveId: String,
                        val isMicrophone: Boolean = false,
                        val isPaidLive: Boolean,
                        val isShowDiamonds: Int,
                        val currentLiveWithUser: LiveUserInfo? = null,) :
    BaseBottomPopup<LiveViewerViewModel>(context) {

//    private val firstDivider = DividerAdapter(false, context.getString(R.string.fb_views_moderator))
    private val secondDivider = DividerAdapter(true, "")

    private var moderatorAdapter = ModeratorListAdapter(listOf(), isShowDiamonds, { viewer ->
//        val index = listAdapter.snapshot().indexOfFirst {
//            it?.userInfo?.userId == viewer.userInfo.userId
//        }
//        if (index >= 0) {
//            val samePersonInModerators = listAdapter.peek(index)
//            if (samePersonInModerators?.canFollow() == true) {
//                samePersonInModerators.setFollowed()
//            } else {
//                samePersonInModerators?.setUnfollowd()
//            }
//            listAdapter.notifyItemChanged(index)
//        }
//        viewModel.follow(viewer)
    }, {
        showPersonBottomDialog(it)
    })

    private lateinit var listAdapter: ViewerListAdapter
    private lateinit var footerAdapter: FooterLoadStateAdapter
    var isProfileLoading = false

    val observer = Observer<UserData?> {
        val person = it ?: return@Observer
//        val c = context
//        LiveEventBus.get(Constants.EVENT_BUS_KEY_USER).post(person)
//        dismiss()
    }

    private val unlockedNumberObserver = Observer<String> {
        Timber.d("unlockedNumber $it")
//        if (it != null) {
//            firstDivider.show = it.isNotEmpty()
//            firstDivider.notifyDataSetChanged()
//            moderatorAdapter.list = it
//            moderatorAdapter.notifyDataSetChanged()
//        } else {
//            moderatorAdapter.list = listOf()
//            moderatorAdapter.notifyDataSetChanged()
//            firstDivider.show = false
//            firstDivider.notifyDataSetChanged()
//        }
        secondDivider.show = isPaidLive
//        val count = VenusNumberFormatter.format(it.toLong())
        secondDivider.t = context.getString(R.string.fb_unlocked) + ": $it"
        secondDivider.notifyDataSetChanged()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun initData() {
        viewModel.initPagerFlow(liveId)
        listAdapter = ViewerListAdapter(ViewerListComparator(), isShowDiamonds, { viewer ->
//            val index = moderatorAdapter.list.indexOfFirst {
//                it.userInfo.userId == viewer.userInfo.userId
//            }
//            if (index >= 0) {
//                val samePersonInModerators = moderatorAdapter.list[index]
//                if (samePersonInModerators.canFollow()) {
//                    samePersonInModerators.setFollowed()
//                } else {
//                    samePersonInModerators.setUnfollowd()
//                }
//                moderatorAdapter.notifyItemChanged(index)
//            }
//            viewModel.follow(viewer)
        }, {
            showPersonBottomDialog(it)
        })
        footerAdapter = FooterLoadStateAdapter()
        viewModel.personalLiveData.observe(this, observer)
        viewModel.unlockedNumberLiveData.observe(this, unlockedNumberObserver)

        viewerList.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = ConcatAdapter(
                moderatorAdapter,
                secondDivider,
                listAdapter.withLoadStateFooter(
                footerAdapter
            ))
        }
        viewModel.bindPaging(listAdapter)
//        lifecycleScope.launch {
//            viewModel.viewerFlow.collectLatest {
//                listAdapter.submitData(it)
//            }
//        }
        lifecycleScope.launch {
            listAdapter.loadStateFlow.collectLatest {
                loadingAnimViewer.isVisible = it.refresh is LoadState.Loading
                txtEmpty.isVisible = it.source.refresh is LoadState.NotLoading &&
                        it.append.endOfPaginationReached &&
                        listAdapter.itemCount < 1
                if (listAdapter.itemCount < 20) {
                    viewerList.scrollToPosition(0)
                }
//                secondDivider.show = listAdapter.itemCount > 0
//                secondDivider.notifyDataSetChanged()
                footerAdapter.isShowNoMore = listAdapter.itemCount >= 200
                footerAdapter.notifyDataSetChanged()
            }
        }

        close.onClick {
            dismiss()
        }

//        btnShare.onClick {
//            onShare()
//            dismiss()
//        }

        viewModel.errorInfo.observeForever {
            if (it.first == 6208) {
                baseToast(it.second)
            }
        }

        LiveEventBus.get(EVENT_BUS_KEY_REMOVE_FROM_LIVE).observeForever {
            if (it is String) {
                viewModel.remove(it)
            }
        }
    }

    private fun showPersonBottomDialog(info: LiveViewerData, isViewingHost: Boolean = false) {
        if (isProfileLoading) {
            return
        }
        isProfileLoading = true
        liveProfileLoadingView.isVisible = true
        viewModel.getPersonalData(info.userInfo.userId, "", 0) {
            liveProfileLoadingView?.isVisible = it == StatusEvent.LOADING
            isProfileLoading = it == StatusEvent.LOADING
        }
    }

    override fun onDismiss() {
        viewModel.personalLiveData.removeObserver(observer)
        viewModel.personalLiveData.postValue(null)
        viewModel.unlockedNumberLiveData.removeObserver(unlockedNumberObserver)
        super.onDismiss()
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_live_viewer_list

    override fun initViewModel(): LiveViewerViewModel {
        return LiveViewerViewModel()
    }

    class ModeratorListAdapter(
        var list: List<LiveViewerData>,
        val isShowDiamonds: Int,
        private val onFollow: (viewerData: LiveViewerData) -> Unit,
        private val onClick: (viewerData: LiveViewerData) -> Unit,
    ) : RecyclerView.Adapter<ViewerListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewerListViewHolder =
            ViewerListViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_viewer_feed,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: ViewerListViewHolder, position: Int) {
            val data = list[position]
            holder.binding.userData = data
            holder.binding.ranking = -1
            holder.binding.hasTipped = false
            holder.binding.level = -1
            holder.binding.isShowDiamonds = isShowDiamonds
            holder.binding.executePendingBindings()

//            holder.itemView.btnFollow.onClick {
//                if (!SessionHelper.isLoggedIn()) {
//                    return@onClick
//                }
//                onFollow.invoke(data)
//                if (data.canFollow()) {
//                    data.setFollowed()
//                } else {
//                    data.setUnfollowd()
//                }
//                holder.itemView.btnFollow.setFollowText(data.relationship)
//                data.relationship?.let { it1 -> holder.itemView.btnFollow.setFollowColorLight(it1) }
//            }
            holder.itemView.onClick {
                onClick(data)
            }
        }

        override fun getItemCount(): Int = list.count()
    }

    class ViewerListAdapter(
        diffCallback: ViewerListComparator,
        val isShowDiamonds: Int,
        private val onFollow: (viewerData: LiveViewerData) -> Unit,
        private val onClick: (viewerData: LiveViewerData) -> Unit,
    ) : PagingDataAdapter<LiveViewerData, ViewerListViewHolder>(diffCallback) {
        override fun onBindViewHolder(holder: ViewerListViewHolder, position: Int) {
            val data = getItem(position) ?: return

            holder.binding.userData = data
            holder.binding.ranking = position
            holder.binding.hasTipped = (data.diamonds ?: 0) > 0
            holder.binding.level = -1
            holder.binding.isShowDiamonds = isShowDiamonds
            holder.binding.executePendingBindings()

            val options = RequestOptions.bitmapTransform(RoundedCorners(holder.itemView.context.dp(16)))
            Glide.with(holder.itemView.context)
                .load(data.userInfo.portrait)
                .apply(options)
                .skipMemoryCache(false)
                .placeholder(R.mipmap.ic_default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.avatar)

//            holder.itemView.btnFollow.onClick {
//                if (!SessionHelper.isLoggedIn()) {
//                    return@onClick
//                }
//                onFollow.invoke(data)
//                if (data.canFollow()) {
//                    data.setFollowed()
//                } else {
//                    data.setUnfollowd()
//                }
//                holder.itemView.btnFollow.setFollowText(data.relationship)
//                data.relationship?.let { it1 -> holder.itemView.btnFollow.setFollowColorLight(it1) }
//            }
            holder.itemView.onClick {
                onClick(data)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewerListViewHolder {
            return ViewerListViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_live_viewer_feed,
                    parent,
                    false
                )
            )
        }
    }

    class ViewerListComparator : DiffUtil.ItemCallback<LiveViewerData>() {
        override fun areItemsTheSame(
            oldItem: LiveViewerData,
            newItem: LiveViewerData,
        ): Boolean {
            return oldItem.userInfo.userId == newItem.userInfo.userId
        }

        override fun areContentsTheSame(
            oldItem: LiveViewerData,
            newItem: LiveViewerData,
        ): Boolean {
            return oldItem.userInfo.userId == newItem.userInfo.userId
        }
    }

    class ViewerListViewHolder(val binding: ItemLiveViewerFeedBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DividerAdapter(var show: Boolean, var t: String) : RecyclerView.Adapter<DividerViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): DividerViewHolder =
            DividerViewHolder(DataBindingUtil.inflate(parent.layoutInflater,
                R.layout.item_live_viewer_divider,
                parent,
                false))

        override fun onBindViewHolder(
            holder: DividerViewHolder,
            position: Int,
        ) {
            if (show){

                holder.binding.text.text = t
            } else {
                holder.binding.text.text = ""
            }
            holder.binding.text.isVisible = show
            holder.itemView.isVisible = show
        }

        override fun getItemCount(): Int = 1
    }

    class DividerViewHolder(val binding: ItemLiveViewerDividerBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FooterLoadStateViewHolder(view: View) : RecyclerView.ViewHolder(view)
}