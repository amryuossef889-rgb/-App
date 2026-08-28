package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PdfBook
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfBookDao {
    @Query("SELECT * FROM PdfBook ORDER BY addedDate DESC")
    fun getAllBooks(): Flow<List<PdfBook>>

    @Query("SELECT * FROM PdfBook WHERE id = :id LIMIT 1")
    fun getBookById(id: Int): Flow<PdfBook?>

    @Query("SELECT * FROM PdfBook WHERE id = :id LIMIT 1")
    suspend fun getBookDirect(id: Int): PdfBook?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: PdfBook): Long

    @Update
    suspend fun updateBook(book: PdfBook)

    @Query("DELETE FROM PdfBook WHERE id = :id AND isBuiltin = 0")
    suspend fun deleteBook(id: Int)
}
