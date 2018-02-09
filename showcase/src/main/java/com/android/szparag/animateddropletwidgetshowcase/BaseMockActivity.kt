package com.android.szparag.animateddropletwidgetshowcase

import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

typealias ResourceId = Int

abstract class BaseMockActivity : AppCompatActivity() {

  abstract val presetString: ResourceId

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  protected fun setStatusAndNavigationBarColour(@ColorRes statusBarColour: ResourceId, @ColorRes navigationBarColour: ResourceId? = statusBarColour) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    navigationBarColour?.let { window.navigationBarColor = resources.getColor(navigationBarColour) }
    window.statusBarColor = resources.getColor(statusBarColour)
  }

  override fun onStart() {
    super.onStart()
    showPresetSnackbar(presetString)
  }

  protected fun showPresetSnackbar(@StringRes stringRes: ResourceId) {
    Snackbar
        .make(findViewById(android.R.id.content), resources.getString(stringRes), Snackbar.LENGTH_LONG)
        .show()
  }

}