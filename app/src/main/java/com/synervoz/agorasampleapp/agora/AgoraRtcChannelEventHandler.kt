package com.synervoz.agorasampleapp.agora

import android.util.Log
import io.agora.rtc.IRtcChannelEventHandler
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcChannel
import java.util.logging.Logger

internal abstract class AgoraRtcChannelEventHandler : IRtcChannelEventHandler() {
    open val TAG = javaClass.name

    override fun onJoinChannelSuccess(rtcChannel: RtcChannel, uid: Int, elapsed: Int) {
        super.onJoinChannelSuccess(rtcChannel, uid, elapsed)
        Log.d(
            TAG,
            String.format("onJoinChannelSuccess channel %s uid %d", rtcChannel.channelId(), uid)
        )
    }

    override fun onUserJoined(rtcChannel: RtcChannel?, uid: Int, elapsed: Int) {
        super.onUserJoined(rtcChannel, uid, elapsed)
        if (rtcChannel == null)
            Log.d(TAG, "onUserJoined: rtcChannel is null, uid = $uid, elapsed = $elapsed")
        else
            Log.d(
                TAG,
                "onUserJoined: rtcChannel ${rtcChannel.channelId()}, uid = $uid, elapsed = $elapsed"
            )
    }

    override fun onChannelWarning(rtcChannel: RtcChannel?, warn: Int) {
        Log.d(TAG, "onChannelWarning: ")
    }

    override fun onChannelError(rtcChannel: RtcChannel?, err: Int) {
        Log.d(TAG, "onChannelError: ")
    }

    override fun onRejoinChannelSuccess(rtcChannel: RtcChannel?, uid: Int, elapsed: Int) {
        Log.d(TAG, "onRejoinChannelSuccess: ")
    }

    override fun onLeaveChannel(rtcChannel: RtcChannel?, stats: IRtcEngineEventHandler.RtcStats?) {
        Log.d(TAG, "onLeaveChannel: ")
    }

    override fun onClientRoleChanged(rtcChannel: RtcChannel?, oldRole: Int, newRole: Int) {
        Log.d(TAG, "onClientRoleChanged: ")
    }

    override fun onUserOffline(rtcChannel: RtcChannel?, uid: Int, reason: Int) {
        Log.d(TAG, "onUserOffline: ")
    }

    override fun onConnectionStateChanged(rtcChannel: RtcChannel?, state: Int, reason: Int) {
        Log.d(TAG, "onConnectionStateChanged: ")
    }

    override fun onConnectionLost(rtcChannel: RtcChannel?) {
        Log.d(TAG, "onConnectionLost: ")
    }

    override fun onTokenPrivilegeWillExpire(rtcChannel: RtcChannel?, token: String?) {
        Log.d(TAG, "onTokenPrivilegeWillExpire: ")
    }

    override fun onRequestToken(rtcChannel: RtcChannel?) {
        Log.d(TAG, "onRequestToken: ")
    }

    override fun onRtcStats(rtcChannel: RtcChannel?, stats: IRtcEngineEventHandler.RtcStats?) {
        Log.d(TAG, "onRtcStats: ")
    }

    override fun onNetworkQuality(
        rtcChannel: RtcChannel?,
        uid: Int,
        txQuality: Int,
        rxQuality: Int
    ) {
        Log.d(TAG, "onNetworkQuality: ")
    }

    override fun onRemoteVideoStats(
        rtcChannel: RtcChannel?,
        stats: IRtcEngineEventHandler.RemoteVideoStats?
    ) {
        Log.d(TAG, "onRemoteVideoStats: ")
    }

    override fun onRemoteAudioStats(
        rtcChannel: RtcChannel?,
        stats: IRtcEngineEventHandler.RemoteAudioStats?
    ) {
        Log.d(TAG, "onRemoteAudioStats: ")
    }

    override fun onRemoteAudioStateChanged(
        rtcChannel: RtcChannel?,
        uid: Int,
        state: Int,
        reason: Int,
        elapsed: Int
    ) {
        Log.d(
            TAG,
            "onRemoteAudioStateChanged: channelId = ${rtcChannel?.channelId()}, uid = $uid, state = $state, reason = $reason, elapsed = $elapsed"
        )
    }

    override fun onAudioPublishStateChanged(
        rtcChannel: RtcChannel?,
        oldState: Int,
        newState: Int,
        elapseSinceLastState: Int
    ) {

        Log.d(TAG, "onAudioPublishStateChanged: oldState = $oldState, newState = $newState")
    }

