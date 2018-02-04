package com.android.szparag.animateddropletwidget

import android.R.color
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Path
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.graphics.ColorUtils
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.android.szparag.animateddropletwidget.AnimatedDropletWidget.WidgetPreset.*
import timber.log.Timber
import java.util.*
import kotlin.math.max
import kotlin.math.min

typealias Millis = Long
typealias ResourceId = Int
typealias Percentage = Int
typealias Factor = Float

/**
 * Created by Przemyslaw Jablonski (github.com/sharaquss, pszemek.me) on 15/11/2017.
 */


//<editor-fold desc="Default XML attribute values for global settings">
private const val ATTRS_DROPLET_COUNT = 5
private const val ATTRS_BACKGROUND_LAYERS_COUNT = 2
private const val ATTRS_ONESHOT_COUNT = 1
private const val ATTRS_GLOBAL_RANDOM_INFLUENCE: Factor = 1.00F
private const val ATTRS_GLOBAL_MAX_DURATION: Millis = 5000 //todo: BASE_ANIMATION_LENGTH_MILLIS?
private const val ATTRS_GLOBAL_COLOUR: ResourceId = color.holo_red_dark
//</editor-fold>

//<editor-fold desc="Default XML attribute values for drawable layer">
private const val ATTRS_DRAWABLE: ResourceId = android.R.mipmap.sym_def_app_icon
private const val ATTRS_DRAWABLE_SIZE: Percentage = 75
private const val ATTRS_DRAWABLE_ALPHA: Percentage = 100
//</editor-fold>

//<editor-fold desc="Default XML attribute values for droplets layers">
private const val ATTRS_DROPLETS_MAX_DURATION: Millis = ATTRS_GLOBAL_MAX_DURATION
private const val ATTRS_DROPLETS_SPAWNSIZE: Percentage = 0
private const val ATTRS_DROPLETS_ENDSIZE_MAX: Percentage = 100
private const val ATTRS_DROPLETS_ENDSIZE_MIN: Percentage =
  ATTRS_DRAWABLE_SIZE + (ATTRS_DROPLETS_ENDSIZE_MAX - ATTRS_DROPLETS_ENDSIZE_MAX) / 3 //todo: this 3 as constant
private const val ATTRS_DROPLETS_FADEOUT: Factor = 1.00F
private const val ATTRS_DROPLETS_THICKNESS = 10.00F
//</editor-fold>

//<editor-fold desc="Default XML attribute values for background layers">
private const val ATTRS_BACKGROUND_MAX_DURATION: Millis = ATTRS_GLOBAL_MAX_DURATION
private const val ATTRS_BACKGROUND_ENDSIZE_MAX: Percentage = 100
private const val ATTRS_BACKGROUND_COLOUR: ResourceId = ATTRS_GLOBAL_COLOUR
private const val ATTRS_BACKGROUND_FADEOUT: Factor = 1.00F
//</editor-fold>

//<editor-fold desc="Default XML attribute values for One-Shot layers">
private const val ATTRS_ONESHOT_MAX_DURATION: Millis = ATTRS_DROPLETS_MAX_DURATION
private const val ATTRS_ONESHOT_COLOUR: ResourceId = ATTRS_GLOBAL_COLOUR
//</editor-fold>

//<editor-fold desc="Default internal values">
private const val BASE_ANIMATION_LENGTH: Millis = 5000L

private const val DRAWABLES_BASE_INTERNAL_MARGIN: Dp = 4
private const val DRAWABLE_BACKGROUND_INTERNAL_MARGIN: Dp = DRAWABLES_BASE_INTERNAL_MARGIN
private const val DRAWABLE_DROPLET_INTERNAL_MARGIN: Dp = DRAWABLES_BASE_INTERNAL_MARGIN * 2
//</editor-fold>

//private const val TAG_INDEX = 1337
private const val TAG_BACKGROUND_VAL = "BACKGROUND"
private const val TAG_DROPLET_VAL = "DROPLET"
//private const val TAG_ONESHOT_VAL = "ONESHOT"
private const val TAG_DRAWABLE_VAL = "DRAWABLE"

