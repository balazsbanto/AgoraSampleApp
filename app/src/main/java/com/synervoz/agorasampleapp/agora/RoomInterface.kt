package com.synervoz.agorasampleapp.agora

import android.view.SurfaceView

interface RoomInterface {

    fun didUpdateConnectionState(state: RoomConnectionState)
    fun didFailToJoinWithError(error: Error)
    fun didFailToPublishWithError(error: Error)
    fun didFailToSubscribeWithError(error: Error)

    fun didUpdatePublisherAudioLevel(audioLevel: Float)
    fun didUpdateSubscriberAudioLevel(audioLevel: Float, userID: String?)

    fun userDidJoin(userID: String)
    fun userDidLeave(userID: String)

    fun userDidMute(userID: String)
    fun userDidUnmute(userID: String)

    // Video methods

    fun didUpdatePublisherVideoView(view: SurfaceView?)
    fun didUpdateSubscriberVideoViews(views: ArrayList<SubscriberVideoView>)

    fun videoDidMute(userID: String)
    fun videoDidUnmute(userID: String)

    fun videoPublisherState(mute: Boolean)
}

class SubscriberVideoView(
    var view: SurfaceView,
    var userID: String?,
    var state: VideoState,
    var stats: VideoStats
)

enum class VideoState {
    NORMAL, FROZEN
}
