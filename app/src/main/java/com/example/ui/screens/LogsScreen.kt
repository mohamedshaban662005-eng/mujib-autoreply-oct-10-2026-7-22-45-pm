package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReplyAll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AutoReplyLogEntity
import com.example.model.ActionType
import com.example.model.SupportedApp
import com.example.ui.components.ActionBadge
import com.example.ui.components.AppBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.ActionHandoffAmber
import com.example.ui.theme.ActionNoReplyGray
import com.example.ui.theme.ActionReplyGreen
import com.example.ui.viewmodel.AutoReplyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: AutoReplyViewModel,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val totalCount by viewModel.totalLogsCount.collectAsState()
    val replyCount by viewModel.replyCount.collectAsState()
    val handoffCount by viewModel.handoffCount.collectAsState()
    val noReplyCount by viewModel.noReplyCount.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(allLogs, selectedFilter, searchQuery) {
        allLogs.filter { log ->
            val matchesFilter = when (selectedFilter) {
                "REPLY" -> log.action == "REPLY"
                "HANDOFF" -> log.action == "HANDOFF"
                "NO_REPLY" -> log.action == "NO_REPLY"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                log.sender.contains(searchQuery, ignoreCase = true) ||
                        log.message.contains(searchQuery, ignoreCase = true) ||
                        log.reply.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // Analytics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "الإجمالي",
                    count = totalCount,
                    icon = Icons.Default.AllInbox,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "رد فوري",
                    count = replyCount,
                    icon = Icons.AutoMirrored.Filled.ReplyAll,
                    color = ActionReplyGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "تحويل بشري",
                    count = handoffCount,
                    icon = Icons.Default.SupportAgent,
                    color = ActionHandoffAmber,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "تجاهل",
                    count = noReplyCount,
                    icon = Icons.Default.Block,
                    color = ActionNoReplyGray,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar and Clear Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في الرسائل أو المرسلين...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("logs_search_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "مسح السجل",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf(
                    "ALL" to "الكل ($totalCount)",
                    "REPLY" to "ردود تلقائية ($replyCount)",
                    "HANDOFF" to "تحويل لموظف ($handoffCount)",
                    "NO_REPLY" to "تجاهل بدون رد ($noReplyCount)"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Logs List
        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (allLogs.isEmpty()) "لا توجد سجلات بعد" else "لا توجد نتائج مطابقة للبحث",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (allLogs.isEmpty())
                                "ستظهر هنا جميع الإشعارات التي تم استقبالها والرد عليها"
                            else
                                "جرب البحث بكلمة أخرى أو تغيير الفلتر",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                LogItemCard(log = log, onDelete = { viewModel.deleteLog(log.id) })
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("مسح سجل العمليات") },
            text = { Text("هل أنت متأكد من مسح جميع سجلات الإشعارات والردود السابقة؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLogs()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("مسح الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun LogItemCard(
    log: AutoReplyLogEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(log.timestamp) {
        SimpleDateFormat("hh:mm a - yyyy/MM/dd", Locale.getDefault()).format(Date(log.timestamp))
    }

    val actionType = try {
        ActionType.valueOf(log.action)
    } catch (e: Exception) {
        ActionType.REPLY
    }

    val app = SupportedApp.fromName(log.app)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: App, Sender, Time, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppBadge(app = app)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.sender,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (log.isSimulated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "محاكاة",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Message Body
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "الرسالة الواردة:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = log.message,
                        fontSize = 13.sp
                    )
                }
            }

            // Reply Body (if any)
            if (log.reply.isNotBlank() && actionType != ActionType.NO_REPLY) {
                Surface(
                    color = if (actionType == ActionType.REPLY)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (actionType == ActionType.REPLY) "الرد التلقائي:" else "رد التحويل لموظف:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (actionType == ActionType.REPLY) ActionReplyGreen else ActionHandoffAmber
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.reply,
                            fontSize = 13.sp,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }

            // Footer: Action Badge, Latency, Reason
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionBadge(action = actionType)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.latencyMs > 0) {
                        Text(
                            text = "${log.latencyMs} ms",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
