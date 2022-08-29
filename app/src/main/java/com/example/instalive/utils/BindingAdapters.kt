package com.example.instalive.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.venus.dm.db.entity.MessageEntity
import splitties.dimensions.dp
import timber.log.Timber
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("android:text")
fun setLongText(view: TextView, num: Long) {
    view.text = num.toString()
}

@BindingAdapter("utf")
fun setUtfText(view: TextView, text: String?) {
    if (text == null) {
        return
    }
    view.text = String(text.toByteArray(), Charsets.UTF_8)
}

@BindingAdapter("glideImageUri")
fun setGlideImageUri(view: ImageView, uri: String?) {
    Timber.d("Glide set image, uri: $uri")
    if (uri != null) {
        Glide.with(view.context)
            .load(uri)
            .into(view)
    }
}

@BindingAdapter("isVisible")
fun setVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}

@BindingAdapter("isRVItemVisible")
fun setRecyclerViewItemVisible(itemView: View, isVisible: Boolean) {
    val param = itemView.layoutParams as RecyclerView.LayoutParams
    if (isVisible) {
        param.height = RecyclerView.LayoutParams.WRAP_CONTENT
        param.width = RecyclerView.LayoutParams.MATCH_PARENT
    } else {
        param.height = 0
        param.width = 0
    }
    itemView.isVisible = isVisible
}


@BindingAdapter("android:layout_marginBottom")
fun setLayoutMarginBottom(view: View, dimen: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.bottomMargin = dimen.toInt()
    view.layoutParams = layoutParams
}

@BindingAdapter("formatNumber")
fun formatNumber(view: TextView, number: Long?) {
    if (number == null) {
        return
    }
    view.text = VenusNumberFormatter.format(number)
}

