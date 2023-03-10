package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var databaseReminder: RemindersDatabase

    @Before
    fun setDBB() {
        databaseReminder = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun dataBaseClose() = databaseReminder.close()

    @Test
    fun saveReminderDetails() = runBlockingTest {
        val reminderDTO = ReminderDTO(
            title = "Football",
            description = "Play it, with full of motivation",
            location = "BarCelona",
            latitude = 79.6214,
            longitude = 6514.3657
        )
        databaseReminder.reminderDao().saveReminder(reminderDTO)
        val loaded = databaseReminder.reminderDao().getReminderById(reminderDTO.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDTO.id))
        assertThat(loaded.id, `is`(reminderDTO.title))
        assertThat(loaded.description, `is`(reminderDTO.description))
        assertThat(loaded.location, `is`(reminderDTO.location))
        assertThat(loaded.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun checkForReminnderFound() = runBlockingTest {
        val reminder = ReminderDTO(
            title = "Football",
            description = "Play it, with full of motivation",
            location = "BarCelona",
            latitude = 79.6214,
            longitude = 6514.3657
        )

        databaseReminder.reminderDao().saveReminder(reminder)
        databaseReminder.reminderDao().deleteAllReminders()
        val reminders = databaseReminder.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))

    }

    @Test
    fun checkReminderById() = runBlockingTest {
        val reminderDatabase = databaseReminder.reminderDao().getReminderById("2")

        assertThat(reminderDatabase, CoreMatchers.nullValue())

    }

}