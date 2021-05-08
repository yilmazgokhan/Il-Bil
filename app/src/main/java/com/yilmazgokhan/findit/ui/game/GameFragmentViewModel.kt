package com.yilmazgokhan.findit.ui.game

import android.os.Handler
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ResourceUtils
import com.google.gson.Gson
import com.yilmazgokhan.findit.R
import com.yilmazgokhan.findit.base.BaseViewModel
import com.yilmazgokhan.findit.data.Game
import com.yilmazgokhan.findit.data.Round
import com.yilmazgokhan.findit.data.local.Cities
import com.yilmazgokhan.findit.data.local.City
import com.yilmazgokhan.findit.data.remote.CityResponse
import com.yilmazgokhan.findit.di.qualifier.IoDispatcher
import com.yilmazgokhan.findit.repository.MainRepository
import com.yilmazgokhan.findit.repository.Resource
import com.yilmazgokhan.findit.util.calculateScore
import com.yilmazgokhan.findit.util.calculateStarCount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * View Model class for [GameFragment]
 */
@ExperimentalCoroutinesApi
class GameFragmentViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val mainRepository: MainRepository
) : BaseViewModel() {

    //region city info
    private val _cityInfo = MutableLiveData<Resource<CityResponse>>()
    val cityInfo: LiveData<Resource<CityResponse>>
        get() = _cityInfo
    //endregion

    //region game info
    private val _round = MutableLiveData<Resource<Round>>()
    val round: LiveData<Resource<Round>>
        get() = _round

    private val _options = MutableLiveData<Resource<List<String>>>()
    val options: LiveData<Resource<List<String>>>
        get() = _options

    private val _finishGame = MutableLiveData<Game>()
    val finishGame: LiveData<Game>
        get() = _finishGame

    private val _timer = MutableLiveData<Int>()
    val timer: LiveData<Int>
        get() = _timer

    private val _correctAnswer = MutableLiveData<Int>()
    val correctAnswer: LiveData<Int>
        get() = _correctAnswer
    //endregion

    //region game list & data
    private var cityListFull = mutableListOf<City>()
    private lateinit var cityList: ArrayList<City>

    private var game: Game
    private lateinit var city: String
    private var count = 3
    private var correctAnswerCount: Int = 0
    //endregion

    //region timer
    private var mHandler: Handler
    private lateinit var mRunnable: Runnable
    //endregion

    init {
        LogUtils.d("$this initialize")
        prepareCities()
        game = Game(0, 0, 0, 0, 0f)
        mHandler = Handler()
    }

    //region Parse JSON
    private fun prepareCities() {
        // TODO: 15.04.2021 When release change from [cities_debug] to [cities.json]
        //val inputStream = ResourceUtils.readRaw2String(R.raw.cities_debug)
        val inputStream = ResourceUtils.readRaw2String(R.raw.cities)
        val cities: Cities =
            Gson().fromJson(inputStream, Cities::class.java)
        cityList = cities.city!!
        cityListFull.addAll(cityList)
    }
    //endregion

    fun clear() {
        cityList.clear()
        cityList.addAll(cityListFull)
        city = ""
        count = 3
        correctAnswerCount = 0
        //Game obj.
        game.correct = 0
        game.time = 0
        game.score = 0
        game.fail = 0
    }

    fun setTimer() {
        mRunnable = Runnable {
            game.time = game.time?.plus(1)
            _timer.postValue(game.time)

            // Schedule the task to repeat after 1 second
            mHandler.postDelayed(mRunnable, 1000)
        }
        mRunnable.run()
    }

    fun prepareGame() {
        val size = cityList.size.minus(1)
        if (size.plus(1) < 1) {
            finishTheGame()
            return
        }
        val index = (0..size).random()
        getCity(cityList[index].osm_id)
        city = cityList[index].name
        cityList.removeAt(index)
        prepareOptions()
    }

    /**
     * Send HTTP Request for get country info
     */
    private fun getCity(osmId: Int) {
        viewModelScope.launch {
            //_cityInfo.postValue(Resource.loading(null))
            mainRepository.getCityBorder(osmId).let {
                if (it.isSuccessful) {
                    _cityInfo.postValue(Resource.success(it.body()))
                } else
                    _cityInfo.postValue(Resource.error(it.errorBody().toString(), null))
            }
        }
    }

    private fun prepareOptions() {
        val list: ArrayList<String> = ArrayList()
        list.add(city)
        val randomCount = cityListFull.size.minus(1)
        for (i in 0 until 3) {
            val index = (0..randomCount).random()
            list.add(cityListFull[index].name)
        }
        _options.postValue(Resource.success(list.shuffled()))
    }

    fun itemSelected(answer: String) {
        viewModelScope.launch {
            val round = Round(null, null, null)
            round.isCorrect = answer == city
            round.name = city
            if (answer == city) {
                round.lifeCount = count
                game.correct = game.correct?.plus(1)
                //Correct answer count for Huawei Achievements
                correctAnswerCount = correctAnswerCount.plus(1)
                _correctAnswer.postValue(correctAnswerCount)
            } else {
                count = count.minus(1)
                game.fail = game.fail?.plus(1)
                if (count == 0) {
                    finishTheGame()
                } else
                    round.lifeCount = count
            }
            _round.postValue(Resource.success(round))
        }
        this.prepareGame()
    }

    private fun finishTheGame() {
        LogUtils.d("$this")
        viewModelScope.launch {
            mHandler.removeCallbacks(mRunnable);
            game.score = game.calculateScore()
            game.starCount = game.correct.calculateStarCount()
            _finishGame.postValue(game)
        }
    }
}