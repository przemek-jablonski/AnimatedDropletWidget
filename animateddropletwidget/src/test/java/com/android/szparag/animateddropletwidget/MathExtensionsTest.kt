package com.android.szparag.animateddropletwidget

import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Random

class MathExtensionsTest {

  private lateinit var random: Random

  @Before fun setup() {
    random = Random(System.currentTimeMillis())
  }

  @Test fun `Drawing with nextFloat() should not go out of given bounds (positive range) (1000 runs)`() {
    val min = 0f
    val max = 5f
    var randomValue: Float
    for (i in 0 until 10000) {
      randomValue = random.nextFloat(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextFloat() should not go out of given bounds (negative range) (1000 runs)`() {
    val min = -100f
    val max = -95f
    var randomValue: Float
    for (i in 0 until 10000) {
      randomValue = random.nextFloat(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextFloat() should not go out of given bounds (mixed range) (1000 runs)`() {
    val min = -10f
    val max = 10f
    var randomValue: Float
    for (i in 0 until 10000) {
      randomValue = random.nextFloat(min, max)
      assertTrue(randomValue in min..max)
    }
  }

  @Test fun `Drawing with nextDouble() should not go out of given bounds (positive range) (1000 runs)`() {
    val min = 0.0
    val max = 15.0
    var randomValue: Double
    for (i in 0 until 10000) {
      randomValue = random.nextDouble(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextDouble() should not go out of given bounds (negative range) (1000 runs)`() {
    val min = -100.0
    val max = -85.0
    var randomValue: Double
    for (i in 0 until 10000) {
      randomValue = random.nextDouble(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextDouble() should not go out of given bounds (mixed range) (1000 runs)`() {
    val min = -50.0
    val max = 50.0
    var randomValue: Double
    for (i in 0 until 10000) {
      randomValue = random.nextDouble(min, max)
      assertTrue(randomValue in min..max)
    }
  }

  @Test fun `Drawing with nextInt() should not go out of given bounds (positive range) (1000 runs)`() {
    val min = 0
    val max = 15
    var randomValue: Int
    for (i in 0 until 10000) {
      randomValue = random.nextInt(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextInt() should not go out of given bounds (negative range) (1000 runs)`() {
    val min = -100
    val max = -85
    var randomValue: Int
    for (i in 0 until 10000) {
      randomValue = random.nextInt(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextInt() should not go out of given bounds (mixed range) (1000 runs)`() {
    val min = -50
    val max = 50
    var randomValue: Int
    for (i in 0 until 10000) {
      randomValue = random.nextInt(min, max)
      assertTrue(randomValue in min..max)
    }
  }

  @Test fun `Drawing with nextLong() should not go out of given bounds (positive range) (1000 runs)`() {
    val min = 0L
    val max = 15L
    var randomValue: Long
    for (i in 0 until 10000) {
      randomValue = random.nextLong(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextLong() should not go out of given bounds (negative range) (1000 runs)`() {
    val min = -100L
    val max = -85L
    var randomValue: Long
    for (i in 0 until 10000) {
      randomValue = random.nextLong(min, max)
      assertTrue(randomValue in min..max)
    }
  }


  @Test fun `Drawing with nextLong() should not go out of given bounds (mixed range) (1000 runs)`() {
    val min = -50L
    val max = 50L
    var randomValue: Long
    for (i in 0 until 10000) {
      randomValue = random.nextLong(min, max)
      assertTrue(randomValue in min..max)
    }
  }


}