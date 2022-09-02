package com.example.instalive.app.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.utils.alphaClick
import com.example.instalive.R
import kotlinx.android.synthetic.main.fragment_live_empty.*
import splitties.fragmentargs.arg

class LiveEmptyFragment : Fragment() {

    var isHost = false

    var showClose: Boolean by arg()

    var onEventListener: OnEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_live_empty, container, false)

    @OptIn(ExperimentalStdlibApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container.setPadding(0, BarUtils.statusBarHeight, 0, 0)
        icGift.isVisible = !isHost
        icGift.alphaClick{
            onEventListener?.onClickGift()
        }
    }

    interface OnEventListener{
        fun onClickGift()
    }
}