@SuppressLint("SetTextI18n")
@BindingAdapter("formatBalance")
fun formatBalance(view: TextView, number: Double?) {
    if (number == null) {
        return
    }
    if (number > 1000) {
        view.text = "$${VenusNumberFormatter.format(number.toLong())}"
    } else if (number < 1) {
        val formatter = DecimalFormat("0.00")
        formatter.roundingMode = RoundingMode.FLOOR
        view.text = "$${formatter.format(number)}"
    } else {
        val formatter = DecimalFormat("###.#")
        formatter.roundingMode = RoundingMode.FLOOR
        view.text = "$${formatter.format(number)}"
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("formatViewCounts")
fun formatViewCounts(view: TextView, number: Long?) {
    if (number == null) {
        view.isVisible = false
        return
    }
    if (number == -1L) {
        view.isVisible = false
    } else {
        view.isVisible = true
        view.text = "${VenusNumberFormatter.format(number)} views"
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("formatVideosCounts")
fun formatVideosCounts(view: TextView, number: Long?) {
    if (number == null) {
        return
    }
    view.text = "${VenusNumberFormatter.format(number)} Videos"
}


@SuppressLint("SetTextI18n")
@BindingAdapter("formatTime")
fun formatTime(view: TextView, timestamp: Long?) {
    if (timestamp == null) {
        view.isVisible = false
        return
    }
    val span = System.currentTimeMillis() / 1000 - timestamp //距离现在的时间跨度，秒
    if (span < 0) {
        view.isVisible = false
        return
    }
    view.isVisible = true
    when {
        span < 3600 -> {
            val minutes = span / 60
            view.text = "${minutes}min"
        }
        span < 86400 -> {
            val hour = span / 3600
            view.text = "${hour}h"
        }
        span < 604800 -> {
            val days = span / 86400
            view.text = "${days}d"
        }
        span < 31536000 -> {
            view.text = DateFormat.format("MM-dd", Date(timestamp * 1000))
        }
        else -> {
            view.text = DateFormat.format("MM-dd-yyyy", Date(timestamp * 1000))
        }
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("formatMessageTime")
fun formatMessageTime(view: TextView, timestamp: Long?) {
    if (timestamp?.equals(0L) == true) {
        view.isVisible = false
        return
    } else {
        view.isVisible = true
    }
    var ts = timestamp
    if (timestamp ?: 0L > System.currentTimeMillis() * 1000) {
        ts = (timestamp ?: 0L) / 10000L
    }
    if (ts == null) {
        view.isVisible = false
        return
    }
    val span = System.currentTimeMillis() / 1000 - ts / 1000 //距离现在的时间跨度，秒
    when {
        span < 86400 -> {
            view.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(ts)
        }
        span < 604800 -> {
            view.text = SimpleDateFormat("EEEE HH:mm", Locale.getDefault()).format(ts)
        }
        else -> {
            view.text = SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.getDefault()).format(ts)
        }
    }
}

@BindingAdapter("musicLength")
fun musicLength(view: TextView, length: Int = 0) {
    val minute = length / 60
    val second = length % 60
    val s = StringBuilder()
    if (minute < 10) {
        s.append("0")
    }
    s.append(minute)
    s.append(":")
    if (second < 10) {
        s.append("0")
    }
    s.append(second)
    view.text = s
}

@BindingAdapter("imageViewAvatar")
fun setImageViewAvatar(view: ImageView, uri: String?) {
    if (view.context == null) return
    val options = RequestOptions.bitmapTransform(RoundedCorners(view.context.dp(12)))
    Glide.with(view.context)
        .load(uri)
        .apply(options)
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(view)
}

@BindingAdapter("drawableTopVector")
fun setDrawableTopVector(view: TextView, @DrawableRes res: Int) {
    view.setCompoundDrawablesRelativeWithIntrinsicBounds(
        null,
        AppCompatResources.getDrawable(view.context, res),
        null,
        null
    )
}

@BindingAdapter("messageVideoLength")
fun formatVideoLength(view: TextView, length: Int) {
    val timeStr = buildString {
        append(length / 1000 / 60)
        append(":")
        val second = length / 1000 % 60
        if (second < 10) {
            append("0$second")
        } else {
            append(second)
        }
    }
    view.text = timeStr
}

@BindingAdapter("formatVideoTime")
fun formatVideoTime(view: TextView, length: Int) {
    val timeStr = buildString {
        append(length / 60)
        append(":")
        val second = length % 60
        if (second < 10) {
            append("0$second")
        } else {
            append(second)
        }
    }
    view.text = timeStr
}

@BindingAdapter("layoutMarginTop")
fun setTopMargin(view: View, topMargin: Int) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, topMargin,
        layoutParams.rightMargin, layoutParams.bottomMargin
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("layoutMarginBottom")
fun setBottomMargin(view: View, bottomMargin: Int) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, layoutParams.topMargin,
        layoutParams.rightMargin, bottomMargin
    )
    view.layoutParams = layoutParams
}

object VenusNumberFormatter {
    private val suffixes: NavigableMap<Long, String> = TreeMap()

    init {
        suffixes[1_000L] = "K"
        suffixes[1_000_000L] = "M"
        suffixes[1_000_000_000L] = "B"
    }
    var thousandFormat = DecimalFormat(",##0")

    fun format(value: Long): String {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return value.toString() //deal with easy case
        val e: Map.Entry<Long, String> = suffixes.floorEntry(value)
        val divideBy = e.key
        val suffix = e.value
        val truncated =
            value / (divideBy / 10) //the number part of the output times 10
        return (truncated / 10.0).toString() + suffix
    }

    fun formatThousand(value: Long): String{
        return thousandFormat.format(value) // 123,456,789.99
    }
}

@BindingAdapter("formatThousand")
fun formatThousand(view: TextView, diamonds:Long?){
    if (diamonds != null) {
        view.text = VenusNumberFormatter.formatThousand(diamonds)
    }
}

@BindingAdapter("messageVideoCover")
fun setMessageVideoCover(view: ShapeableImageView, messageEntity: MessageEntity?) {
    if (messageEntity != null) {
        val localThumbnail = messageEntity.localThumbnail
        if (localThumbnail != null) {
//            view.setImageURI(Uri.fromFile(File(localThumbnail)))
            Glide.with(view.context)
                .load(localThumbnail)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        } else {
            val payload = MessageEntity.Payload.fromJson(messageEntity.payload) ?: return
            if (payload.thumbnail == null && payload.cover == null) {
                return
            }
            Glide.with(view.context)
                .load(payload.thumbnail ?: payload.cover)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        }
    }
}

@BindingAdapter("messageImageCover")
fun setMessageImageCover(view: ShapeableImageView, messageEntity: MessageEntity?) {
    if (messageEntity != null) {
        val localThumbnail = messageEntity.localResPath
        if (localThumbnail != null) {
//            view.setImageURI(Uri.fromFile(File(localThumbnail)))
            Glide.with(view.context)
                .load(localThumbnail)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        } else {
            val payload = MessageEntity.Payload.fromJson(messageEntity.payload) ?: return
            if (payload.thumbnail == null && payload.url == null) {
                return
            }
            Glide.with(view.context)
                .load(payload.thumbnail ?: payload.url)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        }
    }
}


@BindingAdapter("ranking", "hasTipped")
fun formatLiveViewersRanking(view: TextView, ranking: Int?, hasTipped: Boolean?) {
    if (ranking == null || ranking == -1) {
        view.text = "-"
        view.setTextColor(Color.parseColor("#aab2e6"))
        return
    } else {
        val color = if (hasTipped == true) {
            view.text = (ranking + 1).toString()
            when (ranking) {
                0 -> Color.parseColor("#ffbb00")
                1 -> Color.parseColor("#ff5e00")
                2 -> Color.parseColor("#008bff")
                else -> Color.parseColor("#aab2e6")
            }
        } else {
            view.text = "-"
            Color.parseColor("#aab2e6")
        }
        view.setTextColor(color)
    }
}

