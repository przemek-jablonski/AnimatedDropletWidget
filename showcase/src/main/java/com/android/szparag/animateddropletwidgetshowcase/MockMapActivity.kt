package com.android.szparag.animateddropletwidgetshowcase

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.activity_mock_map.*


class MockMapActivity : BaseMockActivity() {

  companion object IntentFactory {
    fun getStartingIntent(packageContext: Context) = Intent(packageContext, MockMapActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//      window.setFlags(
//          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//      )
//    }
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      setStatusAndNavigationBarColour(
//          R.color.mockGoogleBlueTransparent,
//          null
//      )
//    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setStatusAndNavigationBarColour(
          R.color.mockGoogleBlueTransparent,
          null
      )
    }

    setContentView(R.layout.activity_mock_map)
  }

  override fun onStart() {
    super.onStart()
    val statusBarHeight = getStatusbarHeight()
    val toolbarLayoutParams = toolbar.layoutParams
    toolbarLayoutParams as ConstraintLayout.LayoutParams
    toolbarLayoutParams.topMargin += statusBarHeight
    toolbar.layoutParams = toolbarLayoutParams
    toolbar.invalidate()
  }

  //todo: this should be in extensions!
  fun dpFromPx(context: Context, px: Float): Float {
    return px / context.resources.displayMetrics.density
  }

  fun pxFromDp(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
  }

  private fun getStatusbarHeight(): Int {
    val rectangle = Rect()
    val window = window
    window.decorView.getWindowVisibleDisplayFrame(rectangle)
    val statusBarHeight = rectangle.top
    val contentViewTop = window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
    val titleBarHeight = contentViewTop - statusBarHeight
    return statusBarHeight
  }

}
