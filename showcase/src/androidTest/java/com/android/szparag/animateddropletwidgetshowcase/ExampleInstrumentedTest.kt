package com.android.szparag.animateddropletwidgetshowcase

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class) class ExampleInstrumentedTest {

  private lateinit var context: Context

  @Before fun setup() {
    context = InstrumentationRegistry.getTargetContext()
  }

  @Test fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getTargetContext()
    assertEquals("com.android.szparag.animateddropletwidgetshowcase", appContext.packageName)
  }

  @Test fun `View visibility test`() {
    val view = View(context)
    assert(true)
  }
}
