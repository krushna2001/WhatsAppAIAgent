package com.example.whatsappaiagent

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var enabled by remember { mutableStateOf(AgentPrefs.isEnabled(this)) }
                var sender by remember { mutableStateOf("") }
                val senders = remember { mutableStateListOf<String>().apply {
                    addAll(AgentPrefs.allowedSenders(this@MainActivity))
                } }

                Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("WhatsApp AI Agent", style = MaterialTheme.typography.headlineMedium)
                    Text("Prototype · Replies only to sender names you allow")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Switch(checked = enabled, onCheckedChange = {
                            enabled = it
                            AgentPrefs.setEnabled(this@MainActivity, it)
                        })
                        Text(if (enabled) "Auto-reply enabled" else "Auto-reply paused")
                    }
                    Button(onClick = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }) { Text("Grant notification access") }

                    HorizontalDivider()
                    Text("Allowed sender names", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = sender, onValueChange = { sender = it },
                        label = { Text("Exact name shown in WhatsApp notification") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        val name = sender.trim()
                        if (name.isNotEmpty() && !senders.contains(name)) {
                            senders.add(name)
                            AgentPrefs.setAllowedSenders(this@MainActivity, senders.toList())
                            sender = ""
                        }
                    }) { Text("Add sender") }
                    senders.forEach { name ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name)
                            TextButton(onClick = {
                                senders.remove(name)
                                AgentPrefs.setAllowedSenders(this@MainActivity, senders.toList())
                            }) { Text("Remove") }
                        }
                    }
                    Text(
                        "Start with one trusted contact. Notification content is sensitive; it is sent to your configured backend for AI processing.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
