package com.android.szparag.animateddropletwidgetshowcase

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_showcase.*

class ShowcaseActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_showcase)
    screenPickerSkype.setOnClickListener { startActivity(MockSkypeActivity.getStartingIntent(this)) }
    screenPickerMaps.setOnClickListener { startActivity(MockMapActivity.getStartingIntent(this)) }
    screenPickerDigitalAssistant.setOnClickListener { startActivity(MockDigitalAssistantActivity.getStartingIntent(this)) }
  }

}
