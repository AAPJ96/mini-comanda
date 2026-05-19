package com.example.minicomanda.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ComandaConItems(
    @Embedded val comanda: Comanda,

    @Relation(
        entity = ItemComanda::class, // Le especificamos a Room la entidad origen
        parentColumn = "id",
        entityColumn = "comanda_id"
    )
    val items: List<ItemComandaConMenu> // <-- Cambiado de List<ItemComanda> a List<ItemComandaConMenu>
)