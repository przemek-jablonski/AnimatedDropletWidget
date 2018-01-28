package com.android.szparag.animateddropletwidget

import android.R.color
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.support.annotation.CallSuper
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

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
private const val ATTRS_DROPLETS_THICKNESS = 5.00F
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

private const val DRAWABLES_BASE_INTERNAL_MARGIN: Dp = 4
private const val DRAWABLE_BACKGROUND_INTERNAL_MARGIN: Dp = DRAWABLES_BASE_INTERNAL_MARGIN
private const val DRAWABLE_DROPLET_INTERNAL_MARGIN: Dp = DRAWABLES_BASE_INTERNAL_MARGIN * 2
//</editor-fold>

private const val TAG_INDEX = 1337
private const val TAG_BACKGROUND_VAL = "BACKGROUND"
private const val TAG_DROPLET_VAL = "DROPLET"
private const val TAG_ONESHOT_VAL = "ONESHOT"
private const val TAG_DRAWABLE_VAL = "DRAWABLE"

@SuppressLint("BinaryOperationInTimber") //todo: remove timber
open class AnimatedDropletWidget : FrameLayout {

  //<editor-fold desc="View properties">
  /**
   * View properties.
   * Populated with constants with default xml attributes at creation time.
   * Some of those can be overwritten if user chooses to do so in xml
   * @see fetchCustomAttributes
   */
  private var preset = WidgetPreset.NONE

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
  private val circularBackgroundLayers = arrayListOf<View>()
  private val circularDropletsLayers = arrayListOf<View>()
  private var oneShotDropletView: View? = null
  private var drawableView: ImageView? = null

  private val random by lazy { Random() }


  //todo: drawable layoutParams in xml - maybe someone wants drawable to be WRAP_CONTENT?

  //todo: callbacks: onOneShotAnimationStarted() / onOneShotAnimationFinished()
  //todo: callbacks: onBackgroundLayerAnimationStarted() / onDropletLayerAnimationStarted()
  //todo: internal animation values should be stored as fields and shared between animations (with some multiplier)

  enum class WidgetPreset { NONE, DROPLETS, FLOW, RADAR, IRREGULAR }
  enum class WidgetInterpolator { PREDEFINED, ACCELERATE, DECELERATE, ACCELERATE_DECELERATE, BOUNCE, OVERSHOOT }

