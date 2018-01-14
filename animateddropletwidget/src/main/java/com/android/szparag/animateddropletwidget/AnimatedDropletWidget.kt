package com.android.szparag.animateddropletwidget

import android.R.color
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
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

typealias Millis = Long
typealias ResourceId = Int
typealias Percentage = Int

/**
 * Created by Przemyslaw Jablonski (github.com/sharaquss, pszemek.me) on 15/11/2017.
 */


//<editor-fold desc="Default XML attribute values for global settings">
private const val ATTRS_DROPLET_COUNT = 5
private const val ATTRS_BACKGROUND_LAYERS_COUNT = 2
private const val ATTRS_ONESHOT_COUNT = 1
private const val ATTRS_GLOBAL_RANDOM_INFLUENCE = 1.00F
private const val ATTRS_GLOBAL_MAX_DURATION: Millis = 5000 //todo: BASE_ANIMATION_LENGTH_MILLIS?
private const val ATTRS_GLOBAL_COLOUR = color.holo_red_dark
private const val ATTRS_GLOBAL_COLOUR_DISTRIBUTION = 1.00F
//</editor-fold>

//<editor-fold desc="Default XML attribute values for drawable layer">
private const val ATTRS_DRAWABLE = android.R.mipmap.sym_def_app_icon
private const val ATTRS_DRAWABLE_SIZE = 75
private const val ATTRS_DRAWABLE_ALPHA = 100
//</editor-fold>

//<editor-fold desc="Default XML attribute values for droplets layers">
private const val ATTRS_DROPLETS_MAX_DURATION = ATTRS_GLOBAL_MAX_DURATION
private const val ATTRS_DROPLETS_MAX_DURATION_DISTRIBUTION = 1.00F
private const val ATTRS_DROPLETS_SPAWNSIZE = 0
private const val ATTRS_DROPLETS_ENDSIZE_MAX = 100
private const val ATTRS_DROPLETS_ENDSIZE_MIN =
    ATTRS_DRAWABLE_SIZE + (ATTRS_DROPLETS_ENDSIZE_MAX - ATTRS_DROPLETS_ENDSIZE_MAX) / 3 //todo: this 3 as constant
private const val ATTRS_DROPLETS_FADEOUT = 1.00F
private const val ATTRS_DROPLETS_THICKNESS = 1.00F
private const val ATTRS_DROPLETS_THICKNESS_DISTRIBUTION = 1.00F
//</editor-fold>

//<editor-fold desc="Default XML attribute values for background layers">
private const val ATTRS_BACKGROUND_MAX_DURATION = ATTRS_GLOBAL_MAX_DURATION
private const val ATTRS_BACKGROUND_ENDSIZE_MAX = 100
private const val ATTRS_BACKGROUND_COLOUR = ATTRS_GLOBAL_COLOUR
private const val ATTRS_BACKGROUND_COLOUR_DISTRIBUTION = ATTRS_GLOBAL_COLOUR_DISTRIBUTION
//</editor-fold>

//<editor-fold desc="Default XML attribute values for One-Shot layers">
private const val ATTRS_ONESHOT_MAX_DURATION = ATTRS_DROPLETS_MAX_DURATION
private const val ATTRS_ONESHOT_COLOUR = ATTRS_GLOBAL_COLOUR
//</editor-fold>

//<editor-fold desc="Default internal values">
private const val BASE_ANIMATION_LENGTH_MILLIS = 5000L
private const val BASE_ANIMATION_LENGTH_MIN_MILLIS = (BASE_ANIMATION_LENGTH_MILLIS * 0.66f).toLong()
private const val BASE_ANIMATION_REPEAT_DELAY_MILLIS = BASE_ANIMATION_LENGTH_MILLIS / 2
private const val BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS = (BASE_ANIMATION_LENGTH_MILLIS * 2.5f).toLong()
private const val BASE_ANIMATION_BACKGROUND_REPEAT_DELAY_MILLIS = BASE_ANIMATION_LENGTH_MILLIS / 35
private const val BASE_OVAL_STROKE_THICKNESS = 50f
private const val ANIMATION_RANDOM_START_TIME_BOUND_MILLIS = BASE_ANIMATION_LENGTH_MILLIS * 2
private const val ANIMATION_RANDOM_REPEAT_DELAY_BOUND_MILLIS = ANIMATION_RANDOM_START_TIME_BOUND_MILLIS * 2.50
//</editor-fold>

@SuppressLint("LogNotTimber") //todo: temporary, remove that
open class AnimatedDropletWidget : FrameLayout {

