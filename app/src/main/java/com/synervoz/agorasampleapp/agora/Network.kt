//
//  APIRequest.kt
//  SwitchboardCommunication
//
//  Created by Tayyab Javed on 07/01/21.
//  Copyright © 2021 Synervoz Inc. All rights reserved.
//

package com.synervoz.agorasampleapp.agora

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val code: Int, val message: String) : Result<Nothing>()
}

internal class Network {

    companion object {
        val TAG = Network::class.java.name
        var DEFAULT_API_BASE_URL: String = "https://api-dev.synervoz.com"

        suspend fun <T : APIRequest> post(apiRequest: T): Result<String> =
            withContext(Dispatchers.IO) {

                val baseURL = DEFAULT_API_BASE_URL
                val url = URL(baseURL + apiRequest.url)

                val body = apiRequest.body.toByteArray(Charset.forName("utf-8"))
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doInput = true
                    doOutput = true
                    useCaches = false
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty("Content-Length", body.size.toString())

                    with(outputStream) {
                        write(body, 0, body.size)
                    }
                    connect()
                }.run {
                    if (responseCode in 200..299 && inputStream != null) {
                        inputStream.readBytes().toString(Charset.defaultCharset())
                            .let { Result.Success(it) }
                    } else {
                        errorStream?.let {
                            Result.Error(
                                responseCode,
                                it.readBytes().toString(Charset.defaultCharset())
                            )
                        } ?: Result.Error(responseCode, "Unknown error")
                    }
                }
            }
    }
}
