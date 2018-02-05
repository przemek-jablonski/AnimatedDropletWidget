package com.android.szparag.animateddropletwidget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.annotation.RequiresApi
import android.support.annotation.StyleableRes
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationSet
import android.widget.ImageView
import com.android.szparag.animateddropletwidget.AnimatedDropletWidget.ColourTransition
import com.android.szparag.animateddropletwidget.AnimatedDropletWidget.WidgetPreset
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

typealias Widget = View
typealias Width = Int
typealias Height = Int
typealias Dp = Int
typealias Px = Int

private val animationListenerCallbackStub: (Animation?) -> Unit = {}

/**
 * Draws random Float between min and max range (both are inclusive!).
 * @param min lowest bound of randomization, inclusive. Floats will not be drawn below that internal value.
 * @param max highest bound of randomization, inclusive. Floats will not be drawn above that internal value.
 *
 * @return random Float between specified input range.
 */
internal fun Random.nextFloat(min: Float, max: Float) = nextFloat() * (max - min) + min

/**
 * Draws random Double between min and max range (both are inclusive!).
 * @param min lowest bound of randomization, inclusive. Doubles will not be drawn below that internal value.
 * @param max highest bound of randomization, inclusive. Doubles will not be drawn above that internal value.
 *
 * @return random Double between specified input range.
 */
internal fun Random.nextDouble(min: Double, max: Double) = nextDouble() * (max - min) + min

/**
 * Draws random Int between min and max range (both are inclusive!).
 * @param min lowest bound of randomization, inclusive. Ints will not be drawn below that internal value.
 * @param max highest bound of randomization, inclusive. Ints will not be drawn above that internal value.
 *
 * @return random Int between specified input range.
 */
internal fun Random.nextInt(min: Int, max: Int) = (nextFloat() * (max - min) + min).roundToInt()

/**
 * Draws random Long between min and max range (both are inclusive!).
 * @param min lowest bound of randomization, inclusive. Longs will not be drawn below that internal value.
 * @param max highest bound of randomization, inclusive. Longs will not be drawn above that internal value.
 *
 * @return random Long between specified input range.
 */
internal fun Random.nextLong(min: Long, max: Long) = (nextFloat() * (max - min) + min).roundToLong()

/**
 * Introduces random variation of a given number by a given factor.
 * Factor is understood by being both highest and lowest bound for this draw.
 *
 * eg. 10f.randomVariation(random, 1f) will generate random number in inclusive range between <(10f-1f);(10f+1f)>
 *
 * @receiver input / output. Base of the draw range.
 * @param random pseudo-random number generator
 * @see Random
 * @param factor upper and lower bound of drawing
 *
 */
internal fun Float.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextFloat(this - this * factor, this + this * factor) else this

/**
 * Introduces random variation of a given number by a given factor.
 * Factor is understood by being both highest and lowest bound for this draw.
 *
 * eg. 10f.randomVariation(random, 1f) will generate random number in inclusive range between <(10f-1f);(10f+1f)>
 *
 * @receiver input / output. Base of the draw range.
 * @param random pseudo-random number generator
 * @see Random
 * @param factor upper and lower bound of drawing
 *
 */
internal fun Double.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextDouble(this - this * factor, this + this * factor) else this

/**
 * Introduces random variation of a given number by a given factor.
 * Factor is understood by being both highest and lowest bound for this draw.
 *
 * eg. 10f.randomVariation(random, 1f) will generate random number in inclusive range between <(10f-1f);(10f+1f)>
 *
 * @receiver input / output. Base of the draw range.
 * @param random pseudo-random number generator
 * @see Random
 * @param factor upper and lower bound of drawing
 *
 */
internal fun Int.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextInt((this - this * factor).toInt(), (this + this * factor).toInt()) else this

/**
 * Introduces random variation of a given number by a given factor.
 * Factor is understood by being both highest and lowest bound for this draw.
 *
 * eg. 10f.randomVariation(random, 1f) will generate random number in inclusive range between <(10f-1f);(10f+1f)>
 *
 * @receiver input / output. Base of the draw range.
 * @param random pseudo-random number generator
 * @see Random
 * @param factor upper and lower bound of drawing
 *
 */
internal fun Long.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextLong((this - this * factor).toLong(), (this + this * factor).toLong()) else this

/**
 * Clamps internal value between range between min and max input parameters.
 * Range is inclusive from both sides.
 */
internal fun Float.clamp(min: Float, max: Float) = coerceAtLeast(min).coerceAtMost(max)

/**
 * Clamps internal value between range between min and max input parameters.
 * Range is inclusive from both sides.
 */
internal fun Double.clamp(min: Double, max: Double) = coerceAtLeast(min).coerceAtMost(max)

/**
 * Clamps internal value between range between min and max input parameters.
 * Range is inclusive from both sides.
 */
