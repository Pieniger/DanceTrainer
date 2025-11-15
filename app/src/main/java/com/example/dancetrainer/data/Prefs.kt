package com.example.dancetrainer.data

import android.content.Context

object Prefs {

    private const val FILE = "prefs"
    private const val KEY_STYLE = "style_name"
    private const val KEY_TREE_URI = "tree_uri"
    private const val KEY_TTS_ENABLED = "tts_enabled"

    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getStyle(ctx: Context): String =
        sp(ctx).getString(KEY_STYLE, "") ?: ""

    fun setStyle(ctx: Context, style: String) {
        sp(ctx).edit().putString(KEY_STYLE, style).apply()
    }

    fun getTreeUri(ctx: Context): String? =
        sp(ctx).getString(KEY_TREE_URI, null)

    fun setTreeUri(ctx: Context, uri: String?) {
        sp(ctx).edit().putString(KEY_TREE_URI, uri).apply()
    }

    fun isVoiceEnabled(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_TTS_ENABLED, true)

    fun setVoiceEnabled(ctx: Context, enabled: Boolean) {
        sp(ctx).edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
    }
}
