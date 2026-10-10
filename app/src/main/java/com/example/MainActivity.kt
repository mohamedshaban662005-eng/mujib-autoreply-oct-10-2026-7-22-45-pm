package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.KnowledgeBaseScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.SettingsAndPromptScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.theme.ActionReplyGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AutoReplyViewModel

enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    SIMULATOR("المحاكاة", Icons.Default.Science, "tab_simulator"),
    KNOWLEDGE_BASE("قاعدة المعرفة", Icons.Default.AutoStories, "tab_knowledge_base"),
    LOGS("سجل الردود", Icons.Default.History, "tab_logs"),
    SETTINGS("الإعدادات", Icons.Default.Tune, "tab_settings")
}

class MainActivity : ComponentActivity() {
    private val viewModel: AutoReplyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermission()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AutoReplyViewModel) {
    var selectedTab by remember { mutableStateOf(NavTab.SIMULATOR) }
    val isServiceActive by viewModel.isServiceActive.collectAsState()
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()

    // BackHandler: Navigate back to SIMULATOR tab if currently on another tab
    BackHandler(enabled = selectedTab != NavTab.SIMULATOR) {
        selectedTab = NavTab.SIMULATOR
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مُجيب الرد التلقائي",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isServiceActive && isPermissionGranted)
                                    "الخدمة نشطة وجاهزة للرد"
                                else if (!isPermissionGranted)
                                    "بانتظار إذن الإشعارات"
                                else
                                    "الخدمة متوقفة مؤقتاً",
                                fontSize = 11.sp,
                                color = if (isServiceActive && isPermissionGranted)
                                    ActionReplyGreen
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = if (isServiceActive && isPermissionGranted)
                            ActionReplyGreen.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isServiceActive && isPermissionGranted) ActionReplyGreen
                                        else MaterialTheme.colorScheme.outline
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isServiceActive && isPermissionGranted) "نشط" else "متوقف",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isServiceActive && isPermissionGranted) ActionReplyGreen
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(imageVector = tab.icon, contentDescription = tab.title)
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    NavTab.SIMULATOR -> SimulatorScreen(viewModel = viewModel)
                    NavTab.KNOWLEDGE_BASE -> KnowledgeBaseScreen(viewModel = viewModel)
                    NavTab.LOGS -> LogsScreen(viewModel = viewModel)
                    NavTab.SETTINGS -> SettingsAndPromptScreen(viewModel = viewModel)
                }
            }
        }
    }
}
