package com.yilmazgokhan.findit.kit

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.jos.JosApps
import com.huawei.hms.jos.games.AchievementsClient
import com.huawei.hms.jos.games.Games
import com.huawei.hms.jos.games.RankingsClient
import com.yilmazgokhan.findit.util.Constants.LEADERBOARD_ID


class GameService constructor(context: Context) {

    private lateinit var rankingsClient: RankingsClient
    private lateinit var achievementsClient: AchievementsClient

    init {
        LogUtils.d("$this")
    }

    /**
     * Initialize game & Leaderboard
     *
     * "Publish Game" & "Leaderboard"
     * @param activity the methods require activity
     */
    fun initialize(activity: Activity) {
        //Game Launch
        val appsClient = JosApps.getJosAppsClient(activity)
        appsClient.init()

        //Leaderboard
        rankingsClient = Games.getRankingsClient(activity)
        //Achievements
        achievementsClient = Games.getAchievementsClient(activity)
    }

    /**
     * If the leaderboard switch is set to 1, your game submits the updated score of the player.
     *
     * @param stateValue's value must set 1 for save user's score to Leaderboard
     */
    fun rankingSwitchStatus(stateValue: Int) {
        val task = rankingsClient.setRankingSwitchStatus(stateValue)
        task.addOnSuccessListener {
            // Success to set the value. The server will return the latest value.
        }
        task.addOnFailureListener { e -> // errCode information
            if (e is ApiException) {
                LogUtils.d("$this ${e.message}")
                val result = "rtnCode:" + (e as ApiException).statusCode
            }
        }
    }

    /**
     * Submit user's score to Game service
     *
     * @param score calculating on [Extensions]
     */
    fun submitScoreWithResult(score: Long) {
        val scoreTask = rankingsClient.submitScoreWithResult(LEADERBOARD_ID, score)
        scoreTask.addOnSuccessListener {
            LogUtils.d("$this")
            //success to get ScoreSubmissionInfo bean data.
        }
        scoreTask.addOnFailureListener { e ->
            LogUtils.d("$this")
            if (e is ApiException) {
                LogUtils.d("$this ${e.message}")
                val result = "rtnCode:" + (e as ApiException).statusCode
            }
        }
    }

    /**
     * Get leaderboard from Game Service
     *
     *
     * If the user does not enable Game Services, result code 7218 is returned.
     * Your game needs to actively instruct users to go to Me > Settings > Game Services
     * on AppGallery and enable Game Services, so the leaderboard feature will be available.
     */
    fun obtainLeaderBoard(
        onSuccess: ((intent: Intent) -> Unit)? = null,
        onFail: ((e: Exception) -> Unit)? = null
    ) {
        val intentTask = rankingsClient.totalRankingsIntent
        intentTask.addOnSuccessListener { intent ->
            try {
                //use startActivityForResult to launch activity via status code as 100
                onSuccess?.invoke(intent)
            } catch (e: Exception) {
                // An exception occurs during the launch of the activity.
                LogUtils.d("$this ${e.message}")
            }
        }
        intentTask.addOnFailureListener { e ->
            if (e is ApiException) {
                LogUtils.d("$this ${e.message}")
                val result = "rtnCode:" + (e as ApiException).statusCode
                onFail?.invoke(e)
            }
        }
    }

    /**
     * Get achievements from Game Service
     *
     * If the user does not enable Game Services, result code 7218 is returned.
     * Your game needs to actively instruct users to go to Me > Settings > Game Services
     * on AppGallery and enable Game Services, so the leaderboard feature will be available.
     */
    fun obtainAchievements(
        onSuccess: ((intent: Intent) -> Unit)? = null,
        onFail: ((e: Exception) -> Unit)? = null
    ) {
        val task: Task<Intent> = achievementsClient.showAchievementListIntent
        task.addOnSuccessListener { intent ->
            if (intent == null) {
                LogUtils.d("$this intent = null")
            } else {
                try {
                    //startActivityForResult(intent, 1)
                    onSuccess?.invoke(intent)
                } catch (e: java.lang.Exception) {
                    LogUtils.d("$this Achievement Activity is Invalid")
                }
            }
        }.addOnFailureListener { e ->
            if (e is ApiException) {
                val rtnCode = e.statusCode
                LogUtils.d("$this $rtnCode")
                // If result code 7204 is returned, HUAWEI AppAssistant is not available for
                // displaying the achievement list page, and then the SDK will display a message
                // indicating a service access failure. In this case, you can ignore the result code.
                if (rtnCode == 7204) {
                    LogUtils.d("$this AppAssistant does not support the display of achievements interface for some reason. You can ignore the error code")
                }
            }
        }
    }

    /**
     * Unlock achievement
     *
     * @param achievementId from [Extensions]
     */
    fun unlockAchievements(achievementId: String) {
        val task: Task<Void> = achievementsClient.reachWithResult(achievementId)
        task.addOnSuccessListener { LogUtils.i("$this reach success") }
            .addOnFailureListener { e ->
                if (e is ApiException) {
                    val result = ("rtnCode:${e.statusCode}")
                    LogUtils.e("$this reach result $result")
                }
            }
    }
}