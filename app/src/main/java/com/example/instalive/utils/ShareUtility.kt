package com.example.instalive.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.example.instalive.InstaLiveApp
import com.example.instalive.R
import splitties.systemservices.clipboardManager

object ShareUtility {
    fun shareInstagramStory(context: Context, url: String) {
        context.packageManager.getPackageInfo(
            "com.instagram.android",
            PackageManager.GET_META_DATA
        )
        val attributionLinkUrl = Uri.parse(url)
        val sourceApplication = "com.fambase.venus"

        val intent = Intent("com.instagram.share.ADD_TO_STORY")
        intent.putExtra("source_application", sourceApplication);
        intent.putExtra("content_url", attributionLinkUrl);
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (context.packageManager.resolveActivity(intent, 0) != null) {
            if (context is AppCompatActivity) {
                context.startActivityForResult(intent, 0)
            }
        }
    }

    fun shareMessenger(context: Context, url: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent
            .putExtra(
                Intent.EXTRA_TEXT,
                url
            )
        sendIntent.type = "text/plain"
        sendIntent.setPackage("com.facebook.orca")
        try {
            context.startActivity(sendIntent)
        } catch (ex: ActivityNotFoundException) {
            marsToast(R.string.fb_share)
        }
    }


    fun shareEmail(
        context: Context,
        title: String,
        text: String,
    ) {
        val intent = Intent(Intent.ACTION_SENDTO)
        val mailTo =
            "mailto:?subject=${Uri.encode(title)}&body=${Uri.encode(text)}"
        intent.data = Uri.parse(mailTo)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            marsToast(R.string.fb_share)
        }
    }

    fun shareEmail(
        context: Context,
        url: String,
    ) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        val name = url.replace("mailto:", "", false)
        val tos = arrayOf(name)
        val ccs = arrayOf("")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, tos)
        emailIntent.putExtra(Intent.EXTRA_CC, ccs)
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        emailIntent.type = "message/rfc822"
        context.startActivity(Intent.createChooser(emailIntent, ""))
    }

    /**
     * Share on Whatsapp (if installed)
     *
     * @param context activity which launches the intent
     * @param text     text to share
     * @param url      url to share
     */
    fun shareWhatsapp(context: Context, text: String) {
        val pm = context.packageManager
        try {
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "text/plain"
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA)
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp")
            waIntent.putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(
                Intent
                    .createChooser(
                        waIntent,
                        context.getString(R.string.fb_share)
                    )
            )
        } catch (e: PackageManager.NameNotFoundException) {
            marsToast(context.getString(R.string.fb_share))

        }
    }

    fun shareSms(context: Context, text: String, phone: String? = null) {
        val message = Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""))
        if (phone != null) {
            message.data = Uri.parse("smsto:$phone")
        }
        message.putExtra("sms_body", text)
        context.startActivity(message)
    }

    fun shareCopy(text: String) {
        val clip = ClipData.newPlainText(
            InstaLiveApp.appInstance.getString(R.string.app_name),
            text
        )
        clipboardManager.setPrimaryClip(clip)
        marsToast(R.string.fb_copied)
    }

}