  //<editor-fold desc="Custom view Lifecycle callbacks">
  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context, attrs, defStyleAttr
  ) {
    Timber.plant(Timber.DebugTree())
    Timber.d("ctor")
    clipChildren = false
    context.theme.obtainStyledAttributes(attrs, R.styleable.AnimatedDropletWidget, defStyleAttr, 0)
      ?.let { fetchCustomAttributes(it) }
    constructChildViews()
    attachChildViews()
  }


  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    Timber.d("onMeasure, widthMeasureSpec: $widthMeasureSpec, heightMeasureSpec: $heightMeasureSpec")
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
      View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
      View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT)
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
    Timber.d("onLayout, changed: $changed, left: $left, top: $top, right: $right, bottom: $bottom")
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
    startAnimations()
    Single.timer(1000, TimeUnit.MILLISECONDS).subscribeBy(onSuccess = {
      printViewParentAndChildren()
    })
  }
  //</editor-fold>

  @CallSuper
  protected fun fetchCustomAttributes(attrs: TypedArray) {
    Timber.d("fetchCustomAttributes")
    Timber.d("viewProperties: \n ${printViewAttributes()}")
    try {
      drawableSrc = attrs.getResourceId(R.styleable.AnimatedDropletWidget_drawable_src, drawableSrc)
      drawableSize = attrs.getInt(R.styleable.AnimatedDropletWidget_drawable_size, drawableSize)
      drawableAlpha = attrs.getInt(R.styleable.AnimatedDropletWidget_drawable_alpha, drawableAlpha)
      preset.fromInt(attrs.getInt(R.styleable.AnimatedDropletWidget_preset, preset.ordinal))
      circularDropletsLayersCount = attrs.getInt(
        R.styleable.AnimatedDropletWidget_droplet_count, circularDropletsLayersCount
      )
      backgroundLayersCount = attrs.getInt(
        R.styleable.AnimatedDropletWidget_background_layers_count, backgroundLayersCount
      )
      oneshotLayersCount = attrs.getInt(R.styleable.AnimatedDropletWidget_oneshot_count, oneshotLayersCount)
      globalRandomInfluence = attrs.getFloat(
        R.styleable.AnimatedDropletWidget_global_random_influence, globalRandomInfluence
      )
      globalMaxDuration = attrs.getInt(
        R.styleable.AnimatedDropletWidget_global_max_duration_ms, globalMaxDuration.toInt()
      ).toLong() //todo: double casting here
      globalColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_global_colour, globalColour)
      globalColourDistribution = attrs.getFloat(
        R.styleable.AnimatedDropletWidget_global_colour_distribution, globalColourDistribution
      )
      globalInterpolator.fromInt(
        attrs.getInt(
          R.styleable.AnimatedDropletWidget_global_interpolator, globalInterpolator.ordinal
        )
      )

      dropletsMaxDuration = attrs.getInt(
        R.styleable.AnimatedDropletWidget_droplets_max_duration, dropletsMaxDuration.toInt()
      ).toLong()
      dropletsMaxDurationDistribution = attrs.getFloat(
        R.styleable.AnimatedDropletWidget_droplets_max_duration, dropletsMaxDurationDistribution
      )
      dropletsSpawnsize = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_spawnsize, dropletsSpawnsize)
      dropletsEndsizeMin = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_endsize_min, dropletsEndsizeMin)
      dropletsEndsizeMax = attrs.getInt(R.styleable.AnimatedDropletWidget_droplets_endsize_max, dropletsEndsizeMax)
      dropletsFadeout = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_fadeout, dropletsFadeout)
      dropletsThickness = attrs.getFloat(R.styleable.AnimatedDropletWidget_droplets_thickness, dropletsThickness)
      dropletsThicknessDistribution = attrs.getFloat(
        R.styleable.AnimatedDropletWidget_droplets_thickness_distribution, dropletsThicknessDistribution
      )
      dropletsInterpolator.fromInt(
        attrs.getInt(
          R.styleable.AnimatedDropletWidget_droplets_interpolator, dropletsInterpolator.ordinal
        )
      )

      backgroundMaxDuration = attrs.getInt(
        R.styleable.AnimatedDropletWidget_background_max_duration, backgroundMaxDuration.toInt()
      ).toLong()
      backgroundEndsizeMax = attrs.getInt(
        R.styleable.AnimatedDropletWidget_background_endsize_max, backgroundEndsizeMax
      )
      backgroundColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_background_colour, backgroundColour)
      backgroundColourDistribution = attrs.getFloat(
        R.styleable.AnimatedDropletWidget_background_colour_distibution, backgroundColourDistribution
      )
      backgroundInterpolator.fromInt(
        attrs.getInt(
          R.styleable.AnimatedDropletWidget_background_interpolator, backgroundInterpolator.ordinal
        )
      )

      oneshotColour = attrs.getResourceId(R.styleable.AnimatedDropletWidget_oneshot_colour, oneshotColour)
      oneshotInterpolator.fromInt(
        attrs.getInt(
          R.styleable.AnimatedDropletWidget_oneshot_interpolator, oneshotInterpolator.ordinal
        )
      )


      attrs.getInt(
        R.styleable.AnimatedDropletWidget_global_max_duration_ms, globalMaxDuration.toInt()
      ).toLong().apply {
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

    } finally {
      attrs.recycle()
      Timber.d("viewProperties: \n ${printViewAttributes()}")
    }
  }

  private fun attachChildViews() {
    Timber.d("attachChildViews")
    addViews(circularBackgroundLayers)
    addViews(circularDropletsLayers)
    oneShotDropletView?.let { addView(it) }
    drawableView?.let { addView(it) }
  }


  //<editor-fold desc="Constructing views (background layers, droplet layers, oneshot and front drawable)">

  private fun constructChildViews() {
    Timber.d("constructChildViews")
    constructBackgroundLayers(
      layerCount = backgroundLayersCount,
      colourRes = backgroundColour,
      colourDistribution = backgroundColourDistribution,
      randomFactor = globalRandomInfluence
    )
    constructDropletLayers(
      layerCount = circularDropletsLayersCount,
      colourRes = globalColour, //todo: this should be dropletColour, even if it can't be changed in xml
      colourDistribution = globalColourDistribution, //todo: this should be dropletColour, even if it can't be changed in xml
      thickness = dropletsThickness,
      thicknessDistribution = dropletsThicknessDistribution,
      randomFactor = globalRandomInfluence
    )
    constructDropletOneshot()
    constructFrontDrawable(
      drawableAlpha = drawableAlpha, drawableSize = drawableSize, drawableSrc = drawableSrc
    )

  }

  @Suppress("DEPRECATION")
  private fun constructBackgroundLayers(layerCount: Int, @ColorRes colourRes: ResourceId, colourDistribution: Factor,
    randomFactor: Float) {
    Timber.d("constructBackgroundLayers, layerCount: $layerCount, colourRes: $colourRes, colourDistribution: $colourDistribution, randomFactor: $randomFactor")
    //todo: figure out colour distribution
    (0 until backgroundLayersCount).mapTo(circularBackgroundLayers) { index ->
      ImageView(context).apply { tag = "$TAG_BACKGROUND_VAL[$index]" }
    }
  }

  private fun constructDropletLayers(layerCount: Int, @ColorRes colourRes: ResourceId, colourDistribution: Factor,
    thickness: Float, thicknessDistribution: Factor, randomFactor: Float) {
    Timber.d("constructDropletLayers, layerCount: $layerCount, colourRes: $colourRes, colourDistribution: $colourDistribution, thickness: $thickness, thicknessDistribution: $thicknessDistribution, randomFactor: $randomFactor")
    (0 until circularDropletsLayersCount).mapTo(circularDropletsLayers) { index ->
      ImageView(context).apply { tag = "$TAG_DROPLET_VAL[$index]" }
    }
  }

  private fun constructDropletOneshot() {
    Timber.d("constructDropletOneshot")
  }


  @Suppress("DEPRECATION")
  private fun constructFrontDrawable(drawableAlpha: Percentage,
    drawableSize: Percentage, @DrawableRes drawableSrc: ResourceId) {
    Timber.d("constructFrontDrawableView, drawableAlpha: $drawableAlpha, drawableSize: $drawableSize, drawableSrc: $drawableSrc")
    if (drawableAlpha != 0 && drawableSize != 0 && drawableSrc != android.R.color.transparent) {
      drawableView = createImageViewWithDrawable(context, resources.getDrawable(drawableSrc)).apply {
        alpha = drawableAlpha / 100f
        tag = TAG_DRAWABLE_VAL
      }
    }
  }
  //</editor-fold>

  //<editor-fold desc="Constructing Drawable (mDrawable) for background and droplet layers">
  private fun setDrawableForBackgroundLayers(width: Int, height: Int) {
    Timber.d("setDrawableForBackgroundLayers, width: $width, height: $height")
    circularBackgroundLayers.forEach { layer ->
      layer as ImageView
      layer.setImageDrawable(createCircularBackgroundDrawable(width, height, backgroundColour))
    }
  }

  private fun setDrawableForDropletLayers(width: Int, height: Int) {
    Timber.d("setDrawableForDropletLayers, width: $width, height: $height")
    //todo: there should be dropletsColour alongside globalColour
    circularDropletsLayers.forEach { layer ->
      layer as ImageView
      layer.setImageDrawable(createCircularDropletDrawable(width, height, dropletsThickness, globalColour))
    }
  }
  //</editor-fold>

  private fun startAnimations() {
    Timber.d("startAnimations")
    //constructing drawables for background and droplet layers
    setDrawableForBackgroundLayers(measuredWidth, measuredHeight)
    setDrawableForDropletLayers(measuredWidth, measuredHeight)
    animateBackgroundLayers(
      maxDuration = backgroundMaxDuration,
      endsizeMin = backgroundEndsizeMin,
      endsizeMax = backgroundEndsizeMax,
      interpolator = backgroundInterpolator,
      randomFactor = globalRandomInfluence
    )

    animateDropletLayers(
      maxDuration = dropletsMaxDuration,
      durationDistribution = dropletsMaxDurationDistribution,
      spawnSize = dropletsSpawnsize,
      endsizeMin = dropletsEndsizeMin,
      endsizeMax = dropletsEndsizeMax,
      fadeout = dropletsFadeout,
      interpolator = dropletsInterpolator,
      randomFactor = globalRandomInfluence
    )
  }


  private fun animateBackgroundLayers(maxDuration: Millis, endsizeMin: Percentage, endsizeMax: Percentage,
    interpolator: WidgetInterpolator, randomFactor: Float) {
    Timber.d("animateBackgroundLayers, maxDuration: $maxDuration, endsizeMin: $endsizeMin, endsizeMax: $endsizeMax, interpolator: $interpolator, randomFactor: $randomFactor")
    //todo: distribution and random is not used
    val minDuration = (0.66f * maxDuration).toLong()
    var lerpFactor: Float
    circularBackgroundLayers.forEachIndexed { index, layer ->
      lerpFactor = index / circularBackgroundLayers.size.toFloat()
      animateCircularBackground(
        targetView = layer,
        startTime = lerp(minDuration, maxDuration, lerpFactor),
        repeatDelay = (lerp(minDuration, maxDuration, lerpFactor) * 0.33f).toLong(),
        duration = lerp(minDuration, maxDuration, lerpFactor),
        randomFactor = randomFactor
      )
    }
  }

  private fun animateDropletLayers(maxDuration: Millis, durationDistribution: Factor, spawnSize: Percentage,
    endsizeMin: Percentage, endsizeMax: Percentage, fadeout: Factor, interpolator: WidgetInterpolator,
    randomFactor: Float) {
    Timber.d("animateDropletLayers, maxDuration: $maxDuration, durationDistribution: $durationDistribution, spawnSize: $spawnSize, endsizeMin: $endsizeMin, endsizeMax: $endsizeMax, fadeout: $fadeout, interpolator: $interpolator, randomFactor: $randomFactor")
    //todo: distribution and random is not used
    val minDuration = (0.66f * maxDuration).toLong()
    var lerpFactor: Float
    circularDropletsLayers.forEachIndexed { index, layer ->
      lerpFactor = index / circularBackgroundLayers.size.toFloat()
      animateCircularDroplet(
        targetView = layer,
        duration = lerp(minDuration, maxDuration, lerpFactor),
        startTime = lerp(minDuration, maxDuration, lerpFactor),
        repeatDelay = (lerp(minDuration, maxDuration, lerpFactor) * 0.33f).toLong(),
        spawnSize = spawnSize,
        endSize = lerp(endsizeMin, endsizeMax, lerpFactor),
        randomFactor = randomFactor
      )
    }
  }

