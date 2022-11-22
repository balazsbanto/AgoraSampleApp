package com.synervoz.agorasampleapp.agora

data class RoomConnectionState(
    @JvmField
    var connectEnabled: Boolean = false,
    @JvmField
    var publishEnabled: Boolean = false,
    @JvmField
    var subscribeEnabled: Boolean = false,
    @JvmField
    var isConnected: Boolean = false,
    @JvmField
    var isPublishing: Boolean = false,
    @JvmField
    var isSubscribing: Boolean = false,
    @JvmField
    var publishVideoEnabled: Boolean = false,
    @JvmField
    var subscribeVideoEnabled: Boolean = false,
    @JvmField
    var numberOfSubscribers: Int = 0,
    @JvmField
    var numberOfVideoSubscribers: Int = 0
) {
    fun toDictionary(): MutableMap<String, Any> {
        return mutableMapOf(
            "connectEnabled" to connectEnabled,
            "isConnected" to isConnected,
            "isPublishing" to isPublishing,
            "isSubscribing" to isSubscribing,
            "numberOfSubscribers" to numberOfSubscribers,
            "numberOfVideoSubscribers" to numberOfVideoSubscribers,
            "publishEnabled" to publishEnabled,
            "publishVideoEnabled" to publishVideoEnabled,
            "subscribeVideoEnabled" to subscribeVideoEnabled,
            "subscribeEnabled" to subscribeEnabled
        )
    }
}
