package com.aldaz.fernando.duckhunt

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

// Esta clase será nuestro gestor centralizado para las preferencias encriptadas.
// Necesita el 'context' de la aplicación para poder funcionar.
class EncryptedSharedPreferencesManager(context: Context) {

    // 1. CREAR LA CLAVE MAESTRA
    // Esta es la clave principal que se usará para encriptar tanto las claves como los valores.
    // MasterKeys.getOrCreate() la genera de forma segura y la guarda en el Keystore de Android.
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // 2. DEFINIR EL NOMBRE DEL ARCHIVO DE PREFERENCIAS
    // Este será el nombre del archivo XML que verás en el Device File Explorer.
    private val sharedPrefsFile = "duckhunt_secret_prefs"

    // 3. INICIALIZAR ENCRYPTEDSHAREDPREFERENCES
    // Aquí se crea la instancia, vinculando el nombre del archivo, la clave maestra y los esquemas de encriptación.
    private val sharedPreferences = EncryptedSharedPreferences.create(
        sharedPrefsFile,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Esquema para encriptar las claves (ej. "HIGH_SCORE")
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  // Esquema para encriptar los valores (ej. 500)
    )

    // --- MÉTODOS PÚBLICOS PARA INTERACTUAR ---

    /**
     * Guarda un valor de tipo String de forma encriptada.
     * @param key La clave para identificar el dato (ej. "PLAYER_NAME").
     * @param value El valor a guardar (ej. "Fernando").
     */
    fun saveData(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply() // 'apply()' guarda los cambios de forma asíncrona.
    }

    /**
     * Guarda un valor de tipo Int de forma encriptada.
     * @param key La clave para identificar el dato (ej. "HIGH_SCORE").
     * @param value El valor a guardar (ej. 1500).
     */
    fun saveData(key: String, value: Int) {
        sharedPreferences.edit()
            .putInt(key, value)
            .apply()
    }

    /**
     * Lee un valor de tipo String.
     * @param key La clave del dato a leer.
     * @return El valor desencriptado. Si no se encuentra, devuelve una cadena vacía.
     */
    fun getString(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    /**
     * Lee un valor de tipo Int.
     * @param key La clave del dato a leer.
     * @return El valor desencriptado. Si no se encuentra, devuelve 0.
     */
    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }
}
