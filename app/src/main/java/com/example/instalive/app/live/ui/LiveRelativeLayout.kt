package com.example.instalive.app.live.ui

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.view.SurfaceView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.instalive.R
import com.example.instalive.model.LiveUserInfo
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.layout_live_video.view.*
import kotlinx.coroutines.*
import splitties.dimensions.dp

/**
 * 多人连麦的单个直播流布局
 */
class LiveRelativeLayout : RelativeLayout {
    companion object {
        const val Raise_Hand_Can = "raise_hand_can"
        const val Raise_Hand_Doing = "raise_hand_doing"
        const val Raise_Hand_Cannot = "raise_hand_cannot"
    }

    var pauseJob: Job? = null

    var liveUserInfo: LiveUserInfo? = null
        set(value) {
            Glide.with(this)
                .load(value?.portrait)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(this.context, 25, 8)))
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.ic_default_avatar)
                .into(MAPortrait)

            Glide.with(this)
                .load(value?.portrait)
                .apply(RequestOptions.bitmapTransform(
                    RoundedCorners(dp(12))))
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.ic_default_avatar)
                .into(callAudioAvatar)

            field = value
        }

    var onLiveVideoViewListener: OnLiveVideoViewListener? = null

//    var mute = 0//0非静音1静音

    var isHost = false

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.layout_live_video, this)
    }

    fun changeMuteState(mute: Int, isChangeUI:Boolean = true) {
        liveUserInfo?.mute = mute
        if (isChangeUI) {
            liveMuteClose.isVisible = mute == 1
            if (mute == 1) {
                voiceAnimCL.isVisible = false
            }
        }
    }

    fun setCardViewCorner(){
        cardView.radius = 0F
    }

    /**
     * true shuohua false meiyou shuohua
     */
    fun changeAudioState(isAudio: Boolean) {
        if (liveUserInfo?.mute == 1) {
            voiceAnimCL.isVisible = false
            return
        }

        voiceAnimCL.isVisible = isAudio
    }

    fun getHangUpGlobalRect(x: Int, y: Int) {
        if (!myHangUp.isVisible) return
        val globalRect = Rect()
        myHangUp.getGlobalVisibleRect(globalRect)
        if (globalRect.contains(x, y)) {
            liveUserInfo?.let { onLiveVideoViewListener?.onHangUPClick(it) }
        }
    }

    fun getAvatarGlobalRect(x: Int, y: Int) {
        if (!portraitView.isVisible) return
        val globalRect = Rect()
        portraitView.getGlobalVisibleRect(globalRect)
        if (globalRect.contains(x, y)) {
            liveUserInfo?.let { onLiveVideoViewListener?.onProfileClick(it) }
        } else {
            nameContainer.getGlobalVisibleRect(globalRect)
            if (globalRect.contains(x, y)) {
                liveUserInfo?.let { onLiveVideoViewListener?.onProfileClick(it) }
            }
        }
    }

    fun getRaiseHandGlobalRect(x: Int, y: Int) {
        if (!raiseHandContainer.isVisible) return
        val globalRect = Rect()
        raiseHandContainer.getGlobalVisibleRect(globalRect)
        if (globalRect.contains(x, y)) {
            onLiveVideoViewListener?.onRaiseHandClick()
        }
    }

    fun setLiveVideoSurfaceView(surface: SurfaceView) {
        liveVideoFL.addView(surface)
    }

    fun removeLiveVideoSurfaceView() {
        liveVideoFL.removeAllViews()
    }

    fun initUserView(isShow: Boolean) {
        nameTV?.text = liveUserInfo?.nickname ?: ""
        portraitView?.setImageURI(Uri.parse(liveUserInfo?.portrait))
        portraitView?.isVisible = true
        nameContainer?.isVisible = true
        myHangUp?.isVisible = isShow
        raiseHandContainer?.isVisible = false
        raiseHand?.setImageResource(R.mipmap.live_raise_hand_little_can)
    }

    fun hideAllChildView() {
        portraitView?.isVisible = false
        nameContainer?.isVisible = false
        myHangUp?.isVisible = false
        liveMuteClose?.isVisible = false
        voiceAnimCL?.isVisible = false
        raiseHandContainer?.isVisible = false
        raiseHand?.setImageResource(R.mipmap.live_raise_hand_little_can)
    }

    fun showRaiseHandUI(isShow: Boolean) {
        raiseHandContainer?.isVisible = isShow
    }

    fun changeRaiseHandUI(state: String) {
        raiseHand?.setImageResource(
            if (state == Raise_Hand_Can) {
                R.mipmap.live_raise_hand_little_can
            } else if (state == Raise_Hand_Doing) {
                R.mipmap.live_raise_hand_little_upping
            } else {
                R.mipmap.live_raise_hand_little_no
            }
        )
    }

    /**
     * @param isVisible
     */
    fun showHost(isVisible: Boolean) {
        hostText?.isVisible = isVisible
        hideAllChildView()
    }

    fun viewRebuild(isShowHostView: Boolean){
        hostText?.isVisible = isShowHostView
        liveMuteClose?.isVisible = liveUserInfo?.mute == 1 && isShowHostView
        voiceAnimCL?.isVisible = false
    }

    fun loading(isVisible: Boolean){
        loadingUserUI?.isVisible = isVisible
        MAPortrait?.isVisible = isVisible
    }

    fun callAudioView(isVisible: Boolean){
        callAudioAvatar?.isVisible = isVisible
        MAPortrait?.isVisible = isVisible
    }

    fun hidePause() {
        pauseMAContainer?.isVisible = false
        MAPortrait?.isVisible = false
        loadingUserUI?.isVisible = false//loading视图目前先根据第一次关闭暂停视图进行隐藏
        pauseJob?.cancel()
        pauseJob = null
    }

    fun showPause() {
        if (pauseJob == null) {
            pauseJob = GlobalScope.launch(Dispatchers.IO) {
                delay(8000)
                withContext(Dispatchers.Main) {
                    pauseMAContainer?.isVisible = true
                    MAPortrait?.isVisible = true
                }
                pauseJob?.cancel()
                pauseJob = null
            }
        }
    }

    interface OnLiveVideoViewListener {
        fun onHangUPClick(liveUserInfo: LiveUserInfo)
        fun onProfileClick(liveUserInfo: LiveUserInfo)
        fun onRaiseHandClick()
    }

}
