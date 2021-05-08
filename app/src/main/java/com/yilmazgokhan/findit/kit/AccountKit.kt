package com.yilmazgokhan.findit.kit

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.hwid.HuaweiIdAuthManager


class AccountKit constructor(context: Context) {

    companion object {
        const val HUAWEI_ID_LOGIN_CODE = 8888
    }

    private var service: AccountAuthService? = null
    private var authParams: AccountAuthParams? = null

    init {
        authParams =
            AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).setAuthorizationCode()
                .createParams()
        AGConnectAuth.getInstance()
    }

    /**
     * Prepare AccountAuthService object
     */
    fun prepare(activity: Activity) {
        service = AccountAuthManager.getService(activity, authParams)
    }

    /**
     * Get Sign In intent from Account Kit
     */
    fun getSignInIntent(): Intent? {
        return service?.signInIntent
    }

    /**
     * Login with Huawei ID
     */
    fun loginWithHuaweiId(
        requestCode: Int, data: Intent?,
        onSuccess: (() -> Unit)? = null,
        onFail: ((e: Exception) -> Unit)? = null
    ) {
        if (requestCode == HUAWEI_ID_LOGIN_CODE) {
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                // The sign-in is successful, and the user's ID information and authorization code are obtained.
                val authAccount = authAccountTask.result
                LogUtils.d("$this serverAuthCode:" + authAccount.authorizationCode)
                onSuccess?.invoke()
            } else {
                LogUtils.d("$this " + (authAccountTask.exception as ApiException).statusCode)
                onFail?.invoke(authAccountTask.exception)
            }
        } else {
            // The sign-in failed.
            LogUtils.e(HuaweiIdAuthManager.parseAuthResultFromIntent(data).exception)
            onFail?.invoke(HuaweiIdAuthManager.parseAuthResultFromIntent(data).exception)
        }
    }

    /**
     * Silent Sign In
     */
    fun silentSignIn(
        activity: Activity,
        onSuccess: (() -> Unit)? = null,
        onFail: ((e: Exception) -> Unit)? = null
    ) {
        val authParams: AccountAuthParams =
            AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams()
        val service: AccountAuthService = AccountAuthManager.getService(activity, authParams)
        val task: Task<AuthAccount> = service.silentSignIn()
        task.addOnSuccessListener {
            // Obtain the user's ID information.
            //Log.i(TAG, "displayName:" + authAccount.displayName)
            // Obtain the ID type (0: HUAWEI ID; 1: AppTouch ID).
            //Log.i(TAG, "accountFlag:" + authAccount.accountFlag);
            onSuccess?.invoke()
        }
        task.addOnFailureListener { e -> // The sign-in failed. Try to sign in explicitly using getSignInIntent().
            if (e is ApiException) {
                val apiException = e as ApiException
                //Log.i(TAG, "sign failed status:" + apiException.statusCode)
                onFail?.invoke(apiException)
            }
        }
    }
}