package com.example.baselibrary.utils

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.example.baselibrary.ui.ClickableMovementMethod
import timber.log.Timber
import java.util.*

/**
 * This is a helper class that should be used with [android.widget.EditText] or [TextView]
 * In order to have hash-tagged words highlighted. It also provides a click listeners for every hashtag
 *
 * Example :
 * #ThisIsHashTagWord
 * #ThisIsFirst#ThisIsSecondHashTag
 * #hashtagendsifitfindsnotletterornotdigitsignlike_thisIsNotHighlithedArea
 *
 */
class HashTagHelper private constructor(
    private val mHashTagWordColor: Int,
    private val searchTrigerListener: OnSearchTriggerListener,
    listener: OnHashTagClickListener?,
    private val isBold: Boolean
) : ClickableForegroundColorSpan.OnHashTagClickListener {
    private var mTextView: TextView? = null
    private val mOnHashTagClickListener: OnHashTagClickListener? = listener

    object Creator {
        fun create(
            color: Int,
            searchTrigerListener: OnSearchTriggerListener,
            listener: OnHashTagClickListener?,
            isBold: Boolean = false
        ): HashTagHelper {
            return HashTagHelper(color, searchTrigerListener, listener, isBold)
        }
    }

    interface OnHashTagClickListener {
        fun onHashTagClicked(hashTag: String?)

        fun onUsernameClicked(username: String?)
    }

    interface OnSearchTriggerListener {
        fun onSearch(hashTag: String)

        fun onRecommendation()

        fun onSearchClosed()
    }

    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            text: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            Timber.d("onTextChanged: %s", text)
            if (text.isNotEmpty()) {
                eraseAndColorizeAllText(text)
                tryTriggerHashTagSearch(text)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    fun handle(textView: TextView?, originalText: String? = null) {
        if (mTextView == null) {
            mTextView = textView
            mTextView?.addTextChangedListener(mTextWatcher)
            // in order to use spannable we have to set buffer type
            mTextView?.setText(originalText ?: mTextView?.text, TextView.BufferType.SPANNABLE)
            if (mTextView is AppCompatEditText) {
                (mTextView as AppCompatEditText).setSelection(originalText?.length ?: (mTextView?.text?.length ?: 0))
            }
            if (mOnHashTagClickListener != null) {
                // we need to set this in order to get onClick event
                mTextView?.movementMethod = ClickableMovementMethod.instance

                // after onClick clicked text become highlighted
                mTextView?.highlightColor = Color.TRANSPARENT
            } else {
                // hash tags are not clickable, no need to change these parameters
            }
            setColorsToAllHashTagsOrUsername(mTextView?.text ?: "")
        } else {
            throw RuntimeException("TextView is not null. You need to create a unique HashTagHelper for every TextView")
        }
    }

    private fun eraseAndColorizeAllText(text: CharSequence) {
        val spannable = mTextView?.text as Spannable
        val spans =
            spannable.getSpans(0, text.length, CharacterStyle::class.java)
        for (span in spans) {
            if (span is ClickableForegroundColorSpan) { //这里保留hide和more的StyleSpan
                try {
                    spannable.removeSpan(span)
                } catch (e: Exception){}
            }
        }
        setColorsToAllHashTagsOrUsername(text)
    }

    private fun tryTriggerHashTagSearch(text: CharSequence) {
        val lastHash = text.lastIndexOf("#")
        if (lastHash == -1) {
            //no hash sign, return
            return
        }
        if (lastHash == text.lastIndex) {
            //only hash at the end, trigger hashtag recommendation
            searchTrigerListener.onRecommendation()
            return
        }
        val substring = text.substring(lastHash + 1)
        substring.forEach {
            if (!Character.isLetterOrDigit(it)) {
                //contains non letter or digit chars, return
                searchTrigerListener.onSearchClosed()
                return@tryTriggerHashTagSearch
            }
        }
        if (substring.length in 1..20 && text.length < 150) {
            searchTrigerListener.onSearch(substring)
        } else {
            searchTrigerListener.onSearchClosed()
        }
    }

    private fun setColorsToAllHashTagsOrUsername(text: CharSequence) {
        val text = if (text.endsWith(".")) {
            text.substring(0, text.length - 1)
        } else {
            text
        }
        var startIndexOfNextHashSign: Int
        var index = 0
        while (index < text.length - 1) {
            val sign = text[index]
            var nextNotLetterDigitCharIndex =
                index + 1 // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
//            if (sign == '#') {
//                //hashtag
//                startIndexOfNextHashSign = index
//                nextNotLetterDigitCharIndex =
//                    findNextValidHashTagChar(text, startIndexOfNextHashSign)
//                setColorForHashTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex)
//            }
            if (sign == '@') {
                startIndexOfNextHashSign = index
                nextNotLetterDigitCharIndex =
                    findNextValidUsernameChar(text, startIndexOfNextHashSign)
                setColorForUsernameToEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex)
            }
            index = nextNotLetterDigitCharIndex
        }
    }

    private fun findNextValidHashTagChar(text: CharSequence, start: Int): Int {
        var nonLetterDigitCharIndex = -1 // skip first sign '#"
        for (index in start + 1 until text.length) {
            val sign = text[index]
            val isValidSign = Character.isLetterOrDigit(sign)
            if (!isValidSign) {
                nonLetterDigitCharIndex = index
                break
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length
        }
        return nonLetterDigitCharIndex
    }

    private fun findNextValidUsernameChar(text: CharSequence, start: Int): Int {
        var nonLetterDigitCharIndex = -1 // skip first sign '@"
        var beforeSign: Char = ' '
        for (index in start + 1 until text.length) {
            val sign = text[index]
            val isValidSign = Character.isLetterOrDigit(sign) || sign == '_' || sign == '.'

            if (!isValidSign) {
                nonLetterDigitCharIndex = if (beforeSign == '.' && sign == ' ') {
                    index - 1
                } else {
                    index
                }
                break
            } else {
                beforeSign = sign
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length
        }
        return nonLetterDigitCharIndex
    }

    private fun setColorForHashTagToTheEnd(
        startIndex: Int,
        nextNotLetterDigitCharIndex: Int
    ) {
        val text = mTextView?.text
        val potentialHashtag = text?.substring(startIndex, nextNotLetterDigitCharIndex)
        if (!Utils.HASHTAG_PATTERN.matcher(potentialHashtag).matches()) {
            return
        }
        val s = mTextView?.text as Spannable
        val span: CharacterStyle = ClickableForegroundColorSpan(mHashTagWordColor, this, isBold)
        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun setColorForUsernameToEnd(
        startIndex: Int,
        nextNotLetterDigitCharIndex: Int
    ) {
        val text = mTextView?.text
        val potentialUsername = text?.substring(startIndex, nextNotLetterDigitCharIndex)
        if (!Utils.USERNAME_PATTERN.matcher(potentialUsername).matches()) {
            return
        }
        val s = mTextView?.text as Spannable
        val span: CharacterStyle = ClickableForegroundColorSpan(mHashTagWordColor, this, isBold)
        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun getAllHashTags(withHashes: Boolean): List<String> {
        val text = mTextView?.text.toString()
        val spannable = mTextView?.text as Spannable

        // use set to exclude duplicates
        val hashTags: MutableSet<String> =
            LinkedHashSet()
        for (span in spannable.getSpans(
            0,
            text.length,
            CharacterStyle::class.java
        )) {
            hashTags.add(
                text.substring(
                    if (!withHashes) spannable.getSpanStart(span) + 1 /*skip "#" sign*/ else spannable.getSpanStart(
                        span
                    ),
                    spannable.getSpanEnd(span)
                )
            )
        }
        return ArrayList(hashTags)
    }

    override fun onHashTagClicked(hashTag: String?) {
        mOnHashTagClickListener?.onHashTagClicked(hashTag)
    }

    override fun onUsernameClicked(username: String?) {
        mOnHashTagClickListener?.onUsernameClicked(username)
    }

}