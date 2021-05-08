package com.yilmazgokhan.findit.di

import android.content.Context
import com.yilmazgokhan.findit.kit.AccountKit
import com.yilmazgokhan.findit.kit.GameService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object KitModule {

    @ActivityScoped
    @Provides
    fun accountKitProvider(@ApplicationContext context: Context): AccountKit {
        return AccountKit(context)
    }

    @ActivityScoped
    @Provides
    fun gameServiceProvider(@ApplicationContext context: Context): GameService {
        return GameService(context)
    }
}