package com.udacity.project4.locationreminders.savereminder


import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.lang.Exception

// a constant variable for grant location on 29, and Persmission Code for request, with
// request permission code
private const val TURN_LOCATION_ON = 29
private const val PERMISSION_CODE = 2
private const val REQUEST_PERMISSION_CODE = 3

class SelectLocationFragment : BaseFragment() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    // variable to catch sdk Q and earlier.
    //to ensure that app works on all the different versions including Android Q
    private val sdkQToAbove = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // set all features for map to be get ready for using
    private val mapReady = OnMapReadyCallback { gMap ->
        googleMap = gMap
        setMapStyle(googleMap)
        LocationPermission()
        zoomMap()
        setPOI(googleMap)
        addMapClick(googleMap)

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
        } catch (exc: Exception) {
        }
    }

    private fun LocationPermission() {
        if (locationPermissionGranted()) {
            grantLocationStartGeo()
        }
        var allPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            sdkQToAbove -> {
                allPermissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                PERMISSION_CODE
            }
            else ->
                REQUEST_PERMISSION_CODE
        }
        ActivityCompat.requestPermissions(
            requireActivity(),
            allPermissions,
            resultCode
        )
    }

    private fun grantLocationStartGeo(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val locationBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationService = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            locationService.checkLocationSettings(locationBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        TURN_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                if (context?.let { it1 ->
                        ActivityCompat.checkSelfPermission(
                            it1,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED && context?.let { it1 ->
                        ActivityCompat.checkSelfPermission(
                            it1,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@addOnCompleteListener
                }
                googleMap.isMyLocationEnabled = true
            }
        }
    }

    private fun locationPermissionGranted(): Boolean {
        val foregroundLocationGranted =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundLocationGranted =
            if (sdkQToAbove) {

                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }
        return foregroundLocationGranted && backgroundLocationGranted
    }

    fun zoomMap() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                val currentLatLang = LatLng(location.latitude, location.longitude)
                val zoomLevel = 15f
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLang,
                        zoomLevel
                    )
                )
            }
        }
    }

    private fun setPOI(googleMap: GoogleMap) {
        googleMap.setOnPoiClickListener { poi ->
            binding.btnSave.setOnClickListener {
                onLocationSelected(poi)
            }
            val poiMarker = googleMap.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )
            poiMarker.showInfoWindow()
        }

    }

    private fun addMapClick(map: GoogleMap) {
        map.setOnMapClickListener {
            binding.btnSave.setOnClickListener { view ->
                _viewModel.latitude.value = it.latitude
                _viewModel.longitude.value = it.longitude
                _viewModel.reminderSelectedLocationStr.value = "Location detected"
                findNavController().popBackStack()
            }

            val cameraMove = CameraUpdateFactory.newLatLngZoom(it, 15f)
            map.moveCamera(cameraMove)
            val poiMarker = map.addMarker(MarkerOptions().position(it))
            poiMarker.showInfoWindow()
        }

    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(mapReady)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        //onLocationSelected()

        return binding.root
    }

    // let the user confirms on the selected location,
    // and then we back with selected locatin details for view model,
    // and then we navigate back to the fragment to save the reminder details and add the geofence
    private fun onLocationSelected(poi: PointOfInterest) {
        val latLng = poi.latLng
        _viewModel.reminderSelectedLocationStr.value = poi.name
        _viewModel.latitude.value = latLng.latitude
        _viewModel.longitude.value = latLng.longitude
        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
