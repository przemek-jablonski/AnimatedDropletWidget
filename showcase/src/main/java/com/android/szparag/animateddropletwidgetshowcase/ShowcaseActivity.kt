package com.android.szparag.animateddropletwidgetshowcase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_showcase.*
import kotlin.reflect.KClass

class ShowcaseActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_showcase)
    screenPickerSkype doOnClick { this startActivity MockSkypeActivity::class.java}
    screenPickerMaps doOnClick { this startActivity MockMapActivity::class.java }
    screenPickerDigitalAssistant doOnClick { this startActivity MockDigitalAssistantActivity::class.java }
    screenPickerSportsTracker doOnClick { this startActivity MockSportsTrackerActivity::class.java }
  }
}

