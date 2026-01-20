package com.aldaz.fernando.duckhunt

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RankingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ranking)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        OperacionesSqLite()
    }
    fun OperacionesSqLite() {
        // 1. Instanciar el DBHelper
        val dbHelper = RankingPlayerDBHelper(this)

        // --- FORZAR CREACIÓN FÍSICA ---
        // Al obtener writableDatabase, Android crea el archivo CazarPatos.db en el disco
        val db = dbHelper.writableDatabase
        android.util.Log.i("SQLITE_DEBUG", "Base de datos abierta en la ruta: ${db.path}")
        // ------------------------------

        android.util.Log.i("SQLITE_DEBUG", "Iniciando operaciones de prueba...")

        // 2. Limpiar base de datos (Borrar todo)
        dbHelper.deleteAllRanking()
        android.util.Log.i("SQLITE_DEBUG", "Base de datos limpiada.")

        // 3. Inserción mediante Query directa (SQL String)
        dbHelper.insertRankingByQuery(Player("Jugador9", 10))
        android.util.Log.i("SQLITE_DEBUG", "Inserción por query realizada.")

        // 4. Lectura de un solo registro (Búsqueda por nombre)
        val patosCazados = dbHelper.readDucksHuntedByPlayer("Jugador9")
        android.util.Log.i("SQLITE_DEBUG", "Patos cazados recuperados para Jugador9: $patosCazados")

        // 5. Actualización de registro
        dbHelper.updateRanking(Player("Jugador9", 5))
        android.util.Log.i("SQLITE_DEBUG", "Actualización de Jugador9 realizada.")

        // 6. Borrado de registro específico
        dbHelper.deleteRanking("Jugador9")
        android.util.Log.i("SQLITE_DEBUG", "Borrado de Jugador9 realizado.")

        // 7. Inserción estándar mediante ContentValues
        dbHelper.insertRanking(Player("Jugador9", 7))
        android.util.Log.i("SQLITE_DEBUG", "Inserción estándar realizada.")

        // 8. Leer todos los registros para verificar la lista
        val listaPlayersQuery = dbHelper.readAllRankingByQuery()
        android.util.Log.i("SQLITE_DEBUG", "Total registros leídos por Query: ${listaPlayersQuery.size}")

        // 9. Ejecutar procesos finales del laboratorio: Grabar ranking real y mostrar en UI
        GrabarRankingSQLite()
        LeerRankingsSQLite()

        android.util.Log.i("SQLITE_DEBUG", "Operaciones finalizadas con éxito.")
    }


    fun GrabarRankingSQLite() {
        val dbHelper = RankingPlayerDBHelper(this)

        val jugadores = arrayListOf(
            Player("Fernando.Aldaz", 11),
            Player("Jugador2", 6),
            Player("Jugador3", 3),
            Player("Jugador4", 9)
        )

        // Ordenar antes de grabar (opcional, ya que el método de lectura lo ordena)
        jugadores.sortByDescending { it.huntedDucks }

        for (jugador in jugadores) {
            dbHelper.insertRanking(jugador)
        }
    }

    /**
     * Lee la información de la base de datos y la presenta en el RecyclerView
     */
    fun LeerRankingsSQLite() {
        val dbHelper = RankingPlayerDBHelper(this)

        // Obtener los datos desde SQLite (ya vienen ordenados por DESC según el DBHelper)
        val jugadoresSQLite = dbHelper.readAllRanking()

        val recyclerViewRanking: RecyclerView = findViewById(R.id.recyclerViewRanking)
        recyclerViewRanking.layoutManager = LinearLayoutManager(this)
        recyclerViewRanking.adapter = RankingAdapter(jugadoresSQLite)
        recyclerViewRanking.setHasFixedSize(true)
    }
}
