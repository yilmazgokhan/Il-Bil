package com.yilmazgokhan.findit

import android.app.Application
import com.huawei.hms.api.HuaweiMobileServicesUtil
import com.huawei.hms.maps.MapsInitializer
import com.yilmazgokhan.findit.util.Constants.API_KEY
import dagger.hilt.android.HiltAndroidApp


/**
 * Core application class
 */
@HiltAndroidApp
class FindItApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initHuaweiMap()
        initGameService()
    }

    /**
     * Initialize Huawei Map
     */
    private fun initHuaweiMap() {
        MapsInitializer.setApiKey(API_KEY)
    }

    /**
     * Initialize Huawei Game Service
     */
    private fun initGameService() {
        HuaweiMobileServicesUtil.setApplication(this)
    }
}