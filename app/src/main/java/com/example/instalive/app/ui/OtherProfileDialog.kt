package com.example.instalive.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.views.BaseBottomPopup
import com.example.instalive.InstaLiveApp
import com.venus.framework.util.isNeitherNullNorEmpty
import splitties.alertdialog.appcompat.*
import splitties.dimensions.dp
import splitties.views.onClick
import splitties.views.textColorResource
import com.example.instalive.R
import com.example.instalive.app.conversation.ConversationListActivity
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.model.UserData
import kotlinx.android.synthetic.main.dialog_other_profile.view.*
import splitties.activities.start

@SuppressLint("ViewConstructor")
@ExperimentalStdlibApi
class OtherProfileDialog(
    context: Context,
    private var userId: String?,
    private var username: String?,
    private var userData: UserData?,
    private var myRole: Int,
    private var targetUserRole: Int,
    private val conversationId: String?,
    private val liveId: String?,
    private val isHostOpen: Boolean, //是否主播打开的
    private val isTargetLiveMicrophone: Boolean, // 目标是否已经连麦
    private val isMeLiveMicrophone: Boolean, // 自己是否已经连麦
    private var isLiveMute: Boolean, //是否已经静音 true 已经静音 false 没有静音
    private val isFriendRequest: Int = 0,
    private val from: Int = 1, //1:default，2:group，3:qrcode
    private val isSecure: Boolean = false,
) : BaseBottomPopup<OtherProfileViewModel>(context) {
    private val createConversation = MutableLiveData<ConversationsEntity>()
    private val userDataLiveData = MutableLiveData<UserData>()

    @SuppressLint("SetTextI18n")
    override fun initData() {
        if (isSecure && from == 2) {
            hostWindow.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        if (userData != null) {
            initView()
        }

        initListener()

        initObserver()
    }

    private fun initListener() {
        startChat.onClick{
            userData?.id?.let { it1 ->
                viewModel.createConversation(it1){
                    context?.start<ConversationListActivity> {  }
                }
            }
        }

        loadingAnimContainer.onClick {}
    }

    private fun initObserver() {
        createConversation.observe(this) {
            var mainActivityOpen = false

            dismiss()
        }
        userDataLiveData.observe(this) {

        }

    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        val c = context ?: return
        val options = RequestOptions.bitmapTransform(RoundedCorners(c.dp(36)))
        Glide.with(c)
            .load(userData?.portrait)
            .apply(options)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(avatar)

        usernameTv.text = "@${userData?.username}"
        bio.text = userData?.bio

        websiteListLayout.removeAllViews()
            userData?.websiteList?.forEach { website ->
                val imageView = ImageView(c)
                val layoutParams = LinearLayout.LayoutParams(c.dp(36), c.dp(36))
                layoutParams.setMargins(c.dp(22), 0, 0, 0)
                imageView.layoutParams = layoutParams
                Glide.with(c)
                    .load(website.websiteImg)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
                imageView.onClick { _ ->
                    if (isHostOpen) {
                    } else if (isMeLiveMicrophone) {
                    } else {
                        val url = if (website.websiteLink.isEmpty()) {
                            website.websiteBaseUrl
                        } else {
                            website.websiteLink
                        }
                        if (url.isNeitherNullNorEmpty()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                c.startActivity(intent)
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
                websiteListLayout.addView(imageView)
            }
    }

    private fun formatTimeS(seconds: Long): String {
        var second = seconds
        var temp: Int

        val sb = StringBuffer()
        val d = second / (24 * 3600)
        sb.append("${d}d ")
        second %= 24 * 3600

        if (second >= 3600) {
            temp = (second / 3600).toInt()
            sb.append(if (second / 3600 < 10) "0$temp:" else "$temp:")
            temp = (second % 3600 / 60).toInt()
            sb.append(if (temp < 10) "0$temp" else "$temp")
        } else {
            sb.append("00:")
            temp = (second % 3600 / 60).toInt()
            sb.append(if (temp < 10) "0$temp" else "$temp")
        }
        return sb.toString()
    }

    private fun confirm(con: String, userId: String, nickname: String) {
        //删除 二次确认
        val dialogs = context?.alertDialog {
            titleResource = R.string.fb_confirm
            positiveButton(R.string.fb_confirm) {
                it.dismiss()
            }
            cancelButton()
        }
        dialogs?.setOnShowListener {
            val button = dialogs.getButton(DialogInterface.BUTTON_POSITIVE)
            button?.textColorResource = R.color.red_800
        }
        dialogs?.show()
    }

    override fun initViewModel(): OtherProfileViewModel {
        return InstaLiveApp.appInstance.getAppViewModelProvider()[OtherProfileViewModel::class.java]
    }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_other_profile
    }

}