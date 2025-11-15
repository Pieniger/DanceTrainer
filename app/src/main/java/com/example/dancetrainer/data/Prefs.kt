package com.example.dancetrainer.data

import android.content.Context

object Prefs {
    private const val FILE = "prefs"

    private const val KEY_STYLE = "style_name"
    private const val KEY_TTS_ENABLED = "tts_enabled"
    private const val KEY_TREE_URI = "tree_uri" // base folder for all styles

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // Current style (folder name under the selected base folder)
    fun getStyle(ctx: Context): String? =
        sp(ctx).getString(KEY_STYLE, null)

    fun setStyle(ctx: Context, value: String?) {
        sp(ctx).edit().put