//  private fun animateDropletOneshot() {}


  private fun addViews(children: List<View>) = children.forEach { child -> addView(child) }


  override fun addView(child: View?) {
    Timber.d("addView, child: ${child?.complexString}")
    super.addView(child)
  }


  private fun createCircularBackgroundDrawable(width: Int, height: Int, @ColorRes colourRes: ResourceId) =
    ShapeDrawable(OvalShape()).apply {
      this.intrinsicHeight = this@AnimatedDropletWidget.height - DRAWABLE_BACKGROUND_INTERNAL_MARGIN.toPx(context)
      this.intrinsicWidth = this@AnimatedDropletWidget.width - DRAWABLE_BACKGROUND_INTERNAL_MARGIN.toPx(context)
      this.paint.style = FILL
      this.paint.color = resources.getColor(colourRes)
    }.also { Timber.d("createCircularBackgroundDrawable, drawable: ${it.asString()}") }

  private fun createCircularDropletDrawable(width: Int, height: Int,
    strokeThickness: Float, @ColorRes colourRes: ResourceId) = ShapeDrawable(OvalShape()).apply {
    this.intrinsicHeight = width - DRAWABLE_DROPLET_INTERNAL_MARGIN.toPx(context) - strokeThickness.toInt()
    this.intrinsicWidth = height - DRAWABLE_DROPLET_INTERNAL_MARGIN.toPx(context) - strokeThickness.toInt()
    this.paint.strokeWidth = strokeThickness
    this.paint.style = STROKE
    this.paint.color = resources.getColor(colourRes)
  }.also {
      Timber.d("createCircularDropletDrawable, drawable: ${it.asString()}")
    }