private const val BACKGROUND_DURATION_BASE_RANDOM_FACTOR: Factor = 0.05F
private const val BACKGROUND_REPEATDELAY_BASE_RANDOM_FACTOR: Factor = BACKGROUND_DURATION_BASE_RANDOM_FACTOR
private const val BACKGROUND_STARTTIME_BASE_RANDOM_FACTOR: Factor = BACKGROUND_DURATION_BASE_RANDOM_FACTOR / 4F
private const val BACKGROUND_ENDSIZE_BASE_RANDOM_FACTOR: Factor = 0.05F

private const val BACKGROUND_DURATION_MINIMUM_FACTOR: Factor = 0.60F
private const val BACKGROUND_ENDSIZE_MIN_OVERHEAD: Percentage = 5

private const val DROPLETS_DURATION_BASE_RANDOM_FACTOR: Factor = 0.10F
private const val DROPLETS_REPEATDELAY_BASE_RANDOM_FACTOR: Factor = DROPLETS_DURATION_BASE_RANDOM_FACTOR * 2F
private const val DROPLETS_STARTTIME_BASE_RANDOM_FACTOR: Factor = DROPLETS_DURATION_BASE_RANDOM_FACTOR / 4F
private const val DROPLETS_ENDSIZE_BASE_RANDOM_FACTOR: Factor = 0.05F
private const val DROPLETS_SPAWNSIZE_BASE_RANDOM_FACTOR: Factor = 0.05F
private const val DROPLETS_DURATION_MINIMUM_FACTOR: Factor = 0.60F

private const val PRESET_BREATH_DURATION: Millis = 5000

private const val INVALID_RESOURCE_ID: ResourceId = -1

@SuppressLint("BinaryOperationInTimber") //todo: remove timber
open class AnimatedDropletWidget : FrameLayout {

  //<editor-fold desc="View properties">
  /**
   * View properties.
   * Populated with constants with default xml attributes at creation time.
   * Some of those can be overwritten if user chooses to do so in xml
   * @see fetchCustomAttributes
   */
  private var preset = NONE

  @DrawableRes
  private var drawableSrc = ATTRS_DRAWABLE
  private var drawableSize = ATTRS_DRAWABLE_SIZE
  private var drawableAlpha = ATTRS_DRAWABLE_ALPHA

  private var circularDropletsLayersCount = ATTRS_DROPLET_COUNT
  private var backgroundLayersCount = ATTRS_BACKGROUND_LAYERS_COUNT
  private var oneshotLayersCount = ATTRS_ONESHOT_COUNT
  private var globalRandomInfluence = ATTRS_GLOBAL_RANDOM_INFLUENCE
  private var globalMaxDuration: Millis = ATTRS_GLOBAL_MAX_DURATION
  private var globalColour = ATTRS_GLOBAL_COLOUR

  private var dropletsMaxDuration = ATTRS_DROPLETS_MAX_DURATION
  private var dropletsSpawnsize = ATTRS_DROPLETS_SPAWNSIZE
  private var dropletsEndsizeMin = ATTRS_DROPLETS_ENDSIZE_MIN
  private var dropletsEndsizeMax = ATTRS_DROPLETS_ENDSIZE_MAX
  private var dropletsFadeout = ATTRS_DROPLETS_FADEOUT
  private var dropletsThickness = ATTRS_DROPLETS_THICKNESS

  private var backgroundMaxDuration = ATTRS_BACKGROUND_MAX_DURATION
  private var backgroundEndsizeMin = (drawableSize * (1 + BACKGROUND_ENDSIZE_MIN_OVERHEAD)).coerceAtMost(100)
  private var backgroundEndsizeMax = ATTRS_BACKGROUND_ENDSIZE_MAX
  private var backgroundColour = ATTRS_BACKGROUND_COLOUR
  private var backgroundColourAdditional = ATTRS_BACKGROUND_COLOUR
  private var backgroundFadeout = ATTRS_BACKGROUND_FADEOUT

  private var oneshotMaxDuration = ATTRS_ONESHOT_MAX_DURATION
  private var oneshotColour = ATTRS_ONESHOT_COLOUR
  //</editor-fold>

