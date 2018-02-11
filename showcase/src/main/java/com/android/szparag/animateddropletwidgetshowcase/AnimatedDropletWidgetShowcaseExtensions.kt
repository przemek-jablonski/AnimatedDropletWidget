package com.android.szparag.animateddropletwidgetshowcase

import android.animation.Animator
import android.animation.TimeInterpolator
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE

typealias Millis = Long
typealias Seconds = Long

infix fun <T : Any> List<T>.shiftIndexesBy(shift: Int) {
  val shiftedArray = toMutableList()
  return forEachIndexed { index, _ -> shiftedArray[index] = this[(index + shift).rem(size)] }
}

infix fun View.doOnClick(action: (View) -> Unit) =
  setOnClickListener(action)

fun View.changeSize(squareSize: Int) =
  changeSize(squareSize, squareSize)

fun View.changeSize(width: Int, height: Int) {
  val changedLayoutParams = layoutParams
  changedLayoutParams.width = width
  changedLayoutParams.height = height
  layoutParams = changedLayoutParams
}

infix fun Context.startActivity(targetActivity: Class<*>) =
  startActivity(Intent(this, targetActivity))

infix fun <T : View> T.fadeOutWith(interpolator: TimeInterpolator) {
  animate()
    .alpha(0f)
    .setInterpolator(interpolator)
    .setListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {}
      override fun onAnimationCancel(animation: Animator?) {}
      override fun onAnimationStart(animation: Animator?) {}
      override fun onAnimationEnd(animation: Animator?) {
        this@fadeOutWith.hide()
      }
    }).start()
}

infix fun <T : View> T.fadeInWith(interpolator: TimeInterpolator) {
  animate()
    .alpha(1f)
    .setInterpolator(interpolator)
    .setListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {}
      override fun onAnimationEnd(animation: Animator?) {}
      override fun onAnimationCancel(animation: Animator?) {}
      override fun onAnimationStart(animation: Animator?) {
        this@fadeInWith.alpha = 0f
        this@fadeInWith.show()
      }
    }).start()
}

fun View.hide() {
  visibility = GONE
}

fun View.show() {
  visibility = VISIBLE
}

fun Millis.toSeconds() = this / 1000
fun Millis.toMinutes() = this.toSeconds() / 60
fun Millis.toHours() = this.toMinutes() / 60

fun emptyString() = ""

