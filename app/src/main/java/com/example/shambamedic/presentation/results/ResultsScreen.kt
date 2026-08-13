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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.shambamedic.BuildConfig
import com.example.shambamedic.R
import com.example.shambamedic.domain.model.Disease
import com.example.shambamedic.domain.model.Treatment
import com.example.shambamedic.presentation.common.cropDisplayName

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
                    stringResource(R.string.error_invalid_scan),
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
                    Text(stringResource(R.string.action_go_back))
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
                title = { Text(stringResource(R.string.title_diagnosis_result), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = Color.White)
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
                        Text(stringResource(R.string.action_go_back))
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
                                        text = stringResource(R.string.status_expert_diagnosis_received),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                Text(
                                    text = stringResource(R.string.botanist_assessment_label),
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
                                    text = stringResource(R.string.rated_consultation),
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
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
                                    text = stringResource(R.string.status_awaiting_expert_review),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = stringResource(R.string.awaiting_expert_review_body),
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
                                val healthyCropName = stringResource(R.string.status_healthy_crop, cropDisplayName(scan.cropType))
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
                            Text(stringResource(R.string.confidence_score_label), fontSize = 13.sp, color = Color.Gray)
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
                                text = if (isSynced) stringResource(R.string.sync_status_synced) else stringResource(R.string.sync_status_pending),
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
                            Text(stringResource(R.string.about_disease_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Text(stringResource(R.string.symptoms_label), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = disease.symptomDescription, fontSize = 13.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.severity_impact_label), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = disease.severityScale, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }

                // TREATMENTS SECTION
                if (uiState.treatments.isNotEmpty()) {
                    Text(stringResource(R.string.recommended_treatments_title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
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
                                val cropName = cropDisplayName(scan.cropType)
                                Text(stringResource(R.string.healthy_crop_title, cropName), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryGreen)
                                Text(stringResource(R.string.healthy_crop_body), fontSize = 13.sp, color = Color.Gray)
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
                    Text(stringResource(R.string.action_scan_another_leaf), color = Color.White)
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
            InfoRow(stringResource(R.string.info_products), treatment.productNames)
            InfoRow(stringResource(R.string.info_method), treatment.applicationMethod)
            InfoRow(stringResource(R.string.info_dosage), treatment.dosage)
            InfoRow(stringResource(R.string.info_est_cost), treatment.costRangeKes)
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
                text = stringResource(R.string.how_was_consultation),
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
                label = { Text(stringResource(R.string.comment_hint_optional)) },
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
                Text(stringResource(R.string.action_submit_rating), color = Color.White)
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