internal fun Int.clamp(min: Int, max: Int) = coerceAtLeast(min).coerceAtMost(max)

/**
 * Clamps internal value between range between min and max input parameters.
 * Range is inclusive from both sides.
 */
internal fun Long.clamp(min: Long, max: Long) = coerceAtLeast(min).coerceAtMost(max)

/**
 * Lerp - Linear Interpolation
 * Produces linearly interpolated internal value between first and second parameter by factor of factor.
 *
 * @param first Lower range of interpolation.
 * @param second Upper range of interpolation.
 * @param factor Interpolation factor.
 * @return internal value of linear interpolation.
 */
internal fun lerp(first: Float, second: Float, factor: Float) = first + factor * (second - first)

/**
 * Lerp - Linear Interpolation
 * Produces linearly interpolated internal value between first and second parameter by factor of factor.
 *
 * @param first Lower range of interpolation.
 * @param second Upper range of interpolation.
 * @param factor Interpolation factor.
 * @return internal value of linear interpolation.
 */
internal fun lerp(first: Long, second: Long, factor: Float) = (first + factor * (second - first)).toLong()

/**
 * Lerp - Linear Interpolation
 * Produces linearly interpolated internal value between first and second parameter by factor of factor.
 *
 * @param first Lower range of interpolation.
 * @param second Upper range of interpolation.
 * @param factor Interpolation factor.
 * @return internal value of linear interpolation.
 */
internal fun lerp(first: Int, second: Int, factor: Float) = (first + factor * (second - first)).toInt()

/**
 * inverseLerp - Inverse Linear Interpolation
 * Produces factor of linear interpolation, given bounds between first and second parameter and actual internal value.
 *
 * @param first Lower range of interpolation.
 * @param second Upper range of interpolation.
 * @param actual interpolation result.
 * @return factor of linear interpolation.
 */
internal fun inverseLerp(first: Float, second: Float, actual: Float) =
  (actual.clamp(Math.min(first, second), Math.max(first, second)) - first) / (second - first)

/**
 * inverseLerp - Inverse Linear Interpolation
 * Produces factor of linear interpolation, given bounds between first and second parameter and actual internal value.
 *
 * @param first Lower range of interpolation.
 * @param second Upper range of interpolation.
 * @param actual interpolation result.
 * @return factor of linear interpolation.
 */
internal fun inverseLerp(first: Long, second: Long, actual: Long): Float =
  actual.clamp(min(first, second), max(first, second) - first) / (second - first).toFloat()
//  (actual.clamp(Math.min(first, second).toFloat(), Math.max(first, second).toFloat()) - first) / (second - first)

/**
 * inverseLerp - Inverse Linear Interpolation
 * Produces factor of linear interpolation, given bounds between first and second parameter and actual internal value.
 *
 * @param first Lower range of interpolation.
 * @param second Upper range of interpolation.
 * @param actual interpolation result.
 * @return factor of linear interpolation.
 */
internal fun inverseLerp(first: Int, second: Int, actual: Int): Float =
  actual.clamp(min(first, second), max(first, second) - first) / (second - first).toFloat()

/**
 * inverseLerp - Inverse Linear Interpolation
 * Produces factor of linear interpolation, given bounds between first and second parameter and actual internal value.
 *
 * @param first Lower range of interpolation.
 * @param second Upper range of interpolation.
 * @param actual interpolation result.
 * @return factor of linear interpolation.
 */
internal fun inverseLerp(first: Double, second: Double, actual: Double): Float =
  (actual.clamp(min(first, second), max(first, second) - first) / (second - first).toFloat()).toFloat()


internal fun Widget.hide() {
  if (visibility != GONE) visibility = GONE
}

internal fun Widget.show() {
  if (visibility != VISIBLE) visibility = VISIBLE
}

internal fun View.isHidden() = visibility == GONE

internal fun View.isNotHidden() = !isHidden()

internal fun View.isVisible() = visibility == VISIBLE

internal fun createImageViewWithDrawable(context: Context, drawable: Drawable?) =
  ImageView(context).apply { setImageDrawable(drawable) }


internal fun AnimationSet.attach(targetView: View) {
  targetView.animation = this
}

internal fun Animation.setListenerBy(onStart: (Animation?) -> (Unit) = animationListenerCallbackStub,
  onEnd: (Animation?) -> (Unit) = animationListenerCallbackStub,
  onRepeat: (Animation?) -> (Unit) = animationListenerCallbackStub) =
  this.setAnimationListener(object : AnimationListener {
    override fun onAnimationRepeat(animation: Animation?) = onRepeat(animation)
    override fun onAnimationEnd(animation: Animation?) = onEnd(animation)
    override fun onAnimationStart(animation: Animation?) = onStart(animation)
  })

