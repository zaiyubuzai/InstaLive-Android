package com.example.instalive.view

import android.content.Context

import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.example.instalive.R
import java.lang.Exception


/**
 *
 * @ProjectName:    Fambase - Android
 * @Package:        com.fambase.venus.app.ui
 * @ClassName:      MaxLimitRecyclerView
 * @Description:     java类作用描述
 * @Author:         wyp
 * @CreateDate:     2022-03-22 12:34
 * @UpdateUser:     更新者
 * @UpdateDate:     2022-03-22 12:34
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
/**
 * max limit-able RecyclerView
 */
class MaxLimitRecyclerView(context: Context, @Nullable attrs: AttributeSet?, defStyle: Int) :
    RecyclerView(context, attrs, defStyle) {
    private var mMaxHeight = 0
    private var mMaxWidth = 0

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, 0) {}

    private fun inti(attrs: AttributeSet?) {
        if (getContext() != null && attrs != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray =
                    getContext().obtainStyledAttributes(attrs, R.styleable.MaxLimitRecyclerView)
                if (typedArray.hasValue(R.styleable.MaxLimitRecyclerView_limit_maxHeight)) {
                    mMaxHeight = typedArray.getDimensionPixelOffset(
                        R.styleable.MaxLimitRecyclerView_limit_maxHeight,
                        -1
                    )
                }
                if (typedArray.hasValue(R.styleable.MaxLimitRecyclerView_limit_maxWidth)) {
                    mMaxWidth = typedArray.getDimensionPixelOffset(
                        R.styleable.MaxLimitRecyclerView_limit_maxWidth,
                        -1
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray?.recycle()
            }
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        var needLimit = false
        if (mMaxHeight >= 0 || mMaxWidth >= 0) {
            needLimit = true
        }
        if (needLimit) {
            var limitHeight: Int = getMeasuredHeight()
            var limitWith: Int = getMeasuredWidth()
            if (getMeasuredHeight() > mMaxHeight) {
                limitHeight = mMaxHeight
            }
            if (getMeasuredWidth() > mMaxWidth) {
                limitWith = mMaxWidth
            }
            setMeasuredDimension(limitWith, limitHeight)
        }
    }

    init {
        inti(attrs)
    }
}