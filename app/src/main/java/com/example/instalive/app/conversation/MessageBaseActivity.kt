package com.example.instalive.app.conversation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.instalive.app.Constants
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.model.LikeEvent
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URL
import java.util.*

@ExperimentalStdlibApi
abstract class MessageBaseActivity<VDB : ViewDataBinding> : InstaBaseActivity<MessageViewModel, VDB>() {
    private var likeCount = 0
    private var likeJob: Job? = null
    private val likedUuid = mutableSetOf<String>()

    private val emojiList = mutableListOf<Bitmap>()

    override fun initData(savedInstanceState: Bundle?) {
        Constants.DEFAULT_EMOJI_LIST.forEach {
            val bitmap =
                BitmapFactory.decodeResource(resources, it, BitmapFactory.Options())
            emojiList.add(bitmap)
        }
    }

    private fun popGift() {
//        if (giftList.isEmpty()) {
//            return
//        }
//        val firstGift = giftList[0]
//        if (firstGift.giftInfo?.specialEffect?.show == true && !bigAnimIsPlaying.get()) {
//            //如果第一个礼物是特效礼物，就走展示特效礼物
//            val gift = giftList.removeAt(0)
//            val url = gift.giftInfo?.specialEffect?.img
//            firstGiftCardViewJob?.cancel()
//            firstGiftCardViewJob = null
//            bigAnimIsPlaying.set(true)
//
////            var netPath = gift.giftInfo?.specialEffect?.img ?: ""
////            val netPathList = netPath.split("/")
////            if (netPathList.size < 2) return
////            netPath = netPathList.last()
////            netPath = netPath.replace(".svga", "") + gift.giftId
////
////            val list = Gson().fromJson<List<String>>(
////                FambasePreferences.liveGiftCache,
////                object : TypeToken<List<String>>() {}.type
////            ).toMutableList()
////            val gifts = list.filter {
////                it.contains(netPath)
////            }
//            val gifts = DataRepository.findGiftCache(gift.giftInfo?.specialEffect?.img ?: "")
//            try {
//                if (gifts.isNeitherNullNorEmpty()) {
//                    val giftImg = gifts[0]
//                    if (File(giftImg).exists()) {
//                        val file = File(giftImg)
//                        file.inputStream()
//                        svgaParser.decodeFromInputStream(
//                            file.inputStream(),
//                            gift.giftInfo?.specialEffect?.img ?: "",
//                            object : SVGAParser.ParseCompletion {
//                                override fun onComplete(videoItem: SVGAVideoEntity) {
//                                    messageGiftAnim?.imageDrawable = SVGADrawable(videoItem)
//                                    messageGiftAnim?.startAnimation()
//                                    val length = videoItem.frames * 1000 / videoItem.FPS
//                                    giftFirstContainer?.let {
//                                        firstGiftCardViewJob = popGiftCard(gift, it, length) {
//                                            firstGiftCardViewJob?.cancel()
//                                            firstGiftCardViewJob = null
//                                        }
//                                    }
//                                }
//
//                                override fun onError() {
//                                    Timber.d("gift animation error")
//                                }
//                            })
//                        return
//                    }
//                } else {
//                    svgaParser.decodeFromURL(URL(url), object : SVGAParser.ParseCompletion {
//                        override fun onComplete(videoItem: SVGAVideoEntity) {
//                            messageGiftAnim?.imageDrawable = SVGADrawable(videoItem)
//                            messageGiftAnim?.startAnimation()
//                            val length = videoItem.frames * 1000 / videoItem.FPS
//                            giftFirstContainer?.let {
//                                firstGiftCardViewJob = popGiftCard(gift, it, length) {
//                                    firstGiftCardViewJob?.cancel()
//                                    firstGiftCardViewJob = null
//                                }
//                            }
//                        }
//
//                        override fun onError() {
//                            Timber.d("gift animation error")
//                        }
//                    })
//                    viewModel.cacheGift(gift.giftInfo?.specialEffect?.img ?: "")
//                }
//            } catch (e: Exception) {
//            }
//        } else {
//            if (firstGiftCardViewJob != null) {
//                if (secondGiftCardViewJob == null && giftSecondContainer != null) {
//                    val index = giftList.indexOfFirst {
//                        it.giftInfo?.specialEffect?.show != true
//                    }
//                    if (index != -1) {
//                        val gift = giftList.removeAt(index)
//                        secondGiftCardViewJob = popGiftCard(gift, giftSecondContainer) {
//                            secondGiftCardViewJob?.cancel()
//                            secondGiftCardViewJob = null
//                            if (giftList.isNotEmpty()) {
//                                popGift()
//                            }
//                        }
//                    }
//                }
//            } else if (!bigAnimIsPlaying.get()) {
//                val index = giftList.indexOfFirst {
//                    it.giftInfo?.specialEffect?.show != true
//                }
//                if (index != -1) {
//                    val gift = giftList.removeAt(index)
//                    if (giftFirstContainer != null) {
//                        firstGiftCardViewJob = popGiftCard(gift, giftFirstContainer) {
//                            firstGiftCardViewJob?.cancel()
//                            firstGiftCardViewJob = null
//                            if (giftList.isNotEmpty()) {
//                                popGift()
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

//    private fun popGiftCard(
//        giftEvent: LiveGiftEvent,
//        giftContainer: FrameLayout,
//        customLength: Int? = null,
//        onFinish: () -> Unit,
//    ): Job {
//        return lifecycleScope.launch {
//            var duration = when {
//                giftEvent.userInfo.userId == SessionPreferences.id -> {
//                    giftEvent.giftInfo?.card?.durationMe ?: 2000
//                }
//                isHost -> {
//                    giftEvent.giftInfo?.card?.durationHost ?: 2000
//                }
//                else -> {
//                    giftEvent.giftInfo?.card?.durationViewer ?: 2000
//                }
//            }
//            duration = if (customLength != null) {
//                if (customLength > duration) customLength else duration
//            } else {
//                duration
//            }
//            val giftPop = layoutInflater.inflate(R.layout.layout_live_gift_pop, null)
//            giftPop.giftAvatar.onClick {
////                if (giftEvent.userInfo.userId != SessionPreferences.id) {
////                    showPersonBottomDialog(giftEvent.userInfo)
////                }
//            }
//            giftPop.onClick {
//                if (!isHost) {
//                    openGift()
//                }
//            }
//
//            val options = RequestOptions.bitmapTransform(RoundedCorners(activity.dip(12)))
//            Glide.with(activity)
//                .load(giftEvent.giftInfo?.card?.img)
//                .apply(options)
//                .skipMemoryCache(false)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .error(R.drawable.ic_live_gift_default)
//                .placeholder(R.drawable.ic_live_gift_default)
//                .into(giftPop.giftGift)
//            Glide.with(activity)
//                .load(giftEvent.userInfo.portrait)
//                .apply(options)
//                .skipMemoryCache(false)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .error(R.drawable.ic_default_avatar)
//                .placeholder(R.drawable.ic_default_avatar)
//                .into(giftPop.giftAvatar)
////            giftPop.giftAvatar.setImageURI(giftEvent.userInfo.portrait)
//            giftPop.giftUsername.textSize = if (isHost) 15f else 13f
//            giftPop.content.textSize = if (isHost) 12f else 11f
//            giftPop.giftUsername.text = giftEvent.userInfo.nickname
//            giftPop.content.text = giftEvent.giftInfo?.card?.content
////            giftPop.giftGift.setImageURI(giftEvent.giftInfo?.card?.img)
//            giftPop.giftContainer.setBackgroundResource(
//                if (giftEvent.giftInfo?.card?.highlight == true)
//                    R.drawable.bg_interaction_gift_highlighted_container
//                else R.drawable.bg_interaction_gift_container)
//            giftContainer.addView(giftPop)
//            giftPop.liveGiftAnimatorSet(duration, {
//            }, {
//                giftFirstContainer?.removeView(giftPop)
//                onFinish()
//            })
//        }
//    }

    //socket事件去显示emoji
    fun showCornerLikes(voteEvent: LikeEvent) {
        val p = voteEvent.userInfo?.portrait
        if (p != null) {
            Glide.with(this)
                .asBitmap()
                .load(p)
                .circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        showLikeFavor(resource)
                        for (i in 1..voteEvent.likeNum) {
                            lifecycleScope.launch {
                                delay(i * 100L)
                                showLikeFavor(emojiList.random())
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            for (i in 1..voteEvent.likeNum) {
                lifecycleScope.launch {
                    delay(i * 100L)
                    showLikeFavor(emojiList.random())
                }
            }
        }
    }

    //发送者点击发送显示emoji
    fun doLikeFavor() {
        showLikeFavor(emojiList.random())
        if (likeCount == 0) {
                Glide.with(this)
                    .asBitmap()
                    .load(SessionPreferences.portrait)
                    .circleCrop()
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?,
                        ) {
                            showLikeFavor(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
        }
        likeCount++

        likeJob?.cancel()
        likeJob = lifecycleScope.launch {
            delay(500)
            if (likeCount > 0) {
                val uuid = UUID.randomUUID().toString()
                likedUuid.add(uuid)
//                viewModel.likeLive(
//                    likeCount,
//                    uuid,
//                    viewModel.conversationsEntity?.conversationId,
//                    "-1"
//                )
            }
            likeCount = 0
        }
    }

    abstract fun showLikeFavor(resource: Bitmap)
}