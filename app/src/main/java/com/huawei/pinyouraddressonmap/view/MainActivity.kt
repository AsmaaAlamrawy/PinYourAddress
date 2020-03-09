package com.huawei.pinyouraddressonmap.view

import android.os.Bundle
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import android.util.Log
import android.content.IntentSender
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.common.ApiException
import android.os.Looper
import com.huawei.hms.location.*
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.pinyouraddressonmap.R


class MainActivity : MapBaseActivity() {
    private var huaweiMap: HuaweiMap? = null
    private var mapView: MapView? = null
    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    private var addressFeatureName: String? = null
    private var currentLocationMarker: Marker? = null
    private var tappedLocationMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun handleMapIsReady() {
        if (huaweiMap != null) {
            huaweiMap!!.isMyLocationEnabled = true// Enable the my-location overlay.
            huaweiMap!!.uiSettings.isMyLocationButtonEnabled = true
            getDeviceCurrentLocation()
            setTapEventListener()
        }
    }

    override fun setTapEventListener() {
        huaweiMap!!.setOnMapClickListener { latLng ->
            changeCameraTarget(latLng)
            val markerOptions = MarkerOptions().position(latLng)
            tappedLocationMarker =
                locateMarkerWithMarkerOptions(tappedLocationMarker, markerOptions)
        }
    }


    private fun getDeviceCurrentLocation() {
        val fusedLocationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this);
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult != null) {
                    //Process the location callback result.
                    Log.d("Location", "got location")
                    if (locationResult.lastHWLocation != null)
                        setCurrentLocationOnMap(locationResult.lastHWLocation)
                }
            }

        }
        var locationSettingsBuilder = LocationSettingsRequest.Builder()
        var locationRequest: LocationRequest = LocationRequest()
        locationRequest = LocationRequest()
        locationRequest
            .setInterval(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNeedAddress(true)
        locationSettingsBuilder.addLocationRequest(locationRequest)
        val locationSettingsRequest = locationSettingsBuilder.build()
        //check Location Settings
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                //Have permissions， send requests
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.getMainLooper()
                ).addOnSuccessListener {
                    Log.d("requestLocationUpdates", "success")

                }
                fusedLocationProviderClient.getLastLocationWithAddress(locationRequest)
                    .addOnSuccessListener {
                        addressFeatureName = it.featureName
                    }

            }
            .addOnFailureListener { e ->
                //Settings do not meet targeting criteria
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae = e as ResolvableApiException
                        //Calling startResolutionForResult can pop up a window to prompt the user to open the corresponding permissions
                        rae.startResolutionForResult(this@MainActivity, 0)
                    } catch (sie: IntentSender.SendIntentException) {
                        Log.d("api exeption", "error resolving api exeption")
                    }

                }
            }


    }

    private fun setCurrentLocationOnMap(lastHWLocation: HWLocation) {
        changeCameraTarget(LatLng(lastHWLocation.latitude, lastHWLocation.longitude))
        val markerOptions =
            MarkerOptions()
                .position(LatLng(lastHWLocation.latitude, lastHWLocation.longitude))
                .title("Your Location")
                .snippet(addressFeatureName)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_star_24px))

        currentLocationMarker = locateMarkerWithMarkerOptions(currentLocationMarker, markerOptions)
    }

    private fun locateMarkerWithMarkerOptions(
        marker: Marker?,
        markerOptions: MarkerOptions?
    ): Marker {
        marker?.remove()
        return huaweiMap!!.addMarker(markerOptions)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView!!.onSaveInstanceState(mapViewBundle)
    }

}

