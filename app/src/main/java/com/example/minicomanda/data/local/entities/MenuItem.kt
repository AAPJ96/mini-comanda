package com.example.minicomanda.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "precio")
    val precio: Double,

    // Se recomienda usar ColumnInfo para asegurar compatibilidad de nombres
    @ColumnInfo(name = "foto", typeAffinity = ColumnInfo.BLOB)
    val foto: ByteArray? = null,

    @ColumnInfo(name = "extras")
    val extras: String? = null
)
