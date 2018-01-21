package com.android.szparag.animateddropletwidgetshowcase

import com.android.szparag.animateddropletwidget.clamp
import com.android.szparag.animateddropletwidget.inverseLerp
import com.android.szparag.animateddropletwidget.lerp
import com.android.szparag.animateddropletwidget.lerpLong
import com.android.szparag.animateddropletwidget.nextDouble
import com.android.szparag.animateddropletwidget.nextFloat
import com.android.szparag.animateddropletwidget.nextInt
import com.android.szparag.animateddropletwidget.nextLong
import com.android.szparag.animateddropletwidget.randomVariation
import junit.framework.TestCase
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Before
import org.junit.Test
import java.util.Random
import kotlin.math.roundToLong

private const val MATH_TESTS_REPEAT_COUNT = 10000
private const val MATH_EQUALITY_ERROR_THRESHOLD = 0.001f

class MathExtensionsTest {

  private lateinit var random: Random

  @Before fun setup() {
    random = Random(System.currentTimeMillis())
  }

  //<editor-fold desc="nextFloat() / nextDouble() / nextInt() / nextLong()">
  @Test fun `Drawing with nextFloat() should not go out of bounds (positive range) (10000 runs)`() {
    val min = 0f
    val max = 5f
    var output: Float
    repeat {
      output = random.nextFloat(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextFloat() should not go out of bounds (negative range) (10000 runs)`() {
    val min = -100f
    val max = -95f
    var output: Float
    repeat {
      output = random.nextFloat(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextFloat() should not go out of bounds (mixed range) (10000 runs)`() {
    val min = -10f
    val max = 10f
    var output: Float
    repeat {
      output = random.nextFloat(min, max)
      assertThat(output).isBetween(min, max)
    }
  }

  @Test fun `Drawing with nextDouble() should not go out of bounds (positive range) (10000 runs)`() {
    val min = 0.0
    val max = 15.0
    var output: Double
    repeat {
      output = random.nextDouble(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextDouble() should not go out of bounds (negative range) (10000 runs)`() {
    val min = -100.0
    val max = -85.0
    var output: Double
    repeat {
      output = random.nextDouble(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextDouble() should not go out of bounds (mixed range) (10000 runs)`() {
    val min = -50.0
    val max = 50.0
    var output: Double
    repeat {
      output = random.nextDouble(min, max)
      assertThat(output).isBetween(min, max)
    }
  }

  @Test fun `Drawing with nextInt() should not go out of bounds (positive range) (10000 runs)`() {
    val min = 0
    val max = 15
    var output: Int
    repeat {
      output = random.nextInt(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextInt() should not go out of bounds (negative range) (10000 runs)`() {
    val min = -100
    val max = -85
    var output: Int
    repeat {
      output = random.nextInt(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextInt() should not go out of bounds (mixed range) (10000 runs)`() {
    val min = -50
    val max = 50
    var output: Int
    repeat {
      output = random.nextInt(min, max)
      assertThat(output).isBetween(min, max)
    }
  }

  @Test fun `Drawing with nextLong() should not go out of bounds (positive range) (10000 runs)`() {
    val min = 0L
    val max = 15L
    var output: Long
    repeat {
      output = random.nextLong(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextLong() should not go out of bounds (negative range) (10000 runs)`() {
    val min = -100L
    val max = -85L
    var output: Long
    repeat {
      output = random.nextLong(min, max)
      assertThat(output).isBetween(min, max)
    }
  }


  @Test fun `Drawing with nextLong() should not go out of bounds (mixed range) (10000 runs)`() {
    val min = -50L
    val max = 50L
    var output: Long
    repeat {
      output = random.nextLong(min, max)
      assertThat(output).isBetween(min, max)
    }
  }
  //</editor-fold>

  //<editor-fold desc="randomVariation() (Float / Double / Int / Long)">
  @Test fun `randomVariation() (Float) with 0 factor should return input (10000 runs)`() {
    val input = 133.7f
    val factor = 0f
    repeat {
      TestCase.assertEquals(input, input.randomVariation(random, factor))
    }
  }

  @Test fun `randomVariation() (Float) with positive factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 133.7f
    var factor: Float
    var output: Float
    repeat {
      factor = random.nextFloat(0.0001f, 10f)
      output = input.randomVariation(random, factor)
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }

  @Test fun `randomVariation() (Float) with positive factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -133.7f
    var factor: Float
    var output: Float
    repeat {
      factor = random.nextFloat(0.0001f, 10f)
      output = input.randomVariation(random, 0.0001f)
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Float) with negative factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 133.7f
    var factor: Float
    var output: Float
    repeat {
      factor = random.nextFloat(-10f, -0.0001f)
      output = input.randomVariation(random, factor)
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Float) with negative factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -133.7f
    var factor: Float
    var output: Float
    repeat {
      factor = random.nextFloat(-10f, -0.0001f)
      output = input.randomVariation(random, factor)
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }


  @Test fun `randomVariation() (Double) with 0 factor should return input (10000 runs)`() {
    val input = 133.7
    val factor = 0f
    repeat {
      TestCase.assertEquals(input, input.randomVariation(random, factor))
    }
  }

  @Test fun `randomVariation() (Double) with positive factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 133.7
    var factor: Float
    var output: Double
    repeat {
      factor = random.nextFloat(0.0001f, 10f)
      output = input.randomVariation(random, factor)
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }

  @Test fun `randomVariation() (Double) with positive factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -133.7
    var factor: Float
    var output: Double
    repeat {
      factor = random.nextFloat(0.0001f, 10f)
      output = input.randomVariation(random, factor)
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Double) with negative factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 133.7
    var factor: Float
    var output: Double
    repeat {
      factor = random.nextFloat(-10f, -0.0001f)
      output = input.randomVariation(random, factor)
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Double) with negative factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -133.7
    var factor: Float
    var output: Double
    repeat {
      factor = random.nextFloat(-10f, -0.0001f)
      output = input.randomVariation(random, factor)
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }


  @Test fun `randomVariation() (Int) with 0 factor should return input (10000 runs)`() {
    val input = 1337
    val factor = 0f
    repeat {
      TestCase.assertEquals(input, input.randomVariation(random, factor))
    }
  }

  @Test fun `randomVariation() (Int) with positive factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 1337
    var factor: Int
    var output: Int
    repeat {
      factor = random.nextInt(0, 100)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }

  @Test fun `randomVariation() (Int) with positive factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -1337
    var factor: Int
    var output: Int
    repeat {
      factor = random.nextInt(0, 100)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Int) with negative factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 1337
    var factor: Int
    var output: Int
    repeat {
      factor = random.nextInt(-100, -0)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Int) with negative factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -1337
    var factor: Int
    var output: Int
    repeat {
      factor = random.nextInt(-100, -0)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }

  @Test fun `randomVariation() (Long) with 0L factor should return input (10000 runs)`() {
    val input = 1337L
    val factor = 0f
    repeat {
      TestCase.assertEquals(input, input.randomVariation(random, factor))
    }
  }

  @Test fun `randomVariation() (Long) with positive factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 1337L
    var factor: Long
    var output: Long
    repeat {
      factor = random.nextLong(0L, 10000L)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }

  @Test fun `randomVariation() (Long) with positive factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -1337L
    var factor: Long
    var output: Long
    repeat {
      factor = random.nextLong(0L, 10000L)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Long) with negative factor and positive input should not go out of bounds (10000 runs)`() {
    val input = 1337L
    var factor: Long
    var output: Long
    repeat {
      factor = random.nextLong(-10000L, -0L)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input + input * factor, input - input * factor)
    }
  }

  @Test fun `randomVariation() (Long) with negative factor and negative input should not go out of bounds (10000 runs)`() {
    val input = -1337L
    var factor: Long
    var output: Long
    repeat {
      factor = random.nextLong(-10000L, -0L)
      output = input.randomVariation(random, factor.toFloat())
      assertThat(output).isBetween(input - input * factor, input + input * factor)
    }
  }
  //</editor-fold>

  //<editor-fold desc="clamp()">
  @Test fun `clamp() called with out of bounds values should coerce`() {
    val min = 0f
    val max = 10f
    var input: Float
    repeat {
      input = random.nextFloat(min - 10f, min - 0.01f)
      assertThat(input.clamp(max, min)).isEqualTo(min)
      input = random.nextFloat(max + 0.01f, max + 10f)
      assertThat(input.clamp(max, min)).isEqualTo(max)
    }
  }

  @Test fun `clamp() called with in-bounds values should return input`() {
    val min = 0f
    val max = 10f
    var input: Float
    repeat {
      input = random.nextFloat(min, max)
      assertThat(input.clamp(max, min)).isBetween(min, max)
    }
  }

  @Test fun `clamp() called with with edge values should return input`() {
    val min = 0f
    val max = 10f
    var input: Float
    input = max
    assertThat(input.clamp(max, min)).isEqualTo(max)
    input = min
    assertThat(input.clamp(max, min)).isEqualTo(min)
  }

  @Test fun `clamp() called with in-bounds values when min = max should return input`() {
    val min = 0f
    val max = 0f
    val input = 0f
    assertThat(input.clamp(max, min)).isEqualTo(min)
    assertThat(input.clamp(max, min)).isEqualTo(max)
  }
  //</editor-fold>

  //<editor-fold desc="lerp()">
  @Test fun `lerp() (Float) given positive range should interpolate accordingly`() {
    val a = 0f
    val b = 10f
    var factor: Float
    repeat {
      factor = random.nextFloat(0.001f, 0.999f)
      assertThat(lerp(a, b, factor)).isEqualTo(b * factor)
    }
  }

  @Test fun `lerp() (Long) given positive range should interpolate accordingly`() {
    val a = 0L
    val b = 10L
    var factor: Float
    repeat {
      factor = random.nextFloat(0.001f, 0.999f)
      assertThat(lerp(a, b, factor)).isEqualTo(b * factor)
    }
  }

  @Test fun `lerp() (Float) given negative range should interpolate accordingly`() {
    val a = -10f
    val b = 0f
    var factor: Float
    repeat {
      factor = random.nextFloat(0.001f, 0.999f)
      assertThat(lerp(a, b, factor)).isEqualTo(a * (1 - factor), within(MATH_EQUALITY_ERROR_THRESHOLD))
    }
  }

  @Test fun `lerp() (Float) given mixed range should interpolate accordingly`() {
    val a = -5f
    val b = 5f
    var factor: Float
    repeat {
      factor = random.nextFloat(0.001f, 0.999f)
      assertThat(lerp(a, b, factor)).isEqualTo((factor - 0.5f) * 10, within(MATH_EQUALITY_ERROR_THRESHOLD))
    }
  }


  @Test fun `lerp() (Float) given inverted range should interpolate as with 1 - factor`() {
    var a: Float
    var b: Float
    var factor: Float
    repeat {
      a = random.nextFloat(50f, 100f)
      b = random.nextFloat(0f, 50f)
      factor = random.nextFloat(0.001f, 0.999f)
      assertThat(lerp(a, b, factor)).isEqualTo(lerp(b, a, 1 - factor), within(MATH_EQUALITY_ERROR_THRESHOLD))
    }
  }

  @Test fun `lerp() (Float) given positive range and factor over 1 should go out of bounds`() {
    val a = 0f
    val b = 10f
    var factor: Float
    repeat {
      factor = random.nextFloat(1f, 10f)
      assertThat(lerp(a, b, factor)).isEqualTo(b * factor)
    }
  }

  @Test fun `lerpLong() should produce only rounded values of lerp() (Float)`() {
    var a: Float
    var b: Float
    var factor: Float
    repeat {
      a = random.nextFloat(-100f, 100f)
      b = random.nextFloat(-100f, 100f)
      factor = random.nextFloat(0.001f, 0.999f)
      val outputLerpLong = lerpLong(a.roundToLong(), b.roundToLong(), factor)
      assertThat(lerp(a, b, factor).roundToLong()).isBetween(outputLerpLong - 1, outputLerpLong + 1) //approximation due to casting issues
    }
  }
  //</editor-fold>


  //<editor-fold desc="inverseLerp()">
  @Test fun `inverseLerp() given actual value in bounds should return factor accordingly`() {
    val a = 0
    val b = 10
    var actual: Float
    repeat {
      actual = random.nextFloat(0f, 10f)
      assertThat(inverseLerp(a, b, actual)).isEqualTo(actual / b.toFloat(), within(MATH_EQUALITY_ERROR_THRESHOLD))
    }
  }

  @Test fun `inverseLerp() given actual value out of bounds (positive) should return factor ceiling (1)`() {
    val a = 0
    val b = 10
    var actual: Float
    repeat {
      actual = random.nextFloat(10f, 20f)
      assertThat(inverseLerp(a, b, actual)).isEqualTo(1f)
    }
  }

  @Test fun `inverseLerp() given actual value out of bounds (negative) should return factor floor (0)`() {
    val a = 0
    val b = 10
    var actual: Float
    repeat {
      actual = random.nextFloat(-10f, 0f)
      assertThat(inverseLerp(a, b, actual)).isEqualTo(0f)
    }
  }
  //</editor-fold>


  private fun repeat(count: Int = MATH_TESTS_REPEAT_COUNT, action: () -> Unit) {
    for (i in 0 until count) action.invoke()
  }

}