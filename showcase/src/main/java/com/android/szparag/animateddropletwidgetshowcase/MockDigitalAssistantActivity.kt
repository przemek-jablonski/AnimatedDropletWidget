package com.android.szparag.animateddropletwidgetshowcase

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_mock_digital_assistant.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

typealias Seconds = Long

private const val ENCOURAGEMENT_TEXT_SWITCH_TIME: Seconds = 10

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
    val animationLayoutParams = microphoneAnimation.layoutParams
    val maxDimen = max(displayMetrics.widthPixels, displayMetrics.heightPixels) * 1.15f
    animationLayoutParams.width = maxDimen.toInt()
    animationLayoutParams.height = maxDimen.toInt()
    microphoneAnimation.layoutParams = animationLayoutParams
    microphoneAnimation.invalidate()
  }

  private fun setupEncouragementTextSwitchTimer() {
    encouragementStringDisposable = Observable
      .interval(1, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .map { totalSeconds -> totalSeconds.rem(ENCOURAGEMENT_TEXT_SWITCH_TIME) }
      .doOnSubscribe { encouragementTextView.text = encouragementStrings[encouragementStringIndex] }
      .doOnDispose { encouragementTextView.text = "" }
      .subscribeBy { seconds ->
      if (seconds == 0L) {
        encouragementTextSwitch()
      } else {
        encouragementTextAppendDot()
      }
    }
  }

  private fun triggerAnimation() {
    if (animationVisible) hideAnimations()
    else showAnimations()

    animationVisible = !animationVisible
  }

  private fun hideAnimations() {
    encouragementStringDisposable.dispose()
    microphoneAnimation.animate().alpha(0f).setListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {}
      override fun onAnimationEnd(animation: Animator?) {
        microphoneAnimation.visibility = View.INVISIBLE
      }

      override fun onAnimationCancel(animation: Animator?) {}
      override fun onAnimationStart(animation: Animator?) {}
    }).start()
  }

  private fun showAnimations() {
    setupEncouragementTextSwitchTimer()
    microphoneAnimation.animate().alpha(1f).setListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {}
      override fun onAnimationEnd(animation: Animator?) {}
      override fun onAnimationCancel(animation: Animator?) {}
      override fun onAnimationStart(animation: Animator?) {
        microphoneAnimation.alpha = 0f
        microphoneAnimation.visibility = View.VISIBLE
      }
    }).start()
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


  private fun shiftEncouragementStringsArray(array: List<String>, random: Random) {
    val shift = random.nextInt(array.size - 1) + 1
    val shiftedArray = array.toMutableList()
    for (i in 0 until array.size) {
      shiftedArray[i] = array[(i + shift).rem(array.size)]
    }
  }
}
