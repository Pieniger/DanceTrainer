package com.example.dancetrainer.data

import android.content.Context

object Prefs {
    private const val FILE = "prefs"

    // Existing settings
    private const val KEY_STYLE = "style_name"
    private const val KEY_VOICE_ENABLED = "voice_enabled"
    private const val KEY_TREE_URI = "tree_uri"

    // New: Bluetooth / remote key mapping
    private const val KEY_BT_NEXT = "bt_next_key"
    private const val KEY_BT_REROLL = "bt_reroll_key"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // ---- Style ----
    fun getStyle(ctx: Context): String =
        sp(ctx).getString(KEY_STYLE, "Default") ?: "Default"

    fun setStyle(ctx: Context, value: String) {
        sp(ctx).edit().putString(KEY_STYLE, value).apply()
    }

    // ---- TTS / Voice ----
    fun isVoiceEnabled(ctx: Context): Boolean =
        sp(ctx).getBoolean(KEY_VOICE_ENABLED, true)

    fun setVoiceEnabled(ctx: Context, enabled: Boolean) {
        sp(ctx).edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
    }

    // ---- Base folder (SAF tree) ----
    fun getTreeUri(ctx: Context): String? =
        sp(ctx).getString(KEY_TREE_URI, null)

    fun setTreeUri(ctx: Context, uri: String?) {
        sp(ctx).edit().putString(KEY_TREE_URI, uri).apply()
    }

    // ---- Bluetooth / remote key bindings ----
    // null = not configured / disabled

    fun getNextMoveKeyCode(ctx: Context): Int? =
        if (sp(ctx).contains(KEY_BT_NEXT)) sp(ctx).getInt(KEY_BT_NEXT, 0) else null

    fun setNextMoveKeyCode(ctx: Context, code: Int?) {
        val e = sp(ctx).edit()
        if (code == null) e.remove(KEY_BT_NEXT) else e.putInt(KEY_BT_NEXT, code)
        e.apply()
    }

    fun getRerollKeyCode(ctx: Context): Int? =
        if (sp(ctx).contains(KEY_BT_REROLL)) sp(ctx).getInt(KEY_BT_REROLL, 0) else null

    fun setRerollKeyCode(ctx: Context, code: Int?) {
        val e = sp(ctx).edit()
        if (code == null) e.remove(KEY_BT_REROLL) else e.putInt(KEY_BT_REROLL, code)
        e.apply()
    }
}
