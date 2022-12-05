package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminderDTO = mutableListOf<ReminderDTO>()
    var isErrorvar = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
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
}