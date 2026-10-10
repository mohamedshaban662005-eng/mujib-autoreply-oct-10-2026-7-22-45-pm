package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiClient
import com.example.ai.PromptBuilder
import com.example.service.AutoReplyNotificationListenerService
import com.example.ui.components.PermissionCard
import com.example.ui.theme.ActionReplyGreen
import com.example.ui.viewmodel.AutoReplyViewModel

@Composable
fun SettingsAndPromptScreen(
    viewModel: AutoReplyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val profile by viewModel.profile.collectAsState()
    val isServiceActive by viewModel.isServiceActive.collectAsState()
    val delaySeconds by viewModel.autoReplyDelaySeconds.collectAsState()
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()

    var showPromptCode by remember { mutableStateOf(false) }
    var copiedFeedback by remember { mutableStateOf(false) }

    val fullPrompt = remember(profile) {
        PromptBuilder.buildSystemInstruction(profile)
    }

    val hasGeminiApiKey = remember { GeminiClient.hasApiKey() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // Notification Listener Permission
        item {
            PermissionCard(
                isGranted = isPermissionGranted,
                onOpenSettings = {
                    try {
                        context.startActivity(AutoReplyNotificationListenerService.getOpenPermissionIntent())
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }
            )
        }

        // Service Master Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تفعيل الرد التلقائي الحي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "الاستماع لإشعارات واتساب وماسنجر وإنستغرام والرد عليها فورياً",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isServiceActive,
                            onCheckedChange = { viewModel.setServiceActive(it) },
                            modifier = Modifier.testTag("service_master_toggle")
                        )
                    }

                    HorizontalDivider()

                    // Delay before response
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مهلة انتظار قبل الإرسال (محاكاة الكتابة):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$delaySeconds ثوانٍ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 1, 2, 3, 5).forEach { sec ->
                                FilterChip(
                                    selected = delaySeconds == sec,
                                    onClick = { viewModel.setDelaySeconds(sec) },
                                    label = { Text("${sec}s", fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Model Engine Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasGeminiApiKey) ActionReplyGreen.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = if (hasGeminiApiKey) ActionReplyGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "محرك الذكاء الاصطناعي (AI Engine)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (hasGeminiApiKey) "Gemini 3.5 Flash (متصل ونشط)" else "المحرك الذكي المدمج (Rule-Based + AI Ready)",
                                    fontSize = 12.sp,
                                    color = if (hasGeminiApiKey) ActionReplyGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            color = if (hasGeminiApiKey) Color(0xFFE8F5E9) else Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (hasGeminiApiKey) "ONLINE" else "HYBRID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasGeminiApiKey) ActionReplyGreen else Color(0xFF0369A1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "يعمل النظام بموجب البرومبت الإلزامي وقواعد المعرفة. عند توفر مفتاح Gemini API يتصل مباشرة بالسيرفر، وفي حالة عدم توفره ينفذ المحرك المدمج المعالجة الفورية بدقة وبدون أي تأخير.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Live System Instruction Prompt Inspector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "برومبت النظام المُولّد (System Instruction)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "يتم توليده تلقائياً من بيانات قاعدة المعرفة المحدثة",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(fullPrompt))
                                    copiedFeedback = true
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ البرومبت")
                            }
                            IconButton(onClick = { showPromptCode = !showPromptCode }) {
                                Icon(
                                    imageVector = if (showPromptCode) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "عرض"
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = copiedFeedback) {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            copiedFeedback = false
                        }
                        Text(
                            text = "تم نسخ البرومبت بالكامل للحافظة بنجاح!",
                            color = ActionReplyGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = showPromptCode) {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = fullPrompt,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // About the App
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "مُجيب (Mujib AutoReply) v1.0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "نظام متكامل للرد التلقائي على إشعارات منصات المراسلة الفورية عبر الذكاء الاصطناعي، يلتزم التزاماً صارماً بقواعد خدمة العملاء وقاعدة المعرفة المخصصة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
