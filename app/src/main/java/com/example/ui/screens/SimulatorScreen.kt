package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.SupportedApp
import com.example.ui.components.ActionBadge
import com.example.ui.components.AppBadge
import com.example.ui.theme.ActionReplyGreen
import com.example.ui.viewmodel.AutoReplyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    viewModel: AutoReplyViewModel,
    modifier: Modifier = Modifier
) {
    val simApp by viewModel.simApp.collectAsState()
    val simSender by viewModel.simSender.collectAsState()
    val simIsGroup by viewModel.simIsGroup.collectAsState()
    val simMessage by viewModel.simMessage.collectAsState()
    val simHistory by viewModel.simHistory.collectAsState()
    val simWithinHours by viewModel.simWithinWorkingHours.collectAsState()

    val isEvaluating by viewModel.simIsEvaluating.collectAsState()
    val simResult by viewModel.simResult.collectAsState()
    val latencyMs by viewModel.simLatencyMs.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showRawJson by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "مختبر محاكاة الإشعارات والذكاء الاصطناعي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "اختبر ردود النظام وقاعدة المعرفة والتحقق من التزام القواعد فورياً",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 2. Preset Scenarios Chips
        item {
            Text(
                text = "سيناريوهات جاهزة للاختبار السريع:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val presets = listOf(
                    "PRICE_AND_AREA" to "استفسار سعر وتوصيل",
                    "COMPLAINT" to "شكوى تأخير طلب",
                    "NO_QUESTION" to "شكر بدون سؤال 👍",
                    "UNKNOWN_INFO" to "معلومة خارج القاعدة",
                    "AFTER_HOURS" to "خارج ساعات العمل"
                )
                items(presets) { (key, label) ->
                    SuggestionChip(
                        onClick = { viewModel.setSimScenario(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 3. Simulation Parameters Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "بيانات الإشعار الوارد:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // App Selector
                    Text("التطبيق المصدِر للإشعار:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            SupportedApp.WHATSAPP,
                            SupportedApp.MESSENGER,
                            SupportedApp.INSTAGRAM,
                            SupportedApp.FACEBOOK
                        ).forEach { app ->
                            val isSelected = simApp == app
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.simApp.value = app },
                                label = { Text(app.displayName, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    // Sender Name
                    OutlinedTextField(
                        value = simSender,
                        onValueChange = { viewModel.simSender.value = it },
                        label = { Text("اسم المرسل (SENDER)") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_sender_input")
                    )

                    // Message
                    OutlinedTextField(
                        value = simMessage,
                        onValueChange = { viewModel.simMessage.value = it },
                        label = { Text("نص الرسالة الواردة (MESSAGE)") },
                        leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, null) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_message_input")
                    )

                    // Optional History
                    OutlinedTextField(
                        value = simHistory,
                        onValueChange = { viewModel.simHistory.value = it },
                        label = { Text("سياق الرسائل السابقة (HISTORY - اختياري)") },
                        placeholder = { Text("مثال: تم توضيح المقاس واللون للعميل") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Switches: Group and Working Hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = simIsGroup,
                                onCheckedChange = { viewModel.simIsGroup.value = it }
                            )
                            Text("رسالة في مجموعة (جروب)", fontSize = 13.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ضمن ساعات العمل", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = simWithinHours,
                                onCheckedChange = { viewModel.simWithinWorkingHours.value = it },
                                modifier = Modifier.testTag("sim_within_hours_switch")
                            )
                        }
                    }

                    // Evaluate Button
                    Button(
                        onClick = { viewModel.runSimulator() },
                        enabled = !isEvaluating && simMessage.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sim_evaluate_button")
                    ) {
                        if (isEvaluating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("جاري معالجة الإشعار بالذكاء الاصطناعي...")
                        } else {
                            Icon(Icons.Default.Bolt, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("محاكاة واستخراج الرد التلقائي", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Result Section
        simResult?.let { result ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        when (result.action) {
                            ActionType.REPLY -> ActionReplyGreen
                            ActionType.HANDOFF -> MaterialTheme.colorScheme.tertiary
                            ActionType.NO_REPLY -> MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sim_result_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header: Action Badge and Latency
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ActionBadge(action = result.action)
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Speed, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$latencyMs ms", fontSize = 11.sp)
                                }
                            }
                        }

                        // Reason Box
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "السبب (Reason): ${result.reason}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Generated Reply (if applicable)
                        if (result.action != ActionType.NO_REPLY && result.reply.isNotBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الرد المعتمد للإرسال في الإشعار:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    val count = result.reply.length
                                    val isWithinLimit = count <= 300
                                    Text(
                                        text = "$count / 300 حرف",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWithinLimit) ActionReplyGreen else MaterialTheme.colorScheme.error
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = result.reply,
                                        modifier = Modifier.padding(14.dp),
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }

                            // Notification Phone Preview
                            Text(
                                text = "معاينة الإشعار على هاتف العميل:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            NotificationPhonePreview(
                                app = simApp,
                                sender = simSender,
                                message = simMessage,
                                reply = result.reply
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.NotificationsOff, null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "تم تجاهل الإشعار بنجاح (بدون إرسال رد) وفقاً لقواعد النظام.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Toggle Raw JSON
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRawJson = !showRawJson }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "عرض صيغة JSON الناتجة",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (showRawJson) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = showRawJson) {
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("JSON Output", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(
                                                    AnnotatedString(
                                                        """{"action":"${result.action.name}","reply":"${result.reply}","reason":"${result.reason}"}"""
                                                    )
                                                )
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "نسخ",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = """{
  "action": "${result.action.name}",
  "reply": "${result.reply}",
  "reason": "${result.reason}"
}""",
                                        color = Color(0xFF38BDF8),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPhonePreview(
    app: SupportedApp,
    sender: String,
    message: String,
    reply: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(app.brandHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = app.displayName,
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text("الآن", color = Color(0xFF90A4AE), fontSize = 11.sp)
            }

            // Sender & message
            Text(
                text = sender,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = message,
                color = Color(0xFFCFD8DC),
                fontSize = 13.sp
            )

            // Direct auto reply container
            Surface(
                color = Color(0xFF37474F),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = null,
                        tint = ActionReplyGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "تم الرد تلقائياً بواسطة مُجيب:",
                            color = ActionReplyGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = reply,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