internal fun View.asString() = "${asShortString()} (${id.toResourceEntryName(context)})"

internal fun Drawable.asString() = if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) asStringPostKitkat() else asStringPreKitkat()

@RequiresApi(VERSION_CODES.KITKAT)
internal fun Drawable.asStringPostKitkat() =
  "${this::class.java.simpleName}@${hashCode()}, bounds: ${this.bounds}, iHeight: ${this.intrinsicHeight}, iWidth: ${this.intrinsicWidth}, alpha: ${this.alpha}, opacity: ${this.opacity}, visible: ${this.isVisible}"

internal fun Drawable.asStringPreKitkat() =
  "${this::class.java.simpleName}@${hashCode()}, bounds: ${this.bounds}, iHeight: ${this.intrinsicHeight}, iWidth: ${this.intrinsicWidth}, opacity: ${this.opacity}, visible: ${this.isVisible}"

internal fun Any.asShortString() = "${this::class.java.simpleName}@${hashCode()}"

internal fun fromInt(ordinal: Int, default: WidgetPreset = WidgetPreset.values()[0])
  = WidgetPreset.values().find { it.ordinal == ordinal } ?: default

internal fun fromInt(ordinal: Int, default: ColourTransition = ColourTransition.values()[0])
  = ColourTransition.values().find { it.ordinal == ordinal } ?: default


//internal fun WidgetInterpolator.fromInt(ordinal: Int,
//  default: WidgetInterpolator = WidgetInterpolator.internal values()[0]): WidgetInterpolator {
//  WidgetInterpolator.internal values().filter { it.ordinal == ordinal }.forEach { return it }
//  return default
//}

internal fun TypedArray.getInt(@StyleableRes src: Int, defaultVal: Int) = getInt(src, defaultVal)
//    .also { Log.d("AnimatedDropletWidget", getType(src)) }

internal fun Int.toResourceEntryName(context: Context) =
  if (this != View.NO_ID) context.resources.getResourceEntryName(this) ?: "null" else "no-id"

internal fun View.setSize(size: Pair<Width, Height>) = setSize(size.first, size.second)

internal fun View.setSize(width: Int, height: Int) = this.layoutParams?.apply {
  this.width = width
  this.height = height
  this@setSize.layoutParams = this
} ?: setSizeSafe(width, height)

private fun View.setSizeSafe(width: Int, height: Int) {
  this.layoutParams = LayoutParams(width, height)
}

internal fun View.center(containerWidth: Int, containerHeight: Int) {
  this.layoutParams?.let {
    x = (containerWidth - it.width) / 2f
    y = (containerHeight - it.height) / 2f
  } ?: throw RuntimeException()
}

internal fun ViewGroup.getChildren() = (0 until childCount).map { childIndex -> getChildAt(childIndex) }

internal val View.complexString
  get() = StringBuilder(2048).append(
    "${asShortString()}\n" + "\t${toString()}\n" + "\tid: $id, tag: $tag\n" + "\tvisibility: ${visibilityAsString()}, alpha: ${this.alpha}\n" + "\tbackground: ${background?.complexString}\n" + "\tpaddings (top, bot, left, right): ($paddingTop, $paddingBottom, $paddingLeft, $paddingRight)\n" + "\tframe: (top, bot, left, right): ($top, $bottom, $left, $right)\n" + "\tscale (x, y): ($scaleX, $scaleY)\n" + "\trotation: (x, y): ($rotationX, $rotationY)\n" + "\t(x, y): ($x, $y) layoutParams: ${layoutParams?.asString()}\n" + "\tmeasuredWidth: $measuredWidth, measuredHeight: $measuredHeight\n" + "\tanimation: $animation\n" + "\tparent: ${parent?.toString()}" +
        ""
  ).toString()

internal val ImageView.complexString :String
get() = (this as View).complexString + "\timage: ${drawable?.complexString}\n"

internal fun View.visibilityAsString() = when (visibility) {
  View.VISIBLE -> "visible"
  View.INVISIBLE -> "invisible"
  View.GONE -> "gone"
  else -> "UNKNOWN"
}

internal fun LayoutParams.asString() = "${asShortString()}, (width, height): ($width, $height)"


internal val LayoutParams.size: Pair<Width, Height>
  get() = Pair(this.width, this.height)

internal val ViewGroup.size: Pair<Width, Height>
  get() = Pair(this.layoutParams.width, this.layoutParams.height)

private val drawableForegroundRect = Rect()
internal val Drawable.padding: Rect
  get() {
    getPadding(drawableForegroundRect)
    return drawableForegroundRect
  }


internal val Drawable.complexString
  get () = "(width, height): ($intrinsicWidth, $intrinsicHeight), bounds: ${this.bounds}"

internal fun Dp.toPx(context: Context) =
  (this * context.resources.displayMetrics.density).toInt()