package com.example.baselibrary.utils

import android.graphics.Typeface
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt

/**
 * Created by danylo.volokh on 12/22/2015.
 * This class is a combination of [android.text.style.ForegroundColorSpan]
 * and [ClickableSpan].
 *
 * You can set a color of this span plus set a click listener
 */
class ClickableForegroundColorSpan(
    @param:ColorInt private val mColor: Int,
    private val mOnHashTagClickListener: OnHashTagClickListener?,
    private val bold: Boolean
) : ClickableSpan() {

    interface OnHashTagClickListener {
        fun onHashTagClicked(hashTag: String?)
        fun onUsernameClicked(username: String?)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = mColor
        if (bold) {
            ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    override fun onClick(widget: View) {
        val text = (widget as TextView).text
        val s = text as Spanned
        val start = s.getSpanStart(this)
        val end = s.getSpanEnd(this)
        val hashtagOrAt = text.elementAt(start)
        if (hashtagOrAt == '@') {
            mOnHashTagClickListener?.onUsernameClicked(
                text.subSequence(start + 1 /*skip "@" sign*/, end).toString()
            )
        } else if(hashtagOrAt == '#') {
            mOnHashTagClickListener?.onHashTagClicked(
                text.subSequence(start + 1 /*skip "#" sign*/, end).toString()
            )
        }
    }
}