package com.example.shambamedic.presentation.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.shambamedic.R

/**
 * Locale-aware display name for an internal crop type ("maize"/"potato"/"tomato"),
 * shared by Home, Camera, History and Results screens instead of each doing its own
 * cropType.replaceFirstChar { it.uppercase() } (which can't be translated).
 */
@Composable
fun cropDisplayName(cropType: String): String {
    return when (cropType) {
        "maize" -> stringResource(R.string.crop_maize)
        "potato" -> stringResource(R.string.crop_potato)
        "tomato" -> stringResource(R.string.crop_tomato)
        else -> cropType.replaceFirstChar { it.uppercase() }
    }
}

/** Non-Composable equivalent for use in ViewModels that resolve display strings themselves. */
fun cropDisplayName(context: Context, cropType: String): String {
    return when (cropType) {
        "maize" -> context.getString(R.string.crop_maize)
        "potato" -> context.getString(R.string.crop_potato)
        "tomato" -> context.getString(R.string.crop_tomato)
        else -> cropType.replaceFirstChar { it.uppercase() }
    }
}
