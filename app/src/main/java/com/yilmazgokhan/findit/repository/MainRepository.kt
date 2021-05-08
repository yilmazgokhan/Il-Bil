package com.yilmazgokhan.findit.repository

import com.yilmazgokhan.findit.repository.api.ApiHelper
import javax.inject.Inject

class MainRepository @Inject constructor(private val apiHelper: ApiHelper) {

    suspend fun getCityBorder(osmId: Int) = apiHelper.getCityBorder(osmId)
}