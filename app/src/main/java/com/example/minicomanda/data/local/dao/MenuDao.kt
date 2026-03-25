package com.example.minicomanda.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.minicomanda.data.local.entities.MenuItem


@Dao
interface MenuDao {

    @Insert
    suspend fun insert(item: MenuItem)

    @Update
    suspend fun update(item: MenuItem)

    @Delete
    suspend fun delete(item: MenuItem)

    // Asumiendo que tu entidad @Entity se llama "MenuItem" en la base de datos
    /*
    @Query("SELECT * FROM MenuItem")
    suspend fun getAll(): List<MenuItem>

    @Query("SELECT * FROM MenuItem WHERE id = :id")
    suspend fun getById(id: Int): MenuItem?
    */
}