package com.example.shambamedic.presentation.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.shambamedic.BuildConfig
import com.example.shambamedic.domain.model.Disease
import com.example.shambamedic.domain.model.Treatment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    navController: NavHostController,
    scanId: String,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    if (scanId.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Text(
                    // TODO: Add Swahili
                    "Invalid scan reference",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    // TODO: Add Swahili
                    Text("Go Back")
                }
            }
        }
        return
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scanId) {
        viewModel.loadResults(scanId)
    }

    val primaryGreen = Color(0xFF2E7D32)
    val backgroundGrey = Color(0xFFF5F5F5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnosis Result", fontWeight = FontWeight.Bold, color = Color.White) }, // TODO: Add Swahili
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryGreen)
            )
        },
        containerColor = backgroundGrey
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryGreen)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Text(text = uiState.error!!, color = Color.Gray, fontSize = 16.sp)
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                    ) {
                        Text("Go Back") // TODO: Add Swahili
                    }
                }
            }
        } else if (uiState.scan != null) {
            val scan = uiState.scan!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val escalation = uiState.escalation
                if (escalation != null) {
                    if (escalation.status == "resolved" && escalation.botanistDiagnosis != null) {
                        // RESOLVED DIAGNOSIS CARD
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = primaryGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Expert Diagnosis Received", // TODO: Add Swahili
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                Text(
                                    text = "Botanist's Assessment:", // TODO: Add Swahili
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = escalation.botanistDiagnosis,
                                    fontSize = 15.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        if (uiState.showRatingPrompt) {
                            RatingCard(onSubmit = { stars, comment ->
                                viewModel.submitRating(stars, comment)
                            })
                        } else if (uiState.existingRating != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(uiState.existingRating!!.starRating) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFFFA000)
                                    )
                                }
                                Text(
                                    text = "  You rated this consultation", // TODO: Add Swahili
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        // PENDING REVIEW CARD
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFFF57F17)
                                )
                                Text(
                                    text = "Awaiting Expert Review", // TODO: Add Swahili
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "This scan has been sent to a botanist for expert diagnosis. " +
                                        "Check back here once a response is received.", // TODO: Add Swahili
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // DEBUG ONLY, offline UI testing only: using this while also testing
                        // real sync will create local data that can never match the backend -
                        // don't mix the two in the same test session.
                        if (BuildConfig.DEBUG) {
                            var showDebugDialog by remember { mutableStateOf(false) }
                            var debugDiagnosisText by remember { mutableStateOf("") }

                            TextButton(onClick = { showDebugDialog = true }) {
                                Text(
                                    text = "[DEBUG] Simulate Botanist Response",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            if (showDebugDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDebugDialog = false },
                                    title = { Text("Simulate Botanist Response") },
                                    text = {
                                        OutlinedTextField(
                                            value = debugDiagnosisText,
                                            onValueChange = { debugDiagnosisText = it },
                                            label = { Text("Fake diagnosis") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                viewModel.debugSimulateBotanistResponse(debugDiagnosisText)
                                                showDebugDialog = false
                                                debugDiagnosisText = ""
                                            },
                                            enabled = debugDiagnosisText.isNotBlank()
                                        ) {
                                            Text("Confirm")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDebugDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // SCAN SUMMARY CARD - always shown; scan.severity always holds the
                // original low/moderate/severe classification now, independent of
                // escalation status, so this is shown alongside (not replaced by) the
                // escalation card above.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val healthyCropName = "Healthy ${scan.cropType.replaceFirstChar { it.uppercase() }}"
                                Text(
                                    text = uiState.disease?.diseaseName ?: healthyCropName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B1B),
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = scan.severity.replaceFirstChar { it.uppercase() } + " Impact",
                                    fontSize = 15.sp,
                                    color = Color(0xFF555555)
                                )
                            }

                            val severityColor = when (scan.severity.lowercase()) {
                                "low" -> Color(0xFF388E3C)
                                "moderate" -> Color(0xFFF57F17)
                                else -> Color(0xFFB71C1C)
                            }

                            Surface(
                                color = severityColor,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = scan.severity.uppercase(),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Confidence Score", fontSize = 13.sp, color = Color.Gray) // TODO: Add Swahili
                            Text("${(scan.confidenceScore * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
                        }

                        LinearProgressIndicator(
                            progress = { scan.confidenceScore },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = primaryGreen,
                            trackColor = Color(0xFFE8F5E9),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val isSynced = uiState.syncStatus == "synchronized"
                            Icon(
                                imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSynced) primaryGreen else Color.Gray
                            )
                            Text(
                                text = if (isSynced) "Synced to cloud" else "Pending sync", // TODO: Add Swahili
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                if (escalation == null) {
                // DISEASE INFO CARD
                uiState.disease?.let { disease ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("About This Disease", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) // TODO: Add Swahili
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Text("Symptoms:", fontSize = 13.sp, fontWeight = FontWeight.Bold) // TODO: Add Swahili
                            Text(text = disease.symptomDescription, fontSize = 13.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Severity Impact:", fontSize = 13.sp, fontWeight = FontWeight.Bold) // TODO: Add Swahili
                            Text(text = disease.severityScale, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }

                // TREATMENTS SECTION
                if (uiState.treatments.isNotEmpty()) {
                    Text("Recommended Treatments", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) // TODO: Add Swahili
                    uiState.treatments.forEach { treatment ->
                        TreatmentCard(treatment)
                    }
                }

                // HEALTHY CARD
                if (uiState.disease == null && !uiState.isLoading) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = primaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val cropName = scan.cropType.replaceFirstChar { it.uppercase() }
                                Text("$cropName Appears Healthy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryGreen) // TODO: Add Swahili
                                Text("No disease detected in this scan.", fontSize = 13.sp, color = Color.Gray) // TODO: Add Swahili
                            }
                        }
                    }
                }
                }

                // SCAN AGAIN BUTTON
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Another Leaf", color = Color.White) // TODO: Add Swahili
                }
            }
        }
    }
}

@Composable
private fun TreatmentCard(treatment: Treatment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalPharmacy, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF2E7D32))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = treatment.interventionType, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            InfoRow("Products", treatment.productNames)
            InfoRow("Method", treatment.applicationMethod)
            InfoRow("Dosage", treatment.dosage)
            InfoRow("Est. Cost", treatment.costRangeKes)
        }
    }
}

@Composable
private fun RatingCard(onSubmit: (Int, String?) -> Unit) {
    var selectedStars by remember { mutableStateOf(0) }
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How was your consultation?", // TODO: Add Swahili
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..5) {
                    IconButton(onClick = { selectedStars = i }) {
                        Icon(
                            imageVector = if (i <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = if (i <= selectedStars) Color(0xFFFFA000) else Color.Gray
                        )
                    }
                }
            }

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Add a comment (optional)") }, // TODO: Add Swahili
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onSubmit(selectedStars, commentText.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(24.dp),
                enabled = selectedStars > 0
            ) {
                Text("Submit Rating", color = Color.White) // TODO: Add Swahili
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label.uppercase(), fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, color = Color.DarkGray)
    }
}
