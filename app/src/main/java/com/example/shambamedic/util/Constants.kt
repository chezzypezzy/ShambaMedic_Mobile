package com.example.shambamedic.util

import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    const val DB_NAME = "shambamedic_db"
    const val PREFS_NAME = "shambamedic_prefs"

    val TOKEN_KEY = stringPreferencesKey("auth_token")
    val USER_ID_KEY = stringPreferencesKey("user_id")
    val LANGUAGE_KEY = stringPreferencesKey("language_pref")

    const val MIN_CONFIDENCE_THRESHOLD = 0.70f
    const val SYNC_MAX_ATTEMPTS = 5
    val CROP_TYPES = listOf("maize", "potato", "tomato")
}
