package com.example.minicomanda.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ComandaConItems(
    @Embedded val comanda: Comanda,
    @Relation(
        parentColumn = "id",
        entityColumn = "comanda_id"
    )
    val items: List<ItemComanda>
)