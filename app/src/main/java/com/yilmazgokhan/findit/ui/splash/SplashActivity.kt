package com.yilmazgokhan.findit.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.yilmazgokhan.findit.R
import com.yilmazgokhan.findit.base.BaseActivity
import com.yilmazgokhan.findit.data.Game
import com.yilmazgokhan.findit.kit.AccountKit
import com.yilmazgokhan.findit.ui.login.LoginActivity
import com.yilmazgokhan.findit.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    @Inject
    lateinit var accountKit: AccountKit

    //region vars
    private val viewModel: SplashActivityViewModel by viewModels()
    //endregion

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    private lateinit var game: Game

    override fun prepareView(savedInstanceState: Bundle?) {
        LogUtils.d("$this prepareView")
        checkHaveUser()
    }

    /**
     * Check user already login
     *
     * If user already login go to [MainActivity]
     * else go to [LoginActivity]
     */
    private fun checkHaveUser() {
        lifecycleScope.launch {
            //delay(2000)
            delay(500)
            accountKit.silentSignIn(this@SplashActivity,
                onSuccess = {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                },
                onFail = {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}
