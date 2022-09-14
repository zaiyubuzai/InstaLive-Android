package com.example.instalive.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.instalive.R
import com.example.instalive.databinding.ItemGiftsBinding
import com.example.instalive.model.GiftData
import splitties.views.onClick


class GiftListAdapter(
    var viewList: List<GiftData>,
    var giftId: String?,
    val GLST: Int,
    val isLiveMode: Boolean,
    var onReset: (index: Int, giftData:GiftData) -> Unit,
    val showGlobal: (Boolean) -> Unit,
) : RecyclerView.Adapter<GiftListAdapter.GiftsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftsViewHolder {
        return GiftsViewHolder(
            ItemGiftsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GiftsViewHolder, position: Int) {
//        if (GLST == 4){
//            val params = holder.binding.giftImage.layoutParams as ConstraintLayout.LayoutParams
//            params.width = holder.itemView.context.dp(64)
//            params.height = holder.itemView.context.dp(64)
//            holder.binding.giftImage.layoutParams = params
//        }
//        if (isLiveMode){
//            holder.binding.giftName.textColorResource = R.color.black
//            holder.binding.coins1.textColorResource = R.color.gray_333
//        } else {
//            holder.binding.giftName.textColorResource = R.color.white
//            holder.binding.coins1.textColorResource = R.color.color_ccc
//        }
        val giftData = viewList[position]
        holder.binding.giftName.text = holder.itemView.context.getString(
            R.string.lbl_live_gift_coin_cost,
            giftData.coins.toString()
        )
//        holder.binding.coins1.text = holder.itemView.context.getString(
//            R.string.lbl_live_gift_coin_cost,
//            liveGiftDetail.coins.toString()
//        )
//        holder.binding.giftName.text = liveGiftDetail.name
        Glide.with(holder.itemView.context)
            .load(giftData.image)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.mipmap.ic_live_gift_default)
            .fitCenter()
            .into(holder.binding.giftImage)

        holder.itemView.onClick {
            onReset.invoke(position, giftData)
            holder.binding.container.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
        }
        if (giftId == giftData.id) {
            holder.binding.container.setBackgroundResource(R.drawable.bg_live_gifts_list_selected)
        } else {
            holder.binding.container.setBackgroundResource(0)
        }
    }

    override fun getItemCount(): Int {
        return viewList.size
    }

    class GiftsViewHolder(val binding: ItemGiftsBinding) :
        RecyclerView.ViewHolder(binding.root)

}