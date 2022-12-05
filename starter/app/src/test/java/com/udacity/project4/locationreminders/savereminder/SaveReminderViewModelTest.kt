package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private var remindersRepo = FakeDataSource()
    private lateinit var savedReminder: SaveReminderViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutine = MainCoroutineRule()

    @Before
    fun setup() {
        savedReminder = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersRepo
        )
    }

    private fun notcompleteReminderItem(): ReminderDataItem = ReminderDataItem(
        "",
        "Description asdasd",
        "Location asdasd",
        12.123123,
        12.123123
    )

    @Test
    fun returnError() = runBlocking {
        val res = savedReminder.validateEnteredData(notcompleteReminderItem())
        MatcherAssert.assertThat(
            res,
            CoreMatchers.`is`(false)
            )
    }

    private fun fakeData(): ReminderDataItem = ReminderDataItem(
        "Title asd",
        "Description asdasd",
        "Location asdasd",
        12.123123,
        12.123123
    )

    @Test
    fun loading() = runBlocking {
        mainCoroutine.pauseDispatcher()
        savedReminder.saveReminder(fakeData())
        MatcherAssert.assertThat(
            savedReminder.showLoading.value,
            CoreMatchers.`is`(true)
        )

        mainCoroutine.resumeDispatcher()
        MatcherAssert.assertThat(
            savedReminder.showLoading.value,
            CoreMatchers.`is`(false)
        )
    }
}