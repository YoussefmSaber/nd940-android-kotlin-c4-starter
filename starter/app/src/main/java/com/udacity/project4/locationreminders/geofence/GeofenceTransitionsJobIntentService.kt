package com.udacity.project4.locationreminders.geofence

import android.content.*
import androidx.core.app.JobIntentService
import com.google.android.gms.location.*
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.*
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotifications
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context, GeofenceTransitionsJobIntentService::class.java, JOB_ID, intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        val geofenceArr: List<Geofence> = geofenceEvent.triggeringGeofences
        sendNotification(geofenceArr)
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {

        for (triggeringGeofence in triggeringGeofences) {

            val requestId = triggeringGeofence.requestId

            //Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotifications(
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
}
