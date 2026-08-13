package com.example.shambamedic.data.remote.api

import com.example.shambamedic.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ShambaMedicApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequestDto): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequestDto): AuthResponseDto

    @POST("api/scans")
    suspend fun uploadScan(@Body scan: ScanDto): ScanDto

    @GET("api/treatments/updates")
    suspend fun getTreatmentUpdates(): List<TreatmentDto>

    @GET("api/health")
    suspend fun healthCheck(): Map<String, String>

    @POST("api/sync/scans")
    suspend fun syncScan(
        @Header("X-API-Key") apiKey: String,
        @Body scan: ScanSyncDto
    ): ScanSyncDto

    @POST("api/sync/escalations")
    suspend fun syncEscalation(
        @Header("X-API-Key") apiKey: String,
        @Body escalation: EscalationSyncDto
    ): EscalationSyncDto

    @POST("api/sync/ratings")
    suspend fun syncRating(
        @Header("X-API-Key") apiKey: String,
        @Body rating: RatingSyncDto
    ): RatingSyncDto

    @GET("api/sync/escalations")
    suspend fun getResolvedEscalations(
        @Header("X-API-Key") apiKey: String,
        @Query("farmer_ref") farmerRef: String,
        @Query("status") status: String = "resolved"
    ): List<ResolvedEscalationDto>
}
