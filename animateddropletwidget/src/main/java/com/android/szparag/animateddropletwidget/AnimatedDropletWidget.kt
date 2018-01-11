package com.android.szparag.animateddropletwidget

import android.content.Context
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Path
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.annotation.CallSuper
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.Random

/**
 * Created by Przemyslaw Jablonski (github.com/sharaquss, pszemek.me) on 15/11/2017.
 */

private const val BASE_ANIMATION_LENGTH_MILLIS = 5000L
private const val BASE_ANIMATION_LENGTH_MIN_MILLIS = (BASE_ANIMATION_LENGTH_MILLIS * 0.66f).toLong()
private const val BASE_ANIMATION_REPEAT_DELAY_MILLIS = BASE_ANIMATION_LENGTH_MILLIS / 2
private const val BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS = (BASE_ANIMATION_LENGTH_MILLIS * 2.5f).toLong()
private const val BASE_ANIMATION_BACKGROUND_REPEAT_DELAY_MILLIS = BASE_ANIMATION_LENGTH_MILLIS / 35
private const val BASE_OVAL_STROKE_THICKNESS = 50f
private const val ANIMATION_RANDOM_START_TIME_BOUND_MILLIS = BASE_ANIMATION_LENGTH_MILLIS * 2
private const val ANIMATION_RANDOM_REPEAT_DELAY_BOUND_MILLIS = ANIMATION_RANDOM_START_TIME_BOUND_MILLIS * 2.50

typealias Millis = Long
typealias ResourceId = Int
typealias Percentage = Float

open class AnimatedDropletWidget : FrameLayout {

  //todo: unify - there are vars, lateinit vars and vals here
  private var drawableView: ImageView
  private lateinit var circularDropletBackgroundView1: View //todo: 1? 2? wtf
  private lateinit var circularDropletBackgroundView2: View //todo: to layers with layer count as a parameter
  //  private val circularBackgroundViewLayers = arrayListOf<View>()
  private val circularDropletBackgroundLayers = arrayListOf<View>()
  private val circularDropletViewLayers = arrayListOf<View>()

  private var oneShotDropletView: View? = null

  private val random by lazy { Random() }

  @DrawableRes private var drawable = android.R.color.transparent

//  private var drawableMargin: Int = 0
//  private var dropletSpeed: Int = 1
//  private var dropletFadeout: Int = 1
  //todo: drawable layoutParams in xml - maybe someone wants drawable to be WRAP_CONTENT?
  //todo: parse so that srcCompat / src can be used to specify Drawable used

  //todo: callback (with default implementation) onUserClicked()
  //todo: callback (with default implementation) onUserLongPressed()
  //todo: performOneShotDroplet
  //todo: color(s?) as params (reference)
  //todo: interpolators as params (enum)
  //todo: staralpha as a param
  //todo: endalpha as a param
  //todo: repeatoffset as a param
  //todo: internal animation values should be stored as fields and shared between animations (with some multiplier)
  //TODO: MAKE PATH INTERPOLATOR, SO THAT IT STARTS WITH 0 ALPHA, THEN GOES FAST TO MAX ALPHA AND FADES TO 0 AGAIN!!!
  //todo: make method oneShotCircle, so that when battery status changes, it is reflected in animation as well!
  //todo:

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    Log.d("AnimatedDropletWidget", "ctor")
    parseCustomAttributes()
    applyCustomAtrributes()
    setupView()

