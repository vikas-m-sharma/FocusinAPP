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

  @Test
  fun `test neet chapter progress calculation`() {
    val totalTopics = 12
    val completedTopics = 8
    val percentage = ((completedTopics.toFloat() / totalTopics) * 100).toInt()
    assertEquals(66, percentage)
  }

  @Test
  fun `test topic status cycle progression`() {
    fun nextStatus(current: String): String = when (current) {
      "NOT_STARTED" -> "IN_PROGRESS"
      "IN_PROGRESS" -> "COMPLETED"
      "COMPLETED" -> "NEEDS_REVISION"
      else -> "NOT_STARTED"
    }

    assertEquals("IN_PROGRESS", nextStatus("NOT_STARTED"))
    assertEquals("COMPLETED", nextStatus("IN_PROGRESS"))
    assertEquals("NEEDS_REVISION", nextStatus("COMPLETED"))
    assertEquals("NOT_STARTED", nextStatus("NEEDS_REVISION"))
  }

  @Test
  fun `verify google web client id resource exists`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val clientId = context.getString(R.string.google_web_client_id)
    org.junit.Assert.assertTrue(clientId.isNotBlank())
  }

  @Test
  fun `verify auth user representation`() {
    val user = com.example.data.auth.AuthUser(
      uid = "test-uid-123",
      displayName = "Dr. Aryan Sharma",
      email = "aryan.neet@gmail.com",
      photoUrl = "https://example.com/avatar.jpg",
      isGoogleUser = true
    )
    val state: com.example.data.auth.AuthState = com.example.data.auth.AuthState.Authenticated(user)
    org.junit.Assert.assertTrue(state is com.example.data.auth.AuthState.Authenticated)
    assertEquals("Dr. Aryan Sharma", (state as com.example.data.auth.AuthState.Authenticated).user.displayName)
    org.junit.Assert.assertTrue(state.user.isGoogleUser)
  }
}
