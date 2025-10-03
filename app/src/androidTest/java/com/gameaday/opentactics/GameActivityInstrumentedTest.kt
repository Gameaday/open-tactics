package com.gameaday.opentactics

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for GameActivity to verify Android-specific game UI functionality.
 * Tests the tactical RPG game interface on actual Android devices/emulators.
 */
@RunWith(AndroidJUnit4::class)
class GameActivityInstrumentedTest {
    @Test
    fun gameActivity_launches_successfully() {
        // Create intent with required chapter ID
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GameActivity::class.java).apply {
                putExtra("CHAPTER_ID", "chapter_1")
            }

        // Launch the game activity
        ActivityScenario.launch<GameActivity>(intent).use { scenario ->
            // Verify the game container is displayed
            onView(withId(R.id.gameContainer))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun gameActivity_controlsPanel_isDisplayed() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GameActivity::class.java).apply {
                putExtra("CHAPTER_ID", "chapter_1")
            }

        ActivityScenario.launch<GameActivity>(intent).use { scenario ->
            // Verify the game controls are displayed
            onView(withId(R.id.gameControls))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun gameActivity_actionButtons_areDisplayed() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GameActivity::class.java).apply {
                putExtra("CHAPTER_ID", "chapter_1")
            }

        ActivityScenario.launch<GameActivity>(intent).use { scenario ->
            // Verify move button is displayed
            onView(withId(R.id.btnMove))
                .check(matches(isDisplayed()))

            // Verify attack button is displayed
            onView(withId(R.id.btnAttack))
                .check(matches(isDisplayed()))

            // Verify end turn button is displayed
            onView(withId(R.id.btnEndTurn))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun gameActivity_chapterInfo_isDisplayed() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), GameActivity::class.java).apply {
                putExtra("CHAPTER_ID", "chapter_1")
            }

        ActivityScenario.launch<GameActivity>(intent).use { scenario ->
            // Verify chapter objective panel is displayed
            onView(withId(R.id.chapterObjectivePanel))
                .check(matches(isDisplayed()))
        }
    }
}
