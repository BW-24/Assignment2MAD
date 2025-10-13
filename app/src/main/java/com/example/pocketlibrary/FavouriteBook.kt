package com.example.pocketlibrary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_books")
data class FavouriteBook (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "author")
    val author: String?,
    @ColumnInfo(name = "age")
    val year: Int?,
    @ColumnInfo(name = "cover")
    val cover: String?
)