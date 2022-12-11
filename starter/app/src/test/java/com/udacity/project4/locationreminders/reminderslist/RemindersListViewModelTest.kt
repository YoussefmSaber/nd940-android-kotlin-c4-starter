package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.*
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var reminderRepo: FakeDataSource

    @get:Rule
    var mainCoroutine = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        reminderRepo = FakeDataSource()
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderRepo
        )
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun shouldReturnError() = runBlocking {

        reminderRepo.returnError(true)
        saveFakeData()
        viewModel.loadReminders()
        MatcherAssert.assertThat(
            viewModel.showSnackBar.value,
            CoreMatchers.`is`("Reminders were unable to be retrieved")
        )

    }

    @Test
    fun check_loading() = runBlockingTest {

        mainCoroutine.pauseDispatcher()
        saveFakeData()
        viewModel.loadReminders()

        MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(true))

        mainCoroutine.resumeDispatcher()
        MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(false))
    }

    private suspend fun saveFakeData() {
        reminderRepo.saveReminder(
            ReminderDTO(
                "title asdasd",
                "description asdasd",
                "location asdasd",
                12.123123,
                12.123123
            )
        )
    }
}