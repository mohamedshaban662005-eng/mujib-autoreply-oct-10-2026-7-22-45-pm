package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AutoReplyEngine
import com.example.data.local.AppDatabase
import com.example.data.local.AutoReplyLogEntity
import com.example.data.repository.KnowledgeBaseRepository
import com.example.model.*
import com.example.service.AutoReplyNotificationListenerService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AutoReplyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KnowledgeBaseRepository(application)
    private val database = AppDatabase.getInstance(application)
    private val dao = database.autoReplyDao()

    val profile: StateFlow<BusinessProfile> = repository.profile
    val isServiceActive: StateFlow<Boolean> = repository.isServiceActive
    val autoReplyDelaySeconds: StateFlow<Int> = repository.autoReplyDelaySeconds

    val allLogs: StateFlow<List<AutoReplyLogEntity>> = dao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLogsCount: StateFlow<Int> = dao.getTotalLogsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val replyCount: StateFlow<Int> = dao.getReplyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val handoffCount: StateFlow<Int> = dao.getHandoffCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val noReplyCount: StateFlow<Int> = dao.getNoReplyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isPermissionGranted = MutableStateFlow(
        AutoReplyNotificationListenerService.isPermissionGranted(application)
    )
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    // Simulator State
    val simApp = MutableStateFlow(SupportedApp.WHATSAPP)
    val simSender = MutableStateFlow("أحمد محمود")
    val simIsGroup = MutableStateFlow(false)
    val simMessage = MutableStateFlow("بكام طقم سرير قطن مصري؟ وبتوصلوا لمدينة نصر؟")
    val simHistory = MutableStateFlow("")
    val simWithinWorkingHours = MutableStateFlow(true)

    private val _simIsEvaluating = MutableStateFlow(false)
    val simIsEvaluating: StateFlow<Boolean> = _simIsEvaluating.asStateFlow()

    private val _simResult = MutableStateFlow<AutoReplyResult?>(null)
    val simResult: StateFlow<AutoReplyResult?> = _simResult.asStateFlow()

    private val _simLatencyMs = MutableStateFlow(0L)
    val simLatencyMs: StateFlow<Long> = _simLatencyMs.asStateFlow()

    fun refreshPermission() {
        _isPermissionGranted.value =
            AutoReplyNotificationListenerService.isPermissionGranted(getApplication())
    }

    fun setServiceActive(active: Boolean) {
        repository.setServiceActive(active)
    }

    fun setDelaySeconds(seconds: Int) {
        repository.setAutoReplyDelaySeconds(seconds)
    }

    fun updateProfile(newProfile: BusinessProfile) {
        repository.updateProfile(newProfile)
    }

    fun applyPreset(presetType: String) {
        repository.applyPreset(presetType)
    }

    fun runSimulator() {
        viewModelScope.launch {
            _simIsEvaluating.value = true
            val startTime = System.currentTimeMillis()

            val payload = NotificationPayload(
                app = simApp.value,
                sender = simSender.value,
                isGroup = simIsGroup.value,
                message = simMessage.value,
                history = simHistory.value,
                localTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
                withinWorkingHours = simWithinWorkingHours.value
            )

            val currentProfile = profile.value
            val result = AutoReplyEngine.processNotification(payload, currentProfile)
            val latency = System.currentTimeMillis() - startTime

            _simResult.value = result
            _simLatencyMs.value = latency
            _simIsEvaluating.value = false

            // Also record in logs as simulated run
            dao.insertLog(
                AutoReplyLogEntity(
                    app = payload.app.id,
                    sender = payload.sender,
                    isGroup = payload.isGroup,
                    message = payload.message,
                    action = result.action.name,
                    reply = result.reply,
                    reason = result.reason,
                    timestamp = System.currentTimeMillis(),
                    latencyMs = latency,
                    isSimulated = true,
                    status = "SIMULATED"
                )
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun setSimScenario(preset: String) {
        when (preset) {
            "PRICE_AND_AREA" -> {
                simApp.value = SupportedApp.WHATSAPP
                simSender.value = "أحمد"
                simIsGroup.value = false
                simMessage.value = "بكام طقم السرير القطن وبتوصلوا مدينة نصر؟"
                simHistory.value = ""
                simWithinWorkingHours.value = true
            }
            "COMPLAINT" -> {
                simApp.value = SupportedApp.MESSENGER
                simSender.value = "محمود"
                simIsGroup.value = false
                simMessage.value = "الطلب اتأخر وماحدش بيرد عليا وفين المندوب؟!"
                simHistory.value = ""
                simWithinWorkingHours.value = true
            }
            "NO_QUESTION" -> {
                simApp.value = SupportedApp.WHATSAPP
                simSender.value = "سارة"
                simIsGroup.value = false
                simMessage.value = "👍 شكراً جزيلاً"
                simHistory.value = "تم توضيح المقاس واللون للعميلة"
                simWithinWorkingHours.value = true
            }
            "UNKNOWN_INFO" -> {
                simApp.value = SupportedApp.INSTAGRAM
                simSender.value = "نورا"
                simIsGroup.value = false
                simMessage.value = "هل بتفصلوا مقاسات خاصة لسرير كينج 220 سم؟"
                simHistory.value = ""
                simWithinWorkingHours.value = true
            }
            "AFTER_HOURS" -> {
                simApp.value = SupportedApp.WHATSAPP
                simSender.value = "كريم"
                simIsGroup.value = false
                simMessage.value = "عايز اطلب لحاف فيبر تركي ضروري"
                simHistory.value = ""
                simWithinWorkingHours.value = false
            }
        }
    }
}
