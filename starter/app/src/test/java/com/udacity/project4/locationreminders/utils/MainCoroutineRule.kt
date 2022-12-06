package com.udacity.project4.locationreminders.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {

    /**
     * Invoked when a test is about to start
     * Sets the given dispatcher as an underlying dispatcher of Dispatchers.Main.
     */

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    /**
     * Invoked when a test method finishes (whether passing or failing)
     * Resets state of the Dispatchers.Main to the original main dispatcher.
     */

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}