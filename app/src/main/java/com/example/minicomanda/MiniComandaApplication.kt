package com.example.minicomanda

import android.app.Application
import androidx.room.Room
import com.example.minicomanda.data.local.database.AppDatabase
import com.example.minicomanda.data.local.dao.*

class MiniComandaApplication : Application() {

    // Singleton de la base de datos
    lateinit var database: AppDatabase
        private set

    // Acceso rápido a los DAOs
    val salaDao: SalaDao get() = database.salaDao()
    val itemMenuDao: ItemMenuDao get() = database.itemMenuDao()
    val comandaDao: ComandaDao get() = database.comandaDao()
    val itemComandaDao: ItemComandaDao get() = database.itemComandaDao()

    override fun onCreate() {
        super.onCreate()
        instance = this               // <-- mover aquí, antes de todo

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "minicomanda_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        lateinit var instance: MiniComandaApplication
            private set
    }
}