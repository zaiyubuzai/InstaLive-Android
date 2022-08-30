package com.venus.livesdk

import android.content.Context

interface ILiveManager {

    /**
     * 初始化
     */
    fun initComponents(context: Context, isAnchor: Boolean, volume: Int)

    /**
     * 加入直播间
     * @param context: 上下文
     * @param isAnchor: 是否为主播
     * @param any: 自定义参数
     * @return 是否操作成功
     */
    fun joinChannel(
        context: Context,
        isAnchor: Boolean,
        any: Any
    ): Boolean

    /**
     * 更新视频采集参数（分辨率，码率）
     * @param width: 分辨率宽度
     * @param height: 分辨率高度
     * @param bitrate: 码率
     */
    fun setVideoEncoderResolution(
        width: Int,
        height: Int,
        bitrate: Int,
    )

    /**
     * 静音/关闭静音
     * @param enable: true：静音；false：关闭静音。
     */
    fun muteLocalAudioStream(enable: Boolean)

    /**
     * 关闭摄像头/打开摄像头
     * @param enable: true:打开摄像头;false:关闭摄像头.
     */
    fun setCameraVideo(enable: Boolean)

    /**
     * 美颜
     * @param enable: true:打开美颜;false:关闭美颜.
     */
    fun setBeautyEffectOptions(enable: Boolean)

    /**
     * 设置摄像头镜像
     */
    fun setCameraMirroring(any: Any)

    /**
     * 翻转镜头
     */
    fun switchCamera()

    /**
     * 切换直播间
     */
    fun switchLiveRoom(any: Any, any1: Any)

    /**
     * 离开直播间
     */
    fun leaveLiveRoom()
}