//  //<editor-fold desc="Creating layers">
////  
////  private fun createCircularDropletsLayers(layerCount: Int, @ColorRes colourRes: ResourceId,
////    colourDistribution: Float, maxDuration: Millis, durationDistribution: Factor,
////    spawnSize: Percentage, endSizeMin: Percentage, endSizeMax: Percentage, fadeout: Factor,
////    thickness: Float, thicknessDistribution: Factor, interpolator: WidgetInterpolator,
////    randomFactor: Float, tag: String = TAG_DROPLET_VAL) {
////    //todo: figure out distribution factors
////    //todo: randomFactor not used
////    //todo: interpolator not used
////    Timber.d(
////      
////      "createCircularDropletsLayers, layerCount: $layerCount, colourRes: $colourRes, " + "colourDistribution: $colourDistribution, maxDuration: $maxDuration, durationDistribution: $durationDistribution, " + "spawnSize: $spawnSize, endSizeMin: $endSizeMin, endSizeMax: $endSizeMax, fadeout: $fadeout, thickness: $thickness, " + "thicknessDistribution: $thicknessDistribution, interpolator: $interpolator, randomFactor: $randomFactor, tag: $tag"
////    )
////
////
////    (0 until layerCount).mapTo(circularDropletsLayers) { layerIndex ->
////      createCircularDropletView(thickness, colourRes, "$tag[$layerIndex]", randomFactor)
////    }
////    //todo: start animation should be as a separate function in onLayoutFirstMeasurementApplied
////    addViews(children = circularDropletsLayers, childApply = { child, index ->
////      val layerDependency = index / layerCount.toFloat()
////      animateCircularDroplet(
////        child,
////        maxDuration,
////        lerp(0L, maxDuration, layerDependency),
////        lerp(0L, maxDuration, layerDependency),
////        spawnSize,
////        lerp(endSizeMin, endSizeMax, layerDependency),
////        randomFactor
////      )
////    })
////  }
//
////  
////  private fun createCircularDropletBackgroundLayers(layerCount: Int, @ColorRes colorRes: Int,
////    colourDistribution: Float, duration: Millis, randomFactor: Float,
////    tag: String = TAG_BACKGROUND_VAL) {
////    Timber.d( "createCircularDropletBackgroundLayers, layerCount: $layerCount")
//////    (0 until layerCount).mapTo(circularBackgroundLayers) {
//////      createCircularBackgroundView(
//////        colourRes = ColorUtils.blendARGB(
//////          resources.getColor(colorRes), Color.TRANSPARENT, it / layerCount.toFloat()
//////        ), alpha = 100, tag = "$tag[$it]"
//////      ) //todo: colours distribution
//////    }
////
////    addViews(circularBackgroundLayers, { child, index ->
////      animateCircularBackground(
////        targetView = child, startTime = lerp(
////          0, duration, index / layerCount.toFloat().randomVariation(random, randomFactor)
////        ), //todo apply index
////        duration = duration, //todo apply index
////        randomFactor = randomFactor
////      )
////    })
////  }
//
//  
//  private fun createFrontDrawable(alpha: Percentage, size: Int, @DrawableRes src: Int,
//    tag: String = TAG_DRAWABLE_VAL) {
//    Timber.d( "createFrontDrawable, alpha: $alpha, size: $size, src: $src")
//    if (alpha != 0 && size != 0 && src != android.R.color.transparent) {
//      drawableView =
//          createFrontDrawableView(alpha = alpha, size = size, drawableRes = src, tag = tag)
//      addView(drawableView)
//    }
//  }
//  //</editor-fold>
//
//  //<editor-fold desc="Creating view layers (droplets and backgrounds)">
//  
//  private fun createCircularDropletView(thickness: Float, @ColorRes colourRes: ResourceId,
//    tag: String, randomFactor: Float) = createImageViewWithDrawable(
//    context, createCircularDropletDrawable(thickness, colourRes)
//  ).apply {
//    this.tag = tag
//    val size = this@AnimatedDropletWidget.size
//    this.setSize(size)
//  }.also {
//      Timber.d(
//        
//        "createCircularDropletView, thickness: $thickness, " + "colourRes: ${colourRes.toResourceEntryName(
//          context
//        )}, view: ${it.asString()}"
//      )
//    }
//
//  
//  private fun createCircularBackgroundView(@ColorInt colourRes: ResourceId, alpha: Percentage = 100,
//    tag: String) =
//    createImageViewWithDrawable(context, createCircularBackgroundDrawable(colourRes)).apply {
//      this.tag = tag
//      this.setSize(this@AnimatedDropletWidget.layoutParams.size)
//    }.also {
//        //        this.alpha = (alpha/100f).clamp(1f, 0f)
//        Timber.d(
//          
//          "createCircularBackgroundView, alpha: $alpha, " + "colourRes: $colourRes, view: ${it.asString()}"
//        )
//      }
//
//  
//  private fun createFrontDrawableView(alpha: Percentage,
//    size: Percentage, @DrawableRes drawableRes: ResourceId, tag: String) =
//    createImageViewWithDrawable(
//      context,
//      drawableRes.let { resources.getDrawable(drawableRes) }).apply {
//      this.tag = tag
////        this.setTag(TAG_INDEX, TAG_DRAWABLE_VAL)
//      this.alpha = alpha / 100f
////      val tmp = this@AnimatedDropletWidget.layoutParams.width
////      this.setSize(
////        (this@AnimatedDropletWidget.layoutParams.width * size / 100f).toInt(),
////        (this@AnimatedDropletWidget.layoutParams.height * size / 100f).toInt()
////      )
////      this.center(
////        this@AnimatedDropletWidget.layoutParams.width,
////        this@AnimatedDropletWidget.layoutParams.height
////      )
//    }.also {
//        Timber.d(
//          
//          "createFrontDrawableView, drawableRes: ${drawableRes.toResourceEntryName(context)}" + "alpha: $alpha, size: $size, view: ${it.asString()}"
//        )
//      }
//  //</editor-fold>
//
//  //<editor-fold desc="Creating drawables">
//  

