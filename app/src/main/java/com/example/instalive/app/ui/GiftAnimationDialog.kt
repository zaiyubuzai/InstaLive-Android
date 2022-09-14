package com.example.instalive.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.view.isVisible
import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.R
import com.example.instalive.model.GiftInfo
import com.lxj.xpopup.core.BottomPopupView
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.venus.framework.util.isNeitherNullNorEmpty
import kotlinx.android.synthetic.main.dialog_gift_animation.view.*
import splitties.views.imageDrawable
import java.io.File
import java.net.URL


@SuppressLint("ViewConstructor")
class GiftAnimationDialog(
    context: Context,
    val giftInfo: GiftInfo,
    val isLive:Boolean = false,
    val finishCallback:()->Unit = {},
    val forCardCallback:(Int)->Unit = {},
) : BottomPopupView(context) {

    lateinit var svgaParser: SVGAParser

    override fun onCreate() {
        super.onCreate()

        svgaParser = SVGAParser(context)
        giftAnim.callback = object : SVGACallback {
            override fun onFinished() {
//                Timber.d("Multi: pod Gift anim onFinished")
                giftAnim?.clear()
                finishCallback.invoke()
                this@GiftAnimationDialog.dismiss()
            }

            override fun onPause() {
//                Timber.d("Multi:Live Gift anim onPause")
            }

            override fun onRepeat() {
//                Timber.d("Multi:Live Gift anim onRepeat")
            }

            override fun onStep(frame: Int, percentage: Double) {
            }
        }

        if (giftInfo.specialEffect.show) {
            //如果第一个礼物是特效礼物，就走展示特效礼物
            val url = giftInfo.specialEffect.img
//            Timber.d("Multi:show anim: $url")
            if (url.isNeitherNullNorEmpty()) {
                    if (url.startsWith("http").not() && url.endsWith(".svga")) {
                        svgaParser.decodeFromAssets(url, object : SVGAParser.ParseCompletion {
                            override fun onComplete(videoItem: SVGAVideoEntity) {
//                                Timber.d("Multi: asset, process svga...")
                                giftAnim?.isVisible = true
                                giftAnim?.imageDrawable = SVGADrawable(videoItem)
                                giftAnim?.startAnimation()
                            }

                            override fun onError() {
                                this@GiftAnimationDialog.dismiss()
                            }
                        })
                    }else{
                        this@GiftAnimationDialog.dismiss()
                    }
                    return
                val giftFile = File(
                    appInstance.cacheDir,
                    "live_animations/${Uri.parse(url).pathSegments.last()}"
                )
                if (giftFile.exists()) {
//                    Timber.d("Multi:Live animation cache hit!! ${Uri.parse(url).pathSegments.last()}")
                    val inputStream = giftFile.inputStream()
                    svgaParser.decodeFromInputStream(
                        inputStream,
                        Uri.parse(url).pathSegments.last(),
                        object : SVGAParser.ParseCompletion {
                            override fun onComplete(videoItem: SVGAVideoEntity) {
//                                Timber.d("Multi: cache, process svga...")
                                inputStream.close()
                                giftAnim?.isVisible = true
                                giftAnim?.imageDrawable = SVGADrawable(videoItem)
                                giftAnim?.startAnimation()

                                if (giftInfo.card.show.not() || isLive.not()) {
//                                    Timber.d("Multi: did not need show card...")
                                    return
                                }
                                val length = videoItem.frames * 1000 / videoItem.FPS
                                forCardCallback.invoke(length)
                            }

                            override fun onError() {
                                this@GiftAnimationDialog.dismiss()
                            }
                        }, true
                    )
                } else {
                    svgaParser.decodeFromURL(URL(url), object : SVGAParser.ParseCompletion {
                        override fun onComplete(videoItem: SVGAVideoEntity) {
//                            Timber.d("Multi: url, process svga...")
                            giftAnim?.isVisible = true
                            giftAnim?.imageDrawable = SVGADrawable(videoItem)
                            giftAnim?.startAnimation()

                            if (giftInfo.card.show.not() || isLive.not()) {
//                                Timber.d("Multi: did not need show card...")
                                return
                            }
                            val length = videoItem.frames * 1000 / videoItem.FPS
                            forCardCallback.invoke(length)
                        }

                        override fun onError() {
                            this@GiftAnimationDialog.dismiss()
                        }
                    })
                }
            }else{
                dismiss()
            }
        }else{
            dismiss()
        }
    }

    private fun playAnimVideo(uri: String){
        videoView.isVisible = true
        videoView.setVideoURI(Uri.parse(uri))
        videoView.setOnPreparedListener { player ->
            player.start()
            player.isLooping = false
        }
        videoView.setOnCompletionListener {
            videoView.isVisible = false
        }
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_gift_animation
}