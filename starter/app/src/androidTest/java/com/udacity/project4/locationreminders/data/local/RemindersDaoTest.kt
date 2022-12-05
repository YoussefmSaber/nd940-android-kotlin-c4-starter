package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        return database.close()
    }

    @Test
    fun insertDataTest() = runBlockingTest {
        val data = ReminderDTO(
            "title asdasd",
            "description asdasd",
            "location asdasd",
            12.123123,
            12.123123
        )

        database.reminderDao().saveReminder(data)

        val loadDataList = database.reminderDao().getReminders()
        MatcherAssert.assertThat(loadDataList.size, `is`(1))

        val loadData = loadDataList[0]
        MatcherAssert.assertThat(loadData.id, `is`(data.id))
        MatcherAssert.assertThat(loadData.title, `is`(data.title))
        MatcherAssert.assertThat(loadData.description, `is`(data.description))
        MatcherAssert.assertThat(loadData.location, `is`(data.location))
        MatcherAssert.assertThat(loadData.latitude, `is`(data.latitude))
        MatcherAssert.assertThat(loadData.longitude, `is`(data.longitude))
    }

}