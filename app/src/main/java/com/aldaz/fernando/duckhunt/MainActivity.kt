package com.aldaz.fernando.duckhunt

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
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
import android.view.Menu
import android.view.MenuItem
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    // Variables de Vistas (UI)
    private lateinit var textViewUser: TextView
    private lateinit var textViewCounter: TextView
    private lateinit var textViewTime: TextView
    private lateinit var imageViewDuck: ImageView

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

    companion object {
        const val EXTRA_LOGIN = "EXTRA_LOGIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializar Vistas
        textViewUser = findViewById(R.id.textViewUser)
        textViewCounter = findViewById(R.id.textViewCounter)
        textViewTime = findViewById(R.id.textViewTime)
        imageViewDuck = findViewById(R.id.imageViewDuck)

        // Inicializar Gestores
        prefsManager = EncryptedSharedPreferencesManager(this)
        fileManager = InternalFileManager(this)
        externalFileManager = ExternalFileManager(this)

        // Configurar Eventos
        imageViewDuck.setOnClickListener {
            if (gameOver) return@setOnClickListener
            handleDuckClick()
        }

        // Configurar datos del usuario
        val extras = intent.extras
        if (extras != null) {
            var usuario = extras.getString(EXTRA_LOGIN) ?: "Player1"
            usuario = usuario.substringBefore("@")
            textViewUser.text = usuario
        }

        initializeScreen()
        initializeSound()
        initializeCountdown()
    }

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
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(10)
            .build()
        soundId = soundPool.load(this, R.raw.gunshot, 1)
        soundPool.setOnLoadCompleteListener { _, _, _ -> isLoaded = true }
    }

    private fun initializeScreen() {
        val display = this.resources.displayMetrics
        screenWidth = display.widthPixels
        screenHeight = display.heightPixels
    }

    private fun moveDuck() {
        val min = imageViewDuck.getWidth()/2
        val maximoX = screenWidth - imageViewDuck.getWidth()
        val maximoY = screenHeight - imageViewDuck.getHeight()
        // Generamos 2 números aleatorios, para la coordenadas x , y
        val randomX = Random.nextInt(0,maximoX - min + 1)
        val randomY = Random.nextInt(92,maximoY - min + 1)

        imageViewDuck.animate()
            .x(randomX.toFloat())
            .y(randomY.toFloat())
            .setDuration(300) // animación suave
            .start()

    }

    private var countDownTimer = object : CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val secondsRemaining = millisUntilFinished / 1000
            textViewTime.setText("${secondsRemaining}s")
        }
        override fun onFinish() {
            textViewTime.setText("0s")
            gameOver = true
            showGameOverDialog()
            val nombreJugador = textViewUser.text.toString()
            val patosCazados = textViewCounter.text.toString()
            procesarPuntajePatosCazados(nombreJugador, patosCazados.toInt()) //Firestore
        }
    }


    private fun saveGameResultToFile() {
        val oldHistory = fileManager.readFile("game_history.txt")
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val newEntry = "Fecha: $currentDate, Usuario: ${textViewUser.text}, Puntaje: $counter"
        val newHistory = if (oldHistory.contains("no existe") || oldHistory.isBlank()) newEntry else "$oldHistory\n$newEntry"
        fileManager.writeFile("game_history.txt", newHistory)
    }

    private fun initializeCountdown() {
        gameOver = false
        countDownTimer.start()
    }

    private fun showGameOverDialog() {
        val builder = AlertDialog.Builder(this)
        builder
            .setMessage("¡¡Felicidades!!\nHas conseguido cazar $counter patos")
            .setTitle("Fin del juego")
            .setIcon(R.drawable.duck)
            .setCancelable(false)
            .setPositiveButton("Reiniciar") { _, _ -> restartGame() }
            .setNegativeButton("Ver Ranking") { _, _ ->
                val intent = Intent(this, RankingActivity::class.java)
                startActivity(intent)
            }
        builder.create().show()
    }

    private fun restartGame() {
        counter = 0
        textViewCounter.text = counter.toString()
        gameOver = false
        moveDuck()
        initializeCountdown()
    }

    private fun jugarOnline() {
        val intentWeb = Intent(Intent.ACTION_VIEW, Uri.parse("https://duckhuntjs.com/"))
        startActivity(intentWeb)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_nuevo_juego -> { restartGame(); true }
            R.id.action_jugar_online -> { jugarOnline(); true }
            R.id.action_ranking -> {
                startActivity(Intent(this, RankingActivity::class.java))
                true
            }
            R.id.action_salir -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        countDownTimer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
    fun procesarPuntajePatosCazados(nombreJugador:String, patosCazados:Int){
        val jugador = Player(nombreJugador,patosCazados)
        //Trata de obtener id del documento del ranking específico,
        // si lo obtiene lo actualiza, caso contrario lo crea
        val db = Firebase.firestore
        db.collection("ranking")
            .whereEqualTo("username", jugador.username)
            .get()
            .addOnSuccessListener { documents ->
                if(documents!= null &&
                    documents.documents != null &&
                    documents.documents.count()>0
                ){
                    val idDocumento = documents.documents.get(0).id
                    val jugadorLeido = documents.documents.get(0).toObject(Player::class.java)
                    if(jugadorLeido!!.huntedDucks < patosCazados )
                    {
                        Log.w(EXTRA_LOGIN, "Puntaje actual mayor, por lo tanto actualizado")
                        actualizarPuntajeJugador(idDocumento, jugador)
                    }
                    else{
                        Log.w(EXTRA_LOGIN, "No se actualizo puntaje, por ser menor al actual")
                    }
                }
                else{
                    ingresarPuntajeJugador(jugador)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error getting documents", exception)
                Toast.makeText(this, "Error al obtener datos de jugador", Toast.LENGTH_LONG).show()
            }
    }
    fun ingresarPuntajeJugador(jugador:Player){
        val db = Firebase.firestore
        db.collection("ranking")
            .add(jugador)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this,"Puntaje usuario ingresado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error adding document", exception)
                Toast.makeText(this,"Error al ingresar el puntaje", Toast.LENGTH_LONG).show()
            }
    }
    fun actualizarPuntajeJugador(idDocumento:String, jugador:Player){
        val db = Firebase.firestore
        db.collection("ranking")
            .document(idDocumento)
            //.update(contactoHashMap)
            .set(jugador) //otra forma de actualizar
            .addOnSuccessListener {
                Toast.makeText(this,"Puntaje de usuario actualizado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error updating document", exception)
                Toast.makeText(this,"Error al actualizar el puntaje" , Toast.LENGTH_LONG).show()
            }
    }
    fun eliminarPuntajeJugador(idDocumentoSeleccionado:String){
        val db = Firebase.firestore
        db.collection("ranking")
            .document(idDocumentoSeleccionado)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this,"Puntaje de usuario eliminado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error deleting document", exception)
                Toast.makeText(this,"Error al eliminar el puntaje" , Toast.LENGTH_LONG).show()
            }
    }

}