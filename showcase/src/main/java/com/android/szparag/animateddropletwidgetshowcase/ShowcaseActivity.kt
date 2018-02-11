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
    screenPickerSkype.setOnClickListener { this startActivity MockSkypeActivity::class.java}
    screenPickerMaps.setOnClickListener { this startActivity MockMapActivity::class.java }
    screenPickerDigitalAssistant.setOnClickListener { this startActivity MockDigitalAssistantActivity::class.java }
    screenPickerSportsTracker.setOnClickListener { this startActivity MockSportsTrackerActivity::class.java }
//    this startActivity MockSportsTrackerActivity::class.java
  }
}


infix fun Context.startActivity(targetActivity: Class<*>) =
  startActivity(Intent(this, targetActivity))
