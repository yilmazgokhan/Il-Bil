package com.yilmazgokhan.findit.repository.api

import com.yilmazgokhan.findit.data.remote.CityResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("details.php?osmtype=R&format=json&polygon_geojson=1")
    suspend fun getCityBorder(@Query("osmid") osmId: Int): Response<CityResponse>
}