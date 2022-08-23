package com.example.instalive.app.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.model.CountryCodeData
import com.example.baselibrary.utils.readAssetsFile
import com.example.instalive.R
import com.example.instalive.app.InstaLivePreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.dialog_select_country.*
import kotlinx.android.synthetic.main.item_country_code_layout.view.*
import splitties.dimensions.dp
import splitties.systemservices.layoutInflater
import splitties.views.inflate
import splitties.views.onClick

class SelectCountryDialog constructor(val onCountryClicked: (data: CountryCodeData) -> Unit) : DialogFragment() {
    private var countryList: List<CountryCodeData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CommentDialog)
        if (context is AppCompatActivity) {
//            BarUtils.setStatusBarLightMode(context as AppCompatActivity, true)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.setNavigationOnClickListener {
            dismissAllowingStateLoss()
        }
//        container.setPadding(0, BarUtils.getStatusBarHeight(), 0, BarUtils.getNavBarHeight())
        val countryCode = InstaLivePreferences.countryCodeJson?:activity?.assets?.readAssetsFile("country_codes.json")
        if (!countryCode.isNullOrEmpty()) {
            val type = object : TypeToken<List<CountryCodeData>>() {}.type
            countryList = Gson().fromJson(countryCode, type)
            if (!countryList.isNullOrEmpty()) {
                list.setIndexTextSize(12)
                list.setIndexBarColor("#00000000")
                list.setIndexBarCornerRadius(context?.dp(3)?:10)
                list.setIndexBarTransparentValue(1.toFloat())
                list.setIndexBarTextColor("#ffffff")
                list.setIndexBarStrokeVisibility(false)
                list.layoutManager = LinearLayoutManager(list.context)
                list.adapter = CountryAdapter(countryList ?: listOf()) {
                    onCountryClicked(it)
                    dismissAllowingStateLoss()
                }
            }
        } else {
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_select_country, container, false)
    }

    class CountryAdapter(
        var list: List<CountryCodeData>,
        val onCountryClicked: (data: CountryCodeData) -> Unit
    ) : RecyclerView.Adapter<CountryViewHolder>(), SectionIndexer {
        private val alphabetList: MutableList<String> = mutableListOf()
        private val sectionItemList: MutableList<AlphabetItem> = mutableListOf()

        init {
            list.forEachIndexed { index, data ->
                val name = data.name
                val word = name.substring(0, 1)
                if (!alphabetList.contains(word)) {
                    alphabetList.add(word)
                    sectionItemList.add(AlphabetItem(index, word))
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
            return CountryViewHolder(parent.layoutInflater.inflate(R.layout.item_country_code_layout))
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
            val countryCodeData = list[position]
            holder.itemView.countryName.text = countryCodeData.name
            holder.itemView.countryCode.text = countryCodeData.dialCode
            holder.itemView.onClick {
                onCountryClicked(countryCodeData)
            }
        }

        override fun getSections(): Array<String> {
            return alphabetList.toTypedArray()
        }

        override fun getSectionForPosition(position: Int): Int = 0

        override fun getPositionForSection(sectionIndex: Int): Int {
            return sectionItemList[sectionIndex].position
        }

        data class AlphabetItem(val position: Int, val word: String)
    }

    class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}