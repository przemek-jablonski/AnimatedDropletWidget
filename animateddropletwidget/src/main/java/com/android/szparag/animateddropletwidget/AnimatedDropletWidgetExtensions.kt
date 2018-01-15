package com.android.szparag.animateddropletwidget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.annotation.RequiresApi
import android.support.annotation.StyleableRes
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationSet
import android.widget.ImageView
import com.android.szparag.animateddropletwidget.AnimatedDropletWidget.WidgetInterpolator
import com.android.szparag.animateddropletwidget.AnimatedDropletWidget.WidgetPreset
import java.util.Random

typealias Widget = View

private val animationListenerCallbackStub: (Animation?) -> Unit = {}

fun Random.nextFloat(min: Float, max: Float) =
    nextFloat() * (max - min) + min

//fun Random.nextDouble(min: Double, max: Double) =
//    nextDouble() * (max - min) + min
//
//fun Random.nextInt(min: Int, max: Int) =
//    nextInt() * (max - min) + min
//
//fun Random.nextLong(min: Long, max: Long) =
//    nextLong() * (max - min) + min

fun Float.randomVariation(random: Random, factor: Float) =
    if (factor != 0f) random.nextFloat(this - this * factor, this + this * factor) else this
//
//fun Double.randomVariation(random: Random, factor: Float) =
//    random.nextDouble(this - this * factor, this + this * factor)
//
//fun Int.randomVariation(random: Random, factor: Float) =
//    random.nextInt((this - this * factor).toInt(), (this + this * factor).toInt())
//
//fun Long.randomVariation(random: Random, factor: Float) =
//    random.nextLong((this - this * factor).toLong(), (this + this * factor).toLong())

fun Widget.hide() {
  if (visibility != GONE) this.visibility = GONE
}

fun Widget.show() {
  if (visibility != VISIBLE) visibility = VISIBLE
}


fun createImageViewWithDrawable(context: Context, drawable: Drawable?) =
    ImageView(context).apply { setImageDrawable(drawable) }


fun Float.clamp(max: Float, min: Float) =
    this.coerceAtLeast(min).coerceAtMost(max)

fun inverseLerp(first: Int, second: Int, factor: Float)
    = (factor.clamp(Math.max(first, second).toFloat(), Math.min(first, second).toFloat()) - first) / (second - first)


fun AnimationSet.attach(targetView: View) {
  targetView.animation = this
}

fun Animation.setListenerBy(
    onStart: (Animation?) -> (Unit) = animationListenerCallbackStub,
    onEnd: (Animation?) -> (Unit) = animationListenerCallbackStub,
    onRepeat: (Animation?) -> (Unit) = animationListenerCallbackStub
) = this.setAnimationListener(object : AnimationListener {
  override fun onAnimationRepeat(animation: Animation?) = onRepeat(animation)
  override fun onAnimationEnd(animation: Animation?) = onEnd(animation)
  override fun onAnimationStart(animation: Animation?) = onStart(animation)
})


fun lerp(first: Float, second: Float, factor: Float) =
    first + factor * (second - first)

fun lerp(first: Long, second: Long, factor: Float) =
    first + factor * (second - first)

fun lerpLong(first: Long, second: Long, factor: Float) =
    lerp(first, second, factor).toLong()

fun View.asString() = asShortString()

fun Drawable.asString() = if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) asStringPostKitkat() else asStringPreKitkat()

@RequiresApi(VERSION_CODES.KITKAT)
fun Drawable.asStringPostKitkat() = "${this::class.java.simpleName}@${hashCode()}, bounds: ${this.bounds}, iHeight: ${this
    .intrinsicHeight}, iWidth: ${this.intrinsicWidth}, alpha: ${this.alpha}, opacity: ${this.opacity}, visible: ${this.isVisible}"

fun Drawable.asStringPreKitkat() = "${this::class.java.simpleName}@${hashCode()}, bounds: ${this.bounds}, iHeight: ${this
    .intrinsicHeight}, iWidth: ${this.intrinsicWidth}, opacity: ${this.opacity}, visible: ${this.isVisible}"

fun Any.asShortString() = "${this::class.java.simpleName}@${hashCode()}"

fun WidgetPreset.fromInt(ordinal: Int, default: WidgetPreset = WidgetPreset.values()[0]): WidgetPreset {
  WidgetPreset.values()
      .filter { it.ordinal == ordinal }
      .forEach { return it }
  return default
}

fun WidgetInterpolator.fromInt(ordinal: Int, default: WidgetInterpolator = WidgetInterpolator.values()[0]): WidgetInterpolator {
  WidgetInterpolator.values()
      .filter { it.ordinal == ordinal }
      .forEach { return it }
  return default
}

fun TypedArray.getInt(@StyleableRes src: Int, defaultVal: Int)
    = getInt(src, defaultVal)
//    .also { Log.d("AnimatedDropletWidget", getType(src)) }

fun Int.toResourceEntryName(context: Context) = context.resources.getResourceEntryName(this) ?: "null"

fun View.setSize(width: Int, height: Int) =
    this.layoutParams
        ?.apply {
          this.width = width
          this.height = height
          this@setSize.layoutParams = this
        }
        ?: setSizeSafe(width, height)

fun View.setSizeSafe(width: Int, height: Int) {
  this.layoutParams = LayoutParams(width, height)
}

fun View.center(containerWidth: Int, containerHeight: Int) {
  this.layoutParams?.let {
    x = (containerWidth - it.width) / 2f
    y = (containerHeight - it.height) / 2f
  }
      ?: throw RuntimeException()
}

fun ViewGroup.getChildren() = (0 until childCount).map { childIndex -> getChildAt(childIndex) }