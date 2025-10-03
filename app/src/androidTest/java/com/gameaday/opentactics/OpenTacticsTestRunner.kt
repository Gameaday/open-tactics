package com.gameaday.opentactics

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Custom test runner for instrumented tests.
 * Can be extended to provide custom test application or configurations.
 */
class OpenTacticsTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application {
        // Can return a test application if needed for mocking
        return super.newApplication(cl, className, context)
    }
}
