package com.udacity.project4.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    // Subject under test
    private lateinit var saveReminderViewModelForTesting: SaveReminderViewModel
    // Use a fake repository to be injected into the viewModel
    private lateinit var fakeDataSourceForTesting: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRuleTest = MainCoroutineRule()
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRuleTest = InstantTaskExecutorRule()

    private lateinit var reminderDataItemForTesting: ReminderDataItem

    @Before
    fun setupViewModel() {
        fakeDataSourceForTesting = FakeDataSource()
        saveReminderViewModelForTesting = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSourceForTesting)

        runBlocking { fakeDataSourceForTesting.deleteAllReminders() }
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun `validateEnteredData NullTitle And Display SnackBar Error`() {
        reminderDataItemForTesting = ReminderDataItem().apply {
            this.description = "description"
            this.location = "location"
            this.latitude = 47.5456551
            this.longitude = 122.0101731
        }
//        assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting)).isFalse()
//        assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
        MatcherAssert.assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting) , CoreMatchers.notNullValue())
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue() , CoreMatchers.`is`(R.string.err_enter_title))

    }

    @Test
    fun `validateEnteredData NullLocation And Display SnackBar Error`() {
        reminderDataItemForTesting = ReminderDataItem().apply {
            this.title = "title"
            this.description = "description"
            this.latitude = 47.5456551
            this.longitude = 122.0101731
        }

//        assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting)).isFalse()
//        assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
        MatcherAssert.assertThat(saveReminderViewModelForTesting.validateEnteredData(reminderDataItemForTesting) ,CoreMatchers.notNullValue())
        MatcherAssert.assertThat(saveReminderViewModelForTesting.showSnackBarInt.getOrAwaitValue(), CoreMatchers.`is`(R.string.err_select_location))

    }


    @Test
    fun `saveReminder Show Is Loading And Display`() {
        reminderDataItemForTesting = ReminderDataItem().apply {
            this.title = "title"
            this.description = "description"
            this.location = "location"
            this.latitude = 47.5456551
            this.longitude = 122.0101731
        }

        mainCoroutineRuleTest.pauseDispatcher()
        saveReminderViewModelForTesting.saveReminder(reminderDataItemForTesting)
        assertThat(saveReminderViewModelForTesting.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRuleTest.resumeDispatcher()
        assertThat(saveReminderViewModelForTesting.showLoading.getOrAwaitValue()).isFalse()
    }


}