package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminderDTO = mutableListOf<ReminderDTO>()
    private var isErrorvar = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(isErrorvar) {
            return Result.Error("Reminders not found")
        }
        reminderDTO.let {
            return Result.Success(ArrayList(it))
        }

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTO.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(isErrorvar) {
            return Result.Error("Can\'t load Data")
        }
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