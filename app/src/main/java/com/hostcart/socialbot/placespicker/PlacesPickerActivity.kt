package com.hostcart.socialbot.placespicker

import android.Manifest
import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hostcart.socialbot.R
import com.hostcart.socialbot.utils.PermissionsUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.hostcart.socialbot.utils.AppUtils
import com.hostcart.socialbot.utils.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_places_picker_dark.*
import kotlinx.android.synthetic.main.activity_places_picker_light.*
import kotlinx.android.synthetic.main.places_bottomsheet_dark.*
import kotlinx.android.synthetic.main.places_bottomsheet_light.*
import java.util.*


class PlacesPickerActivity : ScopedActivity(), OnMapReadyCallback, NearbyPlacesAdapter.OnClickListener {


    private lateinit var viewModel: PlacesPickerViewModel
    private lateinit var mMap: GoogleMap
    private var markerOptions: MarkerOptions? = null
    private var mMarker: Marker? = null
    private val places = mutableListOf<Place>()
    private lateinit var mAdapter: NearbyPlacesAdapter

    private var rvPlaces: RecyclerView? = null

    private val REQUEST_CODE_ASK_PERMISSIONS = 1
    private val REQUIRED_SDK_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)


    override fun onMapReady(map: GoogleMap?) {
        mMap = map!!
        mMap.setOnCameraMoveListener {
            mMarker?.position = mMap.cameraPosition.target
            if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
                if (switch_nearby_places_dark.isChecked) {
                    viewModel.markerMoved(map.cameraPosition.target)
                }
            } else {
                if (switch_nearby_places_light.isChecked) {
                    viewModel.markerMoved(map.cameraPosition.target)
                }
            }
        }
        checkPermissions()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            setContentView(R.layout.activity_places_picker_dark)
        } else {
            setContentView(R.layout.activity_places_picker_light)

            val window = getWindow()
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite))
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }

        rvPlaces = findViewById(R.id.rv_places)

        viewModel = ViewModelProviders
                .of(this, PlacesPickerViewModelFactory(this, this))
                .get(PlacesPickerViewModel::class.java)


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupAdapter()

        viewModel.currentLocationLiveData.observe(this, Observer {
            if (mMarker == null) {
                markerOptions = MarkerOptions().position(it!!)
                mMarker = mMap.addMarker(markerOptions)
            }

            mMarker?.position = it

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        })

        viewModel.nearbyPlacesLiveData.observe(this, Observer {
            if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
                if (switch_nearby_places_dark.isChecked) {
                    places.clear()
                    places.addAll(it)
                    rvPlaces!!.adapter!!.notifyDataSetChanged()
                }
            } else {
                if (switch_nearby_places_light.isChecked) {
                    places.clear()
                    places.addAll(it)
                    rvPlaces!!.adapter!!.notifyDataSetChanged()
                }
            }
        })
        viewModel.showLocationDialogLiveData.observe(this, Observer {
            enableGps()
        })

        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            get_location_dark.setOnClickListener {
                checkPermissions()
            }
        } else {
            get_location_light.setOnClickListener{
                checkPermissions()
            }
        }

        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            tv_select_this_location_dark.setOnClickListener {
                showDialog()
            }
        } else {
            tv_select_this_location_light.setOnClickListener {
                showDialog()
            }
        }

        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            switch_nearby_places_dark.setOnCheckedChangeListener { switch, isChecked ->
                if (isChecked) {
                    if (!PermissionsUtil.hasLocationPermissions(this@PlacesPickerActivity)) {
                        switch.toggle()
                        Toast.makeText(this@PlacesPickerActivity, R.string.missing_permissions, Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.markerMoved(mMap.cameraPosition.target)
                    }
                } else {
                    places.clear()
                    rvPlaces!!.adapter!!.notifyDataSetChanged()
//                mAdapter.notifyDataSetChanged()
                }
            }
        } else {
            switch_nearby_places_light.setOnCheckedChangeListener { switch, isChecked ->
                if (isChecked) {
                    if (!PermissionsUtil.hasLocationPermissions(this@PlacesPickerActivity)) {
                        switch.toggle()
                        Toast.makeText(this@PlacesPickerActivity, R.string.missing_permissions, Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.markerMoved(mMap.cameraPosition.target)
                    }
                } else {
                    places.clear()
                    rvPlaces!!.adapter!!.notifyDataSetChanged()
//                mAdapter.notifyDataSetChanged()
                }
            }
        }

        mAdapter.onClickListener = this
    }

    private fun enableGps() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    // Location settings are not satisfied. But could be fixed by showing the
                    // user a dialog.
                    try {
                        // Cast to a resolvable exception.
                        val resolvable = exception as ResolvableApiException
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        resolvable.startResolutionForResult(
                                this,
                                LocationRequest.PRIORITY_HIGH_ACCURACY)
                    } catch (e: IntentSender.SendIntentException) {
                        // Ignore the error.
                    } catch (e: ClassCastException) {
                        // Ignore, should be an impossible error.
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            if (resultCode == RESULT_OK) {
                viewModel.getCurrentLocation()
                if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
                    if (switch_nearby_places_dark.isChecked) {
                        viewModel.markerMoved(mMap.cameraPosition.target)
                    }
                } else {
                    if (switch_nearby_places_light.isChecked) {
                        viewModel.markerMoved(mMap.cameraPosition.target)
                    }
                }
            } else { }
        }
    }

    override fun onClick(view: View, place: Place) {
        showDialog(place)
    }

    private fun setupAdapter() {
        rvPlaces!!.layoutManager = LinearLayoutManager(this)
        mAdapter = NearbyPlacesAdapter(this, places)
        rvPlaces!!.adapter = mAdapter
    }

    private fun checkPermissions() {
        val missingPermissions = ArrayList<String>()
        // check all required dynamic permissions
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (missingPermissions.isNotEmpty()) {
            // request all missing permissions
            val permissions = missingPermissions
                    .toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                for (index in permissions.indices.reversed()) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, R.string.missing_permissions, Toast.LENGTH_LONG).show()
                        finish()
                        return
                    }
                }
                // all permissions were granted
                Log.d("3llomi", "onRequestPermissionsResult")
                viewModel.getCurrentLocation()

            }
        }
    }

    private fun showDialog(place: Place? = null) {
        val dialog = AlertDialog.Builder(this)
        dialog.apply {
            setTitle(getString(R.string.user_this_location))

            if (place != null) {
                val message = "${place.name} \n ${place.address}"
                setMessage(message)
            }

            setNegativeButton(R.string.change_location, null)
            setPositiveButton(R.string.select) { _, _ ->
                val data = Intent()
                if (place != null) {
                    data.putExtra(Place.EXTRA_PLACE, place)
                } else {
                    data.putExtra(Place.EXTRA_PLACE, Place("", "", "", mMap.cameraPosition.target))
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }

            dialog.show()
        }
    }
}
