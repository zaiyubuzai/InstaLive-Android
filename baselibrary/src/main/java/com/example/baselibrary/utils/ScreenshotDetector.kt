package com.example.baselibrary.utils

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore

//1、截图/录屏用户id
//2、视频：
//（1）source：home_foryou_view、home_following_view、hashtag_view、profile_view、search_view、nearby_view
//（2）被截图/录屏的视频id
//3、直播：
//（1）source：home、trending、profile
//（2）被截图/录屏的直播间id
//4、profile：
//（1）被截图的profile页面用户的id
object ScreenshotDetector {

    private var contentObserver: ContentObserver? = null
    var callback:((Int)->Unit)? = null

    fun start(context: Context, callback:(Int)->Unit) {
        if (contentObserver == null) {
            contentObserver = context.contentResolver.registerObserver(context)
            ScreenshotDetector.callback = callback
        }
    }

    fun stop(context: Context) {
        callback = null
        contentObserver?.let { context.contentResolver.unregisterContentObserver(it) }
        contentObserver = null
    }

    private fun queryScreenshots(uri: Uri, context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                queryRelativeDataColumn(uri, context)
            } else {
                queryDataColumn(uri, context)
            }
    }

    private fun queryDataColumn(uri: Uri, context: Context) {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DATA
            )
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataColumn)
                    if (path.contains("screenshot", true)) {
                        // do something
                        callback?.invoke(1)
                    }
                }
            }
        } catch (e:Exception){
//            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun queryRelativeDataColumn(uri: Uri, context: Context) {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.RELATIVE_PATH
            )
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val relativePathColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                val displayNameColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val relativeVideoPathColumn =
                    cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                val displayVideoNameColumn =
                    cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(displayNameColumn)
                    val relativePath = cursor.getString(relativePathColumn)
                    val nameVideo = cursor.getString(displayVideoNameColumn)
                    val relativeVideoPath = cursor.getString(relativeVideoPathColumn)
//                    Timber.d("screen shot name: $name")
//                    Timber.d("screen shot video name: $nameVideo")
                    if (name.contains("screenshot", true) or
                        relativePath.contains("screenshot", true)
                    ) {
                        // do something
                        callback?.invoke(1)
                    }
                    if ((nameVideo.contains("screen", true)) or
                        relativeVideoPath.contains("movies", true)) {
                        callback?.invoke(2)
                    }
                }
            }
        } catch (e: Exception){
        }
    }

    private fun ContentResolver.registerObserver(context: Context): ContentObserver {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri?.let { queryScreenshots(it, context) }
            }
        }
        registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
        registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
        return contentObserver
    }
}