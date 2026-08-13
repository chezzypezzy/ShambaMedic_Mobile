package com.example.shambamedic.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request/response body for POST /api/sync/scans. Backend is idempotent on scan_id:
 * 201 on first sync, 200 with identical data on retry.
 */
data class ScanSyncDto(
    @SerializedName("scan_id") val scanId: String,
    @SerializedName("farmer_ref") val farmerRef: String,
    @SerializedName("farmer_name") val farmerName: String?,
    @SerializedName("farmer_phone") val farmerPhone: String?,
    @SerializedName("disease_id") val diseaseId: String?,
    @SerializedName("crop_type") val cropType: String,
    @SerializedName("confidence_score") val confidenceScore: Float,
    // "low" | "moderate" | "severe" only - the backend 422s on anything else.
    @SerializedName("severity") val severity: String,
    @SerializedName("scan_timestamp") val scanTimestamp: Long,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

/**
 * Request/response body for POST /api/sync/escalations. The backend does not accept
 * (or return) a status field here - it is structurally absent on its side.
 */
data class EscalationSyncDto(
    @SerializedName("escalation_id") val escalationId: String,
    @SerializedName("scan_id") val scanId: String,
    @SerializedName("escalated_timestamp") val escalatedTimestamp: Long
)

/**
 * Request/response body for POST /api/sync/ratings. No rated_role field - the backend
 * forces "farmer" server-side regardless of what's sent.
 */
data class RatingSyncDto(
    @SerializedName("rating_id") val ratingId: String,
    @SerializedName("escalation_id") val escalationId: String,
    @SerializedName("star_rating") val starRating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("rating_timestamp") val ratingTimestamp: Long,
    @SerializedName("rated_by") val ratedBy: String
)

/**
 * Response element for GET /api/sync/escalations. The backend also returns botanist_id
 * and a nested scan object; not modeled here since nothing in the app consumes them yet.
 */
data class ResolvedEscalationDto(
    @SerializedName("escalation_id") val escalationId: String,
    @SerializedName("scan_id") val scanId: String,
    @SerializedName("botanist_diagnosis") val botanistDiagnosis: String?,
    @SerializedName("status") val status: String,
    @SerializedName("escalated_timestamp") val escalatedTimestamp: Long,
    @SerializedName("resolved_timestamp") val resolvedTimestamp: Long?
)
