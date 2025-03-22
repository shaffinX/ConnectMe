package com.shaffinimam.i212963

import android.content.Context

object SharedPrefManager {
    private const val PREF_NAME = "MyPrefs"
    private const val KEY_USERNAME = "username"

    fun saveUsername(context: Context, username: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    fun getUsername(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USERNAME, null) // Returns null if not found
    }
}