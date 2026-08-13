package com.example.shambamedic.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.shambamedic.data.local.dao.UserDao
import com.example.shambamedic.data.local.entity.UserEntity
import com.example.shambamedic.domain.model.User
import com.example.shambamedic.domain.model.toDomain
import com.example.shambamedic.domain.model.toEntity
import com.example.shambamedic.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

class UserRepository(
    private val userDao: UserDao,
    private val dataStore: DataStore<Preferences>
) {

    suspend fun registerUser(name: String, phoneNumber: String, pin: String): Result<User> {
        return try {
            val passwordHash = hashPin(pin)
            val userId = UUID.randomUUID().toString()
            val user = User(
                userId = userId,
                name = name,
                phoneNumber = phoneNumber,
                role = "farmer",
                languagePreference = "en",
                lastSyncTimestamp = null
            )
            userDao.insertUser(user.toEntity(passwordHash))
            
            dataStore.edit { prefs ->
                prefs[Constants.USER_ID_KEY] = userId
                prefs[Constants.TOKEN_KEY] = "temp_token_for_demo" // Actual token would come from API
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(phoneNumber: String, pin: String): Result<User> {
        val userEntity = userDao.getUserByPhone(phoneNumber)
            ?: return Result.failure(Exception("User not found"))
        
        val providedHash = hashPin(pin)
        return if (userEntity.passwordHash == providedHash) {
            val user = userEntity.toDomain()
            dataStore.edit { prefs ->
                prefs[Constants.USER_ID_KEY] = user.userId
            }
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    suspend fun signInWithGoogle(
        googleId: String,
        email: String,
        displayName: String
    ): Result<User> {
        return try {
            val existing = userDao.getUserByGoogleId(googleId)
            val userEntity = existing ?: UserEntity(
                userId = UUID.randomUUID().toString(),
                name = displayName,
                // phone_number has a UNIQUE index; a bare "" would collide across every
                // Google-only account (and OnConflictStrategy.REPLACE would silently wipe
                // the earlier account), so use a per-user placeholder instead.
                phoneNumber = "google_$googleId",
                passwordHash = "",
                role = "farmer",
                languagePreference = "en",
                authProvider = "google",
                googleId = googleId
            )
            if (existing == null) userDao.insertUser(userEntity)

            dataStore.edit { prefs ->
                prefs[Constants.USER_ID_KEY] = userEntity.userId
            }

            Result.success(userEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        val userId = dataStore.data.map { it[Constants.USER_ID_KEY] }.first()
        return userId?.let {
            userDao.getUserById(it)?.toDomain()
        }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(Constants.TOKEN_KEY)
            prefs.remove(Constants.USER_ID_KEY)
        }
    }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { prefs ->
            prefs[Constants.TOKEN_KEY] = token
        }
    }

    suspend fun getAuthToken(): String? {
        return dataStore.data.map { it[Constants.TOKEN_KEY] }.first()
    }

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
