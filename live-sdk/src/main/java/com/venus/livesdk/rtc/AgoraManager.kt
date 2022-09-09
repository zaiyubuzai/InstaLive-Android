package com.venus.livesdk.rtc

import android.content.Context
import android.view.SurfaceView
import com.venus.livesdk.ILiveManager
import io.agora.rtc.Constants
import io.agora.rtc.RtcEngine
import io.agora.rtc.models.ClientRoleOptions
import io.agora.rtc.video.BeautyOptions
import io.agora.rtc.video.CameraCapturerConfiguration
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

/**
 * 声网直播管理器
 */
class AgoraManager : ILiveManager {
    @Volatile
    var mRtcEngine: RtcEngine? = null
    @Volatile
    var iAgoraConfig: IAgoraConfig? = null

    //是否开启双通道
    @Volatile
    var enableDualStreamMode: Boolean = true
    @Volatile
    var enableLocalAudio: Boolean = true

    @Volatile
    var frameRate: VideoEncoderConfiguration.FRAME_RATE = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24
    @Volatile
    var orientationMode: VideoEncoderConfiguration.ORIENTATION_MODE = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE

    @Volatile
    var DEFAULT_BEAUTY_OPTIONS = BeautyOptions(
        BeautyOptions.LIGHTENING_CONTRAST_NORMAL,
        0.7f,
        0.5f,
        0.1f,
        0.3f
    )

    /**
     * 获取直播组件对象
     */
    fun initAgora(mRtcEngine: RtcEngine) {
        this.mRtcEngine = mRtcEngine
    }

    fun getVideoUI(
        context: Context,
        isLocalVideo: Boolean,
        uid: Int,
        renderMode: Int = VideoCanvas.RENDER_MODE_HIDDEN,
        mirrorMode: Int = Constants.VIDEO_MIRROR_MODE_AUTO
    ): SurfaceView {
        val surfaceView = RtcEngine.CreateRendererView(context)
        if (isLocalVideo) {
            mRtcEngine?.setupLocalVideo(
                VideoCanvas(
                    surfaceView,
                    renderMode,
                    uid,
                    mirrorMode
                )
            )
        } else {
            mRtcEngine?.setupRemoteVideo(
                VideoCanvas(
                    surfaceView,
                    renderMode,
                    uid,
                    mirrorMode
                )
            )
        }
        return surfaceView
    }

    /**
     * 设置角色
     */
    fun setClientRole(isAnchor: Boolean) {
        val clientRoleOptions = ClientRoleOptions()
        clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        if (isAnchor) {
            mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER, clientRoleOptions)
        } else {
            mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE, clientRoleOptions)
        }
    }

    override fun initComponents(context: Context, isAnchor: Boolean, volume: Int) {
        mRtcEngine?.setParameters("{\"che.audio.keep.audiosession\":true}")
        mRtcEngine?.enableDualStreamMode(enableDualStreamMode)
        mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        setClientRole(isAnchor)
//        if (isAnchor){
            mRtcEngine?.enableVideo()
//        }
        mRtcEngine?.enableAudioVolumeIndication(300, 3, false)
//        if (isAnchor) {
            mRtcEngine?.enableAudio()
//        }
        if (isAnchor) {
            mRtcEngine?.startPreview()
        }
        mRtcEngine?.setEnableSpeakerphone(true)
        mRtcEngine?.adjustPlaybackSignalVolume(
            volume
        )
        mRtcEngine?.adjustRecordingSignalVolume(
            volume
        )
        mRtcEngine?.setCameraCapturerConfiguration(
            CameraCapturerConfiguration(
                CameraCapturerConfiguration.CAPTURER_OUTPUT_PREFERENCE.CAPTURER_OUTPUT_PREFERENCE_AUTO,
                CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT
            )
        )
        mRtcEngine?.setBeautyEffectOptions(
            true,
            DEFAULT_BEAUTY_OPTIONS
        )
        //获取直播view：getVideoSurfaceView
    }

    override fun joinChannel(context: Context, isAnchor: Boolean, any: Any): Boolean {
        mRtcEngine?.enableLocalAudio(enableLocalAudio)
        if (any is AgoraTokenInfo) {
            mRtcEngine?.leaveChannel()
            mRtcEngine?.joinChannel(any.token, any.channelName, any.optionalInfo, any.optionalUid)
            return true
        }
        return false
    }

    override fun setVideoEncoderResolution(width: Int, height: Int, bitrate: Int) {
        mRtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VideoDimensions(width, height),
                frameRate,
                bitrate,
                orientationMode
            )
        )
    }

    override fun muteLocalAudioStream(enable: Boolean) {
        mRtcEngine?.muteLocalAudioStream(enable)
    }

    override fun setCameraVideo(enable: Boolean) {
        mRtcEngine?.enableLocalVideo(enable)
    }

    override fun setBeautyEffectOptions(enable: Boolean) {
        mRtcEngine?.setBeautyEffectOptions(
            enable,
            DEFAULT_BEAUTY_OPTIONS
        )
    }

    override fun setCameraMirroring(any: Any) {
        if (any is VideoCanvas) {
            mRtcEngine?.setupLocalVideo(any)
        }
    }

    override fun switchCamera() {
        mRtcEngine?.switchCamera()
    }

    override fun switchLiveRoom(any: Any, any1: Any) {
        mRtcEngine?.switchChannel(any as String, any1 as String)
    }

    override fun leaveLiveRoom() {
        iAgoraConfig?.rtcEngine()?.disableVideo()
        iAgoraConfig?.rtcEngine()?.disableAudio()
        iAgoraConfig?.rtcEngine()?.leaveChannel()
    }

    /**
     * 刷新token
     */
    fun renewToken(token: String) {
        mRtcEngine?.renewToken(token)
    }

    fun setRtcEngineAudio(enable: Boolean) {
        if (enable) {
            mRtcEngine?.enableAudio()
        } else {
            mRtcEngine?.disableAudio()
        }
    }

}

data class AgoraTokenInfo(
    val token: String,
    val channelName: String,//roomId
    val optionalInfo: String?,
    val optionalUid: Int
)

