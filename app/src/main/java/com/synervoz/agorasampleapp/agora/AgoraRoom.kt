package com.synervoz.agorasampleapp.agora

import android.util.Log
import com.synervoz.agorasampleapp.MyApp
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcChannel
import io.agora.rtc.models.ChannelMediaOptions
import io.agora.rtc.models.UserInfo
import kotlinx.coroutines.*

internal class AgoraRoom(roomID: String, channel: RtcChannel) : Room() {
    val TAG = javaClass.name

    override lateinit var roomInterface: RoomInterface
    var engineDelegate: AgoraRoomEngineDelegate? = null

    private var _state: RoomConnectionState =
        RoomConnectionState() // can we avoid this duplication?
    private var channel = channel

    // Now public to match iOS SDK (MPS)
    var users = HashMap<Int, AgoraUserState>()

    private var mutedUserIDs: ArrayList<String> = ArrayList()

    override var roomID = roomID

    override var state: RoomConnectionState =
        RoomConnectionState() // can we avoid this duplication?
        get() {
                return _state
            }

    fun isSubscribedToAnyUsers(): Boolean {
        for (user in users.values) {
            if (user.audioSubscribeState == Constants.SUB_STATE_SUBSCRIBED) {
                return true
            }
        }
        return false
    }

    init {
        var result: Int = 0
        // TODO this.stateDispatchQueue = DispatchQueue(label: "SBComms.Room.\(roomID).State")

        engineDelegate = AgoraEngine.shared

        val channelEventHandler = object : AgoraRtcChannelEventHandler() {
            override fun onJoinChannelSuccess(rtcChannel: RtcChannel, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(rtcChannel, uid, elapsed)
                Log.d(TAG, "onJoinChannelSuccess channel ${rtcChannel.channelId()} uid $uid")
                // Joining Agora rooms causes the audio engine AAudio streams to disconnect, so
                // we need to reset the input and output devices
//                SwitchboardSDK.getInstance().resetInputOutputDevices()
                this@AgoraRoom.engineDelegate?.roomDidJoin(this@AgoraRoom)
                updateState { _state.isConnected = true }
            }

            override fun onLeaveChannel(
                rtcChannel: RtcChannel?,
                stats: IRtcEngineEventHandler.RtcStats?
            ) {
                Log.d(TAG, "onLeaveChannel: ")
                this@AgoraRoom.engineDelegate?.roomDidLeave(this@AgoraRoom)
                updateState {
                    _state.isConnected = false
                    _state.isSubscribing = false
                    _state.isPublishing = false
                }
                users.clear()
            }

            override fun onClientRoleChanged(rtcChannel: RtcChannel?, oldRole: Int, newRole: Int) {
                Log.d(TAG, "onClientRoleChanged: oldRole = $oldRole, newROLE = $newRole")
                when (newRole) {
                    Constants.CLIENT_ROLE_BROADCASTER -> updateState { _state.isPublishing = true }
                    Constants.CLIENT_ROLE_AUDIENCE -> updateState { _state.isPublishing = false }
                }
            }

            override fun onAudioPublishStateChanged(
                rtcChannel: RtcChannel?,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
            ) {
                Log.d(
                    TAG,
                    "onAudioPublishStateChanged: oldState = $oldState, newState = $newState"
                )

                if (newState == Constants.PUB_STATE_PUBLISHED) {
                    updateState { _state.isPublishing = true }
                }
            }

            override fun onAudioSubscribeStateChanged(
                rtcChannel: RtcChannel?,
                uid: Int,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
            ) {
                Log.d(
                    TAG,
                    "onAudioSubscribeStateChanged: oldState = $oldState, newState = $newState"
                )
                MyApp.handler.post {
                    val user = users[uid]
                    if (user != null) {
                        user.audioSubscribeState = newState
                    } else {
                        Log.w(
                            TAG,
                            "onAudioSubscribeStateChanged received state change for unknown user"
                        )
                    }
                    updateState {
                        _state.isSubscribing = isSubscribedToAnyUsers()
                    }
                }
            }

            override fun onUserJoined(rtcChannel: RtcChannel?, uid: Int, elapsed: Int) {
                super.onUserJoined(rtcChannel, uid, elapsed)
                users[uid] = AgoraUserState(uid, rtcChannel?.channelId())
                val user = users[uid]

                MyApp.handler.post {
                    val userID = AgoraEngine.shared.userAccount(uid)
                    if (userID != null) {
                        roomInterface.userDidJoin(userID)
                        user?.didJoin = true
                    }
                    _updateSubscriberVideoViews()

                    val wasUsernameSet = uid.toString() != users[uid]?.userID
                    if (state.subscribeEnabled && wasUsernameSet && !isUserMuted(user!!.userID)) {
                        _unmuteUser(users[uid]!!.userID)
                    }
                }
            }

            override fun onUserOffline(rtcChannel: RtcChannel?, uid: Int, reason: Int) {
                Log.d(TAG, "onUserOffline: ")
                MyApp.handler.post {
                    val userID = AgoraEngine.shared.userAccount(uid)
                    if (userID != null) {
                        roomInterface.userDidLeave(userID)
                    }
                    users.remove(uid)
                    _updateSubscriberVideoViews()
                }
            }

            override fun onRemoteVideoStateChanged(
                rtcChannel: RtcChannel?,
                uid: Int,
                state: Int,
                reason: Int,
                elapsed: Int
            ) {
                super.onRemoteVideoStateChanged(rtcChannel, uid, state, reason, elapsed)
                MyApp.handler.post {
                    val userState = this@AgoraRoom.users[uid]
                    if (userState == null) {
                        Log.w(
                            TAG,
                            "onRemoteVideoStateChanged call received for unknown user uid = $uid"
                        )
                        return@post
                    }

                    Log.d(TAG, "onRemoteVideoStateChanged userState.remoteVideoState = $state")

                    userState.remoteVideoState = state
                    _updateSubscriberVideoViews()
                }
            }

            override fun onVideoPublishStateChanged(
                rtcChannel: RtcChannel?,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
            ) {
                super.onVideoPublishStateChanged(
                    rtcChannel,
                    oldState,
                    newState,
                    elapseSinceLastState
                )

                MyApp.handler.post {
                    when (newState) {
                        Constants.PUB_STATE_PUBLISHED -> roomInterface.videoPublisherState(false)
                        Constants.PUB_STATE_NO_PUBLISHED -> roomInterface.videoPublisherState(true)
                    }
                }
            }

            override fun onVideoSubscribeStateChanged(
                rtcChannel: RtcChannel?,
                uid: Int,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
            ) {
                super.onVideoSubscribeStateChanged(
                    rtcChannel,
                    uid,
                    oldState,
                    newState,
                    elapseSinceLastState
                )

                val user = users[uid]
//                    ?: return Log.w(
//                    TAG, "onVideoSubscribeStateChanged call received for unknown user"
//                )

                MyApp.handler.post {
                    when (newState) {
                        Constants.SUB_STATE_NO_SUBSCRIBED -> roomInterface.videoDidMute(user!!.userID)
                        Constants.SUB_STATE_SUBSCRIBED -> roomInterface.videoDidUnmute(user!!.userID)
                    }
                }
            }

            override fun onRemoteAudioStateChanged(
                rtcChannel: RtcChannel?,
                uid: Int,
                state: Int,
                reason: Int,
                elapsed: Int
            ) {
                super.onRemoteAudioStateChanged(rtcChannel, uid, state, reason, elapsed)
                val user = users[uid]
                MyApp.handler.post {
                    user?.let {
                        when (state) {
                            Constants.REMOTE_AUDIO_STATE_STOPPED -> {
                                if (reason == Constants.REMOTE_AUDIO_REASON_REMOTE_MUTED)
                                    if (user.didJoin) {
                                        roomInterface.userDidMute(user.userID)
                                    } else {
                                        user.wasMutedBeforeJoining = true
                                    }
                            }
                            Constants.REMOTE_AUDIO_STATE_STARTING -> {
                                if (reason == Constants.REMOTE_AUDIO_REASON_REMOTE_UNMUTED)
                                    roomInterface.userDidUnmute(user.userID)
                            }
                            Constants.REMOTE_AUDIO_STATE_DECODING -> {
                                if (reason == Constants.REMOTE_AUDIO_REASON_REMOTE_UNMUTED)
                                    roomInterface.userDidUnmute(user.userID)
                            }
                        }
                    }
                }
            }

            override fun onRemoteVideoStats(
                rtcChannel: RtcChannel?,
                stats: IRtcEngineEventHandler.RemoteVideoStats?
            ) {
                super.onRemoteVideoStats(rtcChannel, stats)
                stats ?: return
                val userState = users[stats.uid]
//                    ?: return Log.w(
//                    TAG,
//                    "onRemoteVideoStats call received for unknown user"
//                )
                userState!!.videoStats =
                    VideoStats(
                        stats.decoderOutputFrameRate,
                        stats.rendererOutputFrameRate,
                        stats.packetLossRate,
                        stats.width,
                        stats.height,
                        stats.totalFrozenTime,
                        stats.frozenRate,
                        stats.totalActiveTime,
                        stats.publishDuration
                    )
                MyApp.handler.post {
                    _updateSubscriberVideoViews()
                }
            }
        }

        channel.setRtcChannelEventHandler(channelEventHandler)
        result = channel.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        Log.d(TAG, "setClientRole result: $result")

        result = this.channel.muteAllRemoteAudioStreams(true)
        Log.d(TAG, "setClientRole muteAllRemoteAudioStreams: $result")
    }

