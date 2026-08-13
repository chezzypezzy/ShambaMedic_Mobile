package com.example.shambamedic.ml

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.example.shambamedic.domain.model.ClassificationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val modelFileName = "shambamedic_model.tflite"
    
    private val inputImageWidth = 200
    private val inputImageHeight = 200
    private val inputChannels = 3
    private val modelInputSize = inputImageWidth * inputImageHeight * inputChannels * 4

    private val inferenceDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val inferenceMutex = Mutex()

    init {
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        try {
            val model = loadModelFile(context.assets, modelFileName)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(model, options)
            Log.d("InferenceEngine", "TFLite interpreter initialized successfully on CPU")
        } catch (e: Throwable) {
            Log.e("InferenceEngine", "Failed to initialize TFLite interpreter: ${e.message}")
            interpreter = null
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    suspend fun classify(bitmap: Bitmap, cropType: String = "all"): Result<ClassificationResult> = withContext(inferenceDispatcher) {
        inferenceMutex.withLock {
            val currentInterpreter = interpreter ?: return@withLock Result.failure(Exception("Interpreter not initialized"))

            try {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
                val inputBuffer = convertBitmapToByteBuffer(scaledBitmap)
                val outputArray = Array(1) { FloatArray(ModelLabels.LABELS.size) }

                currentInterpreter.run(inputBuffer, outputArray)

                val scores = outputArray[0]

                // Get the indices that are valid for the selected cropType
                val validIndices = if (cropType == "all") {
                    ModelLabels.LABELS.indices.toList()
                } else {
                    ModelLabels.LABELS.indices.filter { idx ->
                        val label = ModelLabels.LABELS[idx]
                        when (cropType) {
                            "maize" -> label.startsWith("corn")
                            "potato" -> label.startsWith("potato")
                            "tomato" -> label.startsWith("tomato")
                            else -> true
                        }
                    }
                }

                // Find max score only within valid indices
                val maxIndex = validIndices.maxByOrNull { scores[it] }
                    ?: return@withLock Result.failure(Exception("No valid class found for crop: $cropType"))

                val confidence = scores[maxIndex]
                val label = ModelLabels.LABELS[maxIndex]

                if (label == "background" || (cropType != "all" && !ModelLabels.LABELS[maxIndex].startsWith(if (cropType == "maize") "corn" else cropType))) {
                    // This second check is redundant due to validIndices but kept for safety
                    return@withLock Result.failure(Exception("No plant disease detected. Please capture a clear image of a diseased crop leaf."))
                }

                val displayName = ModelLabels.getDisplayName(label)
                val detectedCropType = ModelLabels.getCropTypeFromLabel(label)
                val severity = ClassificationResult.calculateSeverity(confidence)

                Result.success(
                    ClassificationResult(
                        diseaseLabel = displayName,
                        rawLabel = label,
                        confidenceScore = confidence,
                        cropType = detectedCropType,
                        severity = severity
                    )
                )
            } catch (e: Exception) {
                Log.e("InferenceEngine", "Inference error: ${e.message}")
                Result.failure(e)
            }
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = ((pixelValue shr 16 and 0xFF) / 127.5f) - 1.0f
            val g = ((pixelValue shr 8 and 0xFF) / 127.5f) - 1.0f
            val b = ((pixelValue and 0xFF) / 127.5f) - 1.0f
            
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        inferenceDispatcher.close()
    }
}
