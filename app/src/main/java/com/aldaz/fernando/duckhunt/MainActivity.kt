package com.aldaz.fernando.duckhunt

import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
// import android.widget.Button // Ya no se necesita el botón físico
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    // Variables de Vistas (UI)
    private lateinit var textViewUser: TextView
    private lateinit var textViewCounter: TextView
    private lateinit var textViewTime: TextView
    private lateinit var imageViewDuck: ImageView
    // private lateinit var buttonExport: Button // BORRADO: Ya no necesitamos esta variable

    // Variables de Lógica del Juego
    private lateinit var soundPool: SoundPool
    private val handler = Handler(Looper.getMainLooper())
    private var counter = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var soundId: Int = 0
    private var isLoaded = false
    private var gameOver = false

    // Gestores de Almacenamiento
    private lateinit var externalFileManager: ExternalFileManager
    private lateinit var prefsManager: EncryptedSharedPreferencesManager
    private lateinit var fileManager: InternalFileManager

    // Constante para recibir datos del LoginActivity
    companion object {
        const val EXTRA_LOGIN = "EXTRA_LOGIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // --- PASO 1: INICIALIZAR TODAS LAS VARIABLES ---
        // Vistas
        textViewUser = findViewById(R.id.textViewUser)
        textViewCounter = findViewById(R.id.textViewCounter)
        textViewTime = findViewById(R.id.textViewTime)
        imageViewDuck = findViewById(R.id.imageViewDuck)
        // buttonExport = findViewById(R.id.buttonExport) // BORRADO: Ya no se inicializa

        // Gestores
        prefsManager = EncryptedSharedPreferencesManager(this)
        fileManager = InternalFileManager(this)
        externalFileManager = ExternalFileManager(this)

        // --- PASO 2: CONFIGURAR LOS EVENTOS (LISTENERS) ---
        // BORRADO: El listener del botón ya no se configura aquí
        // buttonExport.setOnClickListener { ... }

        imageViewDuck.setOnClickListener {
            if (gameOver) return@setOnClickListener
            handleDuckClick()
        }

        // --- PASO 3: CONFIGURAR EL ESTADO INICIAL DEL JUEGO ---
        val extras = intent.extras
        if (extras != null) {
            val usuario = extras.getString(EXTRA_LOGIN) ?: "Unknown"
            textViewUser.text = usuario
        } else {
            textViewUser.text = "Player1"
            Log.w("MainActivity", "No se recibieron extras del Intent. Usando nombre por defecto.")
        }

        val gameLog = fileManager.readFile("game_history.txt")
        Log.i("INTERNAL_STORAGE", "Historial de partidas anteriores:\n$gameLog")

        initializeScreen()
        initializeSound()
        initializeCountdown()
    }

    // --- FUNCIONES AUXILIARES ORGANIZADAS ---

    private fun handleDuckClick() {
        counter++
        if (isLoaded) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }
        textViewCounter.text = counter.toString()
        imageViewDuck.setImageResource(R.drawable.duck_clicked)
        handler.postDelayed({
            imageViewDuck.setImageResource(R.drawable.duck)
            moveDuck()
        }, 200)
    }

    private fun initializeSound() {
        // ... (código sin cambios)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(10)
            .build()
        soundId = soundPool.load(this, R.raw.gunshot, 1)
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            isLoaded = true
        }
    }

    private fun exportGameHistory() {
        // ... (código sin cambios)
        val historyContent = fileManager.readFile("game_history.txt")

        if (historyContent.isNotBlank() && historyContent != "El archivo no existe." && historyContent != "Error al leer el archivo.") {
            val fileName = "duckhunt_history_${System.currentTimeMillis()}.txt"
            val subfolder = "DuckHuntReports"
            externalFileManager.writeFile(fileName, historyContent, subfolder)
            Toast.makeText(this, "Historial exportado a Documents/$subfolder", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "No hay historial para exportar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeScreen() {
        // ... (código sin cambios)
        val display = this.resources.displayMetrics
        screenWidth = display.widthPixels
        screenHeight = display.heightPixels
    }

    private fun moveDuck() {
        // ... (código sin cambios)
        if (gameOver) return
        val randomX = Random.nextInt(0, screenWidth - imageViewDuck.width)
        val randomY = Random.nextInt(0, screenHeight - imageViewDuck.height)
        imageViewDuck.animate()
            .x(randomX.toFloat())
            .y(randomY.toFloat())
            .setDuration(300)
            .start()
    }

    private val countDownTimer = object : CountDownTimer(20000, 1000) {
        // ... (onTick sin cambios)
        override fun onTick(millisUntilFinished: Long) {
            val secondsRemaining = millisUntilFinished / 1000
            textViewTime.text = "${secondsRemaining}s"
        }
        override fun onFinish() {
            textViewTime.text = "0s"
            gameOver = true
            val currentHighScore = prefsManager.getInt("HIGH_SCORE")
            if (counter > currentHighScore) {
                prefsManager.saveData("HIGH_SCORE", counter)
                Log.d("ENCRYPTED_PREFS", "¡Nuevo récord guardado: $counter!")
            }
            saveGameResultToFile()
            showGameOverDialog()
        }
    }

    private fun saveGameResultToFile() {
        // ... (código sin cambios)
        val oldHistory = fileManager.readFile("game_history.txt")
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val newEntry = "Fecha: $currentDate, Usuario: ${textViewUser.text}, Puntaje: $counter"
        val newHistory = if (oldHistory == "El archivo no existe." || oldHistory.isBlank()) {
            newEntry
        } else {
            "$oldHistory\n$newEntry"
        }
        fileManager.writeFile("game_history.txt", newHistory)
    }

    private fun initializeCountdown() {
        gameOver = false
        countDownTimer.start()
    }

    // --- MODIFICACIÓN PRINCIPAL AQUÍ ---
    private fun showGameOverDialog() {
        val highScore = prefsManager.getInt("HIGH_SCORE")
        val message = "¡Has cazado $counter patos!\n\nRécord actual: $highScore"

        val builder = AlertDialog.Builder(this)
        builder
            .setMessage(message)
            .setTitle(getString(R.string.dialog_title_game_end))
            // Botón Positivo
            .setPositiveButton(getString(R.string.button_restart)) { _, _ ->
                restartGame()
            }
            // Botón Negativo
            .setNegativeButton(getString(R.string.button_close)) { _, _ ->
                finish()
            }
            // AÑADIDO: Botón Neutral para Exportar
            .setNeutralButton("Exportar") { _, _ ->
                exportGameHistory()
            }
            .setCancelable(false)
        builder.create().show()
    }

    private fun restartGame(){
        // ... (código sin cambios)
        counter = 0
        textViewCounter.text = counter.toString()
        moveDuck()
        initializeCountdown()
    }

    override fun onStop() {
        // ... (código sin cambios)
        super.onStop()
        countDownTimer.cancel()
        Log.w(EXTRA_LOGIN, "Juego pausado por ir a segundo plano.")
    }

    override fun onDestroy() {
        // ... (código sin cambios)
        super.onDestroy()
        soundPool.release()
    }
}
