package com.example.instalive.app.live.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.baselibrary.utils.Utils
import com.example.baselibrary.utils.alphaClick
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.utils.GlideEngine
import com.jeremyliao.liveeventbus.LiveEventBus
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.android.synthetic.main.dialog_live_more.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.views.imageResource
import java.util.*

@SuppressLint("ViewConstructor")
class LiveMoreDialog(
    context: Context,
    val activity: Activity,
    val liveId: String,
    val mode: Int,//1:group host; 2:group microphone; 3:call host; 4:call microphone.
    var makeupEnable: Boolean,
    val showMessage: () -> Unit,
    val onMakeupClick: () -> Unit,
    val showFlip: () -> Unit,
    val showGifSelector: () -> Unit,
) : BottomPopupView(context) {
    //文本消息
    private val messageOnClick: (View) -> Unit = {
        if (Utils.isFastClick()) {
            showMessage.invoke()
            dismiss()
        }
    }

    //图片或视频
    private val mediaOnClick: (View) -> Unit = {
        if (Utils.isFastClick()) {
            LiveEventBus.get(Constants.EVENT_BUS_KEY_NOT_GO_BACK).post(Any())
//            MarsEventLogger.logFirebaseEvent("click_img")
            val filterMimeType = ArrayList<String>()
            filterMimeType.add("video/mp4")
            filterMimeType.add("video/quicktime")
            filterMimeType.add("image/jpeg")
            filterMimeType.add("image/jpg")
            filterMimeType.add("image/png")
            PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofVideo())
                .isMaxSelectEnabledMask(true)
                .isCanPreView(false)
                .isWeChatStyle(true)
                .theme(R.style.picture_WeChat_style)
                .imageEngine(GlideEngine.createGlideEngine())
                .isPreviewVideo(false)
                .selectionMode(PictureConfig.SINGLE)
                .maxSelectNum(1)
                .selectCountText(activity.getString(R.string.fb_send))
                .maxVideoSelectNum(1)
                .setLanguage(LanguageConfig.ENGLISH)
                .isOnlyVideo(false)
                .isWithVideoImage(true)
                .selectMaxPrompt(resources.getString(R.string.send))
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .isCamera(false)
                .isShowPreView(false)
                .setFilterMimeType(filterMimeType)
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: MutableList<LocalMedia>?) {
                        if (result != null) {
                            when ((result[0].mimeType).lowercase(Locale.getDefault())) {
                                "video/mp4", "video/quicktime" -> {
                                    var width = result[0].width
                                    var height = result[0].height
//                                    sharedViewModel.sendVideoMessage(
//                                        result[0].realPath,
//                                        RecentConversation.conversationsEntity.conversationId,
//                                        RecentConversation.conversationsEntity.type,
//                                        width,
//                                        height,
//                                        result[0].duration,
//                                        result[0].size,
//                                        3,
//                                        liveId,
//                                        RecentConversation.conversationsEntity.level
//                                    )
                                }
                                "image/jpeg", "image/jpg", "image/png" -> {
//                                    sharedViewModel.sendImageMessage(
//                                        RecentConversation.conversationsEntity.type,
//                                        liveId,
//                                        3,
//                                        result[0].realPath,
//                                        SessionPreferences.recentConversationID,
//                                        result[0].width,
//                                        result[0].height,
//                                        RecentConversation.conversationsEntity.level
//                                    )
                                }
                                else -> {
                                }
                            }
                        }
                    }

                    override fun onCancel() {}

                })
