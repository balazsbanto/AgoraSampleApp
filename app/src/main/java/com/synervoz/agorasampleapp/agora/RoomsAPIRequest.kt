package com.synervoz.agorasampleapp.agora

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal data class RoomsAPIRequest(val name: String, val type: String, val clientId: String, val clientSecret: String) : APIRequest {

    override val url: String = "/rooms"

    override val body: String = RequestBody(name, type, clientId, clientSecret).let {
        json.encodeToString(it)
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun getToken(response: String): String = json.decodeFromString<Response>(response).agora.token
    }

    @Serializable
    private data class RequestBody(
        val name: String,
        val type: String,
        val clientId: String,
        val clientSecret: String
    )

    @Serializable
    data class AgoraProperty(val token: String)

    @Serializable
    data class Response(val agora: AgoraProperty)
}
