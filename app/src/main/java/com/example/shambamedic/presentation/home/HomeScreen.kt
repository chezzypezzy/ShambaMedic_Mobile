package com.example.shambamedic.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shambamedic.R
import com.example.shambamedic.domain.model.Scan
import com.example.shambamedic.presentation.common.cropDisplayName
import com.example.shambamedic.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val primaryGreen = Color(0xFF2E7D32)
    val secondaryGreen = Color(0xFF66BB6A)
    val backgroundGrey = Color(0xFFF5F5F5)

    var showCropPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error?.contains("Session") == true) {
            viewModel.logout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = {
                            navController.navigate(Screen.Notifications.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.content_desc_notifications),
                                tint = Color.White
                            )
                        }
                        if (uiState.unreadNotificationCount > 0) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd),
                                containerColor = Color(0xFFB71C1C)
                            ) {
                                Text(
                                    text = if (uiState.unreadNotificationCount > 9)
                                        "9+" else "${uiState.unreadNotificationCount}",
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = viewModel::triggerSync,
                        enabled = !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = stringResource(R.string.content_desc_sync_now),
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = viewModel::logout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = stringResource(R.string.action_logout),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryGreen)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.History.route) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_history)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_profile)) }
                )
            }
        },
        containerColor = backgroundGrey
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // GREETING SECTION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.greeting_hello, uiState.userName),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(secondaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.userName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // WEEKLY ACTIVITY CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.weekly_activity_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (uiState.weeklyScanCounts.all { it == 0 }) {
                        Text(
                            text = stringResource(R.string.weekly_activity_empty),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val dayLabels = remember {
                            (6 downTo 0).map { daysAgo ->
                                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                                SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            uiState.weeklyScanCounts.forEachIndexed { index, value ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height((value.coerceAtLeast(1) * 12).dp)
                                            .background(
                                                primaryGreen,
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dayLabels[index],
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // STATS GRID
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Default.Coronavirus,
                    value = "${uiState.totalDiseasesFound}",
                    label = stringResource(R.string.stat_diseases_found),
                    color = Color(0xFFB71C1C),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.CheckCircle,
                    value = "${uiState.healthyScansCount}",
                    label = stringResource(R.string.stat_healthy_scans),
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Default.HourglassEmpty,
                    value = "${uiState.pendingReviewCount}",
                    label = stringResource(R.string.stat_awaiting_review),
                    color = Color(0xFFF57F17),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.TrendingUp,
                    value = uiState.mostCommonDisease ?: "N/A",
                    label = stringResource(R.string.stat_most_common),
                    color = Color(0xFF1565C0),
                    isTextValue = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // START SCAN SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.ready_to_scan),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showCropPicker = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_start_new_scan),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // RECENT ACTIVITY PREVIEW
            if (uiState.recentScanCount > 0) {
                Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recent_scans_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { navController.navigate(Screen.History.route) }) {
                            Text(
                                text = stringResource(R.string.action_view_all),
                                fontSize = 13.sp,
                                color = primaryGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.recentScans.forEach { recentScan ->
                            RecentScanRow(
                                recentScan = recentScan,
                                onClick = {
                                    navController.navigate(Screen.Results.createRoute(recentScan.scan.scanId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCropPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCropPicker = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_crop_type),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                CROP_ITEMS.forEach { crop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCropPicker = false
                                navController.navigate(Screen.Camera.createRoute(crop.cropType))
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(crop.backgroundColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            // Deliberately shown together regardless of locale (not driven by
                            // the language toggle) - a low-literacy-friendly bilingual label,
                            // not a translatable-vs-untranslated gap.
                            Text(text = crop.displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = crop.swahiliName, fontSize = 13.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    isTextValue: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(
                text = value,
                fontSize = if (isTextValue) 13.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun RecentScanRow(recentScan: RecentScanDisplay, onClick: () -> Unit) {
    val scan = recentScan.scan
    val label = when {
        recentScan.escalationStatus == "pending" -> stringResource(R.string.status_awaiting_expert_review)
        recentScan.escalationStatus == "resolved" -> stringResource(R.string.status_expert_diagnosis_received)
        scan.diseaseId != null -> stringResource(R.string.status_disease_detected, cropDisplayName(scan.cropType))
        else -> stringResource(R.string.status_healthy_crop, cropDisplayName(scan.cropType))
    }
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Grass, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = sdf.format(java.util.Date(scan.scanTimestamp)), fontSize = 11.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}
