package com.example.instalive.app.live

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.ActivityLiveHostBinding
import com.venus.dm.db.entity.ConversationsEntity
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.neutralButton
import splitties.alertdialog.appcompat.positiveButton
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.bundleOrNull
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec
import timber.log.Timber
import java.util.*

@ExperimentalStdlibApi
class LiveHostActivity : LiveBaseActivity<ActivityLiveHostBinding>() {
//    private var currentFragmentTag: String? = null
//    private lateinit var hostFragment: LiveInteractionHostFragment
//    private val liveFragment: LiveFragment by lazy { LiveFragment() }
//    lateinit var conversationsEntity: ConversationsEntity
//    private var uuid: UUID? = null
//    private var createLiveType: String? = null
//    var giftTicketData: LiveGiftDetail? = null
//    var justNowGiftTicketData: LiveGiftDetail? = null
//    var isLiveNeedResume = false
//    var liveId: String? = null
//    var eventId: String? = null
//
//    //用户是否可以直播，如果用户没有直播权限则不显示
//    private var isCanLive = true

    override fun initData(savedInstanceState: Bundle?) {
//        screenName = "record_video_view"
//
//        withExtras(RecordExtraSpec) {
//            this@LiveHostActivity.isCanLive = isCanLive
//            this@LiveHostActivity.uuid = uuid
//            this@LiveHostActivity.createLiveType = createLiveType
//            this@LiveHostActivity.conversationsEntity = conversationsEntity
//            this@LiveHostActivity.isLiveNeedResume = isLiveNeedResume
//            this@LiveHostActivity.liveId = liveId?:""
//            this@LiveHostActivity.eventId = eventId
//        }
//
//        Timber.d("liveId: $liveId conversationsEntity.type: ${conversationsEntity.type}")
//        if (conversationsEntity.type == 2) {
//            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
//        }
//
//        initBottomViewpager()
//
//        cover.setOnClickListener {}
//
//        touchFl.setOnTouchListener { _, event ->
//            event?.let {
//                liveFragment.isLiveViewTouched(it.rawX.toInt(), it.rawY.toInt())
//            }
//            false
//        }
    }


//    fun getLiveUser(): MutableList<LiveUserInfo> {
//        return liveFragment.liveUsers.toMutableList()
//    }
//
//    fun muteLocalAudioStream(isMute: Boolean) {
//        liveFragment.muteLocalAudioStream(isMute)
//    }
//
//    fun openTicketGiftDialog(){
//        liveFragment.popupOpenGift(3)
//    }
//
//    fun getLiveInitData(): LiveInitInfo? {
//        return liveFragment.getLiveInitData()
//    }
//
//    fun showInteractionFragment(roomId: String, isOpenBeauty: Boolean, conversationId:String) {
//        BarUtils.setStatusBarLightMode(this, false)
//        val hostInteractionFragment = LiveInteractionHostFragment()
//        hostInteractionFragment.liveId = roomId
//        hostInteractionFragment.conversationId = conversationId
//        hostInteractionFragment.conversationsEntity = conversationsEntity
//        hostInteractionFragment.isHost = true
//        hostInteractionFragment.makeUpEnabled = isOpenBeauty
//        hostInteractionFragment.giftSecondContainer = giftSecondContainer
//        hostInteractionFragment.giftFirstContainer = giftFirstContainer
//        hostInteractionFragment.liveLikesAnimView = liveLikesAnimView
//        hostInteractionFragment.giftAnim = giftAnim
//        hostFragment = hostInteractionFragment
//        giftContainer.isVisible = true
//        pager?.isVisible = true
//        pager?.adapter = LiveHostPagerAdapter(hostInteractionFragment, this)
//        pager?.offscreenPageLimit = 3
//        pager?.setCurrentItem(1, false)
//    }
//
//    private fun showFragment(fragment: Fragment) {
//        val existFragment =
//            supportFragmentManager.findFragmentByTag(fragment.javaClass.simpleName)
//        val transaction = supportFragmentManager.beginTransaction()
//
//        if (currentFragmentTag != null) {
//            val oldFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
//            if (oldFragment != null) {
//                transaction.remove(oldFragment)
//            }
//        }
//        currentFragmentTag = if (existFragment == null) {
//            transaction.add(splitties.alertdialog.appcompat.R.id.fragment_container, fragment, fragment.javaClass.simpleName)
//            fragment.javaClass.simpleName
//        } else {
//            transaction.show(existFragment)
//            existFragment.javaClass.simpleName
//        }
//        transaction.commit()
//        supportFragmentManager.executePendingTransactions()
//    }
//
//    private fun initBottomViewpager() {
//        showFragment(liveFragment)
//    }
//
//    override fun initViewModel(): RecordViewModel {
//        return getActivityViewModel(RecordViewModel::class.java)
//    }
//
//    override fun getDataBindingConfig(): DataBindingConfig {
//        return DataBindingConfig(splitties.alertdialog.appcompat.R.layout.record_activity, viewModel)
//    }
//
//    fun hideInteractionContainer() {
//        fragmentHostInteractionContainer?.isVisible = false
//    }
//
//    fun showInteractionContainer() {
//        fragmentHostInteractionContainer?.isVisible = true
//    }
//
//    fun tryHangUp(userName: String, targetUserId:String) {
//        alertDialog {
//            message = getString(splitties.alertdialog.appcompat.R.string.hang_up_live_video, userName)
//            positiveButton(splitties.alertdialog.appcompat.R.string.confirm) {
//                liveFragment.hungUpLiveWith(targetUserId)
//                it.dismiss()
//            }
//            neutralButton(splitties.alertdialog.appcompat.R.string.cancel) {
//                it.dismiss()
//            }
//        }.show()
//    }
//
//    fun showProfile(userId: String, username: String, role:Int){
//        hostFragment.showOtherProfile(userId, username, role)
//    }
//
////    fun hideLiveWithContainer() {
////        liveWithControl?.isVisible = false
////    }
//
//    fun showErrorPrompt(desc: String) {
//        errorPrompt.isVisible = true
//        Glide.with(MarsApp.appInstance)
//            .load(SessionPreferences.portrait)
//            .placeholder(splitties.alertdialog.appcompat.R.drawable.ic_default_avatar)
//            .skipMemoryCache(false)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 8)))
//            .into(activityCover)
//        activityDesc.text = desc
//        errorPrompt.isVisible = true
//        activityDone.onClick {
//            this.finish()
//        }
//        errorPrompt.onClick {}
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        MessageFragment.isStartLiveButtonClick = false
//        sharedViewModel.liveStateInfoLiveData = MutableLiveData()
//        sharedViewModel.liveUsersSizeData = MutableLiveData()
//    }
//
//    fun updateHostLiveWithState(isMicrophone: Boolean, liveWithUserInfo: LiveUserInfo?) {
//        if (this::hostFragment.isInitialized) {
//            hostFragment.isMicrophone = isMicrophone
//            hostFragment.currentLiveWithUser = liveWithUserInfo
//        }
//    }
//
//    fun microphoneState(): Boolean {
//        return liveFragment.isMicrophone
//    }

