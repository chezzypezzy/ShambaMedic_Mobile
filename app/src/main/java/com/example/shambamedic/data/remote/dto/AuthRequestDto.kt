package com.example.shambamedic.data.remote.dto

data class AuthRequestDto(
    val name: String = "",
    val phoneNumber: String,
    val passwordHash: String,
    val role: String = "farmer"
)
