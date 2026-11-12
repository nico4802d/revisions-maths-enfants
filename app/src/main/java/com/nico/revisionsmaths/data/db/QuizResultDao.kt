package com.nico.revisionsmaths.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {

    @Query("SELECT * FROM quiz_results WHERE profileId = :profileId ORDER BY date DESC")
    fun observeForProfile(profileId: Long): Flow<List<QuizResult>>

    @Query("SELECT * FROM quiz_results WHERE profileId = :profileId ORDER BY date DESC LIMIT 1")
    suspend fun latestForProfile(profileId: Long): QuizResult?

    @Insert
    suspend fun insert(result: QuizResult): Long
}
