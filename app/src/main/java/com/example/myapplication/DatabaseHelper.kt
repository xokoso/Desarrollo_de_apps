package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AgroAlerta.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_ALERTS = "alertas"
        const val TABLE_USERS = "usuarios"

        // Columnas Alertas
        const val COLUMN_ID = "id"
        const val COLUMN_QUARTER = "cuartel"
        const val COLUMN_TYPE = "tipo_evento"
        const val COLUMN_VALUE = "valor"
        const val COLUMN_DATE = "fecha"
        const val COLUMN_OBSERVATIONS = "observaciones"

        // Columnas Usuarios
        const val USER_COLUMN_ID = "id"
        const val USER_COLUMN_USERNAME = "username"
        const val USER_COLUMN_PASSWORD = "password"
        const val USER_COLUMN_NAME = "nombre_completo"
        const val USER_COLUMN_ROLE = "rol"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableAlerts = ("CREATE TABLE $TABLE_ALERTS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_QUARTER TEXT,"
                + "$COLUMN_TYPE TEXT,"
                + "$COLUMN_VALUE TEXT,"
                + "$COLUMN_DATE TEXT,"
                + "$COLUMN_OBSERVATIONS TEXT)")

        val createTableUsers = ("CREATE TABLE $TABLE_USERS ("
                + "$USER_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$USER_COLUMN_USERNAME TEXT UNIQUE,"
                + "$USER_COLUMN_PASSWORD TEXT,"
                + "$USER_COLUMN_NAME TEXT,"
                + "$USER_COLUMN_ROLE TEXT)")

        db.execSQL(createTableAlerts)
        db.execSQL(createTableUsers)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // --- MÉTODOS DE USUARIOS ---

    fun registerUser(username: String, passwordHash: String, name: String, role: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(USER_COLUMN_USERNAME, username)
            put(USER_COLUMN_PASSWORD, passwordHash)
            put(USER_COLUMN_NAME, name)
            put(USER_COLUMN_ROLE, role)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun checkUser(username: String, passwordHash: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $USER_COLUMN_ID FROM $TABLE_USERS WHERE $USER_COLUMN_USERNAME=? AND $USER_COLUMN_PASSWORD=?", arrayOf(username, passwordHash))

        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0)
        }
        cursor.close()
        return userId
    }

    fun getUserDetails(userId: Int): Map<String, String>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $USER_COLUMN_ID=?", arrayOf(userId.toString()))

        var userMap: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            userMap = mutableMapOf(
                "username" to cursor.getString(cursor.getColumnIndexOrThrow(USER_COLUMN_USERNAME)),
                "nombre" to cursor.getString(cursor.getColumnIndexOrThrow(USER_COLUMN_NAME)),
                "rol" to cursor.getString(cursor.getColumnIndexOrThrow(USER_COLUMN_ROLE))
            )
        }
        cursor.close()
        return userMap
    }

    // Función para insertar un nuevo reporte
    fun insertAlert(quarter: String, type: String, value: String, date: String, obs: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_QUARTER, quarter)
            put(COLUMN_TYPE, type)
            put(COLUMN_VALUE, value)
            put(COLUMN_DATE, date)
            put(COLUMN_OBSERVATIONS, obs)
        }
        return db.insert(TABLE_ALERTS, null, values)
    }

    // Función para obtener todos los reportes guardados
    fun getAllAlerts(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ALERTS ORDER BY id DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val map = mutableMapOf<String, String>()
                map[COLUMN_ID] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                map[COLUMN_QUARTER] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUARTER))
                map[COLUMN_TYPE] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                map[COLUMN_VALUE] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE))
                map[COLUMN_DATE] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                map[COLUMN_OBSERVATIONS] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATIONS))
                list.add(map)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Función para eliminar un reporte
    fun deleteAlert(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_ALERTS, "$COLUMN_ID=?", arrayOf(id.toString()))
    }
}