  //todo: unify - there are vars, lateinit vars and vals here
  private val circularBackgroundLayers = arrayListOf<View>()
  private val circularDropletsLayers = arrayListOf<View>()
  private var oneShotDropletView: View? = null
  private var drawableView: ImageView? = null

  private val random by lazy { Random() }


  //todo: drawable layoutParams in xml - maybe someone wants drawable to be WRAP_CONTENT?

  //todo: callbacks: onOneShotAnimationStarted() / onOneShotAnimationFinished()
  //todo: callbacks: onBackgroundLayerAnimationStarted() / onDropletLayerAnimationStarted()
  //todo: internal animation values should be stored as fields and shared between animations (with some multiplier)

  enum class WidgetPreset { NONE, DROPLETS, FLOW, RADAR, IRREGULAR, BREATH, RANDOM
  }

//  enum class WidgetInterpolator { PREDEFINED,
//    ACCELERATE,
//    DECELERATE,
//    ACCELERATE_DECELERATE,
//    BOUNCE,
//    OVERSHOOT
//  }

  //<editor-fold desc="Custom view Lifecycle callbacks">
  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    Timber.plant(Timber.DebugTree())
    Timber.d(" [${hashCode()}] ctor")
    clipChildren = false
    context.theme.obtainStyledAttributes(attrs, R.styleable.AnimatedDropletWidget, defStyleAttr, 0)
      ?.let { typedArray ->
        try {
          fetchCustomAttributes(typedArray)
          validateCustomAttributes()
          applyCustomAttributes()
        } finally {
          typedArray.recycle()
          Timber.d(" [${hashCode()}] viewProperties: \n ${printViewAttributes()}")
        }
      }
    constructChildViews()
    attachChildViews()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var maxHeight = 0
    var maxWidth = 0
    var childState = 0

    getChildren().forEach { child ->
      measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
      val params = child.layoutParams as LayoutParams
      maxWidth = Math.max(maxWidth, child.measuredWidth + params.leftMargin + params.rightMargin)
      maxHeight = Math.max(maxHeight, child.measuredHeight + params.topMargin + params.bottomMargin)
      childState = View.combineMeasuredStates(childState, child.measuredState)
    }

    maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground()
    maxHeight += getPaddingTopWithForeground() + getPaddingBottomWithForeground()
    maxHeight = Math.max(maxHeight, suggestedMinimumHeight)
    maxWidth = Math.max(maxWidth, suggestedMinimumWidth)

    //applying measurement dimensions to parent (AnimatedDropletWidget)
    setMeasuredDimension(
      min(View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
      View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT))
    )

    //applying measurement dimensions to children (background layers)
    circularBackgroundLayers.forEach { layer ->
      layer.measure(makeMeasureSpec(measuredWidth, EXACTLY), makeMeasureSpec(measuredHeight, EXACTLY))
    }

    //applying measurement dimensions to children (droplet layers)
    circularDropletsLayers.forEach { layer ->
      layer.measure(makeMeasureSpec(measuredWidth, EXACTLY), makeMeasureSpec(measuredHeight, EXACTLY))
    }

    //applying measurement dimensions to child (oneshot)
    oneShotDropletView?.measure(makeMeasureSpec(measuredWidth, EXACTLY), makeMeasureSpec(measuredHeight, EXACTLY))

    //applying measurement dimensions to child (front drawable)
    drawableView?.measure(
      makeMeasureSpec((measuredWidth * drawableSize / 100f).toInt(), EXACTLY),
      makeMeasureSpec((measuredHeight * drawableSize / 100f).toInt(), EXACTLY)
    )

    printViewParentAndChildren()
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    val parentLeft = getPaddingLeftWithForeground()
    val parentRight = right - left - getPaddingRightWithForeground()
    val parentTop = getPaddingTopWithForeground()
    val parentBottom = bottom - top - getPaddingBottomWithForeground()

    getChildren().forEach { child ->
      if (child.isNotHidden()) {
        val lp = child.layoutParams as LayoutParams
        val width = child.measuredWidth
        val height = child.measuredHeight
        val childLeft = parentLeft + (parentRight - parentLeft - width) / 2 + lp.leftMargin - lp.rightMargin
        val childTop = parentTop + (parentBottom - parentTop - height) / 2 + lp.topMargin - lp.bottomMargin
        child.layout(childLeft, childTop, childLeft + width, childTop + height)
      }
    }

    printViewParentAndChildren()
    constructDrawables()
    constructAnimations()
    startAnimations()
