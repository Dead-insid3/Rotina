package com.chronos.rotina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        PerfilEntity::class,
        PreferenciaEntity::class,
        TagEntity::class,
        MoldeEntity::class,
        MoldePassoEntity::class,
        PassoEntity::class,
        FraseEntity::class,
        EscalaEntity::class,
        CategoriaEntity::class,
        ContaEntity::class,
        RendaEntity::class,
        TarefaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
    abstract fun preferenciaDao(): PreferenciaDao
    abstract fun tagDao(): TagDao
    abstract fun moldeDao(): MoldeDao
    abstract fun passoDao(): PassoDao
    abstract fun fraseDao(): FraseDao
    abstract fun escalaDao(): EscalaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun contaDao(): ContaDao
    abstract fun rendaDao(): RendaDao
    abstract fun tarefaDao(): TarefaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRACAO_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS tarefas (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "titulo TEXT NOT NULL, " +
                        "detalhe TEXT NOT NULL DEFAULT '', " +
                        "prazoMillis INTEGER NOT NULL, " +
                        "insistencia TEXT NOT NULL DEFAULT 'presente', " +
                        "concluida INTEGER NOT NULL DEFAULT 0, " +
                        "criadaEm INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        private val MIGRACAO_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE contas ADD COLUMN porDiaUtil INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE rendas ADD COLUMN porDiaUtil INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun obter(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "machitto.db"
                )
                    .addMigrations(MIGRACAO_2_3, MIGRACAO_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = db
                db
            }
        }

        suspend fun popularSeVazio(db: AppDatabase) {
            if (db.tagDao().contar() > 0) return
            db.perfilDao().salvar(PerfilEntity(id = 1, nome = "", genero = "nb"))
            db.preferenciaDao().definir(PreferenciaEntity("modo_pagamento", "vencimento"))
            db.preferenciaDao().definir(PreferenciaEntity("lembrete_dias_antes", "5"))
            db.preferenciaDao().definir(PreferenciaEntity("tema", "auto"))
            db.tagDao().inserir(tagsSemente())
            db.moldeDao().inserirMoldes(moldesSemente())
            db.moldeDao().inserirPassos(moldePassosSemente())
            db.fraseDao().inserir(frasesSemente())
        }
    }
}
