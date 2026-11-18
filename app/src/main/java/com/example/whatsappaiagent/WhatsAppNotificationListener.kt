package com.example.whatsappaiagent

import android.app.Notification
import android.app.Notification.Action
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WhatsAppNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val inFlight = mutableSetOf<String>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.whatsapp" && sbn.packageName != "com.whatsapp.w4b") return
        if (!AgentPrefs.isEnabled(this)) return

        val extras = sbn.notification.extras ?: return
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val message = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.trim().orEmpty()

        if (sender.isBlank() || message.isBlank() || message == " " || message.length > 2000) return
        if (sender !in AgentPrefs.allowedSenders(this)) return

        val action = sbn.notification.actions
            ?.firstOrNull { candidate -> candidate.remoteInputs?.isNotEmpty() == true } ?: return
        val remoteInput = action.remoteInputs?.firstOrNull() ?: return

        // Prevent duplicate processing of the same notification key while the request is running.
        val dedupeKey = "${sbn.key}:${message.hashCode()}"
        synchronized(inFlight) {
            if (!inFlight.add(dedupeKey)) return
        }

        scope.launch {
            try {
                val reply = AgentApi.generateReply(sender, message)
                if (reply.isNotBlank()) sendInlineReply(action, remoteInput, reply)
            } catch (_: Exception) {
                // Fail closed: do not send a guessed reply when the AI/backend fails.
            } finally {
                synchronized(inFlight) { inFlight.remove(dedupeKey) }
            }
        }
    }

    private fun sendInlineReply(action: Action, remoteInput: RemoteInput, reply: String) {
        val intent = Intent()
        val results = Bundle().apply { putCharSequence(remoteInput.resultKey, reply) }
        RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, results)
        action.actionIntent.send(this, 0, intent)
    }
}
