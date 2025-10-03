package com.gameaday.opentactics

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for ChapterSelectActivity to verify chapter selection UI.
 * Tests the chapter selection interface on Android devices/emulators.
 */
@RunWith(AndroidJUnit4::class)
class ChapterSelectActivityInstrumentedTest {
    @Test
    fun chapterSelectActivity_launches_successfully() {
        ActivityScenario.launch(ChapterSelectActivity::class.java).use { scenario ->
            // Verify the chapter recycler view is displayed
            onView(withId(R.id.chapterRecyclerView))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun chapterSelectActivity_title_isDisplayed() {
        ActivityScenario.launch(ChapterSelectActivity::class.java).use { scenario ->
            // Verify the chapter selection title is displayed
            onView(withText(R.string.chapter_select))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun chapterSelectActivity_backButton_isDisplayed() {
        ActivityScenario.launch(ChapterSelectActivity::class.java).use { scenario ->
            // Verify the back button is displayed
            onView(withId(R.id.btnBack))
                .check(matches(isDisplayed()))
        }
    }
}
