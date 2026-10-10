package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AutoReplyEngine
import com.example.ai.PromptBuilder
import com.example.model.ActionType
import com.example.model.BusinessProfile
import com.example.model.NotificationPayload
import com.example.model.SupportedApp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        assertEquals("Mujib AutoReply", appName)
    }

    @Test
    fun `test prompt builder contains business name and rules`() {
        val profile = BusinessProfile(businessName = "شركة النور للتجارة")
        val prompt = PromptBuilder.buildSystemInstruction(profile)
        assertTrue(prompt.contains("شركة النور للتجارة"))
        assertTrue(prompt.contains("SYSTEM INSTRUCTION"))
        assertTrue(prompt.contains("NO_REPLY"))
        assertTrue(prompt.contains("HANDOFF"))
    }

    @Test
    fun `test auto reply engine handles thank you as NO_REPLY`() = runBlocking {
        val profile = BusinessProfile()
        val payload = NotificationPayload(
            app = SupportedApp.WHATSAPP,
            sender = "محمد",
            isGroup = false,
            message = "شكراً"
        )
        val result = AutoReplyEngine.processNotification(payload, profile)
        assertEquals(ActionType.NO_REPLY, result.action)
        assertEquals("", result.reply)
    }

    @Test
    fun `test auto reply engine handles complaint as HANDOFF`() = runBlocking {
        val profile = BusinessProfile()
        val payload = NotificationPayload(
            app = SupportedApp.MESSENGER,
            sender = "سعيد",
            isGroup = false,
            message = "الاوردر اتأخر ومحدش بيرد عليا"
        )
        val result = AutoReplyEngine.processNotification(payload, profile)
        assertEquals(ActionType.HANDOFF, result.action)
        assertTrue(result.reply.contains("نعتذر") || result.reply.contains("رقم"))
    }
}
