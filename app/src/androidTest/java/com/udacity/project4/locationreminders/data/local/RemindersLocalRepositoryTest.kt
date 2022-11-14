package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.core.IsNull

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var fakeList: MutableList<ReminderDTO>
    private lateinit var database: RemindersDatabase
    private val nothing: Unit = Unit

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
        fakeList = fakeReminderData.asReminderDTOMutableList()

    }

    @Test
    fun nothing_getCount_checkCountListIsZero() = runBlocking {
        // Given
        nothing
        // When
        val countList = remindersLocalRepository.getCountList()
        // Then
        ViewMatchers.assertThat(countList, `is`(0))
    }

    @Test
    fun reminder_saveReminder_checkCountListIsChanged() = runBlocking {
        // Given
        val reminder = fakeList[0]
        val oldCount = remindersLocalRepository.getCountList()
        // When
        remindersLocalRepository.saveReminder(reminder)
        // Then
        val newCount = remindersLocalRepository.getCountList()
        ViewMatchers.assertThat(oldCount, `is`(0))
        ViewMatchers.assertThat(newCount, `is`(1))
    }

    @Test
    fun reminder_deleteReminderById_checkCountListIsChanged() = runBlocking {
        // Given
        val reminder = fakeList[0]
        remindersLocalRepository.saveReminder(reminder)
        val oldCount = remindersLocalRepository.getCountList()
        // When
        remindersLocalRepository.deleteReminder(reminder.id)
        // Then
        val newCount = remindersLocalRepository.getCountList()
        ViewMatchers.assertThat(oldCount, `is`(1))
        ViewMatchers.assertThat(newCount, `is`(0))
    }

    @Test
    fun remindersList_deleteAlReminders_checkCountListIsZero() = runBlocking {
        // Given
        fakeList.forEach { remindersLocalRepository.saveReminder(it) }
        val oldCount = remindersLocalRepository.getCountList()
        // When
        remindersLocalRepository.deleteAllReminders()
        // Then
        val newCount = remindersLocalRepository.getCountList()
        ViewMatchers.assertThat(oldCount, `is`(9))
        ViewMatchers.assertThat(newCount, `is`(0))
    }

    @Test
    fun reminder_getReminderById_ReminderIsValid() = runBlocking {
        // Given
        remindersLocalRepository.saveReminder(fakeList[0])
        // When
        val retrievedReminder = remindersLocalRepository.getReminder(fakeList[0].id)
        // Then
        ViewMatchers.assertThat(retrievedReminder.succeeded, `is`(true))
        ViewMatchers.assertThat(retrievedReminder as ReminderDTO, IsNull.notNullValue())
        ViewMatchers.assertThat(retrievedReminder.id, `is`(fakeList[0].id))
        ViewMatchers.assertThat(retrievedReminder.title, `is`(fakeList[0].title))
        ViewMatchers.assertThat(retrievedReminder.description, `is`(fakeList[0].description))
        ViewMatchers.assertThat(retrievedReminder.location, `is`(fakeList[0].location))
        ViewMatchers.assertThat(retrievedReminder.latitude, `is`(fakeList[0].latitude))
        ViewMatchers.assertThat(retrievedReminder.longitude, `is`(fakeList[0].longitude))
    }

    @Test
    fun nothing_getReminder_returnError() = runBlocking {
        // Given
        nothing
        // When
        val retrievedResult = remindersLocalRepository.getReminder(fakeList[0].id)
        // Then
        ViewMatchers.assertThat(retrievedResult.error, `is`(true))
        val errorMessage = (retrievedResult as Result.Error).message
        ViewMatchers.assertThat(errorMessage, `is`("Reminder not found!"))
    }

    @Test
    fun remindersList_getReminders_RemindersIsValid() = runBlocking {
        // Given
        fakeList.forEach { remindersLocalRepository.saveReminder(it) }
        // When
        val retrievedResult = remindersLocalRepository.getReminders()
        // Then
        ViewMatchers.assertThat(retrievedResult.succeeded, `is`(true))
        val retrievedRemindersList = (retrievedResult as Result.Success<List<ReminderDTO>>).data
        val listValidated = retrievedRemindersList!!.containsAll(fakeList)
        ViewMatchers.assertThat(listValidated, `is`(true))
    }

    @Test
    fun remindersList_getReminders_confirmEmptyList() = runBlocking {
        // Given
        nothing
        // When
        val retrievedResult = remindersLocalRepository.getReminders()
        // Then
        ViewMatchers.assertThat(retrievedResult.succeeded, `is`(true))
        val retrievedReminders = (retrievedResult as Result.Success<List<ReminderDTO>>).data
        val listEmpty = retrievedReminders!!.isEmpty()
        ViewMatchers.assertThat(listEmpty, `is`(true))
    }

    @After
    fun cleanUp() {
        database.close()
    }


    
    
}