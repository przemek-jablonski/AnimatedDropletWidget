package com.android.szparag.animateddropletwidgetshowcase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_mock_digital_assistant.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class MockDigitalAssistantActivity : BaseMockActivity() {

  companion object IntentFactory {
    fun getStartingIntent(packageContext: Context) = Intent(packageContext, MockDigitalAssistantActivity::class.java)
  }

  override val presetString = R.string.showcase_screen_skype_snackbar_content
  private lateinit var encouragementStrings : MutableList<String>
  private lateinit var random : Random

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_mock_digital_assistant)

    random = Random()
    encouragementStrings = resources.getStringArray(R.array.showcase_screen_assistant_encouragements).toMutableList()
    shiftEncouragementStringsArray(encouragementStrings.toMutableList(), random)
    Observable
      .interval(1, TimeUnit.SECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .map { totalSeconds -> totalSeconds.rem(4) }
      .subscribeBy { totalSeconds ->
//        if (remSeconds == 3L) {
//
//        }
      }


  }

  override fun onResume() {
    super.onResume()
    val lapa = microphoneAnimation.layoutParams
    val maxDimen = max(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) * 1.15f
    lapa.width = maxDimen.toInt()
    lapa.height = maxDimen.toInt()
    microphoneAnimation.layoutParams = lapa
    microphoneAnimation.invalidate()
  }

  private fun shiftEncouragementStringsArray(array: List<String>, random: Random) {
    val shift = random.nextInt(array.size-1)+1
    val shiftedArray = array.toMutableList()
    for (i in 0 until array.size) { shiftedArray[i] = array[(i+shift).rem(array.size)] }
  }
}