    private fun _updateSubscriberVideoViews() {
        val views: List<SubscriberVideoView> =
            if (!state.subscribeVideoEnabled || !state.subscribeEnabled || !state.connectEnabled) {
                Log.d(TAG, "[_updateSubscriberVideoViews] returning empty list ")
                ArrayList()
            } else {
                Log.d(TAG, "[_updateSubscriberVideoViews] users.size ${users.size}")
                for (key: Int in users.keys) {
                    Log.d(
                        TAG,
                        "[_updateSubscriberVideoViews] for users.keys = $key, shouldShowVideoView = ${users[key]?.shouldShowVideoView}"
                    )
                }

                users.filter { it.value.shouldShowVideoView }.map {
                    val userAccount = AgoraEngine.shared.userAccount(it.key)
                        ?: it.key.toString()

                    val result = channel.muteRemoteVideoStream(it.key, false)
                    Log.d(
                        TAG,
                        "[_updateSubscriberVideoViews] muteRemoteVideoStream(${it.key}) result = $result"
                    )

                    val videoState = it.value.videoState
                    val videoStats = it.value.videoStats

                    SubscriberVideoView(
                        it.value.videoView!!,
                        userAccount,
                        videoState,
                        videoStats
                    )
                }
            }

        roomInterface.didUpdateSubscriberVideoViews(views as ArrayList<SubscriberVideoView>)
    }

