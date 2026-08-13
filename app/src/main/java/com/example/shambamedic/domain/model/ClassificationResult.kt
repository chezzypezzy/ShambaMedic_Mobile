package com.example.shambamedic.domain.model

data class ClassificationResult(
    val diseaseLabel: String,        // display name e.g. "Common Rust (Maize)"
    val rawLabel: String,            // raw model label e.g. "corn maize common rust"
    val confidenceScore: Float,
    val cropType: String,
    val severity: String,
    // True when the crop-filtered candidate scores were tied (within TIE_EPSILON) at the
    // top, meaning argmax's pick was an arbitrary array-order tie-break rather than a real
    // signal - not to be confused with a genuine low-but-real confidence score.
    val isAmbiguous: Boolean = false
) {
    companion object {
        fun calculateSeverity(score: Float): String {
            return when {
                score >= 0.85f -> "low"
                score >= 0.70f -> "moderate"
                else -> "severe"
            }
        }
    }
}
