package com.example.minicomanda.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.minicomanda.data.local.dao.*
import com.example.minicomanda.data.local.entities.*

@Database(
    entities = [Sala::class, ItemMenu::class, Comanda::class, ItemComanda::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salaDao(): SalaDao
    abstract fun itemMenuDao(): ItemMenuDao
    abstract fun comandaDao(): ComandaDao
    abstract fun itemComandaDao(): ItemComandaDao
}