package com.synervoz.agorasampleapp.agora


abstract class Room {

    @field:JvmSynthetic
    open lateinit var roomID: String
    @field:JvmSynthetic
    open lateinit var state: RoomConnectionState
    @field:JvmSynthetic
    open lateinit var userID: String
    @field:JvmSynthetic
    open lateinit var roomInterface: RoomInterface

    abstract fun join(userID: String)
    abstract fun leave()
    abstract fun subscribe()
    abstract fun unsubscribe()
    abstract fun publish()
    abstract fun unpublish()
    abstract fun muteUser(userID: String)
    abstract fun unmuteUser(userID: String)
    abstract fun isUserMuted(userID: String): Boolean

    abstract fun setSubscriberVideoEnabled(isEnabled: Boolean)
    abstract fun setPublisherVideoEnabled(isEnabled: Boolean)
}
