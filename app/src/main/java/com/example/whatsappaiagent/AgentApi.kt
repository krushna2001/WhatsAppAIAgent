package com.example.whatsappaiagent

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AgentApi {
    private val client = OkHttpClient.Builder()
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    fun generateReply(sender: String, message: String): String {
        val body = JSONObject()
            .put("sender", sender)
            .put("message", message)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(BuildConfig.AGENT_API_URL)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Backend error: ${response.code}")
            val text = response.body?.string() ?: error("Empty backend response")
            return JSONObject(text).optString("reply").trim()
        }
    }
}
