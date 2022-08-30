package com.example.instalive.app.live.ui

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baselibrary.utils.alphaClick
import com.example.baselibrary.utils.onLinearMarsLoadMore
import com.example.baselibrary.utils.onSend
import com.example.baselibrary.views.BaseBottomPopup
import com.example.instalive.R
import com.example.instalive.mentions.Mention
import com.example.instalive.mentions.Mentionable
import com.example.instalive.mentions.Mentions
import com.example.instalive.mentions.StringUtils
import com.example.instalive.utils.marsToast
import com.example.instalive.view.CutCopyPasteEditText
import kotlinx.android.synthetic.main.dialog_live_comment_input.view.*
import kotlinx.android.synthetic.main.dialog_live_comment_input.view.btnSend
import splitties.dimensions.dp
import splitties.mainhandler.mainHandler
import splitties.views.imageResource
import timber.log.Timber
import java.util.*

@ExperimentalStdlibApi
class LiveCommentInputDialog(
    context: Context,
    val initText: String,
    val isGroupOwner: Boolean,
    val insertedMentions: MutableList<Mentionable>?,
    val listener: OnLiveCommentEditListener,
) : BaseBottomPopup<LiveCommentInputViewModel>(context) {

    private lateinit var mentionSearchAdapter: MentionSearchAdapter
    private lateinit var mentions: Mentions
    private var isMentioning = false
    private var keyword = ""
    private var oldKeyword = ""
    private var isMentionResultEmpty = false
    private var mTimerTask: TimerTask? = null
    private var mTimer: Timer? = null
    private var timerCount = 0

    private var isLoadMentionSearch = false

    fun onAtComplete(username: String) {
        val atIndex = edtAddComment?.text?.toString()?.lastIndexOf("@") ?: 100
        if (atIndex >= 0) {
            if (username.length + atIndex + 1 > 100) {
                marsToast(context.getString(R.string.fb_up_to_any_chars, "100"))
            } else {
                val mention = Mention()
                mention.mentionName = username
                mentions.insertMention(mention)
            }
        }
    }

    override fun initData() {
        viewModel.reset()
        startTimer()

        mentionSearchAdapter = MentionSearchAdapter(listOf(), false, {}) {
            onAtComplete(it.username)
            exitMention()
        }
        mentionListView.layoutManager = LinearLayoutManager(context)
        mentionListView.adapter = mentionSearchAdapter
        mentionListView.onLinearMarsLoadMore {
            if (!isLoadMentionSearch) {
                isLoadMentionSearch = true
                Timber.d("getFriendList false")
                getMentionSearchData(false)
            }
        }
        mentions = Mentions.Builder(context, edtAddComment).highlightColor(R.color.default_yellow)
            .maxCharacters(20)
            .queryListener {
                if (it == "@") {
                    keyword = ""
                    isMentioning = true
                    mentionSearchAdapter.isShowEveryone = false
                    val params = mentionListView.layoutParams
                    params.height = context?.dp(248) ?: 500
                    mentionListView.layoutParams = params
                    isLoadMentionSearch = true
                    contentContainer.setBackgroundResource(R.color.colorPrimaryDark)
                    getMentionSearchData(true)
                } else {
                    timerCount = 0
                    keyword = it
                }
            }.suggestionsListener {
                if (!it) {
                    exitMention()
                }
            }.build()
        btnSend.imageResource = if (initText.isEmpty() && initText.trim().isEmpty()
        ) R.mipmap.ic_send_message_no2 else R.mipmap.ic_send_message_yes2

        edtAddComment.onSend {
            if (edtAddComment.text.toString().trim().isNotEmpty()) {
                exitMention()
                listener.onSend(edtAddComment.text.toString())
                edtAddComment.setText("")
                dismiss()
            }
        }
        edtAddComment.addTextChangedListener {
            val text = it.toString()
            if (it.toString().length >= 100) {
                marsToast(context.getString(R.string.fb_up_to_any_chars, "100"))
            }
            btnSend.imageResource = if (text.isEmpty() || text.trim().isEmpty()
            ) {
                R.mipmap.ic_send_message_no2
            } else R.mipmap.ic_send_message_yes2
        }

        btnSend.alphaClick {
            if (edtAddComment.text.toString().trim().isNotEmpty()) {
                exitMention()
                listener.onSend(edtAddComment.text.toString())
                edtAddComment.setText("")
                dismiss()
            }
        }
        edtAddComment.filters = arrayOf(object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                return if (source.toString().contentEquals("\n")) {
                    ""
                } else {
                    null
                }
            }

        }, InputFilter.LengthFilter(100))
        edtAddComment.setText(initText)
        try {
            if (insertedMentions != null) mentions.addMentions(insertedMentions)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        edtAddComment.setSelection(initText.length)
        edtAddComment.setOnCutCopyPasteListener(object :
            CutCopyPasteEditText.OnCutCopyPasteListener {
            override fun onCut() {
            }

            override fun onCopy() {
            }

            override fun onPaste() {
            }
        })

        initObserver()
    }

    private fun initObserver() {
        viewModel.mentionSearchLiveData.observe(context as AppCompatActivity) {
            Timber.d("it.size: ${it.size}")
            if (isMentioning) {
                Timber.d("it.size1: ${it.size}")
                val size = it.size + if (mentionSearchAdapter.isShowEveryone) 2 else 0
                if (size in 0..3) {
                    val params = mentionListView?.layoutParams
                    params?.height = context?.dp(size * 66) ?: 500
                    mentionListView?.layoutParams = params
                } else {
                    val params = mentionListView?.layoutParams
                    params?.height = context?.dp(248) ?: 500
                    mentionListView?.layoutParams = params
                }
                mentionSearchAdapter.list = it
                mentionSearchAdapter.notifyDataSetChanged()
            }

            if (it.isEmpty()) mentionEmpty(keyword)

            isLoadMentionSearch = false
        }
    }

    private fun startTimer() {
        mTimer = Timer()
        mTimerTask = object : TimerTask() {
            override fun run() {
                if (timerCount in 0..3) {
                    timerCount++
                } else if (timerCount >= 4) {
                    mainHandler.post {
                        if (StringUtils.isUsernameWord(keyword) && keyword.isNotEmpty()) {
                            //上次搜索为空
                            if (isMentionResultEmpty) {
                                if (keyword.length > oldKeyword.length) {
                                    if (keyword.substring(0, oldKeyword.length) == oldKeyword) {
                                        //no //本次keyword 单纯增加
                                    } else {
                                        exitMentionEmpty()
                                        //本次keyword 与上次不同
                                        isMentioning = true
                                        if ( !isLoadMentionSearch) {
                                            isLoadMentionSearch = true
                                            contentContainer.setBackgroundResource(R.color.colorPrimaryDark)
                                            getMentionSearchData(true)
                                        }
                                    }
                                } else if (keyword == oldKeyword) {
                                    //no
                                } else if (keyword.length <= oldKeyword.length) {
                                    //本次keyword 删除一些字符了
                                    exitMentionEmpty()
                                    isMentioning = true
                                    if (!isLoadMentionSearch) {
                                        isLoadMentionSearch = true
                                        contentContainer.setBackgroundResource(R.color.colorPrimaryDark)
                                        getMentionSearchData(true)
                                    }
                                }
                            } else {
                                isMentioning = true
                                if (!isLoadMentionSearch) {
                                    isLoadMentionSearch = true
                                    contentContainer.setBackgroundResource(R.color.colorPrimaryDark)
                                    getMentionSearchData(true)
                                }
                            }
                        } else {
                            //字符有表情和特殊字符（除了,._）就停止搜索
                            exitMention()
                        }
                        timerCount = -1
                    }
                }
            }
        }
        //更新帧率24
        mTimer?.schedule(mTimerTask, 0, 120)
    }

    private fun exitMention() {
        exitMentionEmpty()
        mentionSearchAdapter.list = listOf()
        mentionSearchAdapter.notifyDataSetChanged()
        isMentioning = false
        mentionListView?.isVisible = false
        contentContainer?.setBackgroundResource(R.color.transparent)
    }

    //进入搜索为空模式
    fun mentionEmpty(keyword: String) {
        isMentionResultEmpty = true
        oldKeyword = keyword
    }

    //对出搜索为空模式
    fun exitMentionEmpty() {
        isMentionResultEmpty = false
        oldKeyword = ""
    }

    private fun getMentionSearchData(isRefresh: Boolean) {
        Timber.d("keyword: $keyword")
        mainHandler.post {
            if (isRefresh && mentionSearchAdapter.list.isEmpty()) {
                val params = mentionListView.layoutParams
                params.height = context?.dp(248) ?: 500
                mentionListView.layoutParams = params
            }
        }
        if (isRefresh && keyword.isNotEmpty()) mentionSearchAdapter.isShowEveryone = false
        viewModel.mentionSearch(isRefresh, "", keyword, {
            if (isRefresh) {
                if (isMentioning) {
                    mentionListView?.visibility = if (it) View.INVISIBLE else View.VISIBLE
                    loadingMention?.isVisible = it
                } else {
                    mentionListView?.isVisible = false
                    loadingMention?.isVisible = false
                }
            }
            if (!it) isLoadMentionSearch = false
        }, {
            isLoadMentionSearch = false
        })
    }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_live_comment_input
    }

    override fun onDismiss() {
        super.onDismiss()
        listener.onDismiss(edtAddComment.text?.trim().toString(), mentions.insertedMentions)
        if (mTimerTask != null) {
            mTimerTask?.cancel()
            mTimerTask = null
        }
        if (mTimer != null) {
            mTimer?.cancel()
            mTimer = null
        }
    }

    interface OnLiveCommentEditListener {
        fun onSend(text: String)
        fun onDismiss(text: String, insertedMentions: MutableList<Mentionable>?)
    }

    override fun initViewModel(): LiveCommentInputViewModel {
        return LiveCommentInputViewModel()
    }
}