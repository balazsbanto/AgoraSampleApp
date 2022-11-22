package com.synervoz.agorasampleapp.agora

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.SyncStateContract
import android.util.Log
import android.util.Log.d
import android.view.SurfaceView
import com.synervoz.agorasampleapp.MyApp
import io.agora.rtc.Constants
import io.agora.rtc.Constants.*
import io.agora.rtc.RtcEngine
import io.agora.rtc.RtcEngineConfig
import io.agora.rtc.models.UserInfo
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import java.io.File
import java.util.logging.Logger

internal class AgoraEngine : AgoraRoomEngineDelegate {

    val TAG = javaClass.name

    companion object {
        lateinit var shared: AgoraEngine

        fun instantiate() {
            shared = AgoraEngine()
        }
    }

    private var configured = false
    internal lateinit var engine: RtcEngine

    private val defaultVideoConfig: VideoEncoderConfiguration = VideoEncoderConfiguration(
        VideoEncoderConfiguration.VD_1280x720,
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
        VideoEncoderConfiguration.STANDARD_BITRATE,
        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
    )
    private var videoConfig: VideoEncoderConfiguration = defaultVideoConfig


    internal var room: ArrayList<AgoraRoom> = ArrayList()

    var mRtcEventHandler: AgoraRtcEngineEventHandler = AgoraRtcEngineEventHandler()

    internal var rooms = HashMap<String, AgoraRoom>()
    private var connectedRooms: ArrayList<AgoraRoom> = ArrayList()
    private var publishedRooms: ArrayList<AgoraRoom> = ArrayList()

    override fun roomWillJoin(room: Room) {
        if (connectedRooms.isEmpty()) {
            setExternalAudio(enabled = true)
        }
    }

    override fun roomDidJoin(room: Room) {
        connectedRooms.add(room as AgoraRoom)
        if (publishedRooms.intersect(connectedRooms.toSet()).isNotEmpty()) {
        }
    }

    override fun roomWillLeave(room: Room) {
        connectedRooms.remove(room)
        if (connectedRooms.isEmpty()) {

        }
    }

    override fun roomDidLeave(room: Room) {
        if (connectedRooms.isEmpty()) {
            setExternalAudio(enabled = false)
        }
    }

    override fun roomDidPublish(room: Room) {
        publishedRooms.add(room as AgoraRoom)
        if (publishedRooms.intersect(connectedRooms.toSet()).isNotEmpty()) {
        }
    }

    override fun roomDidUnpublish(room: Room) {
        publishedRooms.remove(room)
        if (publishedRooms.intersect(connectedRooms.toSet()).isEmpty()) {
        }
    }

    private fun setExternalAudio(enabled: Boolean) {
        engine.setExternalAudioSink(
            enabled,
            48000,
            1
        )
        engine.setExternalAudioSource(
            enabled,
            48000,
            1
        )
    }

    private var publisherVideoView: SurfaceView? = null

    private fun configure(appId: String) {
        if (configured) return

        val logConfig = RtcEngineConfig.LogConfig()
        logConfig.filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/something.txt"
        logConfig.fileSize = 5 * 1024

        val rtcEngineConfig = RtcEngineConfig().apply {
            mContext = MyApp.appContext
            mAppId = appId
            mEventHandler = mRtcEventHandler
            mLogConfig = logConfig
        }

        try {
            engine =
                RtcEngine.create(rtcEngineConfig)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            throw RuntimeException("Agora initialization error\n" + Log.getStackTraceString(e))
        }

        engine.setParameters("{\"che.audio.force.bluetooth.a2dp\":1}")
        engine.setChannelProfile(CHANNEL_PROFILE_LIVE_BROADCASTING)

        //region Audio
        engine.enableAudioVolumeIndication(200, 3, true)

        engine.setAudioProfile(
            AUDIO_PROFILE_SPEECH_STANDARD,
            AUDIO_SCENARIO_MEETING
        )

        engine.enableAudio()
        //endregion

        //region Video

        engine.setVideoEncoderConfiguration(videoConfig)
        engine.enableDualStreamMode(true)
        engine.setLocalPublishFallbackOption(STREAM_FALLBACK_OPTION_VIDEO_STREAM_LOW)
        engine.setRemoteSubscribeFallbackOption(STREAM_FALLBACK_OPTION_VIDEO_STREAM_LOW)

        engine.enableVideo()
        engine.enableLocalVideo(false)

        //endregion ...

        configured = true
    }

//    private fun getAgoraLogPath(): String? {
//
//        val folderName = "something"
//        val fileName = "agorasdk.log"
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            val logFolderPath =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/$folderName"
//            File(logFolderPath).mkdirs()
//            try {
//                File("$logFolderPath/$fileName").delete()
//            } catch (exception: SecurityException) {
//                return null
//            }
//
//            return "$logFolderPath/$fileName"
//        }
//
//        val values = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//            put(
//                MediaStore.MediaColumns.RELATIVE_PATH,
//                Environment.DIRECTORY_DOCUMENTS + "/$folderName/"
//            )
//        }
//
//        var uri: Uri = MyApp.appContext.contentResolver.insert(
//            MediaStore.Files.getContentUri("external"),
//            values
//        ) ?: return null
//
//        val filePath = MediaStoreUriHelper.getDataColumn(uri) ?: return null
//        val filePathWithFileNumberReplaced = filePath.replace(" (1)", "")
//
//        MyApp.appContext.contentResolver
//            .delete(uri, null, null)
//
//        try {
//            File(filePathWithFileNumberReplaced).delete()
//        } catch (exception: SecurityException) {
//            return null
//        }
//
//        return filePathWithFileNumberReplaced
//    }

    fun createRoom(roomID: String): AgoraRoom? {
        configure(MyApp.agoraAppId)

        val channel = engine.createRtcChannel(roomID)
        if (channel == null) {
            return null
        }
        val room = AgoraRoom(roomID, channel)
        rooms[roomID] = room
        return room
    }

    fun setPublisherVideoEnabled(isEnabled: Boolean): SurfaceView? {
        if (isEnabled) {
            publisherVideoView =
                RtcEngine.CreateRendererView(MyApp.appContext)
            publisherVideoView?.setZOrderMediaOverlay(true)

            val uid = uid(MyApp.userId) ?: 0

            val videoCanvas = VideoCanvas(publisherVideoView, VideoCanvas.RENDER_MODE_HIDDEN, uid)
            var result = engine.setupLocalVideo(videoCanvas)

            result = engine.enableLocalVideo(true)
            return publisherVideoView
        }

        if (!isEnabled) {
            val result = engine.enableLocalVideo(false)
        }
        return null
    }

    fun switchCamera() {
        engine.switchCamera()
    }

    fun getVideoEncoderConfiguration(): VideoEncoderConfiguration {
        configure(MyApp.agoraAppId)
        return videoConfig
    }

    fun setVideoEncoderConfiguration(config: VideoEncoderConfiguration) {
        configure(MyApp.agoraAppId)
        videoConfig = config
        if (engine != null) {
            engine.setVideoEncoderConfiguration(config)
        }
    }

    fun userAccount(uid: Int): String? {
        var userInfo = UserInfo()
        val result = engine.getUserInfoByUid(uid, userInfo)
        if (result == 0) {
            return userInfo.userAccount
        }
        return null
    }

    fun uid(userAccount: String): Int? {
        var userInfo = UserInfo()
        val result = engine.getUserInfoByUserAccount(userAccount, userInfo)
        if (result == 0) {
            return userInfo.uid
        }
        return null
    }

    fun leaveChannel() {

    }

    fun destroy() {

    }
}
