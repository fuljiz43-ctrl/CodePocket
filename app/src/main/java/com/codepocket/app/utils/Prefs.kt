package com.codepocket.app.utils

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREF_NAME = "codepocket_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_LAST_CATEGORY = "last_category"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isDarkMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun getLastCategory(context: Context): String? =
        prefs(context).getString(KEY_LAST_CATEGORY, null)

    fun setLastCategory(context: Context, category: String?) {
        prefs(context).edit().putString(KEY_LAST_CATEGORY, category).apply()
    }
}
