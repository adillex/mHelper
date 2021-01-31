package com.adillex.mhelper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.adillex.mhelper.databinding.ActivityHelpMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*

class HelpMapActivity : AppCompatActivity() {
    private var binding:ActivityHelpMapBinding? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var googleMap: GoogleMap? = null
    private var event: Event? = null


    override fun onResume() {
        super.onResume()
        binding?.mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding?.mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.mapView?.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_help_map)
        binding?.mapView?.onCreate(savedInstanceState)
        binding?.mapView?.getMapAsync(object: OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
            override fun onMapReady(p0: GoogleMap?) {
                googleMap = p0
                googleMap?.uiSettings?.isZoomControlsEnabled
                googleMap?.setOnMarkerClickListener(this)
                //googleMap?.moveCamera()
                event = intent.getSerializableExtra("event") as Event?
                title = event?.nickname
                val latlng = LatLng(event?.lat!!, event?.lon!!)

                val options = MarkerOptions()
                options.position(latlng)
                googleMap?.addMarker(options)
            }

            override fun onMarkerClick(p0: Marker?): Boolean {
                return false
            }

        })

        this?.let{context->
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions()
            }else{
                fusedLocationClient?.lastLocation
                    ?.addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                        location?.let { location ->
                            val latlng = LatLng(location.latitude, location.longitude)

                            val options = MarkerOptions()
                            options.position(latlng)
                            options.icon(bitmapDescriptorFromVector(context, R.drawable.ic_my_location_black_24dp));
                            googleMap?.addMarker(options)

                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latlng, 15.0f
                                )
                            )
                        }
                    }
            }
        }



    }

    private fun requestPermissions(){
        this?.let{context->
            // Request the permission
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                102
            )
        }
    }
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}


