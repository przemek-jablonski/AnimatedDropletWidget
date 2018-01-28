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
import com.android.szparag.animateddropletwidget.AnimatedDropletWidget.WidgetInterpolator
import com.android.szparag.animateddropletwidget.AnimatedDropletWidget.WidgetPreset
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong

typealias Widget = View
typealias Width = Int
typealias Height = Int

private val animationListenerCallbackStub: (Animation?) -> Unit = {}

/**
 * Min - inclusive
 * Max - inclusive
 */
fun Random.nextFloat(min: Float, max: Float) = nextFloat() * (max - min) + min

fun Random.nextDouble(min: Double, max: Double) = nextDouble() * (max - min) + min

fun Random.nextInt(min: Int, max: Int) = (nextFloat() * (max - min) + min).roundToInt()

fun Random.nextLong(min: Long, max: Long) = (nextFloat() * (max - min) + min).roundToLong()

fun Float.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextFloat(this - this * factor, this + this * factor) else this

fun Double.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextDouble(this - this * factor, this + this * factor) else this

fun Int.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextInt((this - this * factor).toInt(), (this + this * factor).toInt()) else this

fun Long.randomVariation(random: Random, factor: Float) =
  if (factor != 0f) random.nextLong((this - this * factor).toLong(), (this + this * factor).toLong()) else this

//todo: inverse params
fun Float.clamp(max: Float, min: Float) = this.coerceAtLeast(min).coerceAtMost(max)

fun lerp(first: Float, second: Float, factor: Float) = first + factor * (second - first)

fun lerp(first: Long, second: Long, factor: Float) = (first + factor * (second - first)).toLong()

fun lerp(first: Int, second: Int, factor: Float) = (first + factor * (second - first)).toInt()

fun inverseLerp(first: Int, second: Int, actual: Float) = (actual.clamp(
  Math.max(first, second).toFloat(), Math.min(first, second).toFloat()
) - first) / (second - first)


fun Widget.hide() {
  if (visibility != GONE) visibility = GONE
}

fun Widget.show() {
  if (visibility != VISIBLE) visibility = VISIBLE
}

fun View.isHidden() = visibility == GONE

fun View.isNotHidden() = !isHidden()

fun View.isVisible() = visibility == VISIBLE

fun createImageViewWithDrawable(context: Context, drawable: Drawable?) =
  ImageView(context).apply { setImageDrawable(drawable) }


fun AnimationSet.attach(targetView: View) {
  targetView.animation = this
}

fun Animation.setListenerBy(onStart: (Animation?) -> (Unit) = animationListenerCallbackStub,
  onEnd: (Animation?) -> (Unit) = animationListenerCallbackStub,
  onRepeat: (Animation?) -> (Unit) = animationListenerCallbackStub) =
  this.setAnimationListener(object : AnimationListener {
    override fun onAnimationRepeat(animation: Animation?) = onRepeat(animation)

    override fun onAnimationEnd(animation: Animation?) = onEnd(animation)

    override fun onAnimationStart(animation: Animation?) = onStart(animation)
  })

fun View.asString() = "${asShortString()} (${id.toResourceEntryName(context)})"

fun Drawable.asString() = if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) asStringPostKitkat() else asStringPreKitkat()

@RequiresApi(VERSION_CODES.KITKAT)
fun Drawable.asStringPostKitkat() =
  "${this::class.java.simpleName}@${hashCode()}, bounds: ${this.bounds}, iHeight: ${this.intrinsicHeight}, iWidth: ${this.intrinsicWidth}, alpha: ${this.alpha}, opacity: ${this.opacity}, visible: ${this.isVisible}"

fun Drawable.asStringPreKitkat() =
  "${this::class.java.simpleName}@${hashCode()}, bounds: ${this.bounds}, iHeight: ${this.intrinsicHeight}, iWidth: ${this.intrinsicWidth}, opacity: ${this.opacity}, visible: ${this.isVisible}"

fun Any.asShortString() = "${this::class.java.simpleName}@${hashCode()}"

fun WidgetPreset.fromInt(ordinal: Int, default: WidgetPreset = WidgetPreset.values()[0]): WidgetPreset {
  WidgetPreset.values().filter { it.ordinal == ordinal }.forEach { return it }
  return default
}

fun WidgetInterpolator.fromInt(ordinal: Int,
  default: WidgetInterpolator = WidgetInterpolator.values()[0]): WidgetInterpolator {
  WidgetInterpolator.values().filter { it.ordinal == ordinal }.forEach { return it }
  return default
}

fun TypedArray.getInt(@StyleableRes src: Int, defaultVal: Int) = getInt(src, defaultVal)
//    .also { Log.d("AnimatedDropletWidget", getType(src)) }

fun Int.toResourceEntryName(context: Context) =
  if (this != View.NO_ID) context.resources.getResourceEntryName(this) ?: "null" else "no-id"

fun View.setSize(size: Pair<Width, Height>) = setSize(size.first, size.second)

fun View.setSize(width: Int, height: Int) = this.layoutParams?.apply {
  this.width = width
  this.height = height
  this@setSize.layoutParams = this
} ?: setSizeSafe(width, height)

private fun View.setSizeSafe(width: Int, height: Int) {
  this.layoutParams = LayoutParams(width, height)
}

fun View.center(containerWidth: Int, containerHeight: Int) {
  this.layoutParams?.let {
    x = (containerWidth - it.width) / 2f
    y = (containerHeight - it.height) / 2f
  } ?: throw RuntimeException()
}

fun ViewGroup.getChildren() = (0 until childCount).map { childIndex -> getChildAt(childIndex) }

val View.complexString
  get() = StringBuilder(2048).append(
    "${asShortString()}\n" + "\t${toString()}\n" + "\tid: $id, tag: $tag\n" + "\tvisibility: ${visibilityAsString()}, alpha: ${this.alpha}\n" + "\tbackground: ${background?.complexString}\n" + "\tpaddings (top, bot, left, right): ($paddingTop, $paddingBottom, $paddingLeft, $paddingRight)\n" + "\tframe: (top, bot, left, right): ($top, $bottom, $left, $right)\n" + "\tscale (x, y): ($scaleX, $scaleY)\n" + "\trotation: (x, y): ($rotationX, $rotationY)\n" + "\t(x, y): ($x, $y) layoutParams: ${layoutParams?.asString()}\n" + "\tmeasuredWidth: $measuredWidth, measuredHeight: $measuredHeight\n" + "\tanimation: $animation\n" + "\tparent: ${parent?.toString()}" +
        ""
  ).toString()

val ImageView.complexString :String
get() = (this as View).complexString + "\timage: ${drawable?.complexString}\n"

fun View.visibilityAsString() = when (visibility) {
  View.VISIBLE -> "visible"
  View.INVISIBLE -> "invisible"
  View.GONE -> "gone"
  else -> "UNKNOWN"
}

fun LayoutParams.asString() = "${asShortString()}, (width, height): ($width, $height)"


val LayoutParams.size: Pair<Width, Height>
  get() = Pair(this.width, this.height)

val ViewGroup.size: Pair<Width, Height>
  get() = Pair(this.layoutParams.width, this.layoutParams.height)

private val drawableForegroundRect = Rect()
val Drawable.padding: Rect
  get() {
    getPadding(drawableForegroundRect)
    return drawableForegroundRect
  }


val Drawable.complexString
  get () = "(width, height): ($intrinsicWidth, $intrinsicHeight), bounds: ${this.bounds}"