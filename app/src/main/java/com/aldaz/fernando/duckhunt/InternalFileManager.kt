package com.aldaz.fernando.duckhunt

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException

// Esta clase se encargará de escribir y leer archivos en el almacenamiento interno.
class InternalFileManager(private val context: Context) {

    /**
     * Escribe contenido de texto en un archivo en el almacenamiento interno.
     * Si el archivo ya existe, SOBREESCRIBE su contenido.
     *
     * @param fileName El nombre del archivo (ej. "game_log.txt").
     * @param content El texto que se va a guardar.
     */
    fun writeFile(fileName: String, content: String) {
        try {
            // El modo MODE_PRIVATE asegura que el archivo solo sea accesible por esta aplicación.
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            Log.d("INTERNAL_STORAGE", "Éxito al escribir en el archivo interno: $fileName")
        } catch (e: Exception) {
            Log.e("INTERNAL_STORAGE", "Error al escribir en el archivo interno: $fileName", e)
        }
    }

    /**
     * Lee todo el contenido de un archivo de texto desde el almacenamiento interno.
     *
     * @param fileName El nombre del archivo a leer.
     * @return El contenido del archivo como un String. Si no se encuentra, devuelve un mensaje de error.
     */
    fun readFile(fileName: String): String {
        return try {
            val content = context.openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.joinToString("\n")
            }
            Log.d("INTERNAL_STORAGE", "Éxito al leer el archivo interno: $fileName")
            content
        } catch (e: FileNotFoundException) {
            Log.w("INTERNAL_STORAGE", "El archivo interno no existe aún: $fileName")
            "El archivo no existe."
        } catch (e: Exception) {
            Log.e("INTERNAL_STORAGE", "Error al leer el archivo interno: $fileName", e)
            "Error al leer el archivo."
        }
    }
}