package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Add testing implementation to the RemindersLocalRepository.kt
    @get:Rule
    var mainAndroidTestCoroutineRule = MainAndroidTestCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var reminderDataBase: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var reminderLocalRepository: RemindersLocalRepository


    @Before
    fun setUpReminder() {
        reminderDataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = reminderDataBase.reminderDao()
        reminderLocalRepository =
            RemindersLocalRepository(
                remindersDAO,
                Dispatchers.Main
            )
    }

    @After
    fun dataBaseClose() {
        reminderDataBase.close()
    }

    @Test
    fun saveAndGetReminderByID() = mainCoroutineRule.runBlockingTest {
        val reminderDTO = ReminderDTO(
            title = "Football",
            description = "Play it, with full of motivation",
            location = "BarCelona",
            latitude = 79.6214,
            longitude = 6514.3657
        )
        reminderLocalRepository.saveReminder(reminderDTO)
        val reminderLoaded = reminderLocalRepository.getReminder(reminderDTO.id) as Result.Success<ReminderDTO>
        val loadedReminded = reminderLoaded.data

        assertThat(loadedReminded, Matchers.notNullValue())
        assertThat(loadedReminded.id, `is`(reminderDTO.id))
        assertThat(loadedReminded.description, `is`(reminderDTO.description))
        assertThat(loadedReminded.location, `is`(reminderDTO.location))
        assertThat(loadedReminded.latitude, `is`(reminderDTO.latitude))
        assertThat(loadedReminded.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun deleteAllRemindersAndReminders() = mainCoroutineRule.runBlockingTest {
        val reminderDTO = ReminderDTO(
            title = "Football",
            description = "Play it, with full of motivation",
            location = "BarCelona",
            latitude = 79.6214,
            longitude = 6514.3657
        )
        reminderLocalRepository.saveReminder(reminderDTO)
        reminderLocalRepository.deleteAllReminders()
        val reminders = reminderLocalRepository.getReminders() as Result.Success<List<ReminderDTO>>
        val dataReminder = reminders.data
        assertThat(dataReminder.isEmpty(), `is`(true))

    }

    @Test
    fun checkReminderById() = mainCoroutineRule.runBlockingTest {
        val reminderLocalRepository = reminderLocalRepository.getReminder("2") as Result.Error
        assertThat(reminderLocalRepository.message, Matchers.notNullValue())
        assertThat(reminderLocalRepository.message, `is`("Reminder is not found!"))
    }

}