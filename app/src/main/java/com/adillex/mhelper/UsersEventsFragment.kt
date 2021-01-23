package com.adillex.mhelper

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.adillex.mhelper.databinding.FragmentUsersEventsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var mAuth: FirebaseAuth? = null
private var myRef: DatabaseReference? = null
private var database: FirebaseDatabase? = null
private lateinit var binding: FragmentUsersEventsBinding
private var adapter:RecycleViewAdapter? = null

class UsersEventsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_users_events,container,false)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("events")

        binding.EventRCVW.adapter = adapter
        binding.EventRCVW.setHasFixedSize(true)

        myRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Event>()
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        val event = it.getValue<Event>(Event::class.java)
                        if (event?.workingStatus == 1 || event?.workingStatus == 0) {
                            event?.let {
                                list.add(event)
                            }
                        }

                    }
                    adapter?.setEvents(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FirstFragment", error.toString())
            }
        })

            return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UsersEventsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