    fun updateState(updateBlock: () -> Unit) {
        val oldValue = _state.copy()
        updateBlock()
        if (_state != oldValue) {
            Log.d(TAG, "Updated state to $_state")
            if (!oldValue.publishEnabled && _state.publishEnabled) {
                engineDelegate?.roomDidPublish(this)
            } else if (oldValue.publishEnabled && !_state.publishEnabled) {
                engineDelegate?.roomDidUnpublish(this)
            }
            MyApp.handler.post {
                roomInterface.didUpdateConnectionState(_state)
            }
        }
    }

    private fun joinWithToken(token: String) {
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = false
        mediaOptions.autoSubscribeVideo = state.subscribeVideoEnabled
        mediaOptions.publishLocalAudio = false
        mediaOptions.publishLocalVideo = false

        engineDelegate?.roomWillJoin(this)
        val result = channel.joinChannelWithUserAccount(
            token,
            MyApp.userId,
            mediaOptions
        )
        Log.d(TAG, "joinChannelWithUserAccount result: $result")

        if (_state.publishEnabled) {
            publish()
        }
        if (_state.subscribeEnabled) {
            subscribe()
        }
        if (state.subscribeVideoEnabled) {
            MyApp.handler.post {
                setSubscriberVideoEnabled(true)
            }
        }
    }

