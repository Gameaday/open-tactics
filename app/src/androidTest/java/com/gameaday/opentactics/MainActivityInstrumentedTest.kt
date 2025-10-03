package com.gameaday.opentactics

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for MainActivity to verify Android-specific functionality.
 * Tests run on an Android device or emulator to validate UI and activity behavior.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    @Test
    fun mainActivity_launches_successfully() {
        // Launch the main activity
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Verify the activity is displayed
            onView(withId(R.id.btnNewGame))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_newGameButton_isClickable() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Verify New Game button is displayed
            onView(withId(R.id.btnNewGame))
                .check(matches(isDisplayed()))

            // Verify button can be clicked (will launch ChapterSelectActivity)
            onView(withId(R.id.btnNewGame))
                .perform(click())
        }
    }

    @Test
    fun mainActivity_continueButton_isDisplayed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Verify Continue button is displayed
            onView(withId(R.id.btnContinue))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_title_isDisplayed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Verify the title text is displayed
            onView(withText(R.string.app_name))
                .check(matches(isDisplayed()))
        }
    }
}
