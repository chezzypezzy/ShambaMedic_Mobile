package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.model.User
import javax.inject.Inject

class AuthenticateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        phoneNumber: String,
        pin: String,
        isRegistering: Boolean,
        name: String = ""
    ): Result<User> {
        return if (isRegistering) {
            userRepository.registerUser(name, phoneNumber, pin)
        } else {
            userRepository.loginUser(phoneNumber, pin)
        }
    }
}
