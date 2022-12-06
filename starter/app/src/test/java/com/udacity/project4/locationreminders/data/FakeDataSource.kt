package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.*

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminderDTO = mutableListOf<ReminderDTO>()
    private var isErrorvar = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            if(isErrorvar)
                throw Exception("Reminders not found")
            Result.Success(ArrayList(reminderDTO))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTO.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            val reminder = reminderDTO.find {
                it.id == id
            }
            if (reminder == null) {
                throw Exception("Reminder ${id} not found")
            } else {
                Result.Success(reminder)
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        reminderDTO.clear()
    }

    fun returnError(errState: Boolean) {
        isErrorvar = errState
    }
}