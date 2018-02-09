package com.android.szparag.animateddropletwidgetshowcase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_mock_map.*
import java.util.*


class MockMapActivity : BaseMockActivity() {

  override val presetString = R.string.showcase_screen_skype_snackbar_content //todo: change that

  companion object IntentFactory {
    fun getStartingIntent(packageContext: Context) = Intent(packageContext, MockMapActivity::class.java)
  }

  private val random by lazy { Random() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setStatusAndNavigationBarColour(
      R.color.mockGoogleBlueTransparent, null
    )

    setContentView(R.layout.activity_mock_map)
  }

  override fun onStart() {
    super.onStart()
    val statusBarHeight = getStatusbarHeight()
    val toolbarLayoutParams = topToolbar.layoutParams
    toolbarLayoutParams as ConstraintLayout.LayoutParams
    toolbarLayoutParams.topMargin += statusBarHeight
    topToolbar.layoutParams = toolbarLayoutParams
    topToolbar.invalidate()

  }

  override fun onResume() {
    super.onResume()
    spawnAnimations(animationsContainer, random)
  }

  //todo: this should be in extensions!
  fun dpFromPx(context: Context, px: Float): Float {
    return px / context.resources.displayMetrics.density
  }

  fun pxFromDp(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
  }

  @SuppressLint("InflateParams")
  private fun spawnAnimations(target: FrameLayout, random: Random) {
    val rectangle = Rect()
    val window = window
    window.decorView.getWindowVisibleDisplayFrame(rectangle)
    val screenHeight = rectangle.bottom
    val screenWidth = rectangle.right
    val inflater = LayoutInflater.from(this)
    for (i in 0..(random.nextInt(5) + 5)) {
      target.addView(inflater.inflate(R.layout.view_mock_map_anim, null).apply {
        x = random.nextInt(screenWidth).toFloat()
        y = random.nextInt(screenHeight).toFloat()
        if (random.nextFloat() > 0.5f) animate().translationX(random.nextInt(screenWidth).toFloat()).translationY(
            random.nextInt(
              screenHeight
            ).toFloat()
          ).setDuration(random.nextInt(10000).toLong() + 35000).setStartDelay(random.nextInt(20000).toLong()).start()
      }, random.nextInt(75) + 50, random.nextInt(75) + 50)
    }

  }


  private fun getStatusbarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
      result = resources.getDimensionPixelSize(resourceId)
    }
    return result
  }


}
