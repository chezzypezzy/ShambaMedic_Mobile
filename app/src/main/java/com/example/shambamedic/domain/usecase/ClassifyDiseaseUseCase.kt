package com.example.shambamedic.domain.usecase

import android.graphics.Bitmap
import com.example.shambamedic.domain.model.ClassificationResult
import com.example.shambamedic.ml.InferenceEngine
import javax.inject.Inject

class ClassifyDiseaseUseCase @Inject constructor(
    private val inferenceEngine: InferenceEngine
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        cropType: String = "all"
    ): Result<ClassificationResult> {
        return inferenceEngine.classify(bitmap, cropType)
    }
}