//


  //
//  //</editor-fold>
//
//  //<editor-fold desc="Animating views">
//  
//  //todo: add endsize to this
  private fun animateCircularBackground(targetView: View, startTime: Millis, repeatDelay: Millis, duration: Millis,
    randomFactor: Float) {
    Timber.d("animateCircularBackground, targetView: $targetView, startTime: $startTime, duration: $duration, randomFactor: $randomFactor")
    targetView.show()
    AnimationSet(false).also { set ->
      set.addAnimation(
        createScalingAnimation(
          parentContainer = this,
          duration = duration,
          startTime = startTime,
          repeatDelay = repeatDelay,
          xyStart = 0f,
          xyEnd = 1f,
          interpolator = FastOutLinearInInterpolator(),
          timeCutoff = 1.0f
        )
      )
      set.addAnimation(
        createFadeoutAnimation(
          parentContainer = this,
          duration = duration,
          repeatDelay = repeatDelay,
          startTime = startTime,
          alphaStart = 1f,
          alphaEnd = 0.00f,
          interpolator = FastOutLinearInInterpolator(),
          timeCutoff = 0.99f
        )
      )
      set.attach(targetView)
    }.start()
  }


  private fun animateCircularDroplet(targetView: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
    spawnSize: Percentage, endSize: Percentage, randomFactor: Float) {
    Timber.d("animateCircularDroplet, targetView: $targetView, duration: $duration, startTime: $startTime, repeatDelay: $repeatDelay, spawnSize: $spawnSize, endSize: $endSize, randomFactor: $randomFactor")
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
          xyEnd = endSize / 100f,
          interpolator = AnticipateOvershootInterpolator(1.25f),
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
          timeCutoff = 0.98f
        )
      )
      set.attach(targetView)
    }.start()
  }
