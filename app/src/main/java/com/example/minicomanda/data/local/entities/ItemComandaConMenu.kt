package com.example.minicomanda.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ItemComandaConMenu(
    @Embedded val itemComanda: ItemComanda, // Trae los datos del ítem (cantidad, notas, etc.)

    @Relation(
        parentColumn = "item_menu_id", // La columna Clave Foránea en ItemComanda
        entityColumn = "id"          // La Clave Primaria en la tabla ItemMenu
    )
    val itemMenu: ItemMenu // Trae el platillo completo (nombre, precio, etc.)
)