package com.huawei.pinyouraddressonmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import android.Manifest.permission.INTERNET
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import com.huawei.hms.maps.MapView
import android.os.Build
import androidx.core.app.ActivityCompat
import android.util.Log
import android.content.IntentSender
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.common.ApiException
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import android.os.Looper
import com.huawei.hms.location.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "MapViewDemoActivity"
    //Huawei map
    private var hMap: HuaweiMap? = null

    private var mMapView: MapView? = null

    private val REQUEST_CODE = 1

    private val RUNTIME_PERMISSIONS = arrayOf<String>(
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE,
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION,
        INTERNET
    )

    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    private var mLocationRequest: LocationRequest? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }
        val fusedLocationProviderClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        val settingsClient = LocationServices.getSettingsClient(this)
        var mLocationallback: LocationCallback? =null
        val builder = LocationSettingsRequest.Builder()
        mLocationRequest = LocationRequest()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
//check Location Settings
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                //Have permissions， send requests
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationallback,
                    Looper.getMainLooper())
                    .addOnSuccessListener {

                    }

            }
            .addOnFailureListener { e ->
                //Settings do not meet targeting criteria
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae = e as ResolvableApiException
                        //Calling startResolutionForResult can pop up a window to prompt the user to open the corresponding permissions
                        rae.startResolutionForResult(this@MainActivity, 0)
                    } catch (sie: IntentSender.SendIntentException) {
                        //…
                    }

                }
            }

        mLocationallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult != null) {
                    //Process the location callback result.
                    Log.d("Location","got location")
                }
            }

        }
        //get mapview instance
        mMapView = findViewById(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView!!.onCreate(mapViewBundle)
        //get map instance
        mMapView!!.getMapAsync(this)
    }


    override fun onMapReady(map: HuaweiMap?) {
        Log.d(TAG, "onMapReady: ")
        hMap = map;
        hMap!!.isMyLocationEnabled = true// Enable the my-location overlay.
        hMap!!.uiSettings.isMyLocationButtonEnabled = true
    }

    override fun onStart() {
        super.onStart()
        mMapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMapView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView!!.onDestroy()
    }

    override fun onPause() {
        mMapView!!.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.onResume()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mMapView!!.onSaveInstanceState(mapViewBundle)
    }

    private fun hasPermissions(context: Context, runtimePermissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && runtimePermissions != null) {
            for (permission in runtimePermissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
}

