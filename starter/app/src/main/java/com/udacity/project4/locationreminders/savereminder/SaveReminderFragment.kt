package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnableds
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderDataItem: ReminderDataItem

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireContext(),
            0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_save_reminder,
                container, false
            )

        setDisplayHomeAsUpEnableds(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(
                    SaveReminderFragmentDirections
                        .actionSaveReminderFragmentToSelectLocationFragment()
                )
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            if (latitude != null && longitude != null && !title.isNullOrEmpty()) {
                reminderDataItem = ReminderDataItem(
                    title,
                    description,
                    location,
                    latitude,
                    longitude
                )
                if (fineAndBackgroundLocationPermissionsApproved()) {
                    checkDeviceLocationSettings()
                } else {
                    requestFineAndBackgroundLocationPermissions()
                }

                _viewModel.navigationCommand.postValue(
                    NavigationCommand.To(
                        SaveReminderFragmentDirections
                            .actionSaveReminderFragmentToReminderListFragment()
                    )
                )
            } else {

                if (title.isNullOrEmpty())
                    Snackbar.make(
                        requireView(),
                        getString(R.string.err_enter_title),
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        title.isNullOrEmpty()
                    }.show()
                else if (description.isNullOrEmpty())
                    Snackbar.make(
                        requireView(),
                        getString(R.string.err_enter_description),
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        description.isNullOrEmpty()
                    }.show()
                else if (location.isNullOrEmpty())
                    Snackbar.make(
                        requireView(),
                        getString(R.string.err_select_location),
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        location.isNullOrEmpty()
                    }.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestFineAndBackgroundLocationPermissions() {
        if (fineAndBackgroundLocationPermissionsApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                FINE_AND_BACKGROUND_LOCATIONS_REQUEST_CODE
            }
            else -> FINE_LOCATION_REQUEST_CODE
        }

        requestPermissions(permissionsArray, requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fineAndBackgroundLocationPermissionsApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ))

        val backgroundPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantedResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantedResults
        )
        if (grantedResults.isEmpty() ||
            grantedResults[0] == PackageManager.PERMISSION_DENIED ||
            (requestCode == FINE_AND_BACKGROUND_LOCATIONS_REQUEST_CODE &&
                    grantedResults[1] == PackageManager.PERMISSION_DENIED)
        ) {
            _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
        } else {
            checkDeviceLocationSettings()
        }
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient = LocationServices
            .getSettingsClient(requireActivity())

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        TURN_DEVICE_LOCATION_ON_REQUEST_CODE,
                        null,
                        0, 0,
                        0, null
                    )

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.i(
                        "SaveReminderFragment",
                        "Error getting location settings: ${sendEx.message}"
                    )
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                startGeoFence()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGeoFence() {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(
            geofencingRequest,
            geofencePendingIntent
        )?.run {
            addOnSuccessListener {
                _viewModel.saveReminder(reminderDataItem)
            }
            addOnFailureListener {
                _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TURN_DEVICE_LOCATION_ON_REQUEST_CODE) {
            checkDeviceLocationSettings(false)
        }
    }

    private companion object {
        const val GEOFENCE_RADIUS = 350f
        const val FINE_LOCATION_REQUEST_CODE = 33
        const val FINE_AND_BACKGROUND_LOCATIONS_REQUEST_CODE = 34
        const val TURN_DEVICE_LOCATION_ON_REQUEST_CODE = 35
    }
}
