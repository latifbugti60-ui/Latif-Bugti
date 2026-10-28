package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.VoicePresets
import com.example.data.model.VoiceSettings
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
        assertEquals("ElevenVoice AI", appName)
    }

    @Test
    fun `verify voice presets available`() {
        val presets = VoicePresets.allVoices
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it.name.contains("Zara") })
        assertTrue(presets.any { it.name.contains("Hamza") })
        assertTrue(presets.any { it.name.contains("Adam") })
    }

    @Test
    fun `verify voice settings calculation`() {
        val settings = VoiceSettings(speed = 1.2f, pitch = 1.1f)
        val effSpeed = settings.computeEffectiveSpeed(1.0f)
        assertEquals(1.2f, effSpeed, 0.01f)
    }

    @Test
    fun `verify cloned voice persona properties`() {
        val cloned = com.example.data.model.VoicePersona(
            id = "my_voice_test",
            name = "My Voice (Latif)",
            gender = "Male",
            accent = "Instant Voice Clone",
            toneTag = "Natural & Conversational",
            description = "Cloned from personal voice sample",
            category = "My Cloned Voices",
            avatarGradientStart = 0xFF06B6D4,
            avatarGradientEnd = 0xFF7C3AED,
            sampleQuote = "This is my cloned AI voice.",
            isCustom = true,
            isClonedFromMic = true
        )
        assertTrue(cloned.isClonedFromMic)
        assertTrue(cloned.isCustom)
        assertEquals("My Cloned Voices", cloned.category)
    }
}
