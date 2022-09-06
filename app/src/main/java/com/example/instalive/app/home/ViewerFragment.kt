package com.example.instalive.app.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baselibrary.utils.onLinearMarsLoadMore
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.live.LiveAudienceActivity
import com.example.instalive.databinding.FragmentViewerBinding
import kotlinx.android.synthetic.main.fragment_viewer.*
import splitties.activities.start
import timber.log.Timber

@ExperimentalStdlibApi
class ViewerFragment : BaseFragment<HomeViewModel, FragmentViewerBinding>() {

    private lateinit var liveAdapter: LiveAdapter
    override fun initViewModel(): HomeViewModel {
        return getActivityViewModel(HomeViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_viewer, viewModel)
    }

    override fun initData(savedInstanceState: Bundle?) {
        initList()
    }

    private fun initList() {
        liveAdapter = LiveAdapter(listOf()) {
            context?.start<LiveAudienceActivity> {
                putExtra(Constants.EXTRA_LIVE_ID, it)
            }
        }
        liveRecyclerView.layoutManager = LinearLayoutManager(context)
        liveRecyclerView.itemAnimator = null
        liveRecyclerView.setHasFixedSize(true)
        liveRecyclerView.adapter = liveAdapter

        viewModel.liveList.observe(this) {
            swipeRefreshLayout.isRefreshing = false
            if (it == null) return@observe
            it.forEach { mm ->
                Timber.d("${mm.liveOwner.nickname}")
            }

            liveAdapter.liveList = it
            liveAdapter.notifyDataSetChanged()

            noLiveYet.isVisible = it.isEmpty()
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.getLiveList(true, {
            }, {
            })
            swipeRefreshLayout.isRefreshing = true
        }

        liveRecyclerView.onLinearMarsLoadMore {
            viewModel.getLiveList(false, {
            }, {
            })
        }

        viewModel.getLiveList(true, {
        }, {
        })
    }
}