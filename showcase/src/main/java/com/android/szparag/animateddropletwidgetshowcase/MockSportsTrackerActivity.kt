package com.android.szparag.animateddropletwidgetshowcase

import android.annotation.SuppressLint
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_mock_sports_tracker.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class MockSportsTrackerActivity : BaseMockActivity() {

  override val presetString = R.string.showcase_screen_skype_snackbar_content
  private val random = Random()
  private var stopwatchTimerDisposable: Disposable? = null
  private var caloriesTimerDisposable: Disposable? = null
  private var milesTimerDisposable: Disposable? = null
  private var caloriesCount = 0f
  private var milesCount = 0f

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_mock_sports_tracker)
    startStopwatchTimer()
    startCaloriesTimer()
    startMilesTimer()
  }

  override fun onDestroy() {
    super.onDestroy()
    stopwatchTimerDisposable?.dispose()
    caloriesTimerDisposable?.dispose()
    milesTimerDisposable?.dispose()
  }

  private fun startStopwatchTimer() {
    stopwatchTimerDisposable =
        Flowable
          .interval(1, TimeUnit.MILLISECONDS)
          .onBackpressureDrop()
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe { setStopwatchText() }
          .subscribe { milliseconds -> setStopwatchText(milliseconds) }
  }

  //average calorie burn: 260cal for 38 min fast-pace jogging (equivalent of relaxed cycling)
  //6.85cal per minute
  //0.60cal per 5 seconds (sampling interval)
  //rounding to 1.00cal
  private fun startCaloriesTimer() {
    caloriesTimerDisposable =
        Observable
          .interval(5, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe { setCaloriesText() }
          .subscribe {
            caloriesCount += (max(0.00, random.nextGaussian()) * 1f).toFloat()
            setCaloriesText(caloriesCount)
          }
  }

  //average city cycling speed: around 10mph
  //0.167 miles per minute
  //~0.0025 miles per 1 second (sampling interval)
  private fun startMilesTimer() {
    milesTimerDisposable =
        Observable
          .interval(1, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe { setMilesText() }
          .subscribe {
            milesCount += (max(0.00, random.nextGaussian()) * 0.0025f).toFloat()
            setMilesText(milesCount)
          }
  }

  @SuppressLint("SetTextI18n")
  private fun setStopwatchText(millisecondsElapsed: Long = 0L) {
    val millis = millisecondsElapsed.rem(1000)
    val seconds = (millisecondsElapsed / 1000).rem(60)
    val minutes = (millisecondsElapsed / 1000 / 60).rem(60)
    val hours = (millisecondsElapsed / 1000 / 60 / 60).rem(12)
    timeContentTextView.text = "${String.format("%01d", hours)}:${String.format("%02d", minutes)}" +
        ":${String.format("%02d", seconds)}.${String.format("%03d", millis)}\n"
  }

  @SuppressLint("SetTextI18n")
  private fun setCaloriesText(calories: Float = 0.00f) {
    caloriesContentTextView.text = String.format("%.2f", calories)
  }

  @SuppressLint("SetTextI18n")
  private fun setMilesText(miles: Float = 0.00f) {
    milesContentTextView.text = String.format("%.3f", miles)
  }

}
