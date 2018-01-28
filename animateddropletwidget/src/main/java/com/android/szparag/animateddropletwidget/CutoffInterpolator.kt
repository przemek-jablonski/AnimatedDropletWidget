package com.android.szparag.animateddropletwidget

import android.view.animation.Interpolator


/**
 * Interpolator 'wrapper'.
 *
 * Reduces amount of time needed to go from the beginning to the end of given input range.
 * Cutoff parameter value between 0f and 1f speeds up progression of input values in getInterpolation method.
 * If progression achieves maximum value (eg. 1f) and goes on, it is being clamped between inputRangeMin and
 * inputRangeMax values.
 *
 * Uses sourceInterpolator Interpolator to transform input data further.
 */
class CutoffInterpolator(
  private val sourceInterpolator: Interpolator? = null, private val inputRangeMin: Float = 0f, private val inputRangeMax: Float = 1f,
  cutoff: Float) : Interpolator {

  private val lerpLimit = (inputRangeMax - inputRangeMin) + (inputRangeMax - cutoff)

  override fun getInterpolation(input: Float): Float {
    val cutoffInput = lerp(0f, lerpLimit, input).clamp(inputRangeMax, inputRangeMin)
    return sourceInterpolator?.getInterpolation(cutoffInput) ?: cutoffInput
  }

}