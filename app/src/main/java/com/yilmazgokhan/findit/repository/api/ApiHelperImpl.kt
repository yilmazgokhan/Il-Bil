package com.yilmazgokhan.findit.repository.api

import com.yilmazgokhan.findit.data.remote.CityResponse
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {

    override suspend fun getCityBorder(osmId: Int): Response<CityResponse> =
        apiService.getCityBorder(osmId)

}