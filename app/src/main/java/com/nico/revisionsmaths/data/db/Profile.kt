package com.nico.revisionsmaths.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val xp: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val level: Int get() = 1 + xp / XP_PER_LEVEL
    val levelProgress: Float get() = (xp % XP_PER_LEVEL) / XP_PER_LEVEL.toFloat()
    val xpToNextLevel: Int get() = XP_PER_LEVEL - (xp % XP_PER_LEVEL)

    companion object {
        const val XP_PER_LEVEL = 100
    }
}
