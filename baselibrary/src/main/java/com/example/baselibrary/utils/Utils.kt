package com.example.baselibrary.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import splitties.dimensions.dp
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {

    val PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+\$).{8,20}$")
    val HASHTAG_PATTERN = Pattern.compile("(?:|^)#(?=.*[a-zA-Z0-9])[A-Za-z0-9]+(?:|\$)")
    val USERNAME_PATTERN = Pattern.compile("@[A-Za-z0-9_.]{1,23}[A-Za-z0-9_]\\b")
    val USERNAME_SEARCH_PATTERN = Pattern.compile("@[A-Za-z0-9_.]{0,23}[A-Za-z0-9_]\\b")

    //    val URL_PATTERN = Pattern.compile("(ht|Ht)tp(s?):\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)")
    private val URL_PATTERN: Pattern = Pattern.compile(
        "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)",
        Pattern.CASE_INSENSITIVE
    )

    val TEMPLATE_PATTERN = Pattern.compile("\\$\\{\\w+\\}")
    fun processTemplate(template: String, params: Map<String, String?>): String {
        val stringBuffer = StringBuffer()
        val matcher = TEMPLATE_PATTERN.matcher(template)
        while (matcher.find()) {
            val param = matcher.group()
            val value = params[param.substring(2, param.length - 1)]
            matcher.appendReplacement(stringBuffer, value ?: "")
        }
        matcher.appendTail(stringBuffer)
        return stringBuffer.toString()
    }

    fun goToAppSettings(context: Context, applicationPackage: String) {
        val i = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$applicationPackage")
        )
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, i, null)
    }

    private fun getUrlsInContent(content: String): Map<Int, String> {
        val termList: MutableMap<Int, String> = mutableMapOf()
        val matcher: Matcher = URL_PATTERN.matcher(content)
        while (matcher.find()) {
            termList[matcher.start()] = matcher.group()
        }
        return termList
    }

    fun buildTextUrlSpan(
        content: String,
        textView: TextView,
        onClickableSpan: ((String) -> Unit)? = null
    ) {
        if (content.isNotEmpty()) {
            val urlList = getUrlsInContent(content)
            if (urlList.isNotEmpty()) {
                val ss = SpannableString(content)
                for (temp in urlList) {
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onClickableSpan?.invoke(temp.value)
                        }
                    }
                    ss.setSpan(
                        if (onClickableSpan == null) URLSpan(temp.value) else clickableSpan,
                        temp.key,
                        temp.key + temp.value.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val foregroundColorSpan = ForegroundColorSpan(Color.parseColor("#2f65ff"))
                    ss.setSpan(
                        foregroundColorSpan,
                        temp.key,
                        temp.key + temp.value.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                textView.text = ss
                textView.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    fun changeImageViewWH(imageView: ImageView, width: Int, height: Int) {
        val params = imageView.layoutParams as ViewGroup.LayoutParams
        val scale = width.toFloat() / height.toFloat()
        var w = imageView.context?.dp(240) ?: 240
        var h = (w / scale).toInt()
        if (h < imageView.context?.dp(50) ?: 50) {
            h = imageView.context?.dp(50) ?: 50
        } else if (h > imageView.context?.dp(442) ?: 442) {
            h = imageView.context?.dp(442) ?: 442
            w = (h * scale).toInt()
            if (w < imageView.context?.dp(50) ?: 50) {
                w = imageView.context?.dp(50) ?: 50
            }
        }
        params.width = w
        params.height = h
        imageView.layoutParams = params
    }
}