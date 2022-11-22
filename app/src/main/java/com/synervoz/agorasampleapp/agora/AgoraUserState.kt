package com.synervoz.agorasampleapp.agora

import android.provider.SyncStateContract
import android.util.Log
import android.util.Log.d
import android.view.SurfaceView
import com.synervoz.agorasampleapp.MyApp
import io.agora.rtc.Constants
import io.agora.rtc.Constants.SUB_STATE_NO_SUBSCRIBED
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import java.util.logging.Logger

internal class AgoraUserState(uid: Int, channelID: String?) {
    val uid = uid
    val channelID = channelID

    var videoView: SurfaceView? = null
    var videoCanvas: VideoCanvas? = null
    var remoteVideoState: Int? = null
    var didJoin = false
    var wasMutedBeforeJoining = false
    var audioSubscribeState = SUB_STATE_NO_SUBSCRIBED

    var userID: String = ""
        get() {
            return AgoraEngine.shared.userAccount(uid) ?: uid.toString()
        }

    var shouldShowVideoView = false
        get() {
            if (remoteVideoState == null) {
                Log.d("AgoraUserState", "[shouldShowVideoView] remoteVideoState is null")
                return false
            }

            if (remoteVideoState != null) {
                Log.d(
                    "AgoraUserState",
                    "[shouldShowVideoView] remoteVideoState = $remoteVideoState"
                )
                return remoteVideoState == Constants.REMOTE_VIDEO_STATE_DECODING || remoteVideoState == Constants.REMOTE_VIDEO_STATE_FROZEN
            }
            return false
        }

    var videoState: VideoState = VideoState.NORMAL
        get() = if (remoteVideoState == Constants.REMOTE_VIDEO_STATE_DECODING || remoteVideoState == Constants.REMOTE_VIDEO_STATE_DECODING) {
            VideoState.NORMAL
        } else {
            VideoState.FROZEN
        }

    var videoStats = VideoStats()
    var audioStats = IRtcEngineEventHandler.RemoteAudioStats()

    init {
        Log.d("AgoraUserState", "[init] uid = $uid")
        videoView = RtcEngine.CreateRendererView(MyApp.appContext)
        videoView?.setZOrderMediaOverlay(true)
        videoCanvas = VideoCanvas(videoView, VideoCanvas.RENDER_MODE_HIDDEN, channelID, uid)
        val result = AgoraEngine.shared.engine.setupRemoteVideo(videoCanvas)
        Log.d("AgoraUserState", "setupRemoteVideo result: $result")
    }

    private fun toDictionary(stats: IRtcEngineEventHandler.RemoteAudioStats): MutableMap<String, Any> {
        return mutableMapOf(
            "uid" to stats.uid,
            "quality" to stats.quality,
            "networkTransportDelay" to stats.networkTransportDelay,
            "jitterBufferDelay" to stats.jitterBufferDelay,
            "audioLossRate" to stats.audioLossRate,
            "numChannels" to stats.numChannels,
            "receivedSampleRate" to stats.receivedSampleRate,
            "receivedBitrate" to stats.receivedBitrate,
            "totalFrozenTime" to stats.totalFrozenTime,
            "frozenRate" to stats.frozenRate,
            "totalActiveTime" to stats.totalActiveTime,
            "publishDuration" to stats.publishDuration,
            "qoeQuality" to stats.qoeQuality,
            "qualityChangedReason" to stats.qualityChangedReason,
            "mosValue" to stats.mosValue
        )
    }

    fun toDictionary(): MutableMap<String, Any> {
        return mutableMapOf(
            "uid" to uid,
            "channelID" to (channelID ?: ""),
            "didJoin" to didJoin,
            "wasMutedBeforeJoining" to wasMutedBeforeJoining,
            // "audioSubscribeState" to user.audioSubscribeState
            "videoStats" to videoStats.toDictionary(),
            "audioStats" to toDictionary(audioStats)
        )
    }
}
