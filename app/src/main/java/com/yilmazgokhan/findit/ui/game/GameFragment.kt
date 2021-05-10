package com.yilmazgokhan.findit.ui.game

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.LogUtils
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.SupportMapFragment
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MapStyleOptions
import com.huawei.hms.maps.model.Polygon
import com.huawei.hms.maps.model.PolygonOptions
import com.yilmazgokhan.findit.R
import com.yilmazgokhan.findit.base.BaseFragment
import com.yilmazgokhan.findit.data.Game
import com.yilmazgokhan.findit.data.remote.CityResponse
import com.yilmazgokhan.findit.kit.GameService
import com.yilmazgokhan.findit.repository.Status
import com.yilmazgokhan.findit.util.achievementId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.custom_option.*
import kotlinx.android.synthetic.main.custom_popup_finish_game.view.*
import kotlinx.android.synthetic.main.custom_popup_start_game.view.*
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GameFragment : BaseFragment(R.layout.fragment_game), OnMapReadyCallback {

    @Inject
    lateinit var gameService: GameService

    //region vars
    private val viewModel: GameFragmentViewModel by viewModels()
    private var hMap: HuaweiMap? = null
    private var mSupportMapFragment: SupportMapFragment? = null
    private var currentPolygon: Polygon? = null
    private var currentPolygonList: ArrayList<Polygon>? = null
    //endregion

    override fun prepareView(savedInstanceState: Bundle?) {
        LogUtils.d("$this prepareView")
        this.initGameService()
        this.hideOptionContainer()
        this.initHuaweiMap()
        this.createStartGameDialog()
        this.initClicks()
        this.observeModel()
    }

    private fun initGameService() {
        gameService.initialize(requireActivity())
    }

    //region Custom Dialogs
    private fun createStartGameDialog() {
        val dialog = MaterialDialog(requireContext())
            .customView(R.layout.custom_popup_start_game)

        val customView = dialog.getCustomView()
        customView.btnAchievements.setOnClickListener {
            gameService.obtainAchievements(onSuccess = {
                startActivityForResult(it, 1)
            })
        }
        customView.btnLeaderBoard.setOnClickListener {
            gameService.obtainLeaderBoard(onSuccess = {
                startActivityForResult(it, 100)
            },
                onFail = {})
        }
        customView.btnStart.setOnClickListener {
            showOptionContainer()
            viewModel.prepareGame()
            viewModel.setTimer()
            dialog.cancel()
        }
        dialog.cancelable(false)
        dialog.show()
    }

    private fun showFinishGameDialog(game: Game) {
        val dialog = MaterialDialog(requireContext())
            .customView(R.layout.custom_popup_finish_game)
        val customView = dialog.getCustomView()
        customView.tv_correct.text = game.correct.toString()
        customView.tv_score.text = game.score.toString()
        customView.tv_time.text = game.time.toString()
        customView.btnContinue.setOnClickListener {
            dialog.cancel()
            restartGame()
        }

        //set 50% animation
        customView.starAnimationView.setMinAndMaxProgress(0.0f, game.starCount)

        dialog.cancelable(false)
        dialog.show()

        // TODO: 21.04.2021
        try {
            clock_animation.stop()
        } catch (e: Exception) {
            LogUtils.d("$this ${e.message}")
        }

        viewConfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(12))
            .setPosition(-50f, viewConfetti.width + 50f, -50f, -50f)
            .streamFor(300, 5000L)
    }
    //endregion

    private fun restartGame() {
        viewModel.clear()
        createStartGameDialog()
        hMap?.clear()
        currentPolygonList = null
        currentPolygon = null

        first.visibility = View.VISIBLE
        second.visibility = View.VISIBLE
        third.visibility = View.VISIBLE
    }

    //region Huawei Map
    /**
     * Initialize the Huawei Map
     */
    private fun initHuaweiMap() {
        mSupportMapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_container_view_map) as SupportMapFragment?
        mSupportMapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: HuaweiMap?) {
        LogUtils.d("onMapReady")
        hMap = map
        hMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    38.95029428263489, 35.56672633853692
                ), 6f
            )
        )
        hMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        hMap?.uiSettings?.isZoomControlsEnabled = false // Disable zoom-in zoom-out buttons
    }
    //endregion

    /**
     * Initialize buttons clicks listeners
     */
    private fun initClicks() {
        option1.setOnClickListener { viewModel.itemSelected(option1.text.toString()) }
        option2.setOnClickListener { viewModel.itemSelected(option2.text.toString()) }
        option3.setOnClickListener { viewModel.itemSelected(option3.text.toString()) }
        option4.setOnClickListener { viewModel.itemSelected(option4.text.toString()) }
        btnOptionHide.setOnClickListener { hideOptionContainer() }
        btnOptionSHow.setOnClickListener { showOptionContainer() }
    }

    /**
     * Initialize observers
     */
    private fun observeModel() {
        viewModel.cityInfo.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data.let { res ->
                        LogUtils.d("$this SUCCESS")
                        if (it.data?.geometry?.type == ("Polygon"))
                            drawPolygon(it.data)
                        else
                            drawMultiPolygon(it.data)
                    }
                    hideProgressDialog()
                }
                Status.LOADING -> {
                    LogUtils.d("$this LOADING")
                    showProgressDialog()
                }
                Status.ERROR -> {
                    LogUtils.d("$this ERROR")
                    hideProgressDialog()
                }
            }
        })
        viewModel.round.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data?.isCorrect == true)
                        correctAnswer()
                    else wrongAnswer(it.data?.name, it.data?.lifeCount)
                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                }
            }
        })
        viewModel.options.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { it1 -> prepareButtons(it1) }
                }
                Status.LOADING -> {
                }
                Status.ERROR -> {
                }
            }
        })
        viewModel.finishGame.observe(viewLifecycleOwner, Observer {
            gameService.rankingSwitchStatus(1)
            gameService.submitScoreWithResult(it.score?.toLong() ?: 0)
            showFinishGameDialog(it)
            hideOptionContainer()
        })
        viewModel.timer.observe(viewLifecycleOwner, {
            alphaBetView.setText(it.toString())
            if (it.toString() == "1")
                try {
                    clock_animation.animateIndeterminate()
                } catch (e: Exception) {
                    LogUtils.d("$this ${e.message}")
                }
        })
        viewModel.correctAnswer.observe(viewLifecycleOwner, {
            val achievementId = it.achievementId()
            if (achievementId != null)
                gameService.unlockAchievements(achievementId)
        })
    }

    /**
     * Draw country's border
     */
    private fun drawPolygon(response: CityResponse?) {
        val coordinates = response?.geometry?.coordinates as List<List<List<Double>>>
        val borders = coordinates[0].map { it }.map {
            LatLng(
                it[1],
                it[0]
            )
        }
        currentPolygon = hMap?.addPolygon(
            PolygonOptions()
                .addAll(borders)
                .strokeWidth(1f)
                .fillColor(ColorUtils.getColor(R.color.colorQuestion))
        )
        currentPolygon?.isClickable = true

        hMap?.setOnPolylineClickListener { polyline1 ->
            Toast.makeText(
                requireContext(),
                "${response.osm_id}",
                Toast.LENGTH_SHORT
            ).show()
        }


        hMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    response.centroid?.coordinates?.get(1) ?: 0.0,
                    response.centroid?.coordinates?.get(0) ?: 0.0,
                ), hMap?.cameraPosition!!.zoom
            )
        )
    }

    /**
     * Draw country's multi polygon
     */
    private fun drawMultiPolygon(response: CityResponse?) {
        val coordinates = response?.geometry?.coordinates as List<List<List<List<Double>>>>?
        val size = coordinates?.size ?: 0
        currentPolygonList = ArrayList()
        for (i in 0 until size) {
            val sizeInner = coordinates?.get(i)?.size ?: 0
            for (k in 0 until sizeInner) {
                val borders = coordinates?.get(i)?.get(k)?.map { it }?.map {
                    LatLng(
                        it[1],
                        it[0]
                    )
                }
                val polygon = hMap?.addPolygon(
                    PolygonOptions()
                        .addAll(borders)
                        .strokeWidth(1f)
                        .fillColor(ColorUtils.getColor(R.color.colorQuestion))
                )
                if (polygon != null) {
                    currentPolygonList?.add(polygon)
                }
            }
        }

        hMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    response?.centroid?.coordinates?.get(1) ?: 0.0,
                    response?.centroid?.coordinates?.get(0) ?: 0.0,
                ), hMap?.cameraPosition!!.zoom
            )
        )
    }

    private fun prepareButtons(cityList: List<String>) {
        option1.text = cityList[0]
        option2.text = cityList[1]
        option3.text = cityList[2]
        option4.text = cityList[3]
    }

    //region polygon color
    private fun correctAnswer() {
        if (currentPolygon != null) {
            currentPolygon?.fillColor = ColorUtils.getColor(R.color.colorCorrect)
            currentPolygon = null
        } else if (currentPolygonList != null) {
            for (i in 0 until currentPolygonList!!.size)
                currentPolygonList!![i].fillColor = ColorUtils.getColor(R.color.colorCorrect)
            currentPolygonList = null
        }
    }

    private fun wrongAnswer(cityName: String?, count: Int?) {
        if (currentPolygon != null) {
            currentPolygon?.fillColor = ColorUtils.getColor(R.color.colorWrong)
            currentPolygon?.strokeColor = ColorUtils.getRandomColor()
            currentPolygon = null
        } else if (currentPolygonList != null) {
            for (i in 0 until currentPolygonList!!.size) {
                currentPolygonList!![i].fillColor = ColorUtils.getColor(R.color.colorWrong)
                currentPolygonList!![i].strokeColor = ColorUtils.getRandomColor()
            }
            currentPolygonList = null
        }

        when (count) {
            3 ->
                first.visibility = View.GONE
            2 ->
                second.visibility = View.GONE
            1 ->
                third.visibility = View.GONE
        }
    }
    //endregion

    //region container's animations
    private fun hideOptionContainer() {
        val transitionContainer: Transition = Slide(Gravity.BOTTOM)
        transitionContainer.duration = 600
        transitionContainer.addTarget(customOption)
        TransitionManager.beginDelayedTransition(rlContainer, transitionContainer)
        customOption.visibility = View.GONE
        btnOptionSHow.visibility = View.VISIBLE
    }

    private fun showOptionContainer() {
        val transitionContainer: Transition = Slide(Gravity.BOTTOM)
        transitionContainer.duration = 600
        transitionContainer.addTarget(customOption)
        TransitionManager.beginDelayedTransition(rlContainer, transitionContainer)
        customOption.visibility = View.VISIBLE
        btnOptionSHow.visibility = View.GONE
    }
    //endregion
}