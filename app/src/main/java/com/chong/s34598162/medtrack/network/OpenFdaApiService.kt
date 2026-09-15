package com.chong.s34598162.medtrack.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the OpenFDA Drug Label API.
 *
 * Base URL: https://api.fda.gov/
 * Full endpoint: https://api.fda.gov/drug/label.json
 */
interface OpenFdaApiService {
    // Searches the FDA drug label database for a given query.
    @GET("drug/label.json")
    suspend fun searchDrug(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): OpenFdaResponse
}