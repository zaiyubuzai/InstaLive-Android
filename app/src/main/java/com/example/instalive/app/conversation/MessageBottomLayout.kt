package com.example.instalive.app.conversation

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.baselibrary.utils.alphaClick
import com.example.baselibrary.utils.hideKeyboard
import com.example.baselibrary.utils.marsToast
import com.example.instalive.R
import com.venus.dm.db.entity.ConversationsEntity
import kotlinx.android.synthetic.main.message_bottom_layout.*
import kotlinx.android.synthetic.main.message_bottom_layout.view.*
import splitties.alertdialog.appcompat.*
import splitties.views.onClick
import java.io.File
import java.util.*


/**
 *
 * 用于发送消息底部页面
 * @author yikai
 */
@ExperimentalStdlibApi
class MessageBottomLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var mLayoutActionListener: BottomLayoutActionListener? = null
    private var file: File? = null
    var conversationsEntity: ConversationsEntity? = null
    private val screenName = "dm_view"

    init {
        inflate(context, R.layout.message_bottom_layout, this)
        initListener()
    }

    fun setBottomLayoutActionListener(bottomLayoutActionListener: BottomLayoutActionListener?) {
        mLayoutActionListener = bottomLayoutActionListener
    }

    fun onResume() {
        edtChatInput.clearFocus()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        edtChatInput.doAfterTextChanged {
            if (TextUtils.isEmpty(it.toString())) {
                btnSend.setImageResource(R.mipmap.ic_send_message_no2)
                mentionListView?.isVisible = false
                loadingMention?.isVisible = false
                if (!constraintLayout.isVisible) {
                    btnSend.isVisible = false
                    ll_btn.isVisible = true
                }
            } else {
//                keyboardVisibilityEvent(true)
                btnSend.setImageResource(R.mipmap.ic_send_message_yes2)
                btnSend.isVisible = true
                ll_btn.isVisible = false
            }
            if (it.toString().length >= MAX_TEXT_SIZE) {
                marsToast(context.getString(R.string.fb_up_to_any_chars, MAX_TEXT_SIZE.toString()))
            }
        }

        btnSend.alphaClick {
            sendAction()
        }

        ll_btn.onClick {}

        btnPic.alphaClick { v: View? ->
            //选择图片
            mLayoutActionListener?.pickMediaList()
        }

        btnGift.alphaClick {
            conversationsEntity?.let { mLayoutActionListener?.onClickGift(it) }
        }

        btnLike.alphaClick {
            conversationsEntity?.let { mLayoutActionListener?.onClickLike(it) }
        }

    }

    /**
     * 发送message
     */
    private fun sendAction() {
        if (!TextUtils.isEmpty(edtChatInput.text)) {
            mLayoutActionListener?.sendMessage(edtChatInput.text.toString())
            edtChatInput.setText("")
        }
    }

    fun onPause() {
        edtChatInput.clearFocus()
        edtChatInput.hideKeyboard()
    }

    interface BottomLayoutActionListener {
        /**
         * 发送文本的操作
         * @param message 文本内容
         */
        fun sendMessage(message: String?)

        /**
         * 选择多媒体资源路径集合
         * @param list 多媒体资源路径
         */
        fun pickMediaList()

        fun onClickGift(conversationsEntity: ConversationsEntity)

        fun onClickLike(conversationsEntity: ConversationsEntity)
    }

    companion object {
        const val MAX_TEXT_SIZE = 1000
    }
}