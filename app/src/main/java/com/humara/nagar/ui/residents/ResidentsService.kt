package com.humara.nagar.ui.residents

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.residents.model.EmptyRequestBody
import com.humara.nagar.ui.residents.model.GetResidentsRequest
import com.humara.nagar.ui.residents.model.ResidentsResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ResidentsService {
    @POST(NetworkConstants.NetworkAPIConstants.RESIDENTS)
    suspend fun getAllResidents(
        @Query(NetworkConstants.NetworkQueryConstants.PAGE) page: Int,
        @Query(NetworkConstants.NetworkQueryConstants.LIMIT) limit: Int,
        @Body request: EmptyRequestBody
    ): NetworkResponse<ResidentsResponse>

    @POST(NetworkConstants.NetworkAPIConstants.RESIDENTS)
    suspend fun searchResidentList(
        @Query(NetworkConstants.NetworkQueryConstants.SEARCH) search: String,
        @Body request: EmptyRequestBody
    ): NetworkResponse<ResidentsResponse>
}