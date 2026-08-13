package com.example.shambamedic.presentation.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.shambamedic.R
import com.example.shambamedic.presentation.common.cropDisplayName
import com.example.shambamedic.presentation.navigation.Screen
import com.example.shambamedic.util.Constants
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    navController: NavController,
    cropType: String,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(cropType) {
        viewModel.setCropType(cropType)
    }

    LaunchedEffect(cameraPermissionState.status) {
        viewModel.onPermissionResult(cameraPermissionState.status.isGranted)
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                !uiState.hasPermission -> {
                    PermissionScreen(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
                uiState.capturedBitmap != null && (uiState.showRetake || uiState.isEscalated) -> {
                    ReviewScreen(
                        uiState = uiState,
                        onRetake = viewModel::retake,
                        onViewResults = {
                            navController.navigate(Screen.Results.createRoute(uiState.savedScanId ?: ""))
                        }
                    )
                }
                else -> {
                    LiveCameraView(
                        cropType = cropType,
                        isLoading = uiState.isLoading,
                        onBack = { navController.popBackStack() },
                        onImageCaptured = viewModel::onImageCaptured
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveCameraView(
    cropType: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onImageCaptured: (Bitmap) -> Unit
) {
    var captureTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onImageCaptured = onImageCaptured,
            onCaptureReady = { captureAction -> captureTrigger = captureAction },
            modifier = Modifier.fillMaxSize()
        )

        // TOP BAR overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = Color(0xFF2E7D32).copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = cropDisplayName(cropType),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // CENTER overlay (Framing guide)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 280.dp, height = 200.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(16.dp)) // Note: Dashed border needs custom canvas drawing in Compose, simplified to solid for now
                )
                Text(
                    text = stringResource(R.string.camera_frame_hint),
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // BOTTOM BAR overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(3.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .background(Color.White, CircleShape)
                    .clickable { captureTrigger?.invoke() }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Text(
                        text = stringResource(R.string.camera_analysing),
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    onCaptureReady: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(imageCapture) {
        onCaptureReady {
            imageCapture?.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val rotationDegrees = image.imageInfo.rotationDegrees
                        
                        // Decode bitmap from planes
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        image.close()

                        // Apply correct rotation from ImageProxy metadata
                        if (rotationDegrees != 0) {
                            val matrix = Matrix()
                            matrix.postRotate(rotationDegrees.toFloat())
                            bitmap = Bitmap.createBitmap(
                                bitmap, 0, 0,
                                bitmap.width, bitmap.height,
                                matrix, true
                            )
                        }

                        onImageCaptured(bitmap)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("Camera", "Capture failed: ${exception.message}")
                    }
                }
            )
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCaptureUseCase = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                imageCapture = imageCaptureUseCase

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCaptureUseCase
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

@Composable
private fun ReviewScreen(
    uiState: CameraUiState,
    onRetake: () -> Unit,
    onViewResults: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        uiState.capturedBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isEscalated) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                            text = stringResource(R.string.escalated_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = stringResource(R.string.escalated_message),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRetake,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text(stringResource(R.string.action_scan_another_leaf), color = Color.White)
                        }
                    }
                } else if (uiState.isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Text(
                            text = stringResource(R.string.camera_analysing_short),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                    }
                } else if (uiState.classificationResult != null) {
                    val result = uiState.classificationResult
                    val isLowConfidence = result.confidenceScore < Constants.MIN_CONFIDENCE_THRESHOLD

                    if (isLowConfidence) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFF57F17)
                            )
                            Text(
                                text = stringResource(R.string.low_confidence_warning),
                                fontSize = 13.sp,
                                color = Color(0xFF7A5B00)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = result.diseaseLabel, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = cropDisplayName(result.cropType), fontSize = 13.sp, color = Color.Gray)
                        }

                        val severityColor = when (result.severity) {
                            "low" -> Color(0xFF388E3C)
                            "moderate" -> Color(0xFFF57F17)
                            else -> Color(0xFFB71C1C)
                        }

                        Surface(
                            color = severityColor,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = result.severity.uppercase(),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { result.confidenceScore },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2E7D32)
                    )

                    Text(
                        text = stringResource(R.string.confidence_percent, (result.confidenceScore * 100).toInt()),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.action_retake))
                        }
                        Button(
                            onClick = onViewResults,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            enabled = uiState.savedScanId != null && uiState.savedScanId!!.isNotEmpty()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.action_view_results))
                        }
                    }
                } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(text = uiState.error, textAlign = TextAlign.Center, fontSize = 15.sp)
                        Button(onClick = onRetake) {
                            Text(stringResource(R.string.action_retake))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.permission_required_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            text = stringResource(R.string.permission_required_body),
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text(stringResource(R.string.action_grant_permission))
        }
    }
}
