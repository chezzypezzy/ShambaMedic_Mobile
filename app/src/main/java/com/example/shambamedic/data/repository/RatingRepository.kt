package com.example.shambamedic.data.repository

import com.example.shambamedic.data.local.dao.RatingDao
import com.example.shambamedic.data.local.entity.RatingEntity
import com.example.shambamedic.domain.model.Rating
import com.example.shambamedic.domain.model.toDomain
import java.util.UUID

class RatingRepository(
    private val ratingDao: RatingDao
) {

    suspend fun submitRating(
        escalationId: String,
        ratedBy: String,
        starRating: Int,
        comment: String?
    ): Result<Rating> {
        if (starRating !in 1..5) {
            return Result.failure(Exception("Star rating must be between 1 and 5"))
        }

        return try {
            val rating = RatingEntity(
                ratingId = UUID.randomUUID().toString(),
                escalationId = escalationId,
                ratedBy = ratedBy,
                ratedRole = "farmer",
                starRating = starRating,
                comment = comment,
                ratingTimestamp = System.currentTimeMillis()
            )
            ratingDao.insertRating(rating)
            Result.success(rating.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRatingForEscalation(escalationId: String): Rating? {
        return ratingDao.getRatingsForEscalation(escalationId).firstOrNull()?.toDomain()
    }
}
