package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private var reminderDTO = mutableListOf<ReminderDTO>()

    private var isErrorvar = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (isErrorvar) {
            return Result.Error("Reminders were unable to be retrieved")
        }
        reminderDTO.let { return Result.Success(it) }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTO.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminderDTO.find { reminderDTO ->
            reminderDTO.id == id
        }

        return when {
            isErrorvar ->
                Result.Error("Reminder was unable to be retrieved")

            reminder != null ->
                Result.Success(reminder)

            else ->
                Result.Error("Reminder not found!")

        }
    }

    override suspend fun deleteAllReminders() {
        reminderDTO.clear()
    }

    fun returnError(errState: Boolean) {
        isErrorvar = errState
    }
}