    override fun join(userID: String) {
        Log.d(TAG, "join")

        if (_state.connectEnabled) {
            Log.d(TAG, "join already called")
            return
        }
        updateState { _state.connectEnabled = true }

        val coroutineExceptionHandler =
            CoroutineExceptionHandler { _, throwable ->
                Log.d(TAG, "CoroutineExceptionHandler")
                roomInterface.didFailToJoinWithError(Error(throwable))
            }

        MyApp.userId = userID
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch(coroutineExceptionHandler) {
            val result = Network.post(
                RoomsAPIRequest(
                    roomID,
                    "agora",
                    MyApp.clientId,
                    MyApp.clientSecret
                )
            )

            when (result) {
                is Result.Success<String> -> {
                    val token = RoomsAPIRequest.getToken(result.data)
                    Log.d(TAG, "token = $token")
                    withContext(Dispatchers.Main) {
                        joinWithToken(token)
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Could not get token for room: ${result.message}")
                    withContext(Dispatchers.Main) {
                        updateState { _state.connectEnabled = false }
                        roomInterface.didFailToJoinWithError(Error(result.message))
                    }
                }
            }
        }
    }

    override fun leave() {
        Log.d(TAG, "leave")

        if (!_state.connectEnabled) {
            Log.d(TAG, "leave already called")
            return
        }
        engineDelegate?.roomWillLeave(this)
        val result = channel.leaveChannel()
        Log.d(TAG, "leaveChannel result: $result")
        updateState { _state.connectEnabled = false }
    }

    override fun subscribe() {
        Log.d(TAG, "subscribe")
        val result = channel.muteAllRemoteAudioStreams(false)
        Log.d(TAG, "muteAllRemoteAudioStreams result: $result")

        updateState { _state.subscribeEnabled = true }

        for (user in users) {
            if (user.value.userID != user.value.uid.toString() && !mutedUserIDs.contains(user.value.userID)) {
                _unmuteUser(user.value.userID)
            }
        }

        if (state.subscribeVideoEnabled) {
            setSubscriberVideoEnabled(true)
        }
    }

    override fun unsubscribe() {
        Log.d(TAG, "unsubscribe: ")
        if (!_state.subscribeEnabled) {
            Log.d(TAG, "leave already called")
            return
        }

        val result = channel.muteAllRemoteAudioStreams(true)
        Log.d(TAG, "muteAllRemoteAudioStreams result: $result")

        updateState {
            _state.subscribeEnabled = false
            _state.isSubscribing = false
        }
    }

    override fun publish() {
        Log.d(TAG, "publish: ")
        var result = channel.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        Log.d(TAG, "setClientRole result: $result")
        result = channel.muteLocalAudioStream(false)
        Log.d(TAG, "muteLocalAudioStream result: $result")

        if (result != 0) {
            result = channel.muteLocalAudioStream(true)
            Log.d(TAG, "muteLocalAudioStream result: $result")
            result = channel.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
            Log.d(TAG, "setClientRole result: $result")
            updateState {
                _state.publishEnabled = false
                _state.isPublishing = false
            }
            return
        }

        result = AgoraEngine.shared.engine.enableLocalAudio(true)
        Log.d(TAG, "engine.enableLocalAudio result: $result")
        result = AgoraEngine.shared.engine.muteLocalAudioStream(false)
        Log.d(TAG, "engine.muteLocalAudioStream result: $result")

        updateState { _state.publishEnabled = true }
    }

    override fun unpublish() {
        Log.d(TAG, "unpublish: ")

        if (!_state.publishEnabled) {
            Log.d(TAG, "leave already called")
            return
        }
        var result = channel.muteLocalAudioStream(true)
        Log.d(TAG, "muteLocalAudioStream result: $result")

        result = AgoraEngine.shared.engine.enableLocalAudio(false)
        Log.d(TAG, "engine.enableLocalAudio result: $result")

        result = AgoraEngine.shared.engine.muteLocalAudioStream(true)
        Log.d(TAG, "engine.muteLocalAudioStream result: $result")

        updateState {
            _state.publishEnabled = false
            _state.isPublishing = false
        }
    }

    override fun muteUser(userID: String) {
        Log.d(TAG, "muteUser: uid = $userID")
        mutedUserIDs.add(userID)
        _muteUser(userID)
    }

    private fun _muteUser(userID: String) {
        val userInfo = AgoraEngine.shared.uid(userID)
        if (userInfo == null) {
            Log.d(TAG, "[AgoraRoom] muteUser $userID - user not found")
            return
        }
        val result = channel.muteRemoteAudioStream(userInfo, true)
        Log.d(TAG, "[AgoraRoom] muteUser $userID result: $result")
    }

    override fun unmuteUser(userID: String) {
        Log.d(TAG, "unmuteUser: uid = $userID")
        mutedUserIDs.remove(userID)
        _unmuteUser(userID)
    }

    private fun _unmuteUser(userID: String) {
        val userInfo = AgoraEngine.shared.uid(userID)
        if (userInfo == null) {
            Log.d(TAG, "[AgoraRoom] unmuteUser $userID - user not found")
            return
        }
        val result = channel.muteRemoteAudioStream(userInfo, false)
        Log.d(TAG, "[AgoraRoom] unmute $userID result: $result")
    }

    override fun isUserMuted(userID: String) = mutedUserIDs.contains(userID)

    override fun setSubscriberVideoEnabled(isEnabled: Boolean) {
        Log.d(TAG, "setSubscriberVideoEnabled: isEnabled $isEnabled")
        if (state.subscribeVideoEnabled == isEnabled) {
            Log.d(TAG, "[AgoraRoom] subscribeVideoEnabled already called")
            return
        }
        val result = channel.muteAllRemoteVideoStreams(!isEnabled)
        Log.d(TAG, "[AgoraRoom] muteAllRemoteVideoStreams(${!isEnabled} result: $result")

        updateState { _state.subscribeVideoEnabled = isEnabled }
        MyApp.handler.post {
            _updateSubscriberVideoViews()
        }
    }

    override fun setPublisherVideoEnabled(isEnabled: Boolean) {
        Log.d(TAG, "setPublisherVideoEnabled: isEnabled: $isEnabled")
        if (state.publishVideoEnabled == isEnabled) {
            Log.d(TAG, "[AgoraRoom] publishVideoEnabled already called")
            return
        }

        val publisherVideoView = AgoraEngine.shared.setPublisherVideoEnabled(isEnabled)
        roomInterface.didUpdatePublisherVideoView(publisherVideoView)

        val result = channel.muteLocalVideoStream(!isEnabled)
        Log.d(TAG, "[AgoraRoom] muteLocalVideoStream(${!isEnabled}) result: $result")
        updateState { _state.publishVideoEnabled = isEnabled }
    }

    fun reportAudioVolumeIndicationOfSpeakers(
        speakers: ArrayList<IRtcEngineEventHandler.AudioVolumeInfo>,
        totalVolume: Int
    ) {
        for (speaker in speakers) {
            val audioLevel: Float = speaker.volume.toFloat() / 255.0F
            MyApp.handler.post {
                if (speaker.uid == 0) {
                    roomInterface.didUpdatePublisherAudioLevel(audioLevel)
                } else {
                    val userAccount =
                        AgoraEngine.shared.userAccount(speaker.uid) ?: speaker.uid.toString()

                    roomInterface.didUpdateSubscriberAudioLevel(audioLevel, userAccount)
                }
            }
        }
    }

    fun onUserInfoUpdated(uid: Int, userInfo: UserInfo?) {
        MyApp.handler.post {
            _updateSubscriberVideoViews()
            userInfo ?: return@post

            var user = users[uid]
            val userName = userInfo.userAccount

            user?.let {
                if (!user.didJoin) {
                    roomInterface.userDidJoin(userName)
                    user.didJoin = true

                    if (user.wasMutedBeforeJoining) {
                        roomInterface.userDidMute(userName)
                        user.wasMutedBeforeJoining = false
                    }
                }
            }

            if (state.subscribeEnabled && !mutedUserIDs.contains(userName)) {
                _unmuteUser(userName)
            }
        }
    }

    fun onLocalUserRegistered(uid: Int, userAccount: String?) {
        MyApp.handler.post {
            _updateSubscriberVideoViews()
        }
    }

    private fun toDictionary(users: HashMap<Int, AgoraUserState>): MutableMap<String, Any> {
        var items = mutableMapOf<String, Any>()
        for (user in users) {
            items[user.key.toString()] = user.value.toDictionary()
        }
        return items
    }

    fun toDictionary(): MutableMap<String, Any> {
        return mutableMapOf(
            "users" to toDictionary(users),
            "RoomId" to roomID,
            "State" to state.toDictionary()
        )
    }
}

interface AgoraRoomEngineDelegate {
    fun roomWillJoin(room: Room)
    fun roomDidJoin(room: Room)
    fun roomWillLeave(room: Room)
    fun roomDidLeave(room: Room)
    fun roomDidPublish(room: Room)
    fun roomDidUnpublish(room: Room)
}
