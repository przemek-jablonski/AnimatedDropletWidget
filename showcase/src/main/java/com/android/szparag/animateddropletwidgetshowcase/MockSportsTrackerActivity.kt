package com.android.szparag.animateddropletwidgetshowcase

import android.content.Context
import android.content.Intent
import android.os.Bundle

class MockSportsTrackerActivity : BaseMockActivity() {
  override val presetString = R.string.showcase_screen_skype_snackbar_content

  companion object IntentFactory {
    //todo: this can be as an extension
    fun getStartingIntent(packageContext: Context) = Intent(packageContext, MockSportsTrackerActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_mock_sports_tracker)
  }
}
