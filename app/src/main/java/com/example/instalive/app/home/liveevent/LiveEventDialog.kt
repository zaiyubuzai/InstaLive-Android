package com.example.instalive.app.home.liveevent

import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.baselibrary.utils.TimeUtils
import com.example.instalive.utils.marsToast
import com.example.baselibrary.views.BaseBottomPopup
import com.example.instalive.InstaLiveApp
import com.example.instalive.R
import com.example.instalive.databinding.ItemInterestedMemberLayoutBinding
import com.example.instalive.model.EventData
import com.example.instalive.model.InterestedMemberData
import kotlinx.android.synthetic.main.dialog_live_event.view.*
import kotlinx.coroutines.*
import splitties.dimensions.dp
import splitties.mainhandler.mainHandler
import splitties.systemservices.layoutInflater
import splitties.views.onClick
import splitties.views.setCompoundDrawables
import splitties.views.textColorResource
import splitties.views.textResource
import java.text.SimpleDateFormat
import java.util.*

class LiveEventDialog(
    context: Context,
    private val eventData: EventData,
    private val isOwner: Boolean,
    private val goStartLive: (String) -> Unit,
    private val onUpdateEvent: (EventData) -> Unit,
) :
    BaseBottomPopup<LiveEventDialogViewModel>(context) {

    private var btnMode = 1
    private lateinit var adapter: InterestedMembersAdapter
    private var timeJob: Job? = null
//    private val eventDetailObserver: (EventDetailData?) -> Unit = { detail ->
//        detail?.let {
//            addImage?.isVisible = it.interestedMemberCount > 10
//            memberNumber?.isVisible = it.interestedMemberCount > 10
//            memberNumber?.text = VenusNumberFormatter.format((it.interestedMemberCount-10).toLong())
//            it.interestedList?.let {  list ->
//                adapter.membersData = list
//                adapter.notifyDataSetChanged()
//            }
//        }
//    }
    private val interestObserver: () -> Unit = {
        mainHandler.post {
            if (isOwner){
                eventData.isOwnerInterest = !eventData.isOwnerInterest
            } else {
                eventData.isInterest = !eventData.isInterest
            }
            if (btnMode == 0) {
                btnMode = 1
                btnText.textResource = R.string.fb_interested
//                btnText.setCompoundDrawables(start = R.drawable.icon_check_right)
//                btnText.textColorResource = R.color.text_gray_color
//                startLiveBtn.setBackgroundResource(R.drawable.bg_primary_button_brown)
            } else if (btnMode == 1) {
                btnMode = 0
                btnText.textResource = R.string.fb_interest
//                btnText.setCompoundDrawables(start = R.drawable.icon_interest)
                btnText.textColorResource = R.color.black
                startLiveBtn.setBackgroundResource(R.drawable.bg_btn_yellow_12c)
            }
            onUpdateEvent.invoke(eventData)
//            viewModel.getEventDetail(eventData.eventId, {
//                marsToast(it)
//            }, onStatus = {
//                loading?.isVisible = it == StatusEvent.LOADING
//            })
        }
    }
    private val cancelEventObserver: (Any?) -> Unit = {
        marsToast(R.string.fb_cancelled)
        dismiss()
    }

    override fun initData() {
        viewModel.reset()

        moreBtn.isVisible = isOwner && (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 - eventData.eventTS < 0
        eventTime.text = buildEventTimeText()
        eventDesc.text = eventData.description
        eventName.text = eventData.name

        if (isOwner) {
            if ((System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 - eventData.switchStartTS > 0) {
                btnText.textResource = R.string.fb_start_live
                btnText.setCompoundDrawables(start = null)
                btnMode = 2
            } else {
                btnMode = if (eventData.isOwnerInterest) 1 else 0
                btnText.textResource =
                    if (eventData.isOwnerInterest) R.string.fb_interested else R.string.fb_interest
//                btnText.setCompoundDrawables(start = if (eventData.isOwnerInterest) R.drawable.icon_check_right else R.drawable.icon_interest)
//                btnText.textColorResource = if (eventData.isOwnerInterest) R.color.text_gray_color else R.color.black
//                startLiveBtn.setBackgroundResource(if (eventData.isOwnerInterest)R.drawable.bg_primary_button_brown else R.drawable.bg_btn_yellow_12c)
                startTimeJob()
            }
        } else {
            btnMode = if (eventData.isInterest) 1 else 0
            btnText.textResource =
                if (eventData.isInterest) R.string.fb_interested else R.string.fb_interest
//            btnText.setCompoundDrawables(start = if (eventData.isInterest) R.drawable.icon_check_right else R.drawable.icon_interest)
//            btnText.textColorResource = if (eventData.isInterest) R.color.text_gray_color else R.color.black
//            startLiveBtn.setBackgroundResource(if (eventData.isInterest)R.drawable.bg_primary_button_brown else R.drawable.bg_btn_yellow_12c)
        }

        initMemberList()
        initListener()
        initObserver()

//        viewModel.getEventDetail(eventData.eventId, {
//            marsToast(it)
//        }, onStatus = {
//            loading?.isVisible = it == StatusEvent.LOADING
//        })
    }

    private fun startTimeJob() {
        timeJob?.cancel()
        timeJob = CoroutineScope(Dispatchers.IO).launch {
            while (this.isActive) {
                delay(1000)
                if ((System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 - eventData.switchStartTS > 0) {
                    withContext(Dispatchers.Main) {
                        btnText?.textResource = R.string.fb_start_live
                        btnText?.setCompoundDrawables(start = null)
                        btnText?.textColorResource = R.color.black
                        startLiveBtn?.setBackgroundResource(R.drawable.bg_btn_yellow_12c)
                        btnMode = 2
                    }
                }

                if ((System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 - eventData.eventTS > 0){
                    moreBtn?.isVisible = false
                    timeJob?.cancel()
                }
            }
        }
    }

    private fun initMemberList() {
        adapter = InterestedMembersAdapter(listOf())
        val layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        memberList.layoutManager = layoutManager
        memberList.adapter = adapter
        memberList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position != 0) {
                    outRect.left = context.dp(-8)
                }
            }
        })
    }

    private fun initListener() {
        moreBtn.onClick {
            showMoreDialog()
        }

        startLiveBtn.onClick {
            if (btnMode == 2) { //
                goStartLive.invoke(eventData.eventId)
                dismiss()
            } else {
//                viewModel.setEventInterest(
//                    eventData.eventId,
//                    if (btnMode == 0) 1 else 0,
//                    onFailed = {
//                        marsToast(it)
//                    },
//                    onStatus = {
//                        loading?.isVisible = it == StatusEvent.LOADING
//                    },
//                    interestObserver)
            }
        }
    }

    private fun showMoreDialog() {
        val c = context ?: return
        val items = arrayOf(c.getString(R.string.fb_cancel_event), c.getString(R.string.fb_cancel))
        AlertDialog.Builder(c).setItems(items) { dialogInterface: DialogInterface?, i: Int ->
            when (i) {
                0 -> {
//                    viewModel.cancelEvent(eventData.eventId, onFailed = {
//                        marsToast(it)
//                    }, onStatus = {
//                        loading?.isVisible = it == StatusEvent.LOADING
//                    })
                    dialogInterface?.dismiss()
                }
                1 -> {
                    dialogInterface?.dismiss()
                }
            }
        }.create().show()
    }

    private fun initObserver() {
//        viewModel.eventDetailLiveData.observeForever(eventDetailObserver)
//        viewModel.cancelEventLiveData.observeForever(cancelEventObserver)
    }

    //Feb 17 at 9:00 PM
    private fun buildEventTimeText(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = eventData.eventTS * 1000
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val hour = calendar[Calendar.HOUR]
        val minute = calendar[Calendar.MINUTE]
        val amOrPM = calendar[Calendar.AM_PM]
        if (TimeUtils.isTargetDay(calendar, 0)) {
            return "Today at ${if (hour == 0) "12" else  "$hour"}:${if (minute < 10) "0$minute" else minute} ${if (amOrPM == Calendar.AM) "AM" else "PM"}"
        } else if (TimeUtils.isTargetDay(calendar, 1)) {
            return "Tomorrow at ${if (hour == 0) "12" else  "$hour"}:${if (minute < 10) "0$minute" else minute} ${if (amOrPM == Calendar.AM) "AM" else "PM"}"
        }
        val abbrMonthName = SimpleDateFormat("MMM").format(calendar.time)
        return "$abbrMonthName $day at ${if (hour == 0) "12" else  "$hour"}:${if (minute < 10) "0$minute" else minute} ${if (amOrPM == Calendar.AM) "AM" else "PM"}"
    }

    override fun onDismiss() {
        super.onDismiss()
//        viewModel.eventDetailLiveData.removeObserver(eventDetailObserver)
//        viewModel.cancelEventLiveData.removeObserver(cancelEventObserver)
        timeJob?.cancel()
        timeJob = null
    }

    override fun initViewModel(): LiveEventDialogViewModel {
        return LiveEventDialogViewModel()
    }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_live_event
    }

    class InterestedMembersAdapter(
        var membersData: List<InterestedMemberData>
    ) : RecyclerView.Adapter<InterestedMembersAdapter.InterestedMembersViewHolder>() {

        class InterestedMembersViewHolder(val binding: ItemInterestedMemberLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): InterestedMembersViewHolder {
            return InterestedMembersViewHolder(
                DataBindingUtil.inflate(
                    parent.layoutInflater,
                    R.layout.item_interested_member_layout,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: InterestedMembersViewHolder, position: Int) {
            val data = membersData[position]
            val options =
                RequestOptions.bitmapTransform(RoundedCorners(holder.itemView.context.dp(12)))
            Glide.with(holder.itemView.context)
                .load(data.portrait)
                .apply(options)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.avatar)
            holder.binding.nickname.text = data.nickname
            holder.binding.nickname.isVisible = itemCount <= 1
        }

        override fun getItemCount(): Int {
            return membersData.size
        }
    }
}