//
////  
////  private fun animateCircularDroplet(targetView: View, layerIndex: Int, layerCount: Int, layerDependency: Float, randomFactor: Float) {
////    val startTime = random.nextInt(ANIMATION_RANDOM_START_TIME_BOUND_MILLIS.toInt()).toLong()
////    val repeatDelayAddition = random.nextInt(ANIMATION_RANDOM_REPEAT_DELAY_BOUND_MILLIS.toInt()).toLong()
////    val layerValuesMultiplier = layerIndex / layerCount * layerDependency
////    val inverseLerp = inverseLerp(0, layerCount, layerIndex.toFloat())
//////    Timber.d(
//////      
//////      "animateCircularDroplet, layerIndex: $layerIndex, layerCount: $layerCount, layerDependency: $layerDependency, inverseLerp: $inverseLerp, layerValuesMultiplier: $layerValuesMultiplier"
//////    )
//////    targetView.show()
////    AnimationSet(false).also { set ->
////      set.fillAfter = true
////      set.isFillEnabled = true
////      set.addAnimation(
////        createScalingAnimation(
////          parentContainer = this,
////          duration = lerpLong(first = BASE_ANIMATION_LENGTH_MIN_MILLIS, second = BASE_ANIMATION_LENGTH_MILLIS, factor = inverseLerp),
////          startTime = startTime,
////          repeatDelay = BASE_ANIMATION_REPEAT_DELAY_MILLIS + repeatDelayAddition,
////          xyStart = 0.00f,
////          xyEnd = lerp(0.70f, 1.00f, inverseLerp),
////          interpolator = AnticipateOvershootInterpolator(lerp(1.33f, 0.25f, inverseLerp)),
////          timeCutoff = lerp(0.8f, 0.98f, inverseLerp)
////        )
////      )
////      set.addAnimation(
////        createFadeoutAnimation(
////          parentContainer = this,
////          duration = lerp(
////            first = BASE_ANIMATION_LENGTH_MIN_MILLIS,
////            second = BASE_ANIMATION_LENGTH_MILLIS,
////            factor = inverseLerp(0, layerCount, layerIndex.toFloat())
////          ).toLong(),
////          repeatDelay = BASE_ANIMATION_REPEAT_DELAY_MILLIS + repeatDelayAddition,
////          startTime = startTime,
//////              alphaStart = lerp(0.15f, 0.55f, inverseLerp),
////          alphaStart = lerp(0.50f, 1.00f, inverseLerp),
////          alphaEnd = 0.00f,
////          interpolator = AccelerateInterpolator(lerp(1.10f, 0.85f, inverseLerp)),
////          timeCutoff = lerp(0.90f, 0.97f, inverseLerp)
////        )
////      )
////      set.attach(targetView)
////    }.start()
////  }
//
//
//  //</editor-fold>
//
//  //<editor-fold desc="Animations builders">

  private fun createScalingAnimation(parentContainer: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
    xyStart: Float, xyEnd: Float, interpolator: Interpolator, timeCutoff: Float = 1.0f, oneShot: Boolean = false) =
    ScaleAnimation(
      xyStart, xyEnd, xyStart, xyEnd, parentContainer.width / 2f, parentContainer.height / 2f
    ).also { animation ->
      Timber.d(

        "createScalingAnimation, duration: $duration, startTime: $startTime, repeatDelay: $repeatDelay, " + "xyStart: $xyStart, xyEnd: $xyEnd, interpolator: ${interpolator::class.java.simpleName}, timeCutoff: $timeCutoff"
      )
      animation.duration = duration
      animation.startOffset = startTime
      animation.repeatCount = if (oneShot) 0 else Animation.INFINITE
      animation.repeatMode = Animation.RESTART
      animation.interpolator = CutoffInterpolator(sourceInterpolator = interpolator, cutoff = timeCutoff)
      animation.setListenerBy(onStart = {
        //              Timber.d( "createScalingAnimation.onStart")
      }, onEnd = {
        //              Timber.d( "createScalingAnimation.onEnd")
      }, onRepeat = {
        //              Timber.d( "createScalingAnimation.onRepeat")
        animation.startOffset = repeatDelay //todo: should it be here?
      })
    }

  //
