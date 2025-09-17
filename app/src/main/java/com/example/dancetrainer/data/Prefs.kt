package com.example.dancetrainer.data

import android.content.Context

object Prefs {
    private const val FILE = "prefs"
    private const val KEY_STYLE = "style_name"
    private const val KEY_METRONOME_ENABLED = "metronome_enabled"
    private const val KEY_VOICE_ENABLED = "voice_enabled"
    private const val KEY_METRONOME_SOUND = "metronome_sound" // "click" | "bell"
    private const val KEY_TTS_VOICE = "tts_voice_name"        // voice name/id
    private const val KEY_TREE_URI = "tree_uri"                // persisted folder via SAF

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getStyle(ctx: Context): String = sp(ctx).getString(KEY_STYLE, "Default") ?: "Default"
    fun setStyle(ctx: Context, value: String) { sp(ctx).edit().putString(KEY_STYLE, value).apply() }

    fun isMetronomeEnabled(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_METRONOME_ENABLED, true)
    fun setMetronomeEnabled(ctx: Context, value: Boolean) { sp(ctx).edit().putBoolean(KEY_METRONOME_ENABLED, value).apply() }

    fun isVoiceEnabled(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_VOICE_ENABLED, true)
    fun setVoiceEnabled(ctx: Context, value: Boolean) { sp(ctx).edit().putBoolean(KEY_VOICE_ENABLED, value).apply() }

    fun getMetronomeSound(ctx: Context): String = sp(ctx).getString(KEY_METRONOME_SOUND, "click") ?: "click"
    fun setMetronomeSound(ctx: Context, value: String) { sp(ctx).edit().putString(KEY_METRONOME_SOUND, value).apply() }

    fun getTtsVoiceName(ctx: Context): String = sp(ctx).getString(KEY_TTS_VOICE, "default") ?: "default"
    fun setTtsVoiceName(ctx: Context, value: String) { sp(ctx).edit().putString(KEY_TTS_VOICE, value).apply() }

    fun getTreeUri(ctx: Context): String? = sp(ctx).getString(KEY_TREE_URI, null)
    fun setTreeUri(ctx: Context, value: String?) { sp(ctx).edit().putString(KEY_TREE_URI, value).apply() }
}
