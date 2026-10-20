package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences

import java.security.MessageDigest

enum class Rol(val label: String) {
    ADMINISTRADOR("Administrador"), SUPERVISOR("Supervisor"), TECNICO("Técnico")
}

object UserSession {
    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_NAME = "user_full_name"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun login(context: Context, userId: Int, username: String, role: String, fullName: String) {
        getPrefs(context).edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_USER_ROLE, role)
            putString(KEY_USER_NAME, fullName)
            apply()
        }
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    fun getUserId(context: Context): Int = getPrefs(context).getInt(KEY_USER_ID, -1)

    fun getUserRole(context: Context): Rol {
        val roleStr = getPrefs(context).getString(KEY_USER_ROLE, "TECNICO") ?: "TECNICO"
        return try {
            Rol.valueOf(roleStr.uppercase())
        } catch (e: Exception) {
            Rol.TECNICO
        }
    }

    fun getFullName(context: Context): String? = getPrefs(context).getString(KEY_USER_NAME, null)
    fun getUsername(context: Context): String? = getPrefs(context).getString(KEY_USERNAME, null)
    fun isLoggedIn(context: Context): Boolean = getUserId(context) != -1
}