    companion object :
        ActivityIntentSpec<LiveHostActivity, RecordExtraSpec> by activitySpec(RecordExtraSpec)

    object RecordExtraSpec : BundleSpec() {
        var hashtag: String? by bundleOrNull()
        var source: String? by bundleOrNull()
        var uuid: UUID? by bundleOrNull()
        var commentData: String? by bundleOrNull()
        var isCanLive: Boolean = true
        var isLiveNeedResume: Boolean = false
        var createLiveType: String? by bundleOrNull()
        var conversationsEntity: ConversationsEntity by bundle()
        var liveId: String? by bundleOrNull()
        var eventId: String? by bundleOrNull()
    }

    override fun onBackPressed() {
//        if (currentFragmentTag != null) {
////            val fragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
//        } else {
//            super.onBackPressed()
//        }
    }

//    private inner class LiveHostPagerAdapter(
//        val liveInteractionFragment: LiveInteractionHostFragment,
//        act: AppCompatActivity,
//    ) : FragmentStateAdapter(act) {
//        override fun getItemCount(): Int = 2
//
//        override fun createFragment(position: Int): Fragment {
//            return when (position) {
//                0 -> {
//                    val fragment = LiveEmptyFragment()
//                    fragment.showClose = false
//                    fragment.isHost = true
//                    fragment
//                }
//                1 -> liveInteractionFragment
//                else -> LiveEmptyFragment().apply {
//                    isHost = true
//                }
//            }
//        }
//    }

    override fun initViewModel(): LiveViewModel {
        return getActivityViewModel(LiveViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_live_host, viewModel)
    }
}