package com.example.shambamedic.ml

object ModelLabels {

    // Exact order of the crop-specific 17-class model's output layer (labels.txt).
    val LABELS = listOf(
        "Corn___Cercospora_leaf_spot Gray_leaf_spot",
        "Corn___Common_rust",
        "Corn___Northern_Leaf_Blight",
        "Corn___healthy",
        "Potato___Early_blight",
        "Potato___Late_blight",
        "Potato___healthy",
        "Tomato___Bacterial_spot",
        "Tomato___Early_blight",
        "Tomato___Late_blight",
        "Tomato___Leaf_Mold",
        "Tomato___Septoria_leaf_spot",
        "Tomato___Spider_mites Two-spotted_spider_mite",
        "Tomato___Target_Spot",
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
        "Tomato___Tomato_mosaic_virus",
        "Tomato___healthy"
    )

    val LABEL_TO_DISEASE_ID = mapOf(
        "Corn___Cercospora_leaf_spot Gray_leaf_spot" to "disease_maize_002",
        "Corn___Common_rust" to "disease_maize_003",
        "Corn___Northern_Leaf_Blight" to null,
        "Corn___healthy" to null,
        "Potato___Early_blight" to "disease_potato_002",
        "Potato___Late_blight" to "disease_potato_001",
        "Potato___healthy" to null,
        "Tomato___Bacterial_spot" to null,
        "Tomato___Early_blight" to "disease_tomato_002",
        "Tomato___Late_blight" to "disease_tomato_004",
        "Tomato___Leaf_Mold" to "disease_tomato_003",
        "Tomato___Septoria_leaf_spot" to null,
        "Tomato___Spider_mites Two-spotted_spider_mite" to null,
        "Tomato___Target_Spot" to null,
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus" to null,
        "Tomato___Tomato_mosaic_virus" to null,
        "Tomato___healthy" to null
    )

    val HEALTHY_LABELS = setOf(
        "Corn___healthy",
        "Potato___healthy",
        "Tomato___healthy"
    )

    fun getCropTypeFromLabel(label: String): String {
        return when {
            label.startsWith("Corn___") -> "maize"
            label.startsWith("Potato___") -> "potato"
            label.startsWith("Tomato___") -> "tomato"
            else -> "unknown"
        }
    }

    fun getDisplayName(label: String): String {
        return when (label) {
            "Corn___Cercospora_leaf_spot Gray_leaf_spot" -> "Gray Leaf Spot (Maize)"
            "Corn___Common_rust" -> "Common Rust (Maize)"
            "Corn___Northern_Leaf_Blight" -> "Northern Leaf Blight (Maize)"
            "Corn___healthy" -> "Healthy Maize Plant"
            "Potato___Early_blight" -> "Early Blight (Potato)"
            "Potato___Late_blight" -> "Late Blight (Potato)"
            "Potato___healthy" -> "Healthy Potato Plant"
            "Tomato___Bacterial_spot" -> "Bacterial Spot (Tomato)"
            "Tomato___Early_blight" -> "Early Blight (Tomato)"
            "Tomato___Late_blight" -> "Late Blight (Tomato)"
            "Tomato___Leaf_Mold" -> "Leaf Mold (Tomato)"
            "Tomato___Septoria_leaf_spot" -> "Septoria Leaf Spot (Tomato)"
            "Tomato___Spider_mites Two-spotted_spider_mite" -> "Spider Mites (Tomato)"
            "Tomato___Target_Spot" -> "Target Spot (Tomato)"
            "Tomato___Tomato_Yellow_Leaf_Curl_Virus" -> "Yellow Leaf Curl Virus (Tomato)"
            "Tomato___Tomato_mosaic_virus" -> "Mosaic Virus (Tomato)"
            "Tomato___healthy" -> "Healthy Tomato Plant"
            else -> label
        }
    }
}
