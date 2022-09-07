package com.example.instalive.app.live.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baselibrary.views.BaseBottomPopup
import com.example.instalive.R
import com.example.instalive.app.SessionPreferences
import com.example.instalive.utils.marsToast
import com.example.instalive.utils.requestLivePermission
import kotlinx.android.synthetic.main.dialog_live_with_invitation.view.*
import splitties.views.onClick

@SuppressLint("ViewConstructor")
class GoLiveWithInviteDialog(
    context: Context,
    val liveId: String,
    private val hostAvatar: String,
    private val hostName: String,
    private val lockedLive: Boolean = false
) :
    BaseBottomPopup<GoLiveWithViewModel>(context) {

    private var isRefused = false
    private var isAgreed = false

    @SuppressLint("SetTextI18n")
    override fun initData() {
        title.text = "$hostName ${context.getString(R.string.fb_livewith_invite_title)}"
//        btnAccept.text = context.getString(R.string.go_live_with, hostName)
        Glide.with(context)
            .load(hostAvatar)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(avatarHost)

        Glide.with(context)
            .load(SessionPreferences.portrait)
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(avatarMe)

        btnAccept.onClick {
            if (lockedLive){
                marsToast(R.string.fb_with_call_in_unlock_live)
                dismiss()
            } else {
                doRequestPermissions()
            }
        }
        btnDecline.onClick {
            viewModel.declineLiveWith(liveId)
        }
        viewModel.agreeLiveWithData.observe(this) {
            isAgreed = true
            dismiss()
        }
        viewModel.rejectLiveWithData.observe(this) {
            isRefused = true
            dismiss()
        }
    }

    private fun doRequestPermissions() {
        context?.requestLivePermission({
            viewModel.agreeLiveWith(liveId)
        })
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_live_with_invitation

    override fun initViewModel(): GoLiveWithViewModel =
        GoLiveWithViewModel()

    override fun onDismiss() {
        if (!isRefused && !isAgreed){
            viewModel.declineLiveWith(liveId)
        }
        viewModel.agreeLiveWithData = MutableLiveData()
        viewModel.rejectLiveWithData = MutableLiveData()
    }
}