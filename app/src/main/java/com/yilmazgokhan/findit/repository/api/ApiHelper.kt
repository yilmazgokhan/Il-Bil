package com.yilmazgokhan.findit.repository.api

import com.yilmazgokhan.findit.data.remote.CityResponse
import retrofit2.Response

interface ApiHelper {

    suspend fun getCityBorder(osmId: Int): Response<CityResponse>
}