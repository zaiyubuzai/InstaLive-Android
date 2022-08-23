package com.example.baselibrary.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.coordinatorlayout.widget.ViewGroupUtils
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.BaseConstants
import com.example.baselibrary.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import splitties.systemservices.inputMethodManager
import splitties.systemservices.layoutInflater
import timber.log.Timber
import java.util.concurrent.TimeUnit

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

internal val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics

internal val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        displayMetrics
    )

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    GlobalScope.launch(Dispatchers.Main) {
        delay(100)
        inputMethodManager.showSoftInput(this@showKeyboard, InputMethodManager.SHOW_IMPLICIT)
        this@showKeyboard.requestFocus()
    }
}

const val LDPI: Int = DisplayMetrics.DENSITY_LOW
const val MDPI: Int = DisplayMetrics.DENSITY_MEDIUM
const val HDPI: Int = DisplayMetrics.DENSITY_HIGH

const val TVDPI: Int = DisplayMetrics.DENSITY_TV
const val XHDPI: Int = DisplayMetrics.DENSITY_XHIGH
const val XXHDPI: Int = DisplayMetrics.DENSITY_XXHIGH
const val XXXHDPI: Int = DisplayMetrics.DENSITY_XXXHIGH

const val MAXDPI: Int = 0xfffe

//returns dip(dp) dimension value in pixels
fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()

fun Fragment.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Fragment.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()

//return sp dimension value in pixels
fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()

//converts px value into dip or sp
fun Context.px2dip(px: Int): Float = px.toFloat() / resources.displayMetrics.density
fun Context.px2sp(px: Int): Float = px.toFloat() / resources.displayMetrics.scaledDensity

fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

//the same for the views
fun View.dip(value: Int): Int = context.dip(value)
fun View.dip(value: Float): Int = context.dip(value)
fun View.sp(value: Int): Int = context.sp(value)
fun View.sp(value: Float): Int = context.sp(value)
fun View.px2dip(px: Int): Float = context.px2dip(px)
fun View.px2sp(px: Int): Float = context.px2sp(px)
fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)

fun String.appendZero(): String {
    val lastDot = this.lastIndexOf(".")
    if (this.lastIndex - lastDot < 7) {
        val count = 7 - this.lastIndex + lastDot
        for (index in 1..count) {
            this.plus("0")
        }
    }
    return this
}


fun Activity.marsToast(msg: String) {
    if (msg.isEmpty()) {
        return
    }
    val toast = Toast(this)
    val toastView = this.layoutInflater.inflate(R.layout.layout_mars_toast, null)
    toastView.findViewById<TextView>(R.id.toastMsg).text = msg
    toast.duration = Toast.LENGTH_SHORT
    toast.view = toastView
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, dip(48))
    toast.show()
}

fun Activity.marsLongToast(msg: String) {
    if (msg.isEmpty()) {
        return
    }
    val toast = Toast(this)
    val toastView = this.layoutInflater.inflate(R.layout.layout_mars_toast, null)
    toastView.findViewById<TextView>(R.id.toastMsg).text = msg
    toast.duration = Toast.LENGTH_LONG
    toast.view = toastView
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, dip(48))
    toast.show()
}

fun Fragment.marsToast(msg: String, duration: Int = 0) {
    try {
        if (msg.isEmpty()) {
            return
        }
        val toast = Toast(context)
        val toastView = this.layoutInflater.inflate(R.layout.layout_mars_toast, null)
        toastView.findViewById<TextView>(R.id.toastMsg).text = msg
        toast.duration = duration
        toast.view = toastView
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, context?.dip(48) ?: 0)
        toast.show()
    } catch (e: Exception) {
    }
}

fun Context.marsToast(msg: String) {
    if (msg.isEmpty()) {
        return
    }
    val toast = Toast(this)
    val toastView = layoutInflater.inflate(R.layout.layout_mars_toast, null)
    toastView.findViewById<TextView>(R.id.toastMsg).text = msg
    toast.duration = Toast.LENGTH_SHORT
    toast.view = toastView
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, dip(48))
    toast.show()
}

fun Context.marsLongToast(msg: String) {
    if (msg.isEmpty()) {
        return
    }
    val toast = Toast(this)
    val toastView = layoutInflater.inflate(R.layout.layout_mars_toast, null)
    toastView.findViewById<TextView>(R.id.toastMsg).text = msg
    toast.duration = Toast.LENGTH_LONG
    toast.view = toastView
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, dip(48))
    toast.show()
}

