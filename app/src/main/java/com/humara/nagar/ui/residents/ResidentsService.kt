package com.humara.nagar.ui.residents

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.residents.model.ResidentsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ResidentsService {
    @GET(NetworkConstants.NetworkAPIConstants.RESIDENTS)
    suspend fun getAllResidents(
        @Query(NetworkConstants.NetworkQueryConstants.PAGE) page: Int,
        @Query(NetworkConstants.NetworkQueryConstants.LIMIT) limit: Int,
        @Query(NetworkConstants.NetworkQueryConstants.SEARCH) search: String?
    ): NetworkResponse<ResidentsResponse>
}