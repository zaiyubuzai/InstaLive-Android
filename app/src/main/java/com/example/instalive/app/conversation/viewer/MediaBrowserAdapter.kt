package com.example.instalive.app.conversation.viewer

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import cn.jzvd.JzvdStd
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.instalive.R


class MediaBrowserAdapter(
    data: MutableList<VideoMultyItem>?,
    var uuid: String,
    var videoVolume: Boolean
) : BaseMultiItemQuickAdapter<VideoMultyItem, BaseViewHolder>(
    data
) {

    init {
        addItemType(1, R.layout.banner_video)
        addItemType(2, R.layout.banner_image)
    }

    override fun convert(helper: BaseViewHolder, item: VideoMultyItem) {
        when (item.itemType) {
            1 -> {
                //视频
                val jzvdStd = helper.getView<MyJzvdStd>(R.id.player)
                //去掉
                jzvdStd.setUp(item.url, "", JzvdStd.SCREEN_NORMAL, MyJZMediaSystem::class.java)
                jzvdStd.replayTextView.text = ""
                jzvdStd.posterImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                if (videoVolume) {
                    jzvdStd.volume = 1f
                } else {
                    jzvdStd.volume = 0f
                }
                Glide.with(mContext)
                    .load(item.coverUrl)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .into(jzvdStd.posterImageView)
//                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT)\
                if (uuid == item.uuid) {
                    jzvdStd.startVideo()
                }

            }
            2 -> {
                val imageView = helper.getView<ImageView>(R.id.image)
                val loadingProgress = helper.getView<ProgressBar>(R.id.loadingProgress)
                loadingProgress.isVisible = true
                Glide.with(mContext)
                    .load(item.url)
                    .error(R.drawable.picture_image_placeholder)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any,
                            target: Target<Drawable?>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            loadingProgress?.isVisible = false
                            return false
                        }
                    })
                    .into(imageView)
            }
            else -> {
            }
        }
    }
}