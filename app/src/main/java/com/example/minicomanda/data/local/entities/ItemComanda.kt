package com.example.minicomanda.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items_comanda",
    foreignKeys = [
        ForeignKey(
            entity = Comanda::class,
            parentColumns = ["id"],
            childColumns = ["comanda_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemMenu::class,
            parentColumns = ["id"],
            childColumns = ["item_menu_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("comanda_id"),
        Index("item_menu_id")
    ]
)
data class ItemComanda(
    @PrimaryKey
    val id: String,                      // UUID como texto

    @ColumnInfo(name = "comanda_id")
    val comandaId: String,               // FK → comandas

    @ColumnInfo(name = "item_menu_id")
    val itemMenuId: String,              // FK → items_menu

    val cantidad: Int = 1,
    val persona: Int,                    // 1, 2, 3…

    @ColumnInfo(name = "precio_original_unidad")
    val precioOriginalUnidad: Long,      // en centavos, copia del precio del menú

    val estado: String = "EN PREPARACION", // EN PREPARACION, LISTO

    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "fecha_modificacion")
    val fechaModificacion: Long = System.currentTimeMillis(),

    val activo: Boolean = true,
    val sincronizado: Boolean = false
)