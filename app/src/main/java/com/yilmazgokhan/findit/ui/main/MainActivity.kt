package com.yilmazgokhan.findit.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import com.yilmazgokhan.findit.R
import com.yilmazgokhan.findit.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    //region vars
    private val viewModel: MainViewModel by viewModels()
    //endregion

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        //There is no require action.
    }
}