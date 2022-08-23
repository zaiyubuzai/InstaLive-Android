package com.example.baselibrary.utils

import android.text.InputFilter
import android.text.Spanned

class MyLengthFilter(
    /**
     * @return the maximum length enforced by this input filter
     */
    private val max: Int,
    private val onOver: (Int) -> Unit,
) :
    InputFilter {
    override fun filter(
        source: CharSequence, start: Int, end: Int, dest: Spanned,
        dstart: Int, dend: Int
    ): CharSequence? {
        var keep = max - (dest.length - (dend - dstart))
        return if (keep <= 0) {
            //这里，用来给用户提示
            onOver.invoke(max)
            ""
        } else if (keep >= end - start) {
            null // keep original
        } else {
            keep += start
            if (Character.isHighSurrogate(source[keep - 1])) {
                --keep
                if (keep == start) {
                    return ""
                }
            }
            source.subSequence(start, keep)
        }
    }

}