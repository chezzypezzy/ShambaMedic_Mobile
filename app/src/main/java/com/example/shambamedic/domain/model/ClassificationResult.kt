package com.example.shambamedic.domain.model

data class ClassificationResult(
    val diseaseLabel: String,        // display name e.g. "Common Rust (Maize)"
    val rawLabel: String,            // raw model label e.g. "corn maize common rust"
    val confidenceScore: Float,
    val cropType: String,
    val severity: String
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
