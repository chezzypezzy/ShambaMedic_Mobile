package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [Index(value = ["phone_number"], unique = true)]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "role")
    val role: String = "farmer",

    @ColumnInfo(name = "language_preference")
    val languagePreference: String = "en",

    @ColumnInfo(name = "last_sync_timestamp")
    val lastSyncTimestamp: Long? = null,

    @ColumnInfo(name = "auth_provider")
    val authProvider: String = "local",

    @ColumnInfo(name = "google_id")
    val googleId: String? = null
)
