package com.example.instalive.app.conversation.viewer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

import cn.jzvd.Jzvd

import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.PagerSnapHelper
import kotlinx.android.synthetic.main.activity_video_image_play.*
import timber.log.Timber
import cn.jzvd.JzvdStd

import android.view.View
import android.view.WindowManager

import com.chad.library.adapter.base.BaseViewHolder

import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.baselibrary.utils.BarUtils
import com.example.instalive.R
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.app.ChatConstants
import com.venus.framework.util.isNeitherNullNorEmpty
import org.json.JSONArray
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.messageResource
import splitties.alertdialog.appcompat.okButton
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.withExtras
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec
import splitties.views.onClick


class MediaBrowserActivity : AppCompatActivity() {

    private lateinit var videoMultyItem: MutableList<VideoMultyItem>
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mediaBrowserAdapter: MediaBrowserAdapter
    private lateinit var mSnapHelper: PagerSnapHelper

    private lateinit var mediaDataList: String
    private lateinit var uuid: String
    private var position: Int = 0
    private var currentPosition: Int = 0
    private var showVideoVolume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        BarUtils.setNavBarColor(this, Color.parseColor("#191d27"))
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        window.decorView.systemUiVisibility =
//            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
//                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
//
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 跟系統表示要渲染 system bar 背景。
//        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
//        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_video_image_play)

        val set = ConstraintSet()
        set.clone(container)
        set.setMargin(
            R.id.back,
            ConstraintSet.TOP,
            BarUtils.statusBarHeight
        )
        set.applyTo(container)
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT)

        back.onClick {
            finish()
        }

        withExtras(MediaBrowserExtraSpec) {
            this@MediaBrowserActivity.mediaDataList = mediaDataList
            this@MediaBrowserActivity.position = position
            this@MediaBrowserActivity.uuid = uuid
            this@MediaBrowserActivity.showVideoVolume = showVideoVolume
        }

        currentPosition = position

        if (this::mediaDataList.isInitialized) {
            val array = JSONArray(mediaDataList)
            videoMultyItem = mutableListOf()
            for (index in 0 until array.length()) {
                videoMultyItem.add(VideoMultyItem.fromJson(array.getString(index)))
            }

            initView()
        } else {
            finish()
        }

        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).observe(this) { d ->
//            if (d is MessageDeleteEventData) {
//                val data = mediaBrowserAdapter.getItem(currentPosition)
//                data?.let {
//                    if (it.uuid == d.uuid) {
//                        if (it.type == 1) Jzvd.releaseAllVideos()
//                        alertDialog {
//                            messageResource =
//                                if (it.type == 1) R.string.fb_media_browser_deleted_video_content else R.string.fb_media_browser_deleted_photo_content
//                            okButton {
//                                finish()
//                            }
//                            setCancelable(false)
//                        }.show()
//                    } else {
//                        val mediaData = videoMultyItem.filter { v ->
//                            v.uuid == d.uuid
//                        }
//                        if (mediaData.isNeitherNullNorEmpty()) {
//                            val index = videoMultyItem.indexOf(mediaData[0])
//                            videoMultyItem.removeAt(index)
//                            mediaBrowserAdapter.notifyItemRemoved(index)
//                        }
//                    }
//                }
//            }
        }

    }

    private fun initView() {
        mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mediaBrowser.layoutManager = mLayoutManager
        mediaBrowserAdapter = MediaBrowserAdapter(videoMultyItem, uuid, showVideoVolume)
        mSnapHelper = PagerSnapHelper()
        mediaBrowser.onFlingListener = null
        mSnapHelper.attachToRecyclerView(mediaBrowser)
        mediaBrowser.adapter = mediaBrowserAdapter
        mediaBrowser.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentPosition =
                    (mediaBrowser.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            }
        })
        mediaBrowser.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE ->         //停止滚动
                        autoPlay(recyclerView)
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                    }
                    RecyclerView.SCROLL_STATE_SETTLING ->     //惯性滑动
                        Jzvd.releaseAllVideos()
                    else -> {
                    }
                }
            }

            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        mediaBrowser.scrollToPosition(position)
    }

    private fun autoPlay(recyclerView: RecyclerView) {
        val view: View? = mSnapHelper.findSnapView(mLayoutManager)
        if (view != null) {
            if (view is RelativeLayout) {
                Jzvd.releaseAllVideos()
            } else {
                val viewHolder = recyclerView.getChildViewHolder(view) as BaseViewHolder
                if (viewHolder != null) {
                    val myVideoPlayer = viewHolder.getView<JzvdStd>(R.id.player)
//                    myVideoPlayer.startVideo()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        Jzvd.goOnPlayOnResume()
        Timber.d("MediaBrowserActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Jzvd.goOnPlayOnPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Jzvd.releaseAllVideos()
    }

    companion object :
        ActivityIntentSpec<MediaBrowserActivity, MediaBrowserExtraSpec> by activitySpec(
            MediaBrowserExtraSpec
        )

    object MediaBrowserExtraSpec : BundleSpec() {
        var mediaDataList: String by bundle()
        var position: Int = 0
        var uuid: String by bundle()
        var showVideoVolume: Boolean by bundle()
    }
}