fun View.marsToast(msg: String) {
    if (msg.isEmpty()) {
        return
    }
    val toast = Toast(context)
    val toastView = layoutInflater.inflate(R.layout.layout_mars_toast, null)
    toastView.findViewById<TextView>(R.id.toastMsg).text = msg
    toast.duration = Toast.LENGTH_SHORT
    toast.view = toastView
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, context.dip(48))
    toast.show()
}

fun Activity.marsToast(res: Int) {
    marsToast(getString(res))
}

fun Fragment.marsToast(res: Int) {
    marsToast(getString(res))
}

fun Context.marsToast(res: Int) {
    marsToast(getString(res))
}

fun View.marsToast(res: Int) {
    marsToast(context?.getString(res) ?: "")
}

fun AssetManager.readAssetsFile(fileName: String): String =
    open(fileName).bufferedReader().use { it.readText() }


fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            true
        }
        false
    }
}

fun EditText.onSend(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            callback.invoke()
            true
        }
        false
    }
}

fun EditText.onSearch(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            callback.invoke()
            true
        }
        false
    }
}

fun EditText.onSearchKeepKeyboard(callback: () -> Unit) {
    setOnKeyListener { v, keyCode, event ->
        if (event.action == ACTION_UP && event.keyCode == KEYCODE_ENTER) {
            callback()
            requestFocus()
            true
        } else {
            false
        }
    }
}

fun RecyclerView.onLinearMarsLoadMore(gap:Int = 4, callback: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition = manager.findLastVisibleItemPosition()
                val lastCompletePosition = manager.findLastCompletelyVisibleItemPosition()
                val itemCount = manager.itemCount
                if (lastCompletePosition == itemCount) {
                    //说明一屏已经显示完了，不在加载更多了
                    return
                }
                if (itemCount - lastPosition < gap) {
                    callback()
                }
            }
        }
    })
}

fun RecyclerView.onGridMarsLoadMore(callback: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val manager = recyclerView.layoutManager as GridLayoutManager
                val lastPosition = manager.findLastVisibleItemPosition()
                val lastCompletePosition = manager.findLastCompletelyVisibleItemPosition()
                val itemCount = manager.itemCount
                if (lastCompletePosition == itemCount) {
                    //说明一屏已经显示完了，不在加载更多了
                    return
                }
                if (itemCount - lastPosition < 4) {
                    callback()
                }
            }
        }
    })
}

fun View.slideAnimation(isRight:Boolean = true, durationMillis: Long = 500,distance:Int = 300, onStart: () -> Unit, onEnd: () -> Unit) {
    this.startAnimation(
        TranslateAnimation(
            Animation.ABSOLUTE,
            if (isRight) context.dip(distance).toFloat() else 0f,
            Animation.ABSOLUTE,
            if (isRight.not()) context.dip(distance).toFloat() else 0f,
            Animation.RELATIVE_TO_SELF,
            0f,
            Animation.RELATIVE_TO_SELF,
            0f
        ).apply {
            duration = durationMillis
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    onEnd()
                }

                override fun onAnimationStart(animation: Animation?) {
                    onStart()
                }
            })
        }
    )
}


fun View.slideInOrOut(isRight:Boolean = true, durationMillis: Long = 500,distance:Int = 300, onStart: () -> Unit, onEnd: () -> Unit) {
    ObjectAnimator.ofFloat(
        this,
        "translationX",
        if (isRight) context.dip(distance).toFloat() else 0f,
        if (isRight.not()) context.dip(distance).toFloat() else 0f
    )
        .apply {
            duration = durationMillis
            repeatCount = 0
            addListener(
                onStart = {
                    Timber.d("Box: view animator....on start")
                    onStart()
                },
                onEnd = {
                    Timber.d("Box: view animator....on end")
                    onEnd()
                }
            )
            start()
            Timber.d("Box: start to move....")
        }
}

fun View.liveGiftAnimatorSet(d: Int = 2000, onStart: () -> Unit, onEnd: () -> Unit) {
    val t = ObjectAnimator.ofFloat(this, "translationX", -context.dip(204).toFloat(), 0f)
        .apply {
            duration = 100
            repeatCount = 0
        }
    val a = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
        duration = 100
        repeatCount = 0
    }
    AnimatorSet().apply {
        addListener(
            onStart = {
                onStart()
            },
            onEnd = {
                onEnd()
            }
        )
        play(t).before(a)
        play(a).after(d.toLong()).after(t)
        start()
    }
}

fun View.liveGiftAnimationSet(onStart: () -> Unit, onEnd: () -> Unit) {
    val t = TranslateAnimation(
        Animation.ABSOLUTE,
        -context.dip(188).toFloat(),
        Animation.ABSOLUTE,
        0f,
        Animation.RELATIVE_TO_SELF,
        0f,
        Animation.RELATIVE_TO_SELF,
        0f
    ).apply {
        duration = 100
    }
    val a = AlphaAnimation(1F, 0F).apply {
        duration = 100
        startOffset = 1100
    }
    AnimationSet(true).apply {
        addAnimation(t)
        addAnimation(a)
        start()
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                onStart()
            }

            override fun onAnimationEnd(animation: Animation?) {
                onEnd()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }
}

