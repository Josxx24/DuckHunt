package com.aldaz.fernando.duckhunt

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class RankingPlayerDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CazarPatos.db"
        private const val TABLE_NAME = "TBL_RANKING"
        private const val COLUMN_NAME_PLAYER = "PLAYER_NAME"
        private const val COLUMN_NAME_DUCKS = "DUCKS_HUNTED"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${COLUMN_NAME_PLAYER} TEXT," +
                    "${COLUMN_NAME_DUCKS} INTEGER)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun insertRanking(player: Player) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME_PLAYER, player.username)
            put(COLUMN_NAME_DUCKS, player.huntedDucks)
        }
        db?.insert(TABLE_NAME, null, values)
        db?.close()
    }

    fun insertRankingByQuery(player: Player) {
        val db = this.writableDatabase
        val sqlSentence = "INSERT INTO ${TABLE_NAME} " +
                "(${COLUMN_NAME_PLAYER}, ${COLUMN_NAME_DUCKS}) " +
                "VALUES('${player.username}',${player.huntedDucks})"
        db.execSQL(sqlSentence)
        db?.close()
    }

    fun readAllRankingByQuery(): ArrayList<Player> {
        val db = this.readableDatabase
        val sqlSelect = "SELECT * FROM ${TABLE_NAME}"
        val cursor = db.rawQuery(sqlSelect, null)
        val players = arrayListOf<Player>()
        with(cursor) {
            while (moveToNext()) {
                val playerName = getString(getColumnIndexOrThrow(COLUMN_NAME_PLAYER))
                val ducksHunted = getInt(getColumnIndexOrThrow(COLUMN_NAME_DUCKS))
                players.add(Player(playerName, ducksHunted))
            }
        }
        cursor.close()
        db.close()
        return players
    }

    fun readAllRanking(): ArrayList<Player> {
        val db = this.readableDatabase
        val projection = arrayOf(BaseColumns._ID, COLUMN_NAME_PLAYER, COLUMN_NAME_DUCKS)
        val sortOrder = "${COLUMN_NAME_DUCKS} DESC"
        val cursor = db.query(
            TABLE_NAME, projection, null, null, null, null, sortOrder
        )
        val players = arrayListOf<Player>()
        with(cursor) {
            while (moveToNext()) {
                val playerName = getString(getColumnIndexOrThrow(COLUMN_NAME_PLAYER))
                val ducksHunted = getInt(getColumnIndexOrThrow(COLUMN_NAME_DUCKS))
                players.add(Player(playerName, ducksHunted))
            }
        }
        cursor.close()
        db.close()
        return players
    }

    fun readDucksHuntedByPlayer(player: String): Int {
        val db = this.readableDatabase
        val projection = arrayOf(COLUMN_NAME_DUCKS)
        val selection = "${COLUMN_NAME_PLAYER} = ?"
        val selectionArgs = arrayOf(player)
        val cursor = db.query(
            TABLE_NAME, projection, selection, selectionArgs, null, null, null
        )
        var ducksHunted = 0
        with(cursor) {
            if (moveToFirst()) {
                ducksHunted = getInt(getColumnIndexOrThrow(COLUMN_NAME_DUCKS))
            }
        }
        cursor.close()
        db.close()
        return ducksHunted
    }

    fun updateRanking(player: Player) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME_DUCKS, player.huntedDucks)
        }
        val selection = "${COLUMN_NAME_PLAYER} = ?"
        val selectionArgs = arrayOf(player.username)
        db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
    }

    fun deleteRanking(player: String) {
        val db = this.writableDatabase
        val selection = "${COLUMN_NAME_PLAYER} = ?"
        val selectionArgs = arrayOf(player)
        db.delete(TABLE_NAME, selection, selectionArgs)
        db.close()
    }

    fun deleteAllRanking() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}