//

  private fun createFadeoutAnimation(parentContainer: View, duration: Millis, startTime: Millis, repeatDelay: Millis,
    alphaStart: Float, alphaEnd: Float, interpolator: Interpolator, timeCutoff: Float, oneShot: Boolean = false) =
    AlphaAnimation(alphaStart, alphaEnd).also { animation ->
      Timber.d(

        "createFadeoutAnimation, duration: $duration, startTime: $startTime, repeatDelay: $repeatDelay, " + "alphaStart: $alphaStart, alphaEnd: $alphaEnd, interpolator: ${interpolator::class.java.simpleName}, timeCutoff: $timeCutoff"
      )
      animation.duration = duration
      animation.startOffset = startTime
      animation.repeatCount = if (oneShot) 0 else Animation.INFINITE
      animation.repeatMode = Animation.RESTART
      animation.isFillEnabled = true
      animation.fillAfter = true
      animation.fillBefore = false
      animation.interpolator = CutoffInterpolator(sourceInterpolator = interpolator, cutoff = timeCutoff)
      animation.setListenerBy(onStart = {
        //                  Timber.d( "createScalingAnimation.onStart")
      }, onEnd = {
        //                  Timber.d( "createScalingAnimation.onEnd")
      }, onRepeat = {
        //                  Timber.d( "createScalingAnimation.onRepeat")
        animation.startOffset = repeatDelay
      })
    }
