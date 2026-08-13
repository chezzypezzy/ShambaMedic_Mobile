package com.example.shambamedic.data.remote.dto

data class AuthResponseDto(
    val userId: String,
    val token: String,
    val name: String,
    val phoneNumber: String
)
