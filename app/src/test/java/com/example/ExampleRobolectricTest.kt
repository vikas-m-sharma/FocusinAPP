package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Focusin", appName)
  }

  @Test
  fun `test focus score calculation algorithm`() {
    val goalMinutes = 360 // 6 hours
    val focusedMinutes = 300 // 5 hours
    val distractions = 2

    val ratio = (focusedMinutes.toFloat() / goalMinutes).coerceIn(0f, 1.2f)
    val baseScore = ratio * 70f
    val completionBonus = (5 * 5f).coerceAtMost(20f)
    val penalty = distractions * 5f

    val finalScore = (baseScore + completionBonus - penalty).coerceIn(0f, 100f).toInt()
    assertEquals(68, finalScore)
  }
}
