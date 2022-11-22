package com.synervoz.agorasampleapp.agora

data class VideoStats(
    var decoderOutputFrameRate: Int = 0,
    var rendererOutputFrameRate: Int = 0,
    var packetLossRate: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var totalFrozenTime: Int = 0,
    var frozenRate: Int = 0,
    var totalActiveTime: Int = 0,
    var publishDuration: Int = 0
) {
    fun toDictionary(): MutableMap<String, Any> {
        return mutableMapOf(
            "decoderOutputFrameRate" to decoderOutputFrameRate,
            "frozenRate" to frozenRate,
            "height" to height,
            "packetLossRate" to packetLossRate,
            "publishDuration" to publishDuration,
            "rendererOutputFrameRate" to rendererOutputFrameRate,
            "totalActiveTime" to totalActiveTime,
            "totalFrozenTime" to totalFrozenTime,
            "width" to width
        )
    }
}
