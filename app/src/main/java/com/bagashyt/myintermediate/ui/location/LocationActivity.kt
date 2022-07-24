package com.bagashyt.myintermediate.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.bagashyt.myintermediate.R
import com.bagashyt.myintermediate.databinding.ActivityLocationBinding
import com.bagashyt.myintermediate.ui.main.MainActivity
import com.bagashyt.myintermediate.utils.showToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@ExperimentalPagingApi
class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var token: String = ""
    private val locationViewModel: LocationViewModel by viewModels()

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        getDeviceLocation()
        setMapStyle()
        markStoryLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        lifecycleScope.launchWhenCreated {
            launch {
                locationViewModel.getAuthToken().collect { authToken ->
                    if (!authToken.isNullOrEmpty()) token = authToken
                }
            }
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)

        token = intent.getStringExtra(MainActivity.EXTRA_TOKEN) ?: ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f))
                } else {
                    showToast(this, "Please activate location")
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun markStoryLocation() {
        lifecycleScope.launchWhenResumed {
            launch {
                locationViewModel.getListStories(token).collect { result ->
                    result.onSuccess { response ->
                        response.stories.forEach { story ->
                            if (story.lat != null && story.lon != null) {
                                val latLng = LatLng(story.lat, story.lon)

                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(story.name)
                                        .snippet("Lat: ${story.lat}, Lon: ${story.lon}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getDeviceLocation()
            }
        }
}