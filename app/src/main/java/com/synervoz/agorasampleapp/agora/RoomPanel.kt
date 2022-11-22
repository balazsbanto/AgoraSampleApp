package com.synervoz.agorasampleapp.agora

import android.util.Log
import android.view.SurfaceView

class RoomPanel : RoomInterface {
    override fun didUpdateConnectionState(state: RoomConnectionState) {
        Log.d("", "")
    }

    override fun didFailToJoinWithError(error: Error) {
        Log.d("", "")

    }

    override fun didFailToPublishWithError(error: Error) {
        Log.d("", "")

    }

    override fun didFailToSubscribeWithError(error: Error) {
        Log.d("", "")

    }

    override fun didUpdatePublisherAudioLevel(audioLevel: Float) {
        Log.d("", "")

    }

    override fun didUpdateSubscriberAudioLevel(audioLevel: Float, userID: String?) {
        Log.d("", "")

    }

    override fun userDidJoin(userID: String) {
        Log.d("", "")

    }

    override fun userDidLeave(userID: String) {
        Log.d("", "")

    }

    override fun userDidMute(userID: String) {
        Log.d("", "")

    }

    override fun userDidUnmute(userID: String) {
        Log.d("", "")

    }

    override fun didUpdatePublisherVideoView(view: SurfaceView?) {
        Log.d("", "")

    }

    override fun didUpdateSubscriberVideoViews(views: ArrayList<SubscriberVideoView>) {
        Log.d("", "")

    }

    override fun videoDidMute(userID: String) {
        Log.d("", "")

    }

    override fun videoDidUnmute(userID: String) {
        Log.d("", "")

    }

    override fun videoPublisherState(mute: Boolean) {
        Log.d("", "")

    }
}