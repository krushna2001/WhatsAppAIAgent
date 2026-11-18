package com.example.whatsappaiagent

import android.content.Context

object AgentPrefs {
    private const val FILE = "agent_prefs"
    private const val ENABLED = "enabled"
    private const val SENDERS = "senders"

    fun isEnabled(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(ENABLED, value).apply()
    }

    fun allowedSenders(context: Context): Set<String> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getStringSet(SENDERS, emptySet())?.toSet() ?: emptySet()

    fun setAllowedSenders(context: Context, values: List<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putStringSet(SENDERS, values.toSet()).apply()
    }
}
