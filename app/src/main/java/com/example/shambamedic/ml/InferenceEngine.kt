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
    
    private val inputImageWidth = 224
    private val inputImageHeight = 224
    private val inputChannels = 3
    private val modelInputSize = inputImageWidth * inputImageHeight * inputChannels * 4

    private val inferenceDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val inferenceMutex = Mutex()

    // Candidate scores span many orders of magnitude (1.0 down to ~1e-37), so a fixed
    // absolute epsilon like 1e-10 would swallow genuinely-differentiated low scores that
    // just happen to live below that magnitude (e.g. 2.5e-14 vs 3.8e-16 is a real ~65x
    // difference, not a tie). Observed real ties are exact float32 underflow to 0.0, not
    // near-equality, so ties are detected via exact equality instead of a magic epsilon.
    private val tieEpsilon = 0f

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
            // Full type + stack trace, not just e.message: interpreter construction can
            // fail for several very different reasons (op/version mismatch, corrupt
            // model, shape assertion) that a bare message alone doesn't always distinguish.
            Log.e("InferenceEngine", "Failed to initialize TFLite interpreter: ${e.javaClass.name}: ${e.message}", e)
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

                // Restrict the argmax to the labels belonging to the selected crop, so a
                // Potato scan can't resolve to a Corn/Tomato class. Every one of this
                // model's 17 classes belongs to Corn/Potato/Tomato (no "background" or
                // other-crop classes to exclude), so this is purely a selected-crop
                // restriction now, not a filter separating relevant from irrelevant classes.
                val validIndices = if (cropType == "all") {
                    ModelLabels.LABELS.indices.toList()
                } else {
                    ModelLabels.LABELS.indices.filter { idx ->
                        val label = ModelLabels.LABELS[idx]
                        when (cropType) {
                            "maize" -> label.startsWith("Corn___")
                            "potato" -> label.startsWith("Potato___")
                            "tomato" -> label.startsWith("Tomato___")
                            else -> true
                        }
                    }
                }

                // Find max score only within valid indices
                val maxIndex = validIndices.maxByOrNull { scores[it] }
                    ?: return@withLock Result.failure(Exception("No valid class found for crop: $cropType"))

                val confidence = scores[maxIndex]
                val label = ModelLabels.LABELS[maxIndex]

                // A genuine tie among the crop-filtered candidates (all within tieEpsilon of
                // the top score) means maxIndex was picked by array order, not by a real
                // signal distinguishing it from the other candidates - e.g. all-zero scores
                // when the model has no confident match within this crop. This is distinct
                // from a genuine low-but-real confidence score, where exactly one candidate
                // is unambiguously highest even if its value is small.
                val tieCount = validIndices.count { idx -> kotlin.math.abs(scores[idx] - confidence) <= tieEpsilon }
                val isAmbiguous = tieCount > 1

                val displayName = ModelLabels.getDisplayName(label)
                val detectedCropType = ModelLabels.getCropTypeFromLabel(label)
                val severity = ClassificationResult.calculateSeverity(confidence)

                Result.success(
                    ClassificationResult(
                        diseaseLabel = displayName,
                        rawLabel = label,
                        confidenceScore = confidence,
                        cropType = detectedCropType,
                        severity = severity,
                        isAmbiguous = isAmbiguous
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
