package com.fit2081.yuxuan_34286225.nutritrack.shared.utils

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object AuthManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY = "userId"

    /**
     * Mutable state holds current patient's id
     * initially null when no user logged in
     */
    val _userId: MutableState<String?> = mutableStateOf(null)

    /**
     * Set _userId to the provided userId when user logged in
     */
    fun login(context: Context, userId: String){
        _userId.value = userId
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY, userId).apply()
    }

    /**
     * Reset _userId to null when user logged out
     */
    fun logout(context: Context){
        _userId.value = null
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(KEY).apply()
    }

    /**
     * Returns the current logged in user's ID
     */
    fun getPatientId(): String?{
        return _userId.value
    }

    fun loadUserId(context: Context){
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        _userId.value = sharedPrefs.getString(KEY, null)
    }
}