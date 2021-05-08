package com.yilmazgokhan.findit.data.remote

data class CityResponse(
    val osm_id: Int? = null,
    val centroid: Centroid? = null,
    val geometry: Geometry? = null
)