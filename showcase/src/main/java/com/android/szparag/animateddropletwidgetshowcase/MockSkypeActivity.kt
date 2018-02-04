package com.android.szparag.animateddropletwidgetshowcase

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_mock_skype.*
import java.util.concurrent.TimeUnit

class MockSkypeActivity : AppCompatActivity() {

  private lateinit var callTimerDisposable: Disposable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      window.statusBarColor = resources.getColor(R.color.mockSkypeStatusBarColour)
      window.navigationBarColor = resources.getColor(R.color.mockSkypeNavigationBarColour)
    }
    setContentView(R.layout.activity_mock_skype)
    callTimerDisposable = Observable.interval(1, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe {
        setCallDurationTimeTextView(0)
      }.subscribeBy { seconds ->
        setCallDurationTimeTextView(seconds)
      }
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
