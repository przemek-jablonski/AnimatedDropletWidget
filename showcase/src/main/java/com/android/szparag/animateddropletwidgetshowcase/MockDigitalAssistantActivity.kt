package com.android.szparag.animateddropletwidgetshowcase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_mock_digital_assistant.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

private const val ENCOURAGEMENT_TEXT_UPDATE_TIME: Seconds = 1
private const val ENCOURAGEMENT_TEXT_SWITCH_TIME: Seconds = 10
private const val ANIMATION_SIZE_SCREEN_OFFSET = 0.20F

class MockDigitalAssistantActivity : BaseMockActivity() {

  companion object IntentFactory {
    fun getStartingIntent(packageContext: Context) = Intent(packageContext, MockDigitalAssistantActivity::class.java)
  }

  override val presetString = R.string.showcase_screen_skype_snackbar_content
  private lateinit var encouragementStrings: List<String>
  private var encouragementStringIndex = 0
  private lateinit var encouragementStringDisposable: Disposable
  private val random = Random()
  private var animationVisible = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    encouragementStrings = resources.getStringArray(R.array.showcase_screen_assistant_encouragements).toMutableList()
    shiftEncouragementStringsArray(encouragementStrings.toMutableList(), random)
    setContentView(R.layout.activity_mock_digital_assistant)
    setAnimationSize(resources.displayMetrics)
    tapToSpeakButton.setOnClickListener { triggerAnimation() }
  }

  private fun setAnimationSize(displayMetrics: DisplayMetrics) {
    microphoneAnimation.changeSize(
      (max(displayMetrics.widthPixels, displayMetrics.heightPixels) * (1.00f + ANIMATION_SIZE_SCREEN_OFFSET)).toInt()
    )
    microphoneAnimation.invalidate()
  }

  private fun setupEncouragementTextSwitchTimer() {
    encouragementStringDisposable = Observable
      .interval(ENCOURAGEMENT_TEXT_UPDATE_TIME, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .map { totalSeconds -> totalSeconds.rem(ENCOURAGEMENT_TEXT_SWITCH_TIME) }
      .doOnSubscribe { encouragementTextView.text = encouragementStrings[encouragementStringIndex] }
      .doOnDispose { encouragementTextView.text = emptyString() }
      .subscribeBy { seconds ->
        if (seconds == 0L) encouragementTextSwitch() else encouragementTextAppendDot()
      }
  }

  private fun triggerAnimation() {
    if (!animationVisible) showAnimations() else hideAnimations()
    animationVisible = !animationVisible
  }

  private fun hideAnimations() {
    encouragementStringDisposable.dispose()
    microphoneAnimation fadeOutWith AccelerateInterpolator()
  }

  private fun showAnimations() {
    setupEncouragementTextSwitchTimer()
    microphoneAnimation fadeInWith DecelerateInterpolator()
  }

  private fun encouragementTextSwitch() {
    encouragementTextView.text = encouragementStrings[encouragementStringIndex]
    encouragementStringIndex++
    encouragementStringIndex = encouragementStringIndex.rem(encouragementStrings.size)
  }

  @SuppressLint("SetTextI18n")
  private fun encouragementTextAppendDot() {
    encouragementTextView.text = "${encouragementTextView.text}."
  }


  private fun shiftEncouragementStringsArray(array: List<String>, random: Random) =
    array shiftIndexesBy random.nextInt(array.size - 1) + 1
}
