package com.example.baselibrary.ui

import android.text.Selection
import android.text.method.BaseMovementMethod
import android.widget.TextView
import android.text.Spannable
import android.view.MotionEvent
import android.text.style.ClickableSpan
import com.example.baselibrary.R
import com.example.baselibrary.utils.HashTagHelper

/**
 * A movement method that traverses links in the text buffer and fires clicks. Unlike
 * [LinkMovementMethod], this will not consume touch events outside [ClickableSpan]s.
 */
class ClickableMovementMethod : BaseMovementMethod() {

    var mOnHashTagClickListener: HashTagHelper.OnHashTagClickListener? = null

    private val DOUBLE_TAP_TIMEOUT = 300

    private var firstTouchTime = 0L
    override fun canSelectArbitrarily(): Boolean {
        return false
    }

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (link.size > 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                } else {
                    Selection.setSelection(
                        buffer, buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                }
                return true
            } else {
                Selection.removeSelection(buffer)
                if (action == MotionEvent.ACTION_UP) {
//                    widget.performClick()

//                    widget.performClick();
                    if (System.currentTimeMillis() - firstTouchTime > DOUBLE_TAP_TIMEOUT) {
                        firstTouchTime = System.currentTimeMillis()
                    } else {
                        firstTouchTime = 0L
                        if (mOnHashTagClickListener != null) {
                            val name = widget.getTag(R.id.message_tag_first) as String
                            val content = name + ": " + widget.text.toString()
                            mOnHashTagClickListener!!.onHashTagClicked(content)
                        }
                    }
                }
            }
        }
        return false
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.removeSelection(text)
    }

    companion object {
        private var sInstance: ClickableMovementMethod? = null
        val instance: ClickableMovementMethod?
            get() {
                if (sInstance == null) {
                    sInstance = ClickableMovementMethod()
                }
                return sInstance
            }
    }
}