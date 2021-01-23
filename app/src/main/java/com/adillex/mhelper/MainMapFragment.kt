package com.adillex.mhelper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.adillex.mhelper.databinding.FragmentMainMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*



class MainMapFragment : Fragment(){
    private var param1: String? = null
    private var param2: String? = null
    private var googleMap: GoogleMap? = null
    private var mAuth: FirebaseAuth? = null
    private var myRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var myEvents:DatabaseReference? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private lateinit var binding: FragmentMainMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_map, container, false)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("users")
        myEvents = database!!.getReference("events")

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showDialog()
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(object: OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
            override fun onMapReady(p0: GoogleMap?) {
                googleMap = p0
                googleMap?.uiSettings?.isZoomControlsEnabled
                googleMap?.setOnMarkerClickListener(this)
                //googleMap?.moveCamera()

            }

            override fun onMarkerClick(p0: Marker?): Boolean {
                if(p0?.tag != null) {
                    val bundle = Bundle()
                    bundle.putString("eventId", p0?.tag.toString())
                    Log.w("выход из майнмапа", "выход из майнмапа")
                    view?.findNavController()
                        ?.navigate(R.id.action_mainMapFragment_to_eventShowFragment, bundle)
                }
                return false
            }
        })
        activity?.let{context->
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions()
            }else{
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
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
        val options = MarkerOptions()
        myEvents?.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                //val list = ArrayList<Event>()
                p0.children.forEach { dataSnapshot ->
                    val event = dataSnapshot.getValue(Event::class.java)
                    if(event?.userId != mAuth?.uid) {
                        event?.workingStatus?.let {
                            if (it == 1) {
                                val latlng = LatLng(event?.lat!!, event?.lon!!)
                                options.position(latlng)
                                options.title(event?.nickname)
                                options.snippet(event?.usermail)
                                val marker = googleMap?.addMarker(options)
                                marker?.tag = dataSnapshot.key
                            }
                        }
                    }
                }
            }

        })



        return binding.root
    }

    private fun showDialog(){
        activity?.let {
            val alertDialog= AlertDialog.Builder(it)
            alertDialog.setTitle("Закрыть проиложение?");
            alertDialog.setPositiveButton("OK"
            ) { dialog, _ ->
                dialog.dismiss()
                it.finishAffinity()
            }
            alertDialog.setNegativeButton("ОТМЕНА"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
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
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainMapFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private fun requestPermissions(){
        activity?.let{context->
            // Request the permission
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                102
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 102){
            if(grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                        location?.let { location ->
                            val latlng = LatLng(location.latitude, location.longitude)
                            val options = MarkerOptions()
                            options.position(latlng)
                            activity?.let{ context ->
                                options.icon(bitmapDescriptorFromVector(context, R.drawable.ic_my_location_black_24dp));
                            }
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

//    override fun onMarkerClick(p0: Marker?): Boolean {
//        val bundle = Bundle()
//        bundle.putString("eventId", p0?.tag.toString())
//        view?.findNavController()?.navigate(R.id.action_mainMapFragment_to_eventShowFragment, bundle)
//        return false
//    }

}
