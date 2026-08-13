package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.UserEntity

data class User(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val role: String,
    val languagePreference: String,
    val lastSyncTimestamp: Long?,
    val authProvider: String = "local",
    val googleId: String? = null
)

fun UserEntity.toDomain() = User(
    userId = userId,
    name = name,
    phoneNumber = phoneNumber,
    role = role,
    languagePreference = languagePreference,
    lastSyncTimestamp = lastSyncTimestamp,
    authProvider = authProvider,
    googleId = googleId
)

fun User.toEntity(passwordHash: String) = UserEntity(
    userId = userId,
    name = name,
    phoneNumber = phoneNumber,
    passwordHash = passwordHash,
    role = role,
    languagePreference = languagePreference,
    lastSyncTimestamp = lastSyncTimestamp,
    authProvider = authProvider,
    googleId = googleId
)
