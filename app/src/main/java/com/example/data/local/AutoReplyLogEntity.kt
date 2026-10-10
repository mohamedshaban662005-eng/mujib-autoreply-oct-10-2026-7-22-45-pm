package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auto_reply_logs")
data class AutoReplyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val app: String,
    val sender: String,
    val isGroup: Boolean,
    val message: String,
    val action: String, // REPLY, HANDOFF, NO_REPLY
    val reply: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long = 0,
    val isSimulated: Boolean = false,
    val status: String = "SUCCESS" // SUCCESS, FAILED, SENT
)
