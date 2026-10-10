package com.example.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.ai.AutoReplyEngine
import com.example.data.local.AppDatabase
import com.example.data.local.AutoReplyLogEntity
import com.example.data.repository.KnowledgeBaseRepository
import com.example.model.ActionType
import com.example.model.NotificationPayload
import com.example.model.SupportedApp
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class AutoReplyNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase
    private lateinit var repository: KnowledgeBaseRepository

    // Cache to prevent duplicate replies to the same message within 1 minute
    private val processedMessages = ConcurrentHashMap<String, Long>()

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = KnowledgeBaseRepository(this)
        isServiceConnected = true
        Log.i(TAG, "AutoReplyNotificationListenerService started")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
        serviceScope.cancel()
        Log.i(TAG, "AutoReplyNotificationListenerService destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        Log.i(TAG, "Notification listener connected successfully")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // 1. Check if app is supported
        val supportedApp = SupportedApp.fromPackage(packageName)
        if (supportedApp == SupportedApp.OTHER) {
            return
        }

        // 2. Filter out ongoing or progress notifications
        val notification = sbn.notification ?: return
        if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) return

        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        if (title.isBlank() || text.isBlank()) return

        // Ignore summary/system notifications
        if (title.contains("messages", ignoreCase = true) || text.contains("new messages", ignoreCase = true)) {
            return
        }

        val isGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false) ||
                title.contains("group", ignoreCase = true) || title.contains("مجموعة", ignoreCase = true)

        // Deduplication check: key of (package + title + text)
        val dedupeKey = "$packageName:$title:$text"
        val now = System.currentTimeMillis()
        val lastSeen = processedMessages[dedupeKey]
        if (lastSeen != null && (now - lastSeen) < 45_000) {
            return // Skip duplicate
        }
        processedMessages[dedupeKey] = now

        // Clean up old entries
        if (processedMessages.size > 200) {
            processedMessages.entries.removeIf { (now - it.value) > 60_000 }
        }

        serviceScope.launch {
            handleIncomingNotification(sbn, supportedApp, title, text, isGroup)
        }
    }

    private suspend fun handleIncomingNotification(
        sbn: StatusBarNotification,
        app: SupportedApp,
        sender: String,
        message: String,
        isGroup: Boolean
    ) {
        val isActive = repository.isServiceActive.value
        if (!isActive) {
            Log.d(TAG, "Auto-reply is toggled off in settings")
            return
        }

        val profile = repository.profile.value
        val startTime = System.currentTimeMillis()

        val payload = NotificationPayload(
            app = app,
            sender = sender,
            isGroup = isGroup,
            message = message,
            history = "",
            localTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
            withinWorkingHours = true // Can be calculated from profile.workingHours
        )

        val result = AutoReplyEngine.processNotification(payload, profile)
        val latencyMs = System.currentTimeMillis() - startTime

        var replySent = false

        if (result.action == ActionType.REPLY && result.reply.isNotBlank()) {
            val delaySeconds = repository.autoReplyDelaySeconds.value
            if (delaySeconds > 0) {
                delay(delaySeconds * 1000L)
            }
            replySent = sendDirectReply(sbn.notification, result.reply)
        }

        // Save log into database
        val logEntity = AutoReplyLogEntity(
            app = app.id,
            sender = sender,
            isGroup = isGroup,
            message = message,
            action = result.action.name,
            reply = result.reply,
            reason = result.reason,
            timestamp = System.currentTimeMillis(),
            latencyMs = latencyMs,
            isSimulated = false,
            status = if (result.action == ActionType.REPLY) (if (replySent) "SENT" else "REPLY_READY") else result.action.name
        )
        database.autoReplyDao().insertLog(logEntity)
    }

    private fun sendDirectReply(notification: Notification, replyText: String): Boolean {
        return try {
            val actions = notification.actions ?: return false
            var targetAction: Notification.Action? = null
            var targetRemoteInput: RemoteInput? = null

            for (action in actions) {
                val remoteInputs = action.remoteInputs
                if (remoteInputs != null && remoteInputs.isNotEmpty()) {
                    for (ri in remoteInputs) {
                        targetAction = action
                        targetRemoteInput = ri
                        break
                    }
                    if (targetAction != null) break
                }
            }

            if (targetAction != null && targetRemoteInput != null) {
                val intent = Intent()
                val bundle = Bundle()
                bundle.putCharSequence(targetRemoteInput.resultKey, replyText)
                RemoteInput.addResultsToIntent(arrayOf(targetRemoteInput), intent, bundle)
                targetAction.actionIntent.send(this, 0, intent)
                Log.i(TAG, "Sent RemoteInput reply successfully: $replyText")
                true
            } else {
                Log.w(TAG, "No RemoteInput action found on notification")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send direct reply", e)
            false
        }
    }

    companion object {
        private const val TAG = "AutoReplyNotifService"

        @Volatile
        var isServiceConnected: Boolean = false

        fun isPermissionGranted(context: Context): Boolean {
            val pkg = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return flat.contains(pkg)
        }

        fun getOpenPermissionIntent(): Intent {
            return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
    }
}
