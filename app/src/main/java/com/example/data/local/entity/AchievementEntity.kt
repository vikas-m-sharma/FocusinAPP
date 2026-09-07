package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val key: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAtMillis: Long? = null
)
