package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.*
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
        MatcherAssert.assertThat(loadData.id, CoreMatchers.`is`(data.id))
        MatcherAssert.assertThat(loadData.title, CoreMatchers.`is`(data.title))
        MatcherAssert.assertThat(loadData.description, CoreMatchers.`is`(data.description))
        MatcherAssert.assertThat(loadData.location, CoreMatchers.`is`(data.location))
        MatcherAssert.assertThat(loadData.latitude, CoreMatchers.`is`(data.latitude))
        MatcherAssert.assertThat(loadData.longitude, CoreMatchers.`is`(data.longitude))
    }
}