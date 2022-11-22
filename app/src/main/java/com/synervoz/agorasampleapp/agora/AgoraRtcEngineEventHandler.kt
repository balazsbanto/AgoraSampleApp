//
//  AgoraRtcEngineEventHandler.kt
//  SwitchboardCommunication
//
//  Created by Tayyab Javed on 07/01/21.
//  Copyright © 2021 Synervoz Inc. All rights reserved.
//

package com.synervoz.agorasampleapp.agora

import android.util.Log
import android.util.Log.d
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.models.UserInfo
import java.util.logging.Logger
import kotlin.math.abs

internal class AgoraRtcEngineEventHandler : IRtcEngineEventHandler() {
    private val TAG = javaClass.name

    override fun onWarning(warn: Int) {
        Log.d(TAG, "onWarning: warn = $warn")
    }

    override fun onError(err: Int) {
        Log.d(TAG, "onError: err = $err")
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        Log.d(TAG, "onConnectionStateChanged: state = $state, reason = $reason")
    }

    // region CHANNEL/USER

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        if (channel == null) {
            Log.d(TAG, "onJoinChannelSuccess: channel = null. uid = $uid, elapsed = $elapsed")
        }
        Log.d(TAG, "onJoinChannelSuccess: channel = $channel. uid = $uid, elapsed = $elapsed")
    }

    override fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        if (channel == null) {
            Log.d(TAG, "onRejoinChannelSuccess: channel = null. uid = $uid, elapsed = $elapsed")
        }
        Log.d(TAG, "onRejoinChannelSuccess: channel = $channel. uid = $uid, elapsed = $elapsed")
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        if (stats == null) {
            Log.d(TAG, "onLeaveChannel: stats = null")
        }
        Log.d(TAG, "onLeaveChannel: stats = $stats")
    }

    override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
        Log.d(TAG, "onClientRoleChanged: oldRole = $oldRole. newRole = $newRole")
    }

    override fun onLocalUserRegistered(uid: Int, userAccount: String?) {
        if (userAccount == null) {
            Log.d(TAG, "onLocalUserRegistered: uid = $uid, userAccount = null")
        }
        Log.d(TAG, "onLocalUserRegistered: uid = $uid, userAccount = $userAccount")

        for (room in AgoraEngine.shared.rooms.values) {
            room.onLocalUserRegistered(uid, userAccount)
        }
    }

    override fun onUserInfoUpdated(uid: Int, userInfo: UserInfo?) {
        if (userInfo == null) {
            Log.d(TAG, "onUserInfoUpdated: uid = $uid, userInfo = null")
        }
        Log.d(TAG, "onUserInfoUpdated: uid = $uid, userInfo = $userInfo")

        for (room in AgoraEngine.shared.rooms.values) {
            room.onUserInfoUpdated(uid, userInfo)
        }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        Log.d(TAG, "onUserJoined: uid = $uid, elapsed = $elapsed")
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        Log.d(TAG, "onUserOffline: uid = $uid, reason = $reason")
    }

    //endregion

    //region AUDIO

    override fun onAudioVolumeIndication(speakers: Array<AudioVolumeInfo>, totalVolume: Int) {
        // Log.d(TAG, "onAudioVolumeIndication: speakers.size = ${speakers.size}, totalVolume = $totalVolume")
        val engine = AgoraEngine.shared

        var speakersByChannel = HashMap<String, ArrayList<AudioVolumeInfo>>()

        for (roomID in engine.rooms.keys) {
            speakersByChannel[roomID] = ArrayList()
        }

        var publisher: AudioVolumeInfo? = null

        for (speaker in speakers) {

            speaker.uid = abs(speaker.uid) // fix uid being negative sometimes

            if (speaker.uid == 0) {
                publisher = speaker
                continue
            }

            if (speakersByChannel[speaker.channelId] == null) {
                speakersByChannel[speaker.channelId] = ArrayList()
            }

            speakersByChannel[speaker.channelId]?.add(speaker)
        }

        for ((channelID, speakers) in speakersByChannel) {

            var room: AgoraRoom = engine.rooms[channelID] ?: continue

            var speakersWithPublishers = speakers

            if (publisher != null && room.state.isPublishing) {
                speakersWithPublishers.add(publisher)
            }

            room.reportAudioVolumeIndicationOfSpeakers(speakersWithPublishers, totalVolume)
        }
    }

    override fun onLocalAudioStats(stats: LocalAudioStats?) {
        Log.d(TAG, "onLocalAudioStats: ")
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
        Log.d(TAG, "onRemoteAudioStats: ")
    }

    //endregion

    // region VIDEO
    override fun onUserEnableVideo(uid: Int, enabled: Boolean) {
        Log.d(TAG, "onUserEnableVideo: ")
    }

    override fun onUserEnableLocalVideo(uid: Int, enabled: Boolean) {
        Log.d(TAG, "onUserEnableLocalVideo: ")
    }

    override fun onVideoSizeChanged(uid: Int, width: Int, height: Int, rotation: Int) {
        Log.d(TAG, "onVideoSizeChanged: ")
    }

    override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
        Log.d(TAG, "onFirstLocalVideoFrame: ")
    }

    override fun onFirstLocalVideoFramePublished(elapsed: Int) {
        Log.d(TAG, "onFirstLocalVideoFramePublished: ")
    }

    override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
        Log.d(TAG, "onFirstRemoteVideoFrame: ")
    }

    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        Log.d(
            TAG,
            "onRemoteVideoStateChanged: uid = $uid, state = $state, reason = $reason, elapsed = $elapsed"
        )
    }

    override fun onLocalVideoStats(stats: LocalVideoStats?) {
        Log.d(TAG, "onLocalVideoStats: ")
    }

    override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
        Log.d(TAG, "onRemoteVideoStats: ")
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
        Log.d(
            TAG,
            "onFirstRemoteVideoDecoded: uid = $uid, width = $width, height = $height, elapsed = $elapsed"
        )
        Log.d("AgoraUserState", "[REMOTEVIDEOERROR] decoded")
    }

    override fun onUserMuteVideo(uid: Int, muted: Boolean) {
        Log.d(TAG, "onUserMuteVideo: uid = $uid, muted = $muted")
    }

    // endregion
}
