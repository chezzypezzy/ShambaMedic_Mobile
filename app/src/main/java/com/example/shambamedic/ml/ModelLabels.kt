package com.example.shambamedic.ml

object ModelLabels {

    val LABELS = listOf(
        "apple apple scab",
        "apple black rot",
        "apple cedar apple rust",
        "apple healthy",
        "blueberry healthy",
        "cherry including sour powdery mildew",
        "cherry including sour healthy",
        "corn maize cercospora leaf spot gray leaf spot",
        "corn maize common rust",
        "corn maize northern leaf blight",
        "corn maize healthy",
        "grape black rot",
        "grape esca black measles",
        "grape leaf blight isariopsis leaf spot",
        "grape healthy",
        "orange haunglongbing citrus greening",
        "peach bacterial spot",
        "peach healthy",
        "pepper bell bacterial spot",
        "pepper bell healthy",
        "potato early blight",
        "potato late blight",
        "potato healthy",
        "raspberry healthy",
        "soybean healthy",
        "squash powdery mildew",
        "strawberry leaf scorch",
        "strawberry healthy",
        "tomato bacterial spot",
        "tomato early blight",
        "tomato late blight",
        "tomato leaf mold",
        "tomato septoria leaf spot",
        "tomato spider mites two spotted spider mite",
        "tomato target spot",
        "tomato tomato yellow leaf curl virus",
        "tomato tomato mosaic virus",
        "tomato healthy",
        "background"
    )

    val LABEL_TO_DISEASE_ID = mapOf(
        "corn maize cercospora leaf spot gray leaf spot" to "disease_maize_002",
        "corn maize common rust" to "disease_maize_003",
        "corn maize northern leaf blight" to null,
        "corn maize healthy" to null,
        "potato early blight" to "disease_potato_002",
        "potato late blight" to "disease_potato_001",
        "potato healthy" to null,
        "tomato bacterial spot" to null,
        "tomato early blight" to "disease_tomato_002",
        "tomato late blight" to "disease_tomato_004",
        "tomato leaf mold" to "disease_tomato_003",
        "tomato septoria leaf spot" to null,
        "tomato spider mites two spotted spider mite" to null,
        "tomato target spot" to null,
        "tomato tomato yellow leaf curl virus" to null,
        "tomato tomato mosaic virus" to null,
        "tomato healthy" to null,
        "background" to null
    )

    val HEALTHY_LABELS = setOf(
        "corn maize healthy",
        "potato healthy",
        "tomato healthy"
    )

    fun getCropTypeFromLabel(label: String): String {
        return when {
            label.startsWith("corn") -> "maize"
            label.startsWith("potato") -> "potato"
            label.startsWith("tomato") -> "tomato"
            else -> "unknown"
        }
    }

    fun getDisplayName(label: String): String {
        return when (label) {
            "corn maize cercospora leaf spot gray leaf spot" -> "Gray Leaf Spot (Maize)"
            "corn maize common rust" -> "Common Rust (Maize)"
            "corn maize northern leaf blight" -> "Northern Leaf Blight (Maize)"
            "corn maize healthy" -> "Healthy Maize Plant"
            "potato early blight" -> "Early Blight (Potato)"
            "potato late blight" -> "Late Blight (Potato)"
            "potato healthy" -> "Healthy Potato Plant"
            "tomato bacterial spot" -> "Bacterial Spot (Tomato)"
            "tomato early blight" -> "Early Blight (Tomato)"
            "tomato late blight" -> "Late Blight (Tomato)"
            "tomato leaf mold" -> "Leaf Mold (Tomato)"
            "tomato septoria leaf spot" -> "Septoria Leaf Spot (Tomato)"
            "tomato spider mites two spotted spider mite" -> "Spider Mites (Tomato)"
            "tomato target spot" -> "Target Spot (Tomato)"
            "tomato tomato yellow leaf curl virus" -> "Yellow Leaf Curl Virus (Tomato)"
            "tomato tomato mosaic virus" -> "Mosaic Virus (Tomato)"
            "tomato healthy" -> "Healthy Tomato Plant"
            "background" -> "No Plant Detected"
            else -> label
        }
    }
}
