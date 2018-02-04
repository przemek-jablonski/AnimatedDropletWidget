package com.android.szparag.animateddropletwidgetshowcase

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Window
import android.view.WindowManager

class MockSkypeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      window.statusBarColor = resources.getColor(R.color.mockSkypeStatusBarColour)
      window.navigationBarColor = resources.getColor(R.color.mockSkypeNavigationBarColour)
    }
    setContentView(R.layout.activity_mock_skype)
  }
}
