package com.example.shambamedic.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.shambamedic.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryGreen = Color(0xFF2E7D32)
    val backgroundGrey = Color(0xFFF5F5F5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan History", fontWeight = FontWeight.Bold, color = Color.White) }, // TODO: Add Swahili
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryGreen)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    } },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") } // TODO: Add Swahili
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") } // TODO: Add Swahili
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") } // TODO: Add Swahili
                )
            }
        },
        containerColor = backgroundGrey
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // FILTER ROW
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "all" to "All",
                    "maize" to "Maize",
                    "potato" to "Potato",
                    "tomato" to "Tomato",
                    "pending" to "Pending Sync"
                )

                items(filters) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter.first,
                        onClick = { viewModel.applyFilter(filter.first) },
                        label = { Text(filter.second) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryGreen)
                }
            } else if (uiState.filteredScans.isEmpty()) {
                val emptyMessage = when (uiState.selectedFilter) {
                    "all" -> "No scans yet.\nTap a crop on the " +
                        "home screen to begin your first diagnosis."
                    "pending" -> "No pending scans.\n" +
                        "All scans have been synced to the cloud."
                    else -> "No ${uiState.selectedFilter} scans " +
                        "found.\nTry selecting a different filter."
                }
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.ImageSearch, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Text(
                            // TODO: Add Swahili
                            emptyMessage,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredScans) { scanWithDisease ->
                        ScanHistoryCard(
                            scanWithDisease = scanWithDisease,
                            onClick = {
                                navController.navigate(Screen.Results.createRoute(scanWithDisease.scan.scanId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanHistoryCard(scanWithDisease: ScanWithDiseaseName, onClick: () -> Unit) {
    val scan = scanWithDisease.scan
    val primaryGreen = Color(0xFF2E7D32)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // LEFT SEVERITY BAR - reflects the scan's original diagnosis severity,
            // independent of escalation/review status.
            val severityColor = when (scan.severity.lowercase()) {
                "low" -> Color(0xFF388E3C)
                "moderate" -> Color(0xFFF57F17)
                else -> Color(0xFFB71C1C)
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(severityColor, RoundedCornerShape(2.dp))
            )

            // CONTENT
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = scanWithDisease.diseaseName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                Text(text = formatTimestamp(scan.scanTimestamp), fontSize = 12.sp, color = Color.Gray)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Confidence: ${(scan.confidenceScore * 100).toInt()}%", fontSize = 11.sp, color = Color.Gray)

                    val syncColor = if (scan.syncStatus == "synchronized") primaryGreen else Color.Gray
                    Text(
                        text = scan.syncStatus.replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        color = syncColor
                    )
                }
            }

            // SYNC ICON (+ escalation status indicator)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (scanWithDisease.escalationStatus == "pending") {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFF57F17)
                    )
                } else if (scanWithDisease.escalationStatus == "resolved") {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = primaryGreen
                    )
                }
                Icon(
                    imageVector = if (scan.syncStatus == "synchronized") Icons.Default.CloudDone else Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (scan.syncStatus == "synchronized") primaryGreen else Color.Gray
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat(
        "MMM dd, yyyy • HH:mm",
        java.util.Locale.getDefault()
    )
    return sdf.format(java.util.Date(timestamp))
}
