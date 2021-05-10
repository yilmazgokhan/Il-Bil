package com.yilmazgokhan.findit.util

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.yilmazgokhan.findit.R
import com.yilmazgokhan.findit.data.Game
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_10
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_20
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_30
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_40
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_50
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_60
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_70
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_80
import com.yilmazgokhan.findit.util.Constants.ACHIEVEMENTS.ACHIEVEMENT_ALL

/**
 * correct = 80
 * fail = 0
 * time = 200
 *
 * pointOfCorrect = 80 * 100 = 8000
 * pointOfFail = 0 * 50 = 0
 * basePoint = 8000 - 0 = 8000
 *
 * timeRate = 200 / 80 = 2.5
 *
 * resultScore = basePoint / timeRate
 *
 * bonus = 80 / 10 = 8 -> 8 * 55 = 440
 */
fun Game.calculateScore(): Int {
    var pointOfCorrect = 0
    if (this.correct == 0)
        return pointOfCorrect
    var pointOfFail = 0

    when {
        this.correct!! > 70 -> {
            pointOfCorrect = this.correct?.times(100)!!
            if (this.fail != null)
                pointOfFail = this.fail?.times(50)!!
        }
        this.correct!! > 50 -> {
            pointOfCorrect = this.correct?.times(80)!!
            if (this.fail != null)
                pointOfFail = this.fail?.times(40)!!
        }
        this.correct!! > 30 -> {
            pointOfCorrect = this.correct?.times(70)!!

            if (this.fail != null)
                pointOfFail = this.fail?.times(35)!!
        }
        this.correct!! > 20 -> {
            pointOfCorrect = this.correct?.times(50)!!
            if (this.fail != null)
                pointOfFail = this.fail?.times(25)!!
        }
        this.correct!! > 10 -> {
            pointOfCorrect = this.correct?.times(30)!!
            if (this.fail != null)
                pointOfFail = this.fail?.times(15)!!
        }
        else -> {
            pointOfCorrect = this.correct?.times(20)!!
            if (this.fail != null)
                pointOfFail = this.fail?.times(10)!!
        }
    }

    val basePoint = pointOfCorrect - pointOfFail
    var timeRate = 0.0
    if (this.time != 0) {
        timeRate = this.time!!.toDouble() / this.correct!!
    }

    if (timeRate == 0.0)
        timeRate = 1.0
    val resultScoreAsDouble = basePoint.toDouble() / timeRate
    val resultScore = resultScoreAsDouble.toInt()
    val bonusCount: Int = this.correct?.div(10)!!
    val score = resultScore + bonusCount
    return if (score > 0)
        score
    else 0
}

fun Int?.calculateStarCount(): Float {
    return when {
        this!! > 75 -> {
            1f
        }
        this > 60 -> {
            0.44f
        }
        this > 45 -> {
            0.33f
        }
        this > 30 -> {
            0.22f
        }
        this > 15 -> {
            0.11f
        }
        else -> 0.05f
    }
}

fun Int.achievementId(): String? {
    return when {
        this == 10 -> {
            ACHIEVEMENT_10
        }
        this == 20 -> {
            ACHIEVEMENT_20
        }
        this == 30 -> {
            ACHIEVEMENT_30
        }
        this == 40 -> {
            ACHIEVEMENT_40
        }
        this == 50 -> {
            ACHIEVEMENT_50
        }
        this == 60 -> {
            ACHIEVEMENT_60
        }
        this == 70 -> {
            ACHIEVEMENT_70
        }
        this == 80 -> {
            ACHIEVEMENT_80
        }
        this == 81 -> {
            ACHIEVEMENT_ALL
        }
        else -> null
    }
}

/**
 * Show alert dialog
 */
fun Context.showMessage(
    message: String,
    onPositive: (() -> Unit)? = null
) {
    MaterialDialog(this).show {
        cancelable(false)
        cancelOnTouchOutside(false)
        message(text = message)
        positiveButton(R.string.ok) {
            onPositive?.invoke()
        }
    }
}