//    Single.timer(1000, TimeUnit.MILLISECONDS).subscribeBy(onSuccess = {
//      printViewParentAndChildren()
//    })
  }
  //</editor-fold>

  //<editor-fold desc="Fetching and validating xml attributes">
  private fun fetchCustomAttributes(attrs: TypedArray) {
    Timber.d(" [${hashCode()}] viewProperties: \n ${printViewAttributes()}")
      drawableSrc = attrs.getResourceId(R.styleable.AnimatedDropletWidget_drawable_src, drawableSrc)
      drawableSize = attrs.getInt(R.styleable.AnimatedDropletWidget_drawable_size, drawableSize)
      drawableAlpha = attrs.getInt(R.styleable.AnimatedDropletWidget_drawable_alpha, drawableAlpha)

      preset = preset.fromInt(attrs.getInt(R.styleable.AnimatedDropletWidget_preset, preset.ordinal))
      circularDropletsLayersCount = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_layers_count, circularDropletsLayersCount)
      backgroundLayersCount = attrs.getInt(R.styleable.AnimatedDropletWidget_background_layers_count, backgroundLayersCount)
      oneshotLayersCount = attrs.getInt(R.styleable.AnimatedDropletWidget_oneshot_count, oneshotLayersCount)
      globalRandomInfluence = attrs.getFloat(R.styleable.AnimatedDropletWidget_global_random_influence, globalRandomInfluence)
      globalMaxDuration = attrs.getInt(R.styleable.AnimatedDropletWidget_global_max_duration_ms, globalMaxDuration.toInt()).toLong() //todo: double casting here
      globalColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_global_colour, globalColour)

      dropletsMaxDuration = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_max_duration, dropletsMaxDuration.toInt()).toLong()
      dropletsSpawnsize = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_spawnsize, dropletsSpawnsize)
      dropletsEndsizeMin = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_endsize_min, dropletsEndsizeMin)
      dropletsEndsizeMax = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_endsize_max, dropletsEndsizeMax)
      dropletsFadeout = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_fadeout, dropletsFadeout)
      dropletsThickness = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_thickness, dropletsThickness)

      backgroundMaxDuration = attrs.getInt(R.styleable.AnimatedDropletWidget_background_max_duration, backgroundMaxDuration.toInt()).toLong()
      backgroundEndsizeMax = attrs.getInt(R.styleable.AnimatedDropletWidget_background_endsize_max, backgroundEndsizeMax)
      backgroundColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_background_colour, backgroundColour)
      backgroundColourAdditional = attrs.getResourceId(R.styleable.AnimatedDropletWidget_background_colour_additional, INVALID_RESOURCE_ID)

      oneshotColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_oneshot_colour, oneshotColour)

      attrs.getInt(R.styleable.AnimatedDropletWidget_global_max_duration_ms, globalMaxDuration.toInt()).toLong().apply {
        globalMaxDuration = this
        backgroundMaxDuration = this
        dropletsMaxDuration = this
        oneshotMaxDuration = this
      }

      attrs.getInt(
        R.styleable.AnimatedDropletWidget_droplets_max_duration, dropletsMaxDuration.toInt()
      ).toLong().apply {
        dropletsMaxDuration = this
        oneshotMaxDuration = this
      }

      backgroundMaxDuration = attrs.getInt(
        R.styleable.AnimatedDropletWidget_background_max_duration, backgroundMaxDuration.toInt()
      ).toLong()
      backgroundFadeout = attrs.getFloat(R.styleable.AnimatedDropletWidget_background_fadeout, backgroundFadeout)
  }

  private fun validateCustomAttributes() {
    if (drawableAlpha == 0 || drawableSize == 0 || drawableSrc == android.R.color.transparent) {
      drawableAlpha = 0
      drawableSize = 0
      drawableSrc = android.R.color.transparent
    }

    if (backgroundColourAdditional == INVALID_RESOURCE_ID) {
      backgroundColourAdditional = backgroundColour
    }
  }
  //</editor-fold>

  //<editor-fold desc="Applying fetched xml attributes">
  @Suppress("MemberVisibilityCanBePrivate")
  protected fun applyCustomAttributes() {
    applyPresetAttributes()
    Timber.d(" [${hashCode()}] viewProperties (after presets): \n ${printViewAttributes()}")
    applyGlobalAttributes()
    Timber.d(" [${hashCode()}] viewProperties (after global): \n ${printViewAttributes()}")
    applyDetailedAttributes()
    Timber.d(" [${hashCode()}] viewProperties (after detailed): \n ${printViewAttributes()}")
  }

  private fun applyPresetAttributes() {
    when(preset) {
      NONE -> {
        return
      }
      DROPLETS -> {

      }
      FLOW -> {

      }
      RADAR -> {

      }
      IRREGULAR -> {

      }
      BREATH -> {
        backgroundMaxDuration = 5000
        circularDropletsLayersCount = 0
        backgroundLayersCount = 5
        backgroundFadeout = 1f
        backgroundColour = android.R.color.holo_blue_dark //todo: should be lerping between blue and white
        backgroundColourAdditional = android.R.color.white
//        backgroundEndsizeMin = lerp(drawableSize, 100, 0.5f)
        backgroundEndsizeMin = 100
        backgroundEndsizeMax = 100
        globalRandomInfluence = 1.50f
      }
      RANDOM -> {

      }
    }
  }

  private fun applyGlobalAttributes() {

  }

  private fun applyDetailedAttributes() {

  }
  //</editor-fold>

  //<editor-fold desc="Constructing views (background layers, droplet layers, oneshot and front drawable)">
  private fun constructChildViews() {
    constructBackgroundLayers(layerCount = backgroundLayersCount)
    constructDropletLayers(layerCount = circularDropletsLayersCount)
    constructDropletOneshot()
    constructFrontDrawable(drawableAlpha = drawableAlpha, drawableSize = drawableSize, drawableSrc = drawableSrc)
  }

  @Suppress("DEPRECATION")
  private fun constructBackgroundLayers(layerCount: Int) {
    //todo: figure out colour distribution
    (0 until layerCount).mapTo(circularBackgroundLayers) { index ->
      ImageView(context).apply { tag = "$TAG_BACKGROUND_VAL[$index]" }
    }
  }

  private fun constructDropletLayers(layerCount: Int) {
    (0 until layerCount).mapTo(circularDropletsLayers) { index ->
      ImageView(context).apply { tag = "$TAG_DROPLET_VAL[$index]" }
    }
  }

  private fun constructDropletOneshot() {
    //...
  }

  @Suppress("DEPRECATION")
  private fun constructFrontDrawable(drawableAlpha: Percentage,
    drawableSize: Percentage, @DrawableRes drawableSrc: ResourceId) {
    if (drawableAlpha != 0 && drawableSize != 0 && drawableSrc != android.R.color.transparent) {
      drawableView = createImageViewWithDrawable(context, resources.getDrawable(drawableSrc)).apply {
        alpha = drawableAlpha / 100f
        tag = TAG_DRAWABLE_VAL
      }
    }
  }
  //</editor-fold>

  //<editor-fold desc="Attaching layers to this view as children">
  private fun attachChildViews() {
    addViews(circularBackgroundLayers)
    addViews(circularDropletsLayers)
    oneShotDropletView?.let(this::addView)
    drawableView?.let(this::addView)
  }
  //</editor-fold>

  //<editor-fold desc="Constructing Drawable (mDrawable) for background and droplet layers">
  private fun constructDrawables() {
    setDrawableForBackgroundLayers(measuredWidth, measuredHeight)
    setDrawableForDropletLayers(measuredWidth, measuredHeight)
  }

  private fun setDrawableForBackgroundLayers(width: Int, height: Int) {
    circularBackgroundLayers.forEachIndexed { index, layer ->
      layer as ImageView
      layer.setImageDrawable(
        createCircularBackgroundDrawable(
          width,
          height,
          ColorUtils.blendARGB(
            resources.getColor(backgroundColour),
            resources.getColor(backgroundColourAdditional),
            index / circularBackgroundLayers.size.toFloat()
          )
        )
      )
    }
  }

  private fun setDrawableForDropletLayers(width: Int, height: Int) {
    //todo: there should be dropletsColour alongside globalColour
    circularDropletsLayers.forEach { layer ->
      layer as ImageView
      layer.setImageDrawable(createCircularDropletDrawable(width, height, dropletsThickness, globalColour))
    }
  }

  private fun createCircularBackgroundDrawable(width: Int, height: Int, @ColorInt colourInt: Int) =
    ShapeDrawable(OvalShape()).apply {
      this.intrinsicHeight = height
      this.intrinsicWidth = width
      this.paint.style = FILL
      this.paint.color = colourInt
    }

  private fun createCircularDropletDrawable(width: Int, height: Int,
    strokeThickness: Float, @ColorRes colourRes: ResourceId) = ShapeDrawable(OvalShape()).apply {
    this.intrinsicHeight = height
    this.intrinsicWidth = width
    this.paint.strokeWidth = strokeThickness
    this.paint.style = STROKE
    this.paint.color = resources.getColor(colourRes)
  }
  //</editor-fold>

  //<editor-fold desc="Construct animations for each children">
  private fun constructAnimations() {
    constructBackgroundLayersAnimations(
      maxDuration = backgroundMaxDuration,
      endsizeMin = backgroundEndsizeMin,
      endsizeMax = backgroundEndsizeMax,
      fadeout = backgroundFadeout,
      randomFactor = globalRandomInfluence
    )

    constructDropletLayersAnimations(
      maxDuration = dropletsMaxDuration,
      spawnSize = dropletsSpawnsize,
      endsizeMin = dropletsEndsizeMin,
      endsizeMax = dropletsEndsizeMax,
      fadeout = dropletsFadeout,
      randomFactor = globalRandomInfluence
    )
  }

  private fun constructBackgroundLayersAnimations(maxDuration: Millis, endsizeMin: Percentage, endsizeMax: Percentage,
    fadeout: Factor, randomFactor: Float) {
    //todo: distribution and random is not used
    val minDuration = (BACKGROUND_DURATION_MINIMUM_FACTOR * maxDuration).toLong()
    var lerpFactor: Float
    var actualDuration: Millis
    circularBackgroundLayers.forEachIndexed { index, layer ->
      lerpFactor = index / circularBackgroundLayers.size.toFloat()
      actualDuration = lerp(minDuration, maxDuration, lerpFactor)
      animateCircularBackground(
        targetView = layer,
        duration = actualDuration
          .randomVariation(random, BACKGROUND_DURATION_BASE_RANDOM_FACTOR * randomFactor),
        startTime = lerp(0L, maxDuration, lerpFactor)
          .randomVariation(random, BACKGROUND_STARTTIME_BASE_RANDOM_FACTOR * randomFactor)
          .coerceAtLeast(0L),
        repeatDelay = actualDuration
          .randomVariation(random, BACKGROUND_REPEATDELAY_BASE_RANDOM_FACTOR * randomFactor)
          .coerceAtLeast(0L),
        endSize = lerp(endsizeMin, endsizeMax, lerpFactor)
          .randomVariation(random, BACKGROUND_ENDSIZE_BASE_RANDOM_FACTOR * randomFactor)
          .clamp(0, 100),
        fadeout = fadeout
      )
    }
  }

  private fun constructDropletLayersAnimations(maxDuration: Millis, spawnSize: Percentage, endsizeMin: Percentage,
    endsizeMax: Percentage, fadeout: Factor, randomFactor: Float) {
    //todo: distribution and random is not used
    val minDuration = (DROPLETS_DURATION_MINIMUM_FACTOR * maxDuration).toLong()
    var lerpFactor: Float
    var actualDuration: Millis
    circularDropletsLayers.forEachIndexed { index, layer ->
      lerpFactor = index / circularDropletsLayers.size.toFloat()
      actualDuration = lerp(minDuration, maxDuration, lerpFactor)
      animateCircularDroplet(
        targetView = layer,
        duration = actualDuration
          .randomVariation(random, DROPLETS_DURATION_BASE_RANDOM_FACTOR * randomFactor),
        startTime = lerp(0L, maxDuration, lerpFactor)
          .randomVariation(random, DROPLETS_STARTTIME_BASE_RANDOM_FACTOR * randomFactor)
          .coerceAtLeast(0L),
        repeatDelay = actualDuration
          .randomVariation(random, DROPLETS_REPEATDELAY_BASE_RANDOM_FACTOR * randomFactor)
          .coerceAtLeast(0L),
        spawnSize = spawnSize
          .randomVariation(random, DROPLETS_SPAWNSIZE_BASE_RANDOM_FACTOR * randomFactor)
          .clamp(0, 100),
        endSize = lerp(endsizeMin, endsizeMax, lerpFactor)
          .randomVariation(random, DROPLETS_ENDSIZE_BASE_RANDOM_FACTOR * randomFactor)
          .clamp(0, 100),
        fadeout = fadeout
      )
    }
  }

  private fun animateDropletOneshot() {}
  //</editor-fold>

  //<editor-fold desc="Start animations on each children">
  private fun startAnimations() {
    startBackgroundLayersAnimations()
    startDropletLayersAnimations()
  }

  private fun startBackgroundLayersAnimations() {
    circularBackgroundLayers.forEach { animation?.start() }
  }

  private fun startDropletLayersAnimations() {
    circularDropletsLayers.forEach { animation?.start() }
  }
  //</editor-fold


  //<editor-fold desc="AnimationSet construction wrappers">
  private fun animateCircularBackground(targetView: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
    endSize: Percentage, fadeout: Factor) {
    targetView.show()
    AnimationSet(false).also { set ->
      set.addAnimation(
        createScalingAnimation(
          parentContainer = this,
          duration = duration,
          startTime = startTime,
          repeatDelay = repeatDelay,
          xyStart = 0f,
          xyEnd = endSize / 100f - 0.10f,
          interpolator = OvershootInterpolator(1.50f),
          timeCutoff = 1.0f
        )
      )
      set.addAnimation(
        createFadeoutAnimation(
          parentContainer = this,
          duration = duration,
          repeatDelay = repeatDelay,
          startTime = startTime,
          alphaStart = 1f + (0.25f * fadeout) - 0.25f,
          alphaEnd = 0.00f,
          interpolator = AccelerateInterpolator(1.5f),
          timeCutoff = 0.99f - (0.99f * fadeout) + 0.99f
        )
      )
      set.attach(targetView)
    }
  }

  private fun animateCircularDroplet(targetView: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
    spawnSize: Percentage, endSize: Percentage, fadeout: Factor) {
    AnimationSet(false).also { set ->
      set.fillAfter = true
      set.isFillEnabled = true
      set.addAnimation(
        createScalingAnimation(
          parentContainer = this,
          duration = duration,
          startTime = startTime,
          repeatDelay = repeatDelay,
          xyStart = spawnSize / 100f,
          xyEnd = (endSize / 100f).coerceAtMost(100f - dropletsThickness / max(this@AnimatedDropletWidget.height, this@AnimatedDropletWidget.width).toFloat()),
          interpolator = AnticipateOvershootInterpolator(1.05f),
          timeCutoff = 0.95f
        )
      )
      set.addAnimation(
        createFadeoutAnimation(
          parentContainer = this,
          duration = duration,
          repeatDelay = repeatDelay,
          startTime = startTime,
          alphaStart = 0.75f,
          alphaEnd = 0.00f,
          interpolator = AccelerateInterpolator(1.05f),
          timeCutoff = 0.97f - (0.97f * fadeout) + 0.97f
        )
      )
      set.attach(targetView)
    }
  }
  //</editor-fold>

  //<editor-fold desc="Creating animations primitives (scaling and alpha fadeout)">
  private fun createScalingAnimation(parentContainer: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
    xyStart: Float, xyEnd: Float, interpolator: Interpolator, timeCutoff: Float = 1.0f, oneShot: Boolean = false) =
    ScaleAnimation(
      xyStart, xyEnd, xyStart, xyEnd, parentContainer.width / 2f, parentContainer.height / 2f
    ).also { animation ->
      animation.duration = duration
      animation.startOffset = startTime
      animation.repeatCount = if (oneShot) 0 else Animation.INFINITE
      animation.repeatMode = Animation.RESTART
      animation.interpolator = CutoffInterpolator(sourceInterpolator = interpolator, cutoff = timeCutoff)
      animation.setListenerBy(onRepeat = {
        animation.startOffset = repeatDelay //todo: should it be here?
      })
    }

  private fun createFadeoutAnimation(parentContainer: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
    alphaStart: Float, alphaEnd: Float, interpolator: Interpolator, timeCutoff: Float, oneShot: Boolean = false) =
    AlphaAnimation(alphaStart, alphaEnd).also { animation ->
      animation.duration = duration
      animation.startOffset = startTime
      animation.repeatCount = if (oneShot) 0 else Animation.INFINITE
      animation.repeatMode = Animation.RESTART
      animation.isFillEnabled = true
      animation.fillAfter = true
      animation.fillBefore = false
      animation.interpolator = CutoffInterpolator(sourceInterpolator = interpolator, cutoff = timeCutoff)
      animation.setListenerBy(onRepeat = {
        animation.startOffset = repeatDelay
      })
    }
  //</editor-fold>


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

  private fun addViews(children: List<View>) = children.forEach(this::addView)

  override fun addView(child: View?) {
    super.addView(child)
  }

  private fun getPaddingLeftWithForeground() = paddingLeft + (foreground?.run { this.padding.left } ?: 0)

  private fun getPaddingRightWithForeground() = paddingRight + (foreground?.run { this.padding.right } ?: 0)

  private fun getPaddingTopWithForeground() = paddingTop + (foreground?.run { this.padding.top } ?: 0)

  private fun getPaddingBottomWithForeground() = paddingBottom + (foreground?.run { this.padding.bottom } ?: 0)

  private fun printViewParentAndChildren() {
    Timber.v("[parent]:\n${this@AnimatedDropletWidget.complexString}")
    getChildren().forEachIndexed { index, view -> Timber.v("[$index]\n${view.complexString}") }
  }

  //todo: as extension?
  private fun setMeasuredDimension(commonDimension: Int) =
    setMeasuredDimension(commonDimension, commonDimension)

  private fun printViewAttributes() = StringBuilder(1024).append(
    "drawableSrc: ${drawableSrc.toResourceEntryName(
      context
    )}\n" +
        "drawableAlpha: $drawableAlpha\n" +
        "preset: $preset\n" +
        "drawableSize: $drawableSize\n" + "circularDropletsLayersCount: " + "$circularDropletsLayersCount\n" + "backgroundLayersCount: $backgroundLayersCount\n" + "oneshotLayersCount: $oneshotLayersCount\n" + "globalRandomInfluence: $globalRandomInfluence\n" + "globalMaxDuration: $globalMaxDuration\n" + "globalColour: $globalColour\n" + "dropletsMaxDuration: $dropletsMaxDuration\n" + "dropletsSpawnsize: $dropletsSpawnsize\n" + "dropletsEndsizeMin: $dropletsEndsizeMin\n" + "dropletsEndsizeMax: $dropletsEndsizeMax\n" + "dropletsFadeout: $dropletsFadeout\n" + "dropletsThickness: $dropletsThickness\n" + "backgroundMaxDuration: $backgroundMaxDuration\n" + "backgroundEndsizeMin: $backgroundEndsizeMin\n" + "backgroundEndsizeMax: $backgroundEndsizeMax\n" + "backgroundFadeout: $backgroundFadeout\n" + "backgroundColour: $backgroundColour\n" + "oneshotMaxDuration: $oneshotMaxDuration\n" + "oneshotColour: $oneshotColour\n"
  )
}