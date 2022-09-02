package com.example.baselibrary.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 视频路径生成器
 */
object MediaPathUtil {
    private val CACHE_DIR_NAME = "insta_live_temp"
    private val OUTPUT_DIR_NAME = "insta_live"
    private val MUSIC_PATH = "music_path"
    /**
     * 生成编辑后输出视频路径
     *
     * @return
     */
    fun generateVideoPath(context: Context): String {
        val sdcardDir = context.getExternalFilesDir(null) ?: return ""
        val outputPath = sdcardDir.toString() + File.separator + CACHE_DIR_NAME
        val outputFolder = File(outputPath)
        if (!outputFolder.exists()) {
            outputFolder.mkdirs()
        }
        val current = (System.currentTimeMillis() / 1000).toString()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val time = sdf.format(Date(java.lang.Long.valueOf(current + "000")))
        val saveFileName = String.format("fambase_video_%s.mp4", time)
        return "$outputFolder/$saveFileName"
    }

    fun getCustomImageOutputPath(context: Context, filename: String): String {
        val sdcardDir = context.getExternalFilesDir(null) ?: return ""
        val outputDir = sdcardDir.toString() + File.separator + CACHE_DIR_NAME
        val outputFolder = File(outputDir)
        if (!outputFolder.exists()) {
            outputFolder.mkdir()
        }
        return outputDir + File.separator + filename
    }

    fun getCustomGiftOutputPath(context: Context, fileNamePrefix: String? = null, isCache: Boolean = false): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmssSSS")
        val time = sdf.format(Date(currentTime))

        val sdcardDir = getSDCardDir(context)

//        val sdcardDir = appInstance.getExternalFilesDir(null) ?: Environment.getExternalStorageState()
        val outputDir = sdcardDir.toString() + File.separator + if (isCache) CACHE_DIR_NAME else OUTPUT_DIR_NAME
        val outputFolder = File(outputDir)
        if (!outputFolder.exists()) {
            outputFolder.mkdir()
        }
        val tempOutputPath = if (TextUtils.isEmpty(fileNamePrefix)) {
            outputDir + File.separator + "Insta_gift_" + time + ".svga"
        } else {
            outputDir + File.separator + "Insta_gift_" + fileNamePrefix + "_" + time + ".svga"
        }
        return tempOutputPath
    }

    fun getCustomVideoOutputPath(context: Context, fileNamePrefix: String? = null, isVideo: Boolean = true, isCache: Boolean = false): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmssSSS")
        val time = sdf.format(Date(currentTime))
        val sdcardDir = context.getExternalFilesDir(null) ?: return ""
        val outputDir = sdcardDir.toString() + File.separator + if (isCache) CACHE_DIR_NAME else OUTPUT_DIR_NAME
        val outputFolder = File(outputDir)
        if (!outputFolder.exists()) {
            outputFolder.mkdir()
        }
        val tempOutputPath = if (TextUtils.isEmpty(fileNamePrefix)) {
            outputDir + File.separator + "Fambase_video_" + time + if (isVideo) ".mp4" else ".mp3"
        } else {
            outputDir + File.separator + "Fambase_video_" + fileNamePrefix + time + if (isVideo) ".mp4" else ".mp3"
        }
        return tempOutputPath
    }


    fun getGenerateVideoOutputPath(context: Context, fileNamePrefix: String? = null, isVideo: Boolean = true, isCache: Boolean = false): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmssSSS")
        val time = sdf.format(Date(currentTime))
        val outputDir = FileUtils.generateVideoPath(context).absolutePath

        val tempOutputPath: String
        tempOutputPath = if (TextUtils.isEmpty(fileNamePrefix)) {
            outputDir + File.separator + "Fambase_video_" + time + if (isVideo) ".mp4" else ".mp3"
        } else {
            outputDir + File.separator + "Fambase_video_" + fileNamePrefix + time + if (isVideo) ".mp4" else ".mp3"
        }
        return tempOutputPath
    }

    fun getCustomMusicOutputPath(context:Context, fileName: String): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmssSSS")
        val time = sdf.format(Date(currentTime))
        val sdcardDir = context.getExternalFilesDir(null) ?: return ""
        val outputDir = sdcardDir.toString() + File.separator + MUSIC_PATH
        val outputFolder = File(outputDir)
        if (!outputFolder.exists()) {
            outputFolder.mkdir()
        }
        val tempOutputPath: String
        tempOutputPath = if (TextUtils.isEmpty(fileName)) {
            outputDir + File.separator + "Fambase_music_" + time + ".mp3"
        } else {
            outputDir + File.separator + "Fambase_music_" + fileName
        }
        return tempOutputPath
    }

    fun getSDCardDir(context: Context): String{
        val state = Environment.getExternalStorageState()
        val sdcardDir = if (Environment.MEDIA_MOUNTED == state) {
            val baseDirFile: File? = context.getExternalFilesDir(null)
            if (baseDirFile == null) {
                context.getFilesDir().getAbsolutePath()
            } else {
                baseDirFile.absolutePath
            }
        } else {
            context.getFilesDir().getAbsolutePath()
        }
        return sdcardDir
    }

    fun getCustomVoiceOutputPath(context: Context, fileName: String): String {
        val currentTime = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmssSSS")
        val time = sdf.format(Date(currentTime))
        val sdcardDir = context.filesDir
        val outputDir = sdcardDir.toString() + File.separator + OUTPUT_DIR_NAME
        val outputFolder = File(outputDir)
        if (!outputFolder.exists()) {
            outputFolder.mkdir()
        }
        val tempOutputPath: String
        tempOutputPath = if (TextUtils.isEmpty(fileName)) {
            outputDir + File.separator + "Fambase_music_" + time + ".aac"
        } else {
            outputDir + File.separator + "Fambase_music_" + fileName
        }
        return tempOutputPath
    }

    fun coverPath(videoPath: String, context: Context): String? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(videoPath)
        var bitmap = mmr.frameAtTime
        if (null == bitmap)
            bitmap = mmr.getFrameAtTime(0)
        return FileUtils.writeBitmapToFile(
            context,
            bitmap,
            "${System.currentTimeMillis()}.jpg"
        )
    }

    fun getVideoWH(videoPath: String): Array<Int>? {
        val file = File(videoPath)
        if (file.exists()) {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(videoPath)
            val height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val orientation =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            return if ("90" == orientation) arrayOf(
                height?.toInt() ?: 100,
                width?.toInt() ?: 100
            ) else arrayOf(width?.toInt() ?: 100, height?.toInt() ?: 100)
        }
        return null
    }
}