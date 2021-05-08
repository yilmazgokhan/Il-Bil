package com.yilmazgokhan.findit.data

data class Game(
    var correct: Int? = null,
    var fail: Int? = null,
    var time: Int? = null,
    var score: Int? = null,
    var starCount: Float
)