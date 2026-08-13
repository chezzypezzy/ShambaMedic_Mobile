package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.RatingRepository
import com.example.shambamedic.domain.model.Rating
import javax.inject.Inject

class SubmitRatingUseCase @Inject constructor(
    private val ratingRepository: RatingRepository
) {
    suspend operator fun invoke(
        escalationId: String,
        ratedBy: String,
        starRating: Int,
        comment: String?
    ): Result<Rating> {
        return ratingRepository.submitRating(escalationId, ratedBy, starRating, comment)
    }
}
