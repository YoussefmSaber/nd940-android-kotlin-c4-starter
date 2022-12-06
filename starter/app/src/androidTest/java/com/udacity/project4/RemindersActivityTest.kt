package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.*
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.*
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    val dataBindingResourcec = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerResource() {
        IdlingRegistry.getInstance()
            .register(dataBindingResourcec)
    }

    @After
    fun unregisterResource() {
        IdlingRegistry.getInstance()
            .unregister(dataBindingResourcec)
    }

    @Test
    fun snackbarTest() {
        val scenario =
            ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingResourcec.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        onView(withId(R.id.saveReminder))
            .perform(click())

        val snackbarMsg =
            appContext.getString(R.string.err_enter_title)

        onView(withText(snackbarMsg))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun toastTest() {
    val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingResourcec.monitorActivity(scenario)

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("title asdasd"))

        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("description asdasd"))

        onView(withId(R.id.selectLocation))
            .perform(click())
        onView(withId(R.id.save_button))
            .perform(click())

        onView(withId(R.id.saveReminder))
            .perform(click())

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(
            CoreMatchers.not(
                CoreMatchers.`is`(
                    getActivity(scenario).window.decorView
                )
            )
        ))
            .check(matches(isDisplayed()))
        scenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }
}