//            PictureSelector.create(activity)
//                .openGallery(PictureMimeType.ofImage())
//                .isMaxSelectEnabledMask(true)
//                .isCanPreView(false)
//                .imageEngine(GlideEngine.createGlideEngine())
//                .isPreviewVideo(false)
//                .selectionMode(PictureConfig.SINGLE)
//                .maxSelectNum(1)
//                .maxVideoSelectNum(1)
//                .selectCountText(activity.getString(R.string.fb_send))
//                .setLanguage(LanguageConfig.ENGLISH)
//                .isWithVideoImage(true)
//                .selectMaxPrompt(resources.getString(R.string.you_can_select_1_video_or_image_to_respond))
//                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                .isCamera(false)
//                .isShowPreView(false)
//                .isMessageStyle(true)
//                .setFilterMimeType(filterMimeType)
//                .forResult(object : OnResultCallbackListener<LocalMedia> {
//                    override fun onResult(result: MutableList<LocalMedia>?) {
//                        if (result != null) {
//                            logFirebaseEvent("sent_img")
//                            if (result[0].mimeType.startsWith("image")) {
//                                showImage.invoke(result[0].realPath)
////                            } else if (result[0].mimeType.startsWith("video")) {
////                                showVideo.invoke(result[0].realPath)
//                            }
//                        }
//                    }
//
//                    override fun onCancel() {
//                    }
//                })
            dismiss()
        }
    }

    //gif图片
    private val gifOnClick: (View) -> Unit = {
        if (Utils.isFastClick()) {
            showGifSelector.invoke()
            dismiss()
        }
    }

    //美颜
    private val makeupOnClick: (View) -> Unit =  {
        if (Utils.isFastClick()) {
            lifecycleScope.launch {
                makeupEnable = !makeupEnable
                withContext(Dispatchers.Main) {
                    onMakeupClick.invoke()
                    (it as ImageView).setImageResource(if (makeupEnable) R.mipmap.live_beauty_yes else R.mipmap.live_beauty_no)
                }
                delay(500)
                dismiss()
            }
        }
    }

    //相机切换
    private val flipOnClick: (View) -> Unit =  {
        if (Utils.isFastClick()) {
            showFlip.invoke()
            dismiss()
        }
    }

    private val MODE_GROUP_HOST_IMAGE = listOf(
        R.mipmap.live_comments,
//        R.mipmap.live_picture,
//        R.mipmap.live_gif,
        R.mipmap.live_beauty_yes,
        R.mipmap.live_flip
    )
    private val MODE_GROUP_HOST_CLICK =
        listOf(
            messageOnClick,
//            mediaOnClick,
//            gifOnClick,
            makeupOnClick,
            flipOnClick)

    private val MODE_GROUP_MICROPHONE_IMAGE = listOf(
        R.mipmap.live_comments,
//        R.mipmap.live_picture,
//        R.mipmap.live_gif,
        R.mipmap.live_beauty_yes,
        R.mipmap.live_flip
    )
    private val MODE_GROUP_MICROPHONE_CLICK =
        listOf(
            messageOnClick,
//            mediaOnClick,
//            gifOnClick,
            makeupOnClick,
            flipOnClick)

    private val MODE_CALL_HOST_IMAGE = listOf(
        R.mipmap.live_comments,
        R.mipmap.live_picture,
        R.mipmap.live_gif,
    )
    private val MODE_CALL_HOST_CLICK =
        listOf(messageOnClick, mediaOnClick, gifOnClick)

    private val MODE_CALL_MICROPHONE_IMAGE = listOf(
        R.mipmap.live_comments,
        R.mipmap.live_picture,
        R.mipmap.live_gif,
        R.mipmap.live_beauty_yes,
        R.mipmap.live_flip
    )
    private val MODE_CALL_MICROPHONE_CLICK =
        listOf(messageOnClick, mediaOnClick, gifOnClick, makeupOnClick, flipOnClick)

    override fun onCreate() {
        super.onCreate()

        val btnList = listOf(firstBtn, secondBtn, thirdBtn, fourthBtn, fifthBtn)
        val imageList = when (mode) {
            1 -> MODE_GROUP_HOST_IMAGE
            2 -> MODE_GROUP_MICROPHONE_IMAGE
            3 -> MODE_CALL_HOST_IMAGE
            4 -> MODE_CALL_MICROPHONE_IMAGE
            else -> {
                MODE_GROUP_HOST_IMAGE
            }
        }
        val clickList = when (mode) {
            1 -> MODE_GROUP_HOST_CLICK
            2 -> MODE_GROUP_MICROPHONE_CLICK
            3 -> MODE_CALL_HOST_CLICK
            4 -> MODE_CALL_MICROPHONE_CLICK
            else -> {
                MODE_GROUP_HOST_CLICK
            }
        }

        btnList.forEachIndexed { index, imageView ->
            if (index >= imageList.size) {
                imageView.isVisible = false
            } else {
                if (imageList[index] == R.mipmap.live_beauty_yes) {
                    imageView.imageResource = if (!makeupEnable) {
                        R.mipmap.live_beauty_no
                    } else {
                        R.mipmap.live_beauty_yes
                    }
                } else {
                    imageView.imageResource = imageList[index]
                }
                imageView.alphaClick(clickList[index])
            }
        }

    }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_live_more
    }
}