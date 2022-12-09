package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.*
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnableds
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    companion object {
        var latitude = 0.0
        var longitude = 0.0
        var title = ""
        var pointSelectedState: Boolean = false
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnableds(true)

        val map = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        map.getMapAsync(this)


        binding.saveButton.setOnClickListener {
            if (pointSelectedState) {
                onLocationSelected()
            } else {
                Toast.makeText(context, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        _viewModel.reminderSelectedLocationStr.value = title
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("RestrictedApi")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        Log.i(this.toString(), "Function Called")
        setMapMarker(map)
        setPoiMarker(map)
        setMapStyle(map)
        if (isPermissionGranted()
        ) {
            enableMyLocation()
        } else {

            // TODO: HERE
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context, R.raw.map_style
                )
            )
            if (!success) {
                Toast.makeText(context, "Can't apply the map style", Toast.LENGTH_LONG).show()
            }
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(context, "Error ${e}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPoiMarker(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )
            val zoomLevel = 15f
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, zoomLevel))
            poiMarker.showInfoWindow()
            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude
            title = poi.name
        }
        pointSelectedState = true
    }

    private fun setMapMarker(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .snippet(snippet)
                    .title(getString(R.string.dropped_pin))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            latitude = latLng.latitude
            longitude = latLng.longitude
            title = getString(R.string.dropped_pin)
        }
        pointSelectedState = true
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        myLocation()
    }

    @SuppressLint("MissingPermission")
    private fun myLocation() {
        val locationProvider = LocationServices
            .getFusedLocationProviderClient(requireContext())
        val locationTask = locationProvider.lastLocation

        locationTask.addOnCompleteListener(requireActivity()) {
            if (it.isSuccessful) {
                val res = it.result
                res?.run {
                    val latLng = LatLng(latitude, longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            latLng,
                            15f
                        )
                    )
                }
            }
        }
    }


    @SuppressLint("RestrictedApi")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                val packageName = AuthUI.getApplicationContext().packageName
                Snackbar.make(
                    binding.mapFragment,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            }
        }
    }
}
