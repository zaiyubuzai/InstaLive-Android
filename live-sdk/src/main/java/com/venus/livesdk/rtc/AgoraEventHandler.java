package com.venus.livesdk.rtc;

import java.util.ArrayList;

import io.agora.rtc.IRtcEngineEventHandler;

public class AgoraEventHandler extends IRtcEngineEventHandler {
    private ArrayList<EventHandler> mHandler = new ArrayList<>();

    public void addHandler(EventHandler handler) {
        if (!mHandler.contains(handler)) mHandler.add(handler);
    }

    public void removeHandler(EventHandler handler) {
        mHandler.remove(handler);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onJoinChannelSuccess(channel, uid, elapsed);
        }
    }

    @Override
    public void onError(int err) {
        for (EventHandler handler : mHandler) {
            handler.onError(err);
        }
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onLeaveChannel(stats);
        }
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onUserJoined(uid, elapsed);
        }
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        for (EventHandler handler : mHandler) {
            handler.onUserOffline(uid, reason);
        }
    }

    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onLocalVideoStats(stats);
        }
    }

    @Override
    public void onRtcStats(RtcStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRtcStats(stats);
        }
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        for (EventHandler handler : mHandler) {
            handler.onNetworkQuality(uid, txQuality, rxQuality);
        }
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteVideoStats(stats);
        }
    }

    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteAudioStats(stats);
        }
    }

    @Override
    public void onLastmileQuality(int quality) {
        for (EventHandler handler : mHandler) {
            handler.onLastmileQuality(quality);
        }
    }

    @Override
    public void onLastmileProbeResult(LastmileProbeResult result) {
        for (EventHandler handler : mHandler) {
            handler.onLastmileProbeResult(result);
        }
    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteVideoStateChanged(uid, state, reason, elapsed);
        }
    }

    @Override
    public void onTokenPrivilegeWillExpire(String token) {
        for (EventHandler handler : mHandler) {
            handler.onTokenPrivilegeWillExpire(token);
        }
    }

    @Override
    public void onRequestToken() {
        for (EventHandler handler : mHandler) {
            handler.onRequestToken();
        }
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {
        for (EventHandler handler : mHandler) {
            handler.onConnectionStateChanged(state,reason);
        }
    }

    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        for (EventHandler handler : mHandler) {
            handler.onAudioVolumeIndication(speakers,totalVolume);
        }
    }

    @Override
    public void onRemoteAudioStateChanged(int uid, int state, int reason, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteAudioStateChanged(uid, state, reason, elapsed);
        }
    }
    @Override
    public void onUserMuteAudio(int uid, boolean muted){
        for (EventHandler handler : mHandler) {
            handler.onUserMuteAudio(uid, muted);
        }
    }

    public int getEventHandler() {
        return mHandler.size();
    }
}
