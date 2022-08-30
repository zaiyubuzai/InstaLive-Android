package com.venus.livesdk.rtc;

import io.agora.rtc.IRtcEngineEventHandler;

public interface EventHandler {

    void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onUserJoined(int uid, int elapsed);

    void onLastmileQuality(int quality);

    void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result);

    void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats);

    void onRtcStats(IRtcEngineEventHandler.RtcStats stats);

    void onNetworkQuality(int uid, int txQuality, int rxQuality);

    void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats);

    void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats);

    void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed);

    void onTokenPrivilegeWillExpire( String token);

    void onRequestToken();

    void onConnectionStateChanged(int state, int reason);

    void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume);

    void onRemoteAudioStateChanged(int uid, int state, int reason, int elapsed);

    void onUserMuteAudio(int uid, boolean muted);

    void onError(int code);
}
