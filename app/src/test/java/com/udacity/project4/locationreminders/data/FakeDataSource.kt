package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersDTO: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource{

    private var error = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (error)
            return Result.Error("404 error")
        remindersDTO?.let {
            return Result.Success(ArrayList(remindersDTO))
        }
        return Result.Error("Reminders is not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersDTO?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (error){
            return Result.Error("Reminders is not found")
        }else{
            return return Result.Success(ArrayList(remindersDTO))
        }
    }

    override suspend fun deleteAllReminders() {
        remindersDTO?.clear()
    }
    fun setReturnError(value: Boolean) {
        error = value
    }


}