package com.example.dancetrainer.data

import android.content.Context

object Prefs {
    private const val FILE = "prefs"
    private const val KEY_STYLE = "style_name"
    private const val KEY_VOICE_ENABLED = "voice_enabled"
    private const val KEY_TREE_URI = "tree_uri"

    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // ---- Style (dance style name = folder name) ----
    fun getStyle(ctx: Context): String =
        sp(ctx).getString(KEY_STYLE, "Default") ?: "Default"

    fun setStyle(ctx: Context, value: String) {
        sp(ctx).edit()
            .putString(KEY_STYLE, value)
            .apply()
    }

    // ---- TTS on/off ----
    fun isVoiceEnabled(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_VOICE_ENABLED, true)

    fun setVoiceEnabled(ctx: Context, value: Boolean) {
        sp(ctx).edit()
            .putBoolean(KEY_VOICE_ENABLED, value)
            .apply()
    }

    // ---- Base data folder (SAF tree URI, nullable) ----
    fun getTreeUri(ctx: Context): String? =
        sp(ctx).getString(KEY_TREE_URI, null)

    fun setTreeUri(ctx: Context, value: String?) {
        sp(ctx).edit()
            .putString(KEY_TREE_URI, value)
            .apply()
    }
}
