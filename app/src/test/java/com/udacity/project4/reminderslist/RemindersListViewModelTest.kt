package com.udacity.project4.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.utils.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.data.asReminderDTOMutableList
import com.udacity.project4.data.fakeReminderData
import com.udacity.project4.utils.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.RemindersDTOMutableList
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(manifest = "AndroidManifest.xml")
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModelForTesting: RemindersListViewModel
    // Use a fake repository to be injected into the viewModel
    private lateinit var fakeDataSourceForTesting: FakeDataSource
    private lateinit var fakeList: RemindersDTOMutableList
    private val nothing: Unit = Unit // Placeholder for empty test parameters.


    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRuleTest = MainCoroutineRule()
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRuleTest = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        fakeDataSourceForTesting = FakeDataSource()
        remindersListViewModelForTesting = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSourceForTesting)
        fakeList = fakeReminderData.asReminderDTOMutableList()
    }


    @Test
    fun `nothing loadReminders showLoading`() = mainCoroutineRuleTest.runBlockingTest {
        // Given
        nothing
        pauseDispatcher()
        // When
        remindersListViewModelForTesting.loadReminders()
        // Then
        assertThat(remindersListViewModelForTesting.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        resumeDispatcher()
        assertThat(remindersListViewModelForTesting.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @Test
    fun `reminderList loadReminders checkListNotEmpty`() = runBlockingTest {
        // Given
        fakeList.forEach {
            fakeDataSourceForTesting.saveReminder(it)
        }
        // When
        remindersListViewModelForTesting.loadReminders()
        // Then
        val isNotEmptyList = fakeDataSourceForTesting.getCountList() >= 1
        assertThat(isNotEmptyList, CoreMatchers.`is`(true))
    }

    @Test
    fun `emptyReminderList loadReminders checkListEmpty`() = runBlockingTest {
        // Given
        fakeDataSourceForTesting.deleteAllReminders()
        // When
        remindersListViewModelForTesting.loadReminders()
        // Then
        assertThat(remindersListViewModelForTesting.showNoData.getOrAwaitValue(), CoreMatchers.`is`(true))
    }


    @Test
    fun `ReminderList deleteReminder checkListEmpty`() = runBlockingTest {
        // Given
        fakeDataSourceForTesting.saveReminder(fakeList[0])
        // When
        fakeDataSourceForTesting.deleteReminder(fakeList[0].id)
        // Then
        remindersListViewModelForTesting.loadReminders()
        val isEmptyList = fakeDataSourceForTesting.getCountList() == 0
        assertThat(isEmptyList, CoreMatchers.`is`(true))
    }


    @Test
    fun `reminderError loadReminders show error`() {
        // Given
        fakeDataSourceForTesting.setShouldReturnError(true)
        // When
        remindersListViewModelForTesting.loadReminders()
        // Then
        val reminderException = Exception("Reminder Exception!").toString()
        assertThat(remindersListViewModelForTesting.showSnackBar.getOrAwaitValue(), CoreMatchers.`is`(reminderException))
    }
}