  //<editor-fold desc="View properties">
  /**
   * View properties.
   * Populated with constants with default xml attributes at creation time.
   * Some of those can be overwritten if user chooses to do so in xml
   * @see parseCustomAttributes
   */
  private var preset = WidgetPreset.NONE

  @DrawableRes private var drawableSrc = ATTRS_DRAWABLE
  private var drawableSize = ATTRS_DRAWABLE_SIZE
  private var drawableAlpha = ATTRS_DRAWABLE_ALPHA

  private var dropletCount = ATTRS_DROPLET_COUNT
  private var backgroundLayersCount = ATTRS_BACKGROUND_LAYERS_COUNT
  private var oneshotLayersCount = ATTRS_ONESHOT_COUNT
  private var globalRandomInfluence = ATTRS_GLOBAL_RANDOM_INFLUENCE
  private var globalMaxDuration: Millis = ATTRS_GLOBAL_MAX_DURATION
  private var globalColour = ATTRS_GLOBAL_COLOUR
  private var globalColourDistribution = ATTRS_GLOBAL_COLOUR_DISTRIBUTION
  private var globalInterpolator = WidgetInterpolator.PREDEFINED

  private var dropletsMaxDuration = ATTRS_DROPLETS_MAX_DURATION
  private var dropletsMaxDurationDistribution = ATTRS_DROPLETS_MAX_DURATION_DISTRIBUTION
  private var dropletsSpawnsize = ATTRS_DROPLETS_SPAWNSIZE
  private var dropletsEndsizeMin = ATTRS_DROPLETS_ENDSIZE_MIN
  private var dropletsEndsizeMax = ATTRS_DROPLETS_ENDSIZE_MAX
  private var dropletsFadeout = ATTRS_DROPLETS_FADEOUT //todo: what is that?
  private var dropletsThickness = ATTRS_DROPLETS_THICKNESS
  private var dropletsThicknessDistribution = ATTRS_DROPLETS_THICKNESS_DISTRIBUTION
  private var dropletsInterpolator = WidgetInterpolator.PREDEFINED //todo: should it be predefined?

  var backgroundMaxDuration = ATTRS_BACKGROUND_MAX_DURATION
  var backgroundEndsizeMin = drawableSize
  var backgroundEndsizeMax = ATTRS_BACKGROUND_ENDSIZE_MAX
  private var backgroundColour = ATTRS_BACKGROUND_COLOUR
  private var backgroundColourDistribution = ATTRS_BACKGROUND_COLOUR_DISTRIBUTION
  private var backgroundInterpolator = WidgetInterpolator.DECELERATE //todo: ?

  var oneshotMaxDuration = ATTRS_ONESHOT_MAX_DURATION
  var oneshotColour = ATTRS_ONESHOT_COLOUR
  private var oneshotInterpolator = WidgetInterpolator.ACCELERATE_DECELERATE
  //</editor-fold>

  //todo: unify - there are vars, lateinit vars and vals here
  private lateinit var drawableView: ImageView
  private lateinit var circularDropletBackgroundView1: View //todo: 1? 2? wtf
  private lateinit var circularDropletBackgroundView2: View //todo: to layers with layer count as a parameter
  //  private val circularBackgroundViewLayers = arrayListOf<View>()
  private val circularDropletBackgroundLayers = arrayListOf<View>()
  private val circularDropletViewLayers = arrayListOf<View>()

  private var oneShotDropletView: View? = null

  private val random by lazy { Random() }


  //todo: drawable layoutParams in xml - maybe someone wants drawable to be WRAP_CONTENT?

  //todo: callbacks: onOneShotAnimationStarted() / onOneShotAnimationFinished()
  //todo: callbacks: onBackgroundLayerAnimationStarted() / onDropletLayerAnimationStarted()
  //todo: internal animation values should be stored as fields and shared between animations (with some multiplier)

