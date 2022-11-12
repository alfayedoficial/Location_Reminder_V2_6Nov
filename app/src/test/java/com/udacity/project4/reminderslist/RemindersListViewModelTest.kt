package com.udacity.project4.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Reminders
import com.udacity.project4.locationreminders.data.dto.RemindersMutableList
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModelForTesting: RemindersListViewModel
    // Use a fake repository to be injected into the viewModel
    private lateinit var fakeDataSourceForTesting: FakeDataSource
    private lateinit var remindersForTesting : RemindersMutableList


    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRuleTest = MainCoroutineRule()
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRuleTest = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        fakeDataSourceForTesting = FakeDataSource(arrayListOf())
        remindersListViewModelForTesting = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSourceForTesting)
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun loadReminders_showLoading() {
        mainCoroutineRuleTest.pauseDispatcher()
        remindersListViewModelForTesting.loadReminders()
        Truth.assertThat(remindersListViewModelForTesting.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRuleTest.resumeDispatcher()
        Truth.assertThat(remindersListViewModelForTesting.showLoading.getOrAwaitValue()).isFalse()

    }

    @Test
    fun loadReminders_remainderListNotEmpty() = mainCoroutineRuleTest.runBlockingTest {
        remindersForTesting = mutableListOf(
            ReminderDTO("Task NO.1", "description 1", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.2", "description 2", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.3", "description 3", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.4", "description 4", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.5", "description 5", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.6", "description 6", "City ", 30.043457431, 31.2765762)
        )


        fakeDataSourceForTesting.saveReminders(remindersForTesting)
        remindersListViewModelForTesting.loadReminders()

        Truth.assertThat(remindersListViewModelForTesting.remindersList.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun loadReminderById_remainderNotEmpty() = mainCoroutineRuleTest.runBlockingTest {
        remindersForTesting = mutableListOf(
            ReminderDTO("Task NO.1", "description 1", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.2", "description 2", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.3", "description 3", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.4", "description 4", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.5", "description 5", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.6", "description 6", "City ", 30.043457431, 31.2765762)
        )


        fakeDataSourceForTesting.saveReminders(remindersForTesting)
        val result = fakeDataSourceForTesting.getReminder(remindersForTesting[0].id)

        assertThat(result is Result.Success, CoreMatchers.`is`(true))
        result as Result.Success
        Truth.assertThat(result.data).isNotNull()
    }

    @Test
    fun loadReminderById_remainderEmpty() = mainCoroutineRuleTest.runBlockingTest {
        remindersForTesting = mutableListOf(
            ReminderDTO("Task NO.1", "description 1", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.2", "description 2", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.3", "description 3", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.4", "description 4", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.5", "description 5", "City ", 30.043457431, 31.2765762),
            ReminderDTO("Task NO.6", "description 6", "City ", 30.043457431, 31.2765762)
        )


        fakeDataSourceForTesting.saveReminders(remindersForTesting)
        val result = fakeDataSourceForTesting.getReminder("23334525")

        assertThat(result is Result.Error, CoreMatchers.`is`(true))
        result as Result.Error
        assertThat(result.message, CoreMatchers.`is`("Reminder not found!"))
    }

    @Test
    fun loadReminders_updateSnackBarValue() {
        mainCoroutineRuleTest.pauseDispatcher()
        fakeDataSourceForTesting.setCheckReturnError(true)
        remindersListViewModelForTesting.loadReminders()
        mainCoroutineRuleTest.resumeDispatcher()
        Truth.assertThat(remindersListViewModelForTesting.showSnackBar.getOrAwaitValue()).isEqualTo("Error  Can not get reminders")
    }
}