    drawableView = createFrontDrawableView(drawable)
  }

  //<editor-fold desc="Parsing xml attributes (if any)">
  //todo: typedarray as input
  @CallSuper protected fun parseCustomAttributes() {
    Log.d("AnimatedDropletWidget", "parseCustomAttributes")
  }

  @CallSuper protected fun applyCustomAtrributes() {
    Log.d("AnimatedDropletWidget", "applyCustomAtrributes")
  }
  //</editor-fold>

  private fun setupView() {
    Log.d("AnimatedDropletWidget", "setupView")
    addOnLayoutChangeListener(this::onLayoutBoundsChanged)
    clipChildren = false
    drawable = android.R.mipmap.sym_def_app_icon//todo hardcoded
  }

  private fun onLayoutBoundsChanged(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int,
      oldBottom: Int) {
    if (oldLeft == 0 && oldRight == 0 && oldTop == 0 && oldBottom == 0 && left != 0 && right != 0 && top != 0 && bottom != 0)
      onLayoutFirstMeasurementApplied()
  }

  @CallSuper protected fun onLayoutFirstMeasurementApplied() {
    Log.d("AnimatedDropletWidget", "onLayoutFirstMeasurementApplied")

    createCircularDropletBackgroundLayers(2)

    createCircularDropletsLayers(6)

    oneShotDropletView = createCircularDropletView(BASE_OVAL_STROKE_THICKNESS, android.R.color.holo_purple)
        .apply {
          hide()
          addView(this)
        }

    //todo: this is not elegant
    addView(drawableView)
  }

  private fun createCircularDropletsLayers(layerCount: Int) {
    Log.d("AnimatedDropletWidget", "createCircularDropletsLayers, layerCount: $layerCount")
    (0 until layerCount).mapTo(circularDropletViewLayers) { layerIndex ->
      createCircularDropletView(
          thickness = BASE_OVAL_STROKE_THICKNESS - (layerIndex * BASE_OVAL_STROKE_THICKNESS / layerCount),
          colourId = android.R.color.holo_red_dark
      )
    }
    //todo: start animation should be as a separate function in onLayoutFirstMeasurementApplied
    addViews(children = circularDropletViewLayers, childApply = { child, index ->
      child.hide()
      animateCircularDroplet(child, index, layerCount, 1.0f)
    })
  }

  //todo: 2 is hardcoded
  private fun createCircularDropletBackgroundLayers(layerCount: Int) {
    Log.d("AnimatedDropletWidget", "createCircularDropletBackgroundLayers, layerCount: $layerCount")
    (0 until layerCount).mapTo(circularDropletBackgroundLayers) { layerIndex ->
      createCircularBackgroundView(android.R.color.holo_red_dark)
    }
    circularDropletBackgroundView1 = createCircularBackgroundView(android.R.color.holo_red_dark)
    circularDropletBackgroundView1.hide()
    addView(circularDropletBackgroundView1)
    animateCircularBackground(
        targetView = circularDropletBackgroundView1,
        startTime = 0L,
        duration = BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS,
        repeatDelay = BASE_ANIMATION_BACKGROUND_REPEAT_DELAY_MILLIS,
        pathRandomFactor = 0.002f
    )

    circularDropletBackgroundView2 = createCircularBackgroundView(android.R.color.holo_red_dark)
    circularDropletBackgroundView2.hide()
    addView(circularDropletBackgroundView2)
    animateCircularBackground(
        targetView = circularDropletBackgroundView2,
        startTime = BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS / 2,
        duration = BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS,
        repeatDelay = BASE_ANIMATION_BACKGROUND_REPEAT_DELAY_MILLIS,
        pathRandomFactor = 0.01f
    )
  }


  //<editor-fold desc="Creating views">
  private fun createCircularDropletView(thickness: Float, @ColorRes colourId: ResourceId)
      = createImageViewWithDrawable(context, createCircularDropletDrawable(thickness, colourId))
      .also { Log.d("AnimatedDropletWidget", "createCircularDropletView, thickness: $thickness, view: ${it.asString()}") }

  private fun createCircularBackgroundView(@ColorRes colourId: ResourceId)
      = createImageViewWithDrawable(context, createCircularBackgroundDrawable(colourId))
      .also { Log.d("AnimatedDropletWidget", "createCircularBackgroundView, view: ${it.asString()}") }
  //</editor-fold>


  //<editor-fold desc="Creating drawables">
  private fun createCircularDropletDrawable(strokeThickness: Float, @ColorRes colourId: ResourceId) =
      ShapeDrawable(OvalShape()).apply {
        this.intrinsicHeight = this@AnimatedDropletWidget.height
        this.intrinsicWidth = this@AnimatedDropletWidget.width
        this.paint.strokeWidth = strokeThickness
        this.paint.style = STROKE
        this.paint.color = resources.getColor(colourId)
      }.also {
        Log.d("AnimatedDropletWidget", "createCircularDropletDrawable, drawable: ${it.asString()}")
      }

  private fun createCircularBackgroundDrawable(@ColorRes colourId: ResourceId) =
      ShapeDrawable(OvalShape()).apply {
        this.intrinsicHeight = this@AnimatedDropletWidget.height
        this.intrinsicWidth = this@AnimatedDropletWidget.width
        this.paint.style = FILL
        this.paint.color = resources.getColor(colourId)
      }.also {
        Log.d("AnimatedDropletWidget", "createCircularBackgroundDrawable, drawable: ${it.asString()}")
      }

  private fun createFrontDrawableView(@DrawableRes drawableRes: ResourceId?)
      = createImageViewWithDrawable(context, drawableRes?.let { resources.getDrawable(drawableRes) })
  //</editor-fold>

  //<editor-fold desc="Animate views">
  private fun animateCircularBackground(targetView: View, startTime: Millis, duration: Millis, repeatDelay: Millis,
      pathRandomFactor: Float) {
    Log.d("AnimatedDropletWidget", "animateCircularBackground, targetView: ${targetView.asString()}")
    targetView.show()
    AnimationSet(false)
        .also { set ->
          set.addAnimation(createScalingAnimation(
              parentContainer = this,
              duration = duration,
              startTime = startTime,
              repeatDelay = repeatDelay,
              xyStart = 0f,
              xyEnd = 1f,
              interpolator = PathInterpolator(generateDropletBackgroundPath(random, pathRandomFactor)),
              timeCutoff = 1.0f
          ))
          set.addAnimation(createFadeoutAnimation(
              parentContainer = this,
              duration = duration,
              repeatDelay = repeatDelay,
              startTime = startTime,
              alphaStart = 0.12f,
              alphaEnd = 0.00f,
              interpolator = FastOutLinearInInterpolator(),
              timeCutoff = 0.99f
          ))
          set.attach(targetView)
        }.start()
  }

  private fun animateCircularDroplet(targetView: View, layerIndex: Int, layerCount: Int, layerDependency: Float) {
    val startTime = random.nextInt(ANIMATION_RANDOM_START_TIME_BOUND_MILLIS.toInt()).toLong()
    val repeatDelayAddition = random.nextInt(ANIMATION_RANDOM_REPEAT_DELAY_BOUND_MILLIS.toInt()).toLong()
    val layerValuesMultiplier = layerIndex / layerCount * layerDependency
    val inverseLerp = inverseLerp(0, layerCount, layerIndex.toFloat())
    Log.d("AnimatedDropletWidget",
        "animateCircularDroplet, layerIndex: $layerIndex, layerCount: $layerCount, layerDependency: $layerDependency, inverseLerp: " +
            "$inverseLerp, layerValuesMultiplier: $layerValuesMultiplier")
    targetView.show()
    AnimationSet(false)
        .also { set ->
          set.fillAfter = true
          set.isFillEnabled = true
          set.addAnimation(createScalingAnimation(
              parentContainer = this,
              duration = lerpLong(first = BASE_ANIMATION_LENGTH_MIN_MILLIS, second = BASE_ANIMATION_LENGTH_MILLIS, factor = inverseLerp),
              startTime = startTime,
              repeatDelay = BASE_ANIMATION_REPEAT_DELAY_MILLIS + repeatDelayAddition,
              xyStart = 0.00f,
              xyEnd = lerp(0.70f, 1.00f, inverseLerp),
              interpolator = AnticipateOvershootInterpolator(lerp(1.33f, 0.25f, inverseLerp)),
              timeCutoff = lerp(0.8f, 0.98f, inverseLerp)
          ))
          set.addAnimation(createFadeoutAnimation(
              parentContainer = this,
              duration = lerp(
                  first = BASE_ANIMATION_LENGTH_MIN_MILLIS,
                  second = BASE_ANIMATION_LENGTH_MILLIS,
                  factor = inverseLerp(0, layerCount, layerIndex.toFloat())
              ).toLong(),
              repeatDelay = BASE_ANIMATION_REPEAT_DELAY_MILLIS + repeatDelayAddition,
              startTime = startTime,
              alphaStart = lerp(0.15f, 0.55f, inverseLerp),
              alphaEnd = 0.00f,
              interpolator = AccelerateInterpolator(lerp(1.10f, 0.85f, inverseLerp)),
              timeCutoff = lerp(0.90f, 0.97f, inverseLerp)
          ))
          set.attach(targetView)
        }.start()
  }
  //</editor-fold>

  //<editor-fold desc="Animations builders">
  private fun createScalingAnimation(parentContainer: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
      xyStart: Float, xyEnd: Float, interpolator: Interpolator, timeCutoff: Float = 1.0f, oneShot: Boolean = false)
      = ScaleAnimation(xyStart, xyEnd, xyStart, xyEnd, parentContainer.width / 2f, parentContainer.height / 2f)
      .also { animation ->
        Log.d("AnimatedDropletWidget", "createScalingAnimation, duration: $duration, startTime: $startTime, repeatDelay: $repeatDelay, " +
            "xyStart: $xyStart, xyEnd: $xyEnd, interpolator: ${interpolator::class.java.simpleName}, timeCutoff: $timeCutoff")
        animation.duration = duration
        animation.startOffset = startTime
        animation.repeatCount = if (oneShot) 0 else Animation.INFINITE
        animation.repeatMode = Animation.RESTART
        animation.interpolator = CutoffInterpolator(sourceInterpolator = interpolator, cutoff = timeCutoff)
        animation.setListenerBy(onRepeat = { animation.startOffset = repeatDelay })
      }


  private fun createFadeoutAnimation(parentContainer: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
      alphaStart: Float, alphaEnd: Float, interpolator: Interpolator, timeCutoff: Float, oneShot: Boolean = false) =
      AlphaAnimation(alphaStart, alphaEnd)
          .also { animation ->
            Log.d("AnimatedDropletWidget",
                "createFadeoutAnimation, duration: $duration, startTime: $startTime, repeatDelay: $repeatDelay, " +
                    "alphaStart: $alphaStart, alphaEnd: $alphaEnd, interpolator: ${interpolator::class.java.simpleName}, timeCutoff: $timeCutoff")
            animation.duration = duration
            animation.startOffset = startTime
            animation.repeatCount = if (oneShot) 0 else Animation.INFINITE
            animation.repeatMode = Animation.RESTART
            animation.isFillEnabled = true
            animation.fillAfter = true
            animation.fillBefore = false
            animation.interpolator = CutoffInterpolator(sourceInterpolator = interpolator, cutoff = timeCutoff)
            animation.setListenerBy(onRepeat = { animation.startOffset = repeatDelay })
          }
  //</editor-fold>

  fun performOneShotAnimation() {
    Log.d("AnimatedDropletWidget", "performOneShotAnimation")
    oneShotDropletView?.let {
      this.show()
      AnimationSet(false)
          .also { set ->
            set.fillAfter = true
            set.isFillEnabled = true
            set.addAnimation(createScalingAnimation(
                parentContainer = this,
                duration = BASE_ANIMATION_LENGTH_MILLIS,
                startTime = 0,
                repeatDelay = 0,
                xyStart = 0.00f,
                xyEnd = 1.00f,
                interpolator = AnticipateOvershootInterpolator(0.80f),
                timeCutoff = 1.00f,
                oneShot = true
            ))
            set.addAnimation(createFadeoutAnimation(
                parentContainer = this,
                duration = BASE_ANIMATION_LENGTH_MILLIS,
                repeatDelay = 0,
                startTime = 0,
                alphaStart = 1.00f,
                alphaEnd = 0.00f,
                interpolator = AccelerateInterpolator(0.5f),
                timeCutoff = 1.0f,
                oneShot = true
            ))
            set.attach(this)
          }.start()
    }
  }

  private fun addViews(children: List<View>, childApply: (View, Int) -> (Unit) = { _, _ -> }) {
//    Log.d("AnimatedDropletWidget", "addViews, children.count: ${children.size}, children: ${children.map { it.asString() }}, index: $index")
    for (i in children.size - 1 downTo 0) {
      children[i].apply { childApply.invoke(this, i); addView(this) }
    }
  }

  private fun createInterpolator(randomFactor: Float) =
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) createInterpolatorApiLollipop(randomFactor)
      else createInterpolatorPreApiLollipop()

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  private fun createInterpolatorApiLollipop(randomFactor: Float) = PathInterpolator(generateDropletBackgroundPath(random, randomFactor))

  private fun createInterpolatorPreApiLollipop() = AccelerateDecelerateInterpolator()

  //proven that this feels good on the ui with serious laboratory testing. true story
  private fun generateDropletBackgroundPath(random: Random, randomFactor: Float = 0.005f) = Path().apply {
    moveTo(0.000f, 0.000f)
    quadTo(0.065f, 0.325f, 0.150f, 0.400f.randomVariation(random, randomFactor))
    lineTo(0.330f, 0.300f.randomVariation(random, randomFactor / 2f))
    quadTo(0.390f, 0.630f, 0.420f, 0.690f.randomVariation(random, randomFactor))
    lineTo(0.690f, 0.480f.randomVariation(random, randomFactor / 2f))
    quadTo(0.725f, 0.85f, 0.740f, 0.900f.randomVariation(random, randomFactor))
    lineTo(0.930f, 0.710f.randomVariation(random, randomFactor / 2f))
    quadTo(0.965f, 0.925f, 1.000f, 1.000f)
  }

//  private fun widgetAsStringWidthChildren() = StringBuilder(1024)
//      .apply {
//        append("\n\t\t${this@DrawableAnimatedDropletWidget.asString()}")
//        for (i in 0 until this@DrawableAnimatedDropletWidget.childCount) {
//          this.append("\n\t\t\t${this@DrawableAnimatedDropletWidget.getChildAt(i).asString()}")
//        }
//      }.append("").toString()

}