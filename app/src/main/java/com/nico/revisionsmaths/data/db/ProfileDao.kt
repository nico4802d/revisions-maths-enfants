package com.nico.revisionsmaths.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getById(id: Long): Profile?

    @Insert
    suspend fun insert(profile: Profile): Long

    @Update
    suspend fun update(profile: Profile)

    /** Incrément atomique d'XP : évite les écritures perdues en cas de mise à jour concurrente. */
    @Query("UPDATE profiles SET xp = xp + :delta WHERE id = :id")
    suspend fun addXp(id: Long, delta: Int)

    @Delete
    suspend fun delete(profile: Profile)
}
