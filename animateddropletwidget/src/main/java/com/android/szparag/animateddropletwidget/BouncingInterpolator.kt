package com.android.szparag.animateddropletwidget

import android.view.animation.Interpolator

/**
 * An interpolator where the change bounces at the end.
 *
 * (Almost) blatant ripoff of Google's BounceInterpolator, but I needed to be able to specify 'bounciness' factor.
 *
 * Now bounce ( ͡° ͜ʖ ͡°).
 */
private const val STANDARD_BOUNCINESS_FACTOR = 8.0f

class BouncingInterpolator(
    private val bouncinessFactor: Float = STANDARD_BOUNCINESS_FACTOR,
    private val bouncinessInputMultiplier: Float = 1.1226f
) : Interpolator {

  private val standardToCustomBoucinessFactorRatio = bouncinessFactor / STANDARD_BOUNCINESS_FACTOR

  override fun getInterpolation(input: Float): Float {
    val t = input * bouncinessInputMultiplier
    return when {
      t < 0.3535f -> bounce(t)
      t < 0.7408f -> bounce(t - 0.54719f * standardToCustomBoucinessFactorRatio) + (0.7f * standardToCustomBoucinessFactorRatio)
      t < 0.9644f -> bounce(t - 0.8526f * standardToCustomBoucinessFactorRatio) + (0.9f * standardToCustomBoucinessFactorRatio)
      else -> bounce(t - 1.0435f * standardToCustomBoucinessFactorRatio) + (0.95f * standardToCustomBoucinessFactorRatio)
    }
  }

  private fun bounce(t: Float): Float = t * t * bouncinessFactor

}