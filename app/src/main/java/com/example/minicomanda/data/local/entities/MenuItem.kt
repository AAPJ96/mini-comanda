package com.example.minicomanda.data.local.entities

data class MenuItem(
    val id: Int = 0,
    val nombre: String,
    val precio: Double,
    val foto: ByteArray?,
    val extras: String?
)
