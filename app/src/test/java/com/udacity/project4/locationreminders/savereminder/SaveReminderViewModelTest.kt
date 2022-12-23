package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSourcce: FakeDataSource

    @Before
    fun setUpRepo() {
        stopKoin()
        fakeDataSourcce = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSourcce)

    }

    @Test
    fun loadingReminders() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        val reminderDataItem = ReminderDataItem(
            "Reminder",
            "Description",
            "Location",
            1.0,
            2.0
        )
        saveReminderViewModel.saveReminder(
            reminderDataItem
        )
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun returnError() = mainCoroutineRule.runBlockingTest {
        val reminderDataItem = ReminderDataItem(
            "Reminder",
            "",
            "Location",
            1.0,
            2.0
        )
        val isDataValid = saveReminderViewModel.validateEnteredData(reminderDataItem)
        MatcherAssert.assertThat(isDataValid, CoreMatchers.`is`(false))
        MatcherAssert.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_descriptionn)
        )
    }


}