    override fun onVideoPublishStateChanged(
        rtcChannel: RtcChannel?,
        oldState: Int,
        newState: Int,
        elapseSinceLastState: Int
    ) {
        if (rtcChannel == null) {
            Log.d(
                TAG,
                "onVideoPublishStateChanged: channelId = null, oldState = $oldState, newState = $newState, elapseSinceLastState = $elapseSinceLastState"
            )
        } else {
            Log.d(
                TAG,
                "onVideoPublishStateChanged: channelId =  ${rtcChannel.channelId()}, oldState = $oldState, newState = $newState, elapseSinceLastState = $elapseSinceLastState"
            )
        }
    }

    override fun onAudioSubscribeStateChanged(
        rtcChannel: RtcChannel?,
        uid: Int,
        oldState: Int,
        newState: Int,
        elapseSinceLastState: Int
    ) {
        Log.d(TAG, "onAudioSubscribeStateChanged: ")
    }

    override fun onVideoSubscribeStateChanged(
        rtcChannel: RtcChannel?,
        uid: Int,
        oldState: Int,
        newState: Int,
        elapseSinceLastState: Int
    ) {
        if (rtcChannel == null)
            Log.d(
                TAG,
                "onVideoSubscribeStateChanged: channelId = null, uid = $uid, oldState = $oldState, newState = $newState, elapseSinceLastState = $elapseSinceLastState"
            )
        else
            Log.d(
                TAG,
                "onVideoSubscribeStateChanged: channelId = ${rtcChannel.channelId()}, uid = $uid, oldState = $oldState, newState = $newState, elapseSinceLastState = $elapseSinceLastState"
            )
    }

    override fun onActiveSpeaker(rtcChannel: RtcChannel?, uid: Int) {
        Log.d(TAG, "onActiveSpeaker: ")
    }

    override fun onVideoSizeChanged(
        rtcChannel: RtcChannel?,
        uid: Int,
        width: Int,
        height: Int,
        rotation: Int
    ) {
        Log.d(TAG, "onVideoSizeChanged: ")
    }

    override fun onRemoteVideoStateChanged(
        rtcChannel: RtcChannel?,
        uid: Int,
        state: Int,
        reason: Int,
        elapsed: Int
    ) {
        if (rtcChannel == null)
            Log.d(
                TAG,
                "onRemoteVideoStateChanged: channelId = null, uid = $uid, state = $state, reason = $reason, elapsed = $elapsed"
            )
        else
            Log.d(
                TAG,
                "onRemoteVideoStateChanged: channelId = ${rtcChannel.channelId()}, uid = $uid, state = $state, reason = $reason, elapsed = $elapsed"
            )
    }

    override fun onStreamMessage(
        rtcChannel: RtcChannel?,
        uid: Int,
        streamId: Int,
        data: ByteArray?
    ) {
        Log.d(TAG, "onStreamMessage: ")
    }

    override fun onStreamMessageError(
        rtcChannel: RtcChannel?,
        uid: Int,
        streamId: Int,
        error: Int,
        missed: Int,
        cached: Int
    ) {
        Log.d(TAG, "onStreamMessageError: ")
    }

    override fun onChannelMediaRelayStateChanged(rtcChannel: RtcChannel?, state: Int, code: Int) {
        Log.d(TAG, "onChannelMediaRelayStateChanged: ")
    }

    override fun onChannelMediaRelayEvent(rtcChannel: RtcChannel?, code: Int) {
        Log.d(TAG, "onChannelMediaRelayEvent: ")
    }

    override fun onRtmpStreamingStateChanged(
        rtcChannel: RtcChannel?,
        url: String?,
        state: Int,
        errCode: Int
    ) {
        Log.d(TAG, "onRtmpStreamingStateChanged: ")
    }

    override fun onTranscodingUpdated(rtcChannel: RtcChannel?) {
        Log.d(TAG, "onTranscodingUpdated: ")
    }

    override fun onStreamInjectedStatus(
        rtcChannel: RtcChannel?,
        url: String?,
        uid: Int,
        status: Int
    ) {
        Log.d(TAG, "onStreamInjectedStatus: ")
    }

    override fun onRtmpStreamingEvent(rtcChannel: RtcChannel?, url: String?, errCode: Int) {
        Log.d(TAG, "onRtmpStreamingEvent: ")
    }

    override fun onLocalPublishFallbackToAudioOnly(
        rtcChannel: RtcChannel?,
        isFallbackOrRecover: Boolean
    ) {
        Log.d(TAG, "onLocalPublishFallbackToAudioOnly: ")
    }

    override fun onRemoteSubscribeFallbackToAudioOnly(
        rtcChannel: RtcChannel?,
        uid: Int,
        isFallbackOrRecover: Boolean
    ) {
        Log.d(TAG, "onRemoteSubscribeFallbackToAudioOnly: ")
    }
}
