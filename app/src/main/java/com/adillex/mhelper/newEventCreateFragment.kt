package com.adillex.mhelper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.adillex.mhelper.databinding.FragmentNewEventCreateBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.security.Permission

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [newEventCreateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class newEventCreateFragment : Fragment() {
    private var exCharecteristecs:ExCharecteristecs? = null
    private lateinit var binding: FragmentNewEventCreateBinding
    private var myRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var event:Event? = null
    private var myRefUser: DatabaseReference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exCharecteristecs = it.getSerializable("exChar") as ExCharecteristecs?
        }
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_event_create, container, false)

        event = Event()


        database = FirebaseDatabase.getInstance()
        myRefUser = database!!.getReference("users")

        myRef = database!!.getReference("events")
        mAuth = FirebaseAuth.getInstance()
        val manager:LocationManager = activity?.getSystemService( Context.LOCATION_SERVICE) as LocationManager;



        binding.creatWorkB.setOnClickListener{

            event?.usermail = context?.let { it1 -> App.readSharedPreferences(it1,"mail") }
            event?.userId = mAuth?.uid
            event?.workingStatus = 1
            //event?.exCharecteristecs = exCharecteristecs
            event?.description = binding.descriptionET.text.toString()
            event?.nickname = binding.worksNameET.text.toString()

            val key = myRef?.push()?.key
            event?.eventId = key

           // myRef?.child("event")?.child(key!!)?.setValue(event)

            activity?.let{context->
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions()
                }else{
                    if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {

                                    event?.lon = location?.longitude
                                    event?.lat = location?.latitude

                                    myRef?.child(key!!)?.setValue(event)

                                    myRefUser?.child(mAuth?.uid!!)?.child("events")?.child(key!!)
                                        ?.setValue(event)
                                    it.findNavController()
                                        .navigate(R.id.action_addWorkFragment_to_mainMapFragment)
                                }else{
                                    Toast.makeText(activity, "ошибка геоданных геоданные", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }else{
                        Toast.makeText(activity, "включите геоданные", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            newEventCreateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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
                            event?.lon = location.longitude
                            event?.lat = location.latitude
                        }
                    }
            }
        }
    }


}
