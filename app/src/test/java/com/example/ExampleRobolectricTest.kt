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
    assertEquals("FixIt", appName)
  }

  @Test
  fun `verify report metadata configuration`() {
    val metadata = com.example.util.ReportMetadata(
      appName = "FixIt",
      applicationId = "com.aistudio.fixit.mktpl",
      appVersion = "1.0.0",
      startDate = "2026-09-10",
      endDate = "2026-10-10",
      statusFilter = "All Issues"
    )
    assertEquals("FixIt", metadata.appName)
    assertEquals("com.aistudio.fixit.mktpl", metadata.applicationId)
    assertEquals("All Issues", metadata.statusFilter)
  }
}
