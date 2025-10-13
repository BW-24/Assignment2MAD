package com.example.pocketlibrary

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FavouriteBookDAO {
    @Insert
    suspend fun insert(book: FavouriteBook)

    @Update
    suspend fun update(book: FavouriteBook)

    @Delete
    suspend fun delete(book: FavouriteBook)

    // get all books in alphabetical order of title
    @Query("SELECT * FROM favourite_books ORDER BY title ASC")
    suspend fun getAll(): List<FavouriteBook>

    // search by any title or author
    @Query("SELECT * FROM favourite_books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query  || '%' ORDER BY title ASC")
    suspend fun search(query: String): List<FavouriteBook>
}