  enum class WidgetPreset { NONE, DROPLETS, FLOW, RADAR, IRREGULAR }
  enum class WidgetInterpolator { PREDEFINED, ACCELERATE, DECELERATE, ACCELERATE_DECELERATE, BOUNCE, OVERSHOOT }


  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    Log.d("AnimatedDropletWidget", "ctor")
    context.theme.obtainStyledAttributes(attrs, R.styleable.AnimatedDropletWidget, defStyleAttr, 0)?.let { parseCustomAttributes(it) }
    applyCustomAtrributes()
    setupView()
//    drawableView = createFrontDrawableView(drawable)
  }

  //<editor-fold desc="Parsing xml attributes (if any)">
  //todo: typedarray as input
  @CallSuper protected fun parseCustomAttributes(attrs: TypedArray) {
    Log.d("AnimatedDropletWidget", "parseCustomAttributes")
    Log.d("AnimatedDropletWidget", "viewProperties: \n ${printViewAttributes()}")
    try {
      drawableSrc = attrs.getResourceId(R.styleable.AnimatedDropletWidget_drawable_src, drawableSrc)
      drawableSize = attrs.getInt(R.styleable.AnimatedDropletWidget_drawable_size, drawableSize)
      drawableAlpha = attrs.getInt(R.styleable.AnimatedDropletWidget_drawable_alpha, drawableAlpha)
      preset.fromInt(attrs.getInt(R.styleable.AnimatedDropletWidget_preset, preset.ordinal))
      dropletCount = attrs.getInt(R.styleable.AnimatedDropletWidget_droplet_count, dropletCount)
      backgroundLayersCount = attrs.getInt(R.styleable.AnimatedDropletWidget_background_layers_count, backgroundLayersCount)
      oneshotLayersCount = attrs.getInt(R.styleable.AnimatedDropletWidget_oneshot_count, oneshotLayersCount)
      globalRandomInfluence = attrs.getFloat(R.styleable.AnimatedDropletWidget_global_random_influence, globalRandomInfluence)
      globalMaxDuration = attrs.getInt(R.styleable.AnimatedDropletWidget_global_max_duration_ms,
          globalMaxDuration.toInt()).toLong() //todo: double casting here
      globalColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_global_colour, globalColour)
      globalColourDistribution = attrs.getFloat(R.styleable.AnimatedDropletWidget_global_colour_distribution, globalColourDistribution)
      globalInterpolator.fromInt(attrs.getInt(R.styleable.AnimatedDropletWidget_global_interpolator, globalInterpolator.ordinal))

      dropletsMaxDuration = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_max_duration, dropletsMaxDuration.toInt()).toLong()
      dropletsMaxDurationDistribution = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_max_duration,
          dropletsMaxDurationDistribution)
      dropletsSpawnsize = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_spawnsize, dropletsSpawnsize)
      dropletsEndsizeMin = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_endsize_min, dropletsEndsizeMin)
      dropletsEndsizeMax = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_endsize_max, dropletsEndsizeMax)
      dropletsFadeout = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_fadeout, dropletsFadeout)
      dropletsThickness = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_thickness, dropletsThickness)
      dropletsThicknessDistribution = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_thickness_distribution,
          dropletsThicknessDistribution)
      dropletsInterpolator.fromInt(attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_interpolator, dropletsInterpolator.ordinal))

      backgroundMaxDuration = attrs.getInt(R.styleable.AnimatedDropletWidget_background_max_duration,
          backgroundMaxDuration.toInt()).toLong()
      backgroundEndsizeMax = attrs.getInt(R.styleable.AnimatedDropletWidget_background_endsize_max, backgroundEndsizeMax)
      backgroundColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_background_colour, backgroundColour)
      backgroundColourDistribution = attrs.getFloat(R.styleable.AnimatedDropletWidget_background_colour_distibution,
          backgroundColourDistribution)
      backgroundInterpolator.fromInt(
          attrs.getInt(R.styleable.AnimatedDropletWidget_background_interpolator, backgroundInterpolator.ordinal))

      oneshotColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_oneshot_colour, oneshotColour)
      oneshotInterpolator.fromInt(attrs.getInt(R.styleable.AnimatedDropletWidget_oneshot_interpolator, oneshotInterpolator.ordinal))


      attrs.getInt(R.styleable.AnimatedDropletWidget_global_max_duration_ms, globalMaxDuration.toInt()).toLong()
          .apply {
            globalMaxDuration = this
            backgroundMaxDuration = this
            dropletsMaxDuration = this
            oneshotMaxDuration = this
          }

      attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_max_duration, dropletsMaxDuration.toInt()).toLong()
          .apply {
            dropletsMaxDuration = this
            oneshotMaxDuration = this
          }

      backgroundMaxDuration =
          attrs.getInt(R.styleable.AnimatedDropletWidget_background_max_duration, backgroundMaxDuration.toInt()).toLong()


    } finally {
      attrs.recycle()
      Log.d("AnimatedDropletWidget", "viewProperties: \n ${printViewAttributes()}")
    }
  }

  @CallSuper protected fun applyCustomAtrributes() {
    Log.d("AnimatedDropletWidget", "applyCustomAtrributes")
    globalMaxDuration
  }
  //</editor-fold>

  private fun setupView() {
    Log.d("AnimatedDropletWidget", "setupView")
    addOnLayoutChangeListener(this::onLayoutBoundsChanged)
    clipChildren = false
  }

  private fun onLayoutBoundsChanged(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int,
      oldBottom: Int) {
    if (oldLeft == 0 && oldRight == 0 && oldTop == 0 && oldBottom == 0 && left != 0 && right != 0 && top != 0 && bottom != 0)
      onLayoutFirstMeasurementApplied()
  }

  @CallSuper protected fun onLayoutFirstMeasurementApplied() {
    Log.d("AnimatedDropletWidget", "onLayoutFirstMeasurementApplied")

    createCircularDropletsLayers(layerCount = 6)
    createCircularDropletBackgroundLayers(layerCount = 3)


//    oneShotDropletView = createCircularDropletView(BASE_OVAL_STROKE_THICKNESS, android.R.color.holo_purple)
//        .apply {
//          hide()
//          addView(this)
//        }

    if (drawableAlpha != 0 && drawableSize != 0 && drawableSrc != android.R.color.transparent) {
      drawableView = createFrontDrawableView(drawableRes = drawableSrc, alpha = drawableAlpha, size = drawableSize)
      addView(drawableView)
    }
  }

  @SuppressLint("LogConditional")
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
  @SuppressLint("LogConditional")
  private fun createCircularDropletBackgroundLayers(layerCount: Int) {
    Log.d("AnimatedDropletWidget", "createCircularDropletBackgroundLayers, layerCount: $layerCount")
    (0 until layerCount).mapTo(circularDropletBackgroundLayers) { layerIndex ->
      createCircularBackgroundView(android.R.color.holo_red_dark)
    }

    addViews(circularDropletBackgroundLayers, {child, index ->
          animateCircularBackground(
        targetView = child,
        startTime = 0L,
        duration = BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS,
        repeatDelay = BASE_ANIMATION_BACKGROUND_REPEAT_DELAY_MILLIS,
        pathRandomFactor = 0.002f
    )
    })
//    circularDropletBackgroundView1 = createCircularBackgroundView(android.R.color.holo_red_dark)
//    circularDropletBackgroundView1.hide()
//    addView(circularDropletBackgroundView1)
//    animateCircularBackground(
//        targetView = circularDropletBackgroundView1,
//        startTime = 0L,
//        duration = BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS,
//        repeatDelay = BASE_ANIMATION_BACKGROUND_REPEAT_DELAY_MILLIS,
//        pathRandomFactor = 0.002f
//    )

//    circularDropletBackgroundView2 = createCircularBackgroundView(android.R.color.holo_red_dark)
//    circularDropletBackgroundView2.hide()
//    addView(circularDropletBackgroundView2)
//    animateCircularBackground(
//        targetView = circularDropletBackgroundView2,
//        startTime = BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS / 2,
//        duration = BASE_ANIMATION_BACKGROUND_LENGTH_MILLIS,
//        repeatDelay = BASE_ANIMATION_BACKGROUND_REPEAT_DELAY_MILLIS,
//        pathRandomFactor = 0.01f
//    )
  }


  //<editor-fold desc="Creating views">
  @SuppressLint("LogConditional")
  private fun createCircularDropletView(thickness: Float, @ColorRes colourId: ResourceId)
      = createImageViewWithDrawable(context, createCircularDropletDrawable(thickness, colourId))
      .also {
        Log.d("AnimatedDropletWidget", "createCircularDropletView, thickness: $thickness, colourId: ${colourId
            .toResourceEntryName(context)}, view: ${it.asString()}")
      }

  @SuppressLint("LogConditional")
  private fun createCircularBackgroundView(@ColorRes colourId: ResourceId)
      = createImageViewWithDrawable(context, createCircularBackgroundDrawable(colourId))
      .also {
        Log.d("AnimatedDropletWidget", "createCircularBackgroundView, colourId: ${colourId.toResourceEntryName(context)}, " +
            "view: ${it.asString()}")
      }

  @SuppressLint("LogConditional")
  private fun createFrontDrawableView(@DrawableRes drawableRes: ResourceId, alpha: Percentage, size: Percentage)
      = createImageViewWithDrawable(context, drawableRes.let { resources.getDrawable(drawableRes) })
      .apply {
        this.alpha = alpha / 100f
//        this.layoutParams = LayoutParams(this@AnimatedDropletWidget.layoutParams.width, this@AnimatedDropletWidget.layoutParams.height)
        this.setSize(
            (this@AnimatedDropletWidget.layoutParams.width * size / 100f).toInt(),
            (this@AnimatedDropletWidget.layoutParams.height * size / 100f).toInt())
        this.center(this@AnimatedDropletWidget.layoutParams.width, this@AnimatedDropletWidget.layoutParams.height)
      }
      .also {
        Log.d("AnimatedDropletWidget", "createFrontDrawableView, drawableRes: ${drawableRes.toResourceEntryName(context)}" +
            "alpha: $alpha, size: $size, view: ${it.asString()}")
      }
  //</editor-fold>


  //<editor-fold desc="Creating drawables">
  @SuppressLint("LogConditional")
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

  @SuppressLint("LogConditional")
  private fun createCircularBackgroundDrawable(@ColorRes colourId: ResourceId) =
      ShapeDrawable(OvalShape()).apply {
        this.intrinsicHeight = this@AnimatedDropletWidget.height
        this.intrinsicWidth = this@AnimatedDropletWidget.width
        this.paint.style = FILL
        this.paint.color = resources.getColor(colourId)
      }.also {
        Log.d("AnimatedDropletWidget", "createCircularBackgroundDrawable, drawable: ${it.asString()}")
      }

  //</editor-fold>

  //<editor-fold desc="Animate views">
  @SuppressLint("LogConditional")
  //todo: add endsize to this
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
              interpolator = FastOutLinearInInterpolator(),
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

  @SuppressLint("LogConditional")
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
  @SuppressLint("LogConditional")
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
        animation.setListenerBy(
            onStart = { Log.v("AnimatedDropletWidget", "createScalingAnimation.onStart") },
            onEnd = { Log.v("AnimatedDropletWidget", "createScalingAnimation.onEnd") },
            onRepeat = {
              Log.v("AnimatedDropletWidget", "createScalingAnimation.onRepeat")
              animation.startOffset = repeatDelay
            })
      }


  @SuppressLint("LogConditional")
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
            animation.setListenerBy(
                onStart = { Log.v("AnimatedDropletWidget", "createScalingAnimation.onStart") },
                onEnd = { Log.v("AnimatedDropletWidget", "createScalingAnimation.onEnd") },
                onRepeat = {
                  Log.v("AnimatedDropletWidget", "createScalingAnimation.onRepeat")
                  animation.startOffset = repeatDelay
                })
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
      children[i].apply { addView(this); childApply.invoke(this, i);  }
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

  private fun printViewAttributes() = StringBuilder(1024)
      .append(
          "drawableSrc: ${drawableSrc.toResourceEntryName(context)}\n" +
              "drawableAlpha: $drawableAlpha\n" +
              "dropletCount: $dropletCount\n" +
              "backgroundLayersCount: $backgroundLayersCount\n" +
              "oneshotLayersCount: $oneshotLayersCount\n" +
              "globalRandomInfluence: $globalRandomInfluence\n" +
              "globalMaxDuration: $globalMaxDuration\n" +
              "globalColour: $globalColour\n" +
              "globalColourDistribution: $globalColourDistribution\n" +
              "globalInterpolator: $globalInterpolator\n" +
              "dropletsMaxDuration: $dropletsMaxDuration\n" +
              "dropletsMaxDurationDistribution: $dropletsMaxDurationDistribution\n" +
              "dropletsSpawnsize: $dropletsSpawnsize\n" +
              "dropletsEndsizeMin: $dropletsEndsizeMin\n" +
              "dropletsEndsizeMax: $dropletsEndsizeMax\n" +
              "dropletsFadeout: $dropletsFadeout\n" +
              "dropletsThickness: $dropletsThickness\n" +
              "dropletsThicknessDistribution: $dropletsThicknessDistribution\n" +
              "dropletsInterpolator: $dropletsInterpolator\n" +
              "backgroundMaxDuration: $backgroundMaxDuration\n" +
              "backgroundEndsizeMin: $backgroundEndsizeMin\n" +
              "backgroundEndsizeMax: $backgroundEndsizeMax\n" +
              "backgroundColour: $backgroundColour\n" +
              "backgroundColourDistribution: $backgroundColourDistribution\n" +
              "backgroundInterpolator: $backgroundInterpolator\n" +
              "oneshotMaxDuration: $oneshotMaxDuration\n" +
              "oneshotColour: $oneshotColour\n" +
              "oneshotInterpolator: $oneshotInterpolator")
}