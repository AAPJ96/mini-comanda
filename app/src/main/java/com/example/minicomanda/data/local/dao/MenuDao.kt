package com.example.minicomanda.data.local.dao

import com.example.minicomanda.data.local.entities.MenuItem

interface MenuDao {
    fun insert(item: MenuItem)
    fun update(item: MenuItem)
    fun delete(item: MenuItem)
    fun getAll(): List<MenuItem>
    fun getById(id: Int): MenuItem?
}