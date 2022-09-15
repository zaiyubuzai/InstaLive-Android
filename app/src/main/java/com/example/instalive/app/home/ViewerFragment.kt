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
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.fragment_viewer.*
import splitties.activities.start
import splitties.views.onClick
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
        initObserver()
        initListener()
    }

    private fun joinLive(url: String) {
        val index = url.indexOfLast { it == '/' }
        if (index != -1) {
            val id = url.substring(index + 1)
            val index2 = id.indexOfFirst { it == '?' }
            if (index2 != -1) {
                val id2 = id.substring(0, index2)
                context?.start<LiveAudienceActivity> {
                    putExtra(Constants.EXTRA_LIVE_ID, id2)
                }
            } else {
                context?.start<LiveAudienceActivity> {
                    putExtra(Constants.EXTRA_LIVE_ID, id)
                }
            }
        }
    }

    private fun initListener() {
        joinLive.onClick {
            val c = context ?: return@onClick
            XPopup.Builder(c).asInputConfirm(
                getString(R.string.fb_join_a_live),
                "",
                "",
                ""
            ) { url ->
                joinLive(url)
            }.show()
        }
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
    }

    private fun initObserver() {
        viewModel.liveList.observe(this) {
            val list = it ?: listOf()
            swipeRefreshLayout.isRefreshing = false
            list.forEach { mm ->
                Timber.d("${mm.liveOwner.nickname}")
            }

            liveAdapter.liveList = list
            liveAdapter.notifyDataSetChanged()

            noLiveYet.isVisible = list.isEmpty()
        }

        viewModel.getLiveList(true, {
        }, {
        })
    }
}