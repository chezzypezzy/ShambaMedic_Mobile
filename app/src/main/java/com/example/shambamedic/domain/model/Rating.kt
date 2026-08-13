package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.RatingEntity

data class Rating(
    val ratingId: String,
    val escalationId: String,
    val ratedBy: String,
    val ratedRole: String,
    val starRating: Int,
    val comment: String?,
    val ratingTimestamp: Long,
    val syncStatus: String
)

fun RatingEntity.toDomain() = Rating(
    ratingId = ratingId,
    escalationId = escalationId,
    ratedBy = ratedBy,
    ratedRole = ratedRole,
    starRating = starRating,
    comment = comment,
    ratingTimestamp = ratingTimestamp,
    syncStatus = syncStatus
)

fun Rating.toEntity() = RatingEntity(
    ratingId = ratingId,
    escalationId = escalationId,
    ratedBy = ratedBy,
    ratedRole = ratedRole,
    starRating = starRating,
    comment = comment,
    ratingTimestamp = ratingTimestamp,
    syncStatus = syncStatus
)
