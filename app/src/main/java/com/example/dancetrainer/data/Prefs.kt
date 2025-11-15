package com.example.dancetrainer.data

import android.content.Context

/**
 * Central place for simple preferences:
 * - Root folder URI (DocumentTree)
 * - Currently selected style (subfolder name)
 * - Whether TTS is enabled during Dance
 */
object Prefs {

    private const val FILE = "prefs"
    private const val KEY_ROOT_URI = "root_uri"
    private const val KEY_STYLE = "style_name"
    private const val KEY_VOICE_ENABLED = "voice_enabled"

    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getRootUri(ctx: Context): String? =
        sp(ctx).getString(KEY_ROOT_URI, null)

    fun setRootUri(ctx: Context, uri: String?) {
        sp(ctx).edit()
            .putString(KEY_ROOT_URI, uri)
            .apply()
    }

    fun getStyle(ctx: Context): String? =
        sp(ctx).getString(KEY_STYLE, null)

    fun setStyle(ctx: Context, style: String) {
        sp(ctx).edit()
            .putString(KEY_STYLE, style)
            .apply()
    }

    fun isVoiceEnabled(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_VOICE_ENABLED, true)

    fun setVoiceEnabled(ctx: Context, enabled: Boolean) {
        sp(ctx).edit()
            .putBoolean(KEY_VOICE_ENABLED, enabled)
            .apply()
    }
}
