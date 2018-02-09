package com.android.szparag.animateddropletwidgetshowcase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_mock_skype.*
import java.util.concurrent.TimeUnit

class MockSkypeActivity : BaseMockActivity() {

  private lateinit var callTimerDisposable: Disposable
  override val presetString = R.string.showcase_screen_skype_snackbar_content

  companion object IntentFactory {
    fun getStartingIntent(packageContext: Context) = Intent(packageContext, MockSkypeActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setStatusAndNavigationBarColour(R.color.mockSkypeStatusBarColour, R.color.mockSkypeNavigationBarColour)
    }
    setContentView(R.layout.activity_mock_skype)
    callTimerDisposable = Observable
        .interval(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { setCallDurationTimeTextView(0) }
        .subscribeBy { seconds -> setCallDurationTimeTextView(seconds) }
  }


  override fun onDestroy() {
    super.onDestroy()
    callTimerDisposable.dispose()
  }

  @SuppressLint("SetTextI18n")
  private fun setCallDurationTimeTextView(seconds: Long) {
    callDurationTextView.text = "${seconds / 60L}:${String.format("%02d", seconds.rem(60))}"
  }
}
