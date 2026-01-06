package com.aldaz.fernando.duckhunt

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log

// Esta clase se encargará de escribir archivos en el almacenamiento externo público.
class ExternalFileManager(private val context: Context) {

    /**
     * Escribe contenido de texto en un archivo en una carpeta pública (ej. Documents).
     * Cumple con las reglas de Scoped Storage (Android 10+).
     *
     * @param fileName El nombre que tendrá el archivo (ej. "duckhunt_history.txt").
     * @param content El texto a guardar en el archivo.
     * @param subfolder La subcarpeta donde se guardará dentro de Documents (ej. "DuckHuntReports").
     */
    fun writeFile(fileName: String, content: String, subfolder: String) {
        // ContentResolver es el puente para acceder a los datos compartidos del dispositivo.
        val resolver = context.contentResolver

        // ContentValues actúa como un mapa para describir el nuevo archivo a MediaStore.
        val contentValues = ContentValues().apply {
            // El nombre visible del archivo.
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            // El tipo de archivo (MIME type).
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")

            // La ubicación relativa. Esto crea la subcarpeta si no existe.
            // Para Android 10 (API 29) o superior, usamos RELATIVE_PATH.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/$subfolder")
            }
        }

        try {
            // Pedimos a MediaStore que cree el archivo y nos devuelva su URI (su identificador único).
            // Para archivos genéricos (como .txt), usamos MediaStore.Files.
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            if (uri != null) {
                // Si la URI no es nula, el archivo se creó. Ahora escribimos el contenido.
                resolver.openOutputStream(uri).use { outputStream ->
                    outputStream?.write(content.toByteArray())
                }
                Log.d("EXTERNAL_STORAGE", "Éxito al escribir en el archivo externo: $fileName")
            } else {
                Log.e("EXTERNAL_STORAGE", "No se pudo crear el archivo externo. La URI es nula.")
            }

        } catch (e: Exception) {
            Log.e("EXTERNAL_STORAGE", "Error al escribir en el archivo externo: $fileName", e)
        }
    }

    // Nota: La lectura de archivos externos es más compleja, ya que por seguridad
    // generalmente requiere que el usuario seleccione el archivo manualmente a través
    // de un selector de archivos (Intent.ACTION_OPEN_DOCUMENT).
    // Para este ejercicio, nos enfocaremos solo en la escritura, que es directa.
}