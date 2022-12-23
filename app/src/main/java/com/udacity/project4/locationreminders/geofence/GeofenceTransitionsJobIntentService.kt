package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob
    private val remindersLocalRepository: ReminderDataSource by inject()


    companion object {
        private const val JOB_ID = 573
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        // it is check if the user get enter to Geofence or Dwell, one of the 2 types of Geofence
        //so send him a notification to remind him with what he want to do in this area.
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
        ) {
            sendNotification(geofencingEvent.triggeringGeofences)
        }
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = when {
            triggeringGeofences.isNotEmpty() -> {
                // handle if its triggered Geofence with requestid.
                triggeringGeofences[0].requestId
            }
            else -> {
                //if there is nno triggered Geofence, just return
                return
            }
        }

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}
