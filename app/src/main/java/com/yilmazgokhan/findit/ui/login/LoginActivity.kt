package com.yilmazgokhan.findit.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.yilmazgokhan.findit.R
import com.yilmazgokhan.findit.base.BaseActivity
import com.yilmazgokhan.findit.kit.AccountKit
import com.yilmazgokhan.findit.kit.AccountKit.Companion.HUAWEI_ID_LOGIN_CODE
import com.yilmazgokhan.findit.ui.main.MainActivity
import com.yilmazgokhan.findit.util.showMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    @Inject
    lateinit var accountKit: AccountKit

    //region vars
    private val viewModel: LoginViewModel by viewModels()
    //endregion

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        LogUtils.d("$this prepareView")
        setClicks()
    }

    /**
     * Initialize click listeners
     */
    private fun setClicks() {
        huaweiSignInButton.setOnClickListener {
            accountKit.prepare(this@LoginActivity)
            startActivityForResult(accountKit.getSignInIntent(), HUAWEI_ID_LOGIN_CODE)
            showProgressDialog()
        }
        tv_privacy.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(StringUtils.getString(R.string.privacy_policy_link))
            )
            startActivity(browserIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Process the authorization result and obtain the authorization code from AuthAccount.
        super.onActivityResult(requestCode, resultCode, data)
        accountKit.loginWithHuaweiId(requestCode, data,
            onSuccess = {
                LogUtils.d("$this onSuccess")
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            },
            onFail = {
                LogUtils.d("$this onFail")
                hideProgressDialog()
                it.localizedMessage?.let { message -> showMessage(message) }
            })
    }
}