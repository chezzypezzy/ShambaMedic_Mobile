package com.example.shambamedic.presentation.common

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * On API 33+, AppCompatDelegate.setApplicationLocales() is a thin wrapper around the
 * platform LocaleManager - but on some OEM builds (confirmed on a MIUI/Android 16 test
 * device) that wrapper silently no-ops: it reports success and getApplicationLocales()
 * even echoes the requested value back, but the OS-level per-app locale
 * (queryable via `adb shell cmd locale get-app-locales`) never actually changes, so no
 * recomposition/recreation ever happens. Calling LocaleManager directly on API 33+ does
 * work. AppCompatDelegate is kept as the API < 33 backport path.
 */
fun setAppLocale(context: Context, languageTag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager.applicationLocales = LocaleList.forLanguageTags(languageTag)
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}

/** Returns the current per-app language tag (e.g. "en", "sw"), or null if unset. */
fun getAppLocale(context: Context): String? {
    val locales = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager.applicationLocales
    } else {
        null
    }
    if (locales != null && !locales.isEmpty) {
        return locales[0]?.language
    }
    val compatLocales = AppCompatDelegate.getApplicationLocales()
    if (!compatLocales.isEmpty) {
        return compatLocales[0]?.language
    }
    return null
}