//  //</editor-fold>
//
//  fun performOneShotAnimation() {
//    Timber.d( "performOneShotAnimation")
//    oneShotDropletView?.let {
//      this.show()
//      AnimationSet(false).also { set ->
//        set.fillAfter = true
//        set.isFillEnabled = true
//        set.addAnimation(
//          createScalingAnimation(
//            parentContainer = this,
//            duration = BASE_ANIMATION_LENGTH_MILLIS,
//            startTime = 0,
//            repeatDelay = 0,
//            xyStart = 0.00f,
//            xyEnd = 1.00f,
//            interpolator = AnticipateOvershootInterpolator(0.80f),
//            timeCutoff = 1.00f,
//            oneShot = true
//          )
//        )
//        set.addAnimation(
//          createFadeoutAnimation(
//            parentContainer = this,
//            duration = BASE_ANIMATION_LENGTH_MILLIS,
//            repeatDelay = 0,
//            startTime = 0,
//            alphaStart = 1.00f,
//            alphaEnd = 0.00f,
//            interpolator = AccelerateInterpolator(0.5f),
//            timeCutoff = 1.0f,
//            oneShot = true
//          )
//        )
//        set.attach(this)
//      }.start()
//    }
//  }
//
////  
////  private fun addViews(children: List<View>, childApply: (View, Int) -> (Unit)) {
////    Timber.d(
////      
////      "addViews, children.count: ${children.size}, children: ${children.map { it.asString() }}"
////    )
////    children.forEachIndexed { index, child ->
////      this@AnimatedDropletWidget.addView(child)
////      childApply.invoke(child, index)
////    }
////    Timber.d(
////       "addViews, this.children: ${getChildren().map { it.asString() }}"
////    )
////    Timber.d( "[parent]:\n${this@AnimatedDropletWidget.complexString}")
////    getChildren().forEachIndexed { index, view ->
////      Timber.d( "[$index]\n${view.complexString}")
////    }
////  }
//
//  private fun createInterpolator(randomFactor: Float) =
//    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) createInterpolatorApiLollipop(randomFactor)
//    else createInterpolatorPreApiLollipop()
//
//  @RequiresApi(VERSION_CODES.LOLLIPOP)
//  private fun createInterpolatorApiLollipop(randomFactor: Float) =
//    PathInterpolator(generateDropletBackgroundPath(random, randomFactor))
//
//  private fun createInterpolatorPreApiLollipop() = AccelerateDecelerateInterpolator()
//
//  //proven that this feels good on the ui with serious laboratory testing. true story
//  private fun generateDropletBackgroundPath(random: Random, randomFactor: Float = 0.005f) =
//    Path().apply {
//      moveTo(0.000f, 0.000f)
//      quadTo(0.065f, 0.325f, 0.150f, 0.400f.randomVariation(random, randomFactor))
//      lineTo(0.330f, 0.300f.randomVariation(random, randomFactor / 2f))
//      quadTo(0.390f, 0.630f, 0.420f, 0.690f.randomVariation(random, randomFactor))
//      lineTo(0.690f, 0.480f.randomVariation(random, randomFactor / 2f))
//      quadTo(0.725f, 0.85f, 0.740f, 0.900f.randomVariation(random, randomFactor))
//      lineTo(0.930f, 0.710f.randomVariation(random, randomFactor / 2f))
//      quadTo(0.965f, 0.925f, 1.000f, 1.000f)
//    }


  private fun getPaddingLeftWithForeground() = paddingLeft + (foreground?.run { this.padding.left } ?: 0)

  private fun getPaddingRightWithForeground() = paddingRight + (foreground?.run { this.padding.right } ?: 0)

  private fun getPaddingTopWithForeground() = paddingTop + (foreground?.run { this.padding.top } ?: 0)

  private fun getPaddingBottomWithForeground() = paddingBottom + (foreground?.run { this.padding.bottom } ?: 0)

  private fun printViewParentAndChildren() {
    Timber.v("[parent]:\n${this@AnimatedDropletWidget.complexString}")
    getChildren().forEachIndexed { index, view -> Timber.v("[$index]\n${view.complexString}") }
  }

  private fun printViewAttributes() =
    StringBuilder(1024).append("drawableSrc: ${drawableSrc.toResourceEntryName(context)}\n" + "drawableAlpha: $drawableAlpha\n" + "drawableSize: $drawableSize\n" + "circularDropletsLayersCount: $circularDropletsLayersCount\n" + "backgroundLayersCount: $backgroundLayersCount\n" + "oneshotLayersCount: $oneshotLayersCount\n" + "globalRandomInfluence: $globalRandomInfluence\n" + "globalMaxDuration: $globalMaxDuration\n" + "globalColour: $globalColour\n" + "globalColourDistribution: $globalColourDistribution\n" + "globalInterpolator: $globalInterpolator\n" + "dropletsMaxDuration: $dropletsMaxDuration\n" + "dropletsMaxDurationDistribution: $dropletsMaxDurationDistribution\n" + "dropletsSpawnsize: $dropletsSpawnsize\n" + "dropletsEndsizeMin: $dropletsEndsizeMin\n" + "dropletsEndsizeMax: $dropletsEndsizeMax\n" + "dropletsFadeout: $dropletsFadeout\n" + "dropletsThickness: $dropletsThickness\n" + "dropletsThicknessDistribution: $dropletsThicknessDistribution\n" + "dropletsInterpolator: $dropletsInterpolator\n" + "backgroundMaxDuration: $backgroundMaxDuration\n" + "backgroundEndsizeMin: $backgroundEndsizeMin\n" + "backgroundEndsizeMax: $backgroundEndsizeMax\n" + "backgroundColour: $backgroundColour\n" + "backgroundColourDistribution: $backgroundColourDistribution\n" + "backgroundInterpolator: $backgroundInterpolator\n" + "oneshotMaxDuration: $oneshotMaxDuration\n" + "oneshotColour: $oneshotColour\n" + "oneshotInterpolator: $oneshotInterpolator")
}