fun View.fadeOut(durationMillis: Long = 1000, onEnd: (() -> Unit)? = null) {
    alpha = 1f
    animate()
        .alpha(0f)
        .setDuration(durationMillis)
        .setListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                onEnd?.invoke()
                isVisible = false
                this@fadeOut.alpha = 1f
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
}

fun View.fadeIn(durationMillis: Long = 1000, onEnd: (() -> Unit)? = null) {
    alpha = 0f
    animate()
        .alpha(1f)
        .setDuration(durationMillis)
        .setListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                onEnd?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
}





fun View.tinyMoveClickListener(needTime: Boolean = false, onClick: () -> Unit) {
    var downx = 0
    var downy = 0
    var x = 0
    var y = 0
    var subscription: Disposable? = null
    setOnTouchListener { v, event ->
        x = event.x.toInt()
        y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downx = event.x.toInt()
                downy = event.y.toInt()
                if (needTime){
                    subscription = Observable.timer(BaseConstants.CLICK_TIME, TimeUnit.MILLISECONDS).subscribe{
                        val duration = event.eventTime - event.downTime
                        Timber.d("tinyMoveClickListener: down time=$duration")
//                        if (duration > Constants.CLICK_TIME){
                            onClick()
//                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Timber.d("tinyMoveClickListener: up distancex=${x - downx}, distancey=${y-downy}")
                if (needTime) {
                    Timber.d("tinyMoveClickListener: close subscribe")
                    subscription?.dispose()
                }
                if (Math.abs(x - downx) < BaseConstants.CLICK_DISTANCE && Math.abs(y-downy) < BaseConstants.CLICK_DISTANCE){
                    if (needTime.not()){
                        onClick()
                    }
                }
            }
        }
        false
    }
}

fun View.debounceClick(delayMillis: Long = 1000L, onClick: () -> Unit) {
    val debounceClick: (view: View) -> Unit = debounce(delayMillis, scope = MainScope()) {
        onClick.invoke()
    }
    setOnClickListener(debounceClick)
}

fun <T> debounce(
    delayMillis: Long = 1000L,
    scope: CoroutineScope,
    action: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        if (debounceJob == null) {
            debounceJob = scope.launch {
                action(param)
                delay(delayMillis)
                debounceJob = null
            }
        }
    }
}

fun View.doubleClick(onClick: () -> Unit) {
    val doubleClick: (view: View) -> Unit = doubleFunc(scope = MainScope()) {
        onClick.invoke()
    }
    setOnClickListener(doubleClick)
}

fun <T> doubleFunc(
    delayMillis: Long = 500L,
    scope: CoroutineScope,
    action: (T) -> Unit
): (T) -> Unit  {
    var doubleJob: Job? = null
    return { param: T ->
        if (doubleJob == null) {
            doubleJob = scope.launch {
                delay(delayMillis)
                doubleJob = null
            }
        } else {
            action(param)
            doubleJob?.cancel()
            doubleJob = null
        }
    }
}


@SuppressLint("RestrictedApi")
fun View.expand(dx: Int, dy: Int) {
    class MultiTouchDelegate(bound: Rect? = null, delegateView: View) : TouchDelegate(bound, delegateView) {
        val delegateViewMap = mutableMapOf<View, Rect>()
        private var delegateView: View? = null

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x.toInt()
            val y = event.y.toInt()
            var handled = false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    delegateView = findDelegateViewUnder(x, y)
                }
                MotionEvent.ACTION_CANCEL -> {
                    delegateView = null
                }
            }
            delegateView?.let {
                event.setLocation(it.width / 2f, it.height / 2f)
                handled = it.dispatchTouchEvent(event)
            }
            return handled
        }

        private fun findDelegateViewUnder(x: Int, y: Int): View? {
            delegateViewMap.forEach { entry -> if (entry.value.contains(x, y)) return entry.key }
            return null
        }
    }

    val parentView = parent as? ViewGroup
    parentView ?: return

    if (parentView.touchDelegate == null) parentView.touchDelegate = MultiTouchDelegate(delegateView = this)
    post {
        val rect = Rect()
        ViewGroupUtils.getDescendantRect(parentView, this, rect)
        rect.inset(- dx, - dy)
        (parentView.touchDelegate as? MultiTouchDelegate)?.delegateViewMap?.put(this, rect)
    }
}