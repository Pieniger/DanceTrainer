package com.example.dancetrainer.data

import android.content.Context

object Prefs {
    private const val FILE = "prefs"

    private const val KEY_STYLE = "style_name"
    private const val KEY_TTS_ENABLED = "tts_enabled"
    private const val KEY_TREE_URI = "tree_uri" // base folder for all styles

    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // Current style (folder name under the selected base folder)
    fun getStyle(ctx: Context): String? =
        sp(ctx).getString(KEY_STYLE, null)

    fun setStyle(ctx: Context, value: String?) {
        sp(ctx).edit().putString(KEY_STYLE, value).apply()
    }

    // TTS enabled/disabled
    fun isTtsEnabled(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_TTS_ENABLED, true)

    fun setTtsEnabled(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(KEY_TTS_ENABLED, value).apply()
    }

    // SAF-tree URI of base folder (where style subfolders live)
    fun getTreeUri(ctx: Context): String? =
        sp(ctx).getString(KEY_TREE_URI, null)

    fun setTreeUri(ctx: Context, value: String?) {
        sp(ctx).edit().putString(KEY_TREE_URI, value).apply()
    }
}
