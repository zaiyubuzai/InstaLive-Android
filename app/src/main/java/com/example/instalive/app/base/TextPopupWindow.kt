package com.example.instalive.app.base

import android.app.Activity
import android.widget.PopupWindow
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.constraintlayout.widget.ConstraintSet
import com.example.baselibrary.utils.BarUtils

import com.example.instalive.R
import com.example.instalive.databinding.PopupSimpleTextViewerBinding
import splitties.views.onClick


class TextPopupWindow(private val activity: Activity, private val content: String) :
    PopupWindow(activity) {

    val binding: PopupSimpleTextViewerBinding by lazy {
        PopupSimpleTextViewerBinding.inflate(LayoutInflater.from(activity))
    }

    init {
        contentView = binding.root
        contentView.setOnClickListener { dismiss() }
        initView()
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(0x55000000))
    }

    private fun initView() {
        val set = ConstraintSet()
        set.clone(binding.container)
        set.setMargin(R.id.close, ConstraintSet.TOP, BarUtils.statusBarHeight)
        set.applyTo(binding.container)
        binding.messageContent.text = content
        binding.close.onClick {
            dismiss()
        }
        binding.container.onClick {}
    }

    fun show() {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(
                activity.window.decorView, Gravity.CENTER,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }

}