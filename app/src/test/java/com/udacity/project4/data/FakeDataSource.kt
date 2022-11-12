package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Reminders
import com.udacity.project4.locationreminders.data.dto.RemindersMutableList
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var fakeList: RemindersMutableList? = mutableListOf()) : ReminderDataSource {

    private var statusError = false

    fun setCheckReturnError(statusError :Boolean) {
        this.statusError = statusError
    }

    override suspend fun getReminders(): Result<Reminders> {
        return if (statusError) {
            Result.Error("Error  Can not get reminders")
        } else {
            if (fakeList.isNullOrEmpty()) {
                Result.Error("Error  Can not get reminders")
            } else {
                Result.Success(fakeList)
            }
        }
    }

    override suspend fun saveReminders(reminders: RemindersMutableList) {
        fakeList?.addAll(reminders)
    }


    override suspend fun saveReminder(reminder: ReminderDTO) {
        fakeList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO>{
        val reminder = fakeList?.find { reminderDTO ->
            reminderDTO.id == id
        }

        return when {
            statusError -> {
                Result.Error("Reminder not found!")
            }

            reminder != null -> {
                Result.Success(reminder)
            }
            else -> {
                Result.Error("Reminder not found!")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        fakeList?.clear()
    }

}