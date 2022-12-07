package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.Assert.assertThat
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExcuteRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        repository = RemindersLocalRepository(database.reminderDao())
    }

    @After
    fun cleanUp() {
        return database.close()
    }

    @Test
    fun dataNotFound() = runBlockingTest {
        val res = repository.getReminder("123123123") as Error
        val msg = res.message
        assertThat(msg, Matchers.notNullValue())
        assertThat(msg, `is`("Reminder Not Found!"))
    }

    @Test
    fun insertDataTest() = runBlocking {
        val data = ReminderDTO(
            "title asdasd",
            "description asdasd",
            "location asdasd",
            12.123123,
            12.123123
        )

        repository.saveReminder(data)

        val res = repository.getReminder(data.id)

        res as Result.Success
        MatcherAssert.assertThat(res.data != null, CoreMatchers.`is`(true))

        val loadData = res.data
        MatcherAssert.assertThat(loadData.id, `is`(data.id))
        MatcherAssert.assertThat(loadData.title, `is`(data.title))
        MatcherAssert.assertThat(loadData.description, `is`(data.description))
        MatcherAssert.assertThat(loadData.location, `is`(data.location))
        MatcherAssert.assertThat(loadData.latitude, `is`(data.latitude))
        MatcherAssert.assertThat(loadData.longitude, `is`(data.longitude))
    }
}