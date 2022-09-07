package com.example.instalive.app.live.ui

import android.annotation.SuppressLint
import android.content.Context
import com.example.instalive.R
import com.example.instalive.utils.requestLivePermission
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.android.synthetic.main.dialog_raise_your_hand.view.*
import splitties.views.onClick

@SuppressLint("ViewConstructor")
class LiveRaiseYourHandDialog(context: Context, val raiseIt: () -> Unit) : BottomPopupView(context) {

    override fun onCreate() {
        btnNeverMind.onClick {
            dismiss()
        }
        btnRaiseHand.onClick {
            context?.requestLivePermission(
                go = {
                    raiseIt()
                    dismiss()
                }, no = {

                }
            )
        }
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_raise_your_hand
}