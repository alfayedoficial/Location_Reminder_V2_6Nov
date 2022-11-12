package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersLocalRepositoryTest {

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val reminderItemForTesting: ReminderDTO by lazy {  ReminderDTO("Task NO.1", "description 1", "City ", 30.043457431, 31.2765762) }

    @Before
    fun setup() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrieveReminderById() = runBlocking {

        remindersLocalRepository.saveReminder(reminderItemForTesting)

        val result = remindersLocalRepository.getReminder(reminderItemForTesting.id) as? Result.Success

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success


        assertThat(result.data?.title, `is`(reminderItemForTesting.title))
        assertThat(result.data?.description, `is`(reminderItemForTesting.description))
        assertThat(result.data?.latitude, `is`(reminderItemForTesting.latitude))
        assertThat(result.data?.longitude, `is`(reminderItemForTesting.longitude))
        assertThat(result.data?.location, `is`(reminderItemForTesting.location))
    }


    @Test
    fun deleteReminders_EmptyList()= runBlocking {

        remindersLocalRepository.saveReminder(reminderItemForTesting)
        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertThat(result.data, `is` (emptyList()))
    }

    @Test
    fun retrieveReminderById_ReturnError() = runBlocking {

        remindersLocalRepository.saveReminder(reminderItemForTesting)
        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminder(reminderItemForTesting.id)

        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    
    
}