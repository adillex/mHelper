package com.adillex.mhelper

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.adillex.mhelper.databinding.ActivityRecyclerViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class recyclerViewActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var myRef: DatabaseReference? = null
    private var myRefUser: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var adapter:RecycleViewAdapter? = null
    private var binding: ActivityRecyclerViewBinding? = null
    private var code: Int? = null
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        code = intent.getIntExtra("code", 0)
        uid = intent.getStringExtra("uid")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recycler_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("events")
        myRefUser = database!!.getReference("users")

        adapter = RecycleViewAdapter(this@recyclerViewActivity)
        binding?.EventRCVW?.adapter = adapter
        binding?.EventRCVW?.setHasFixedSize(true)

        title = when (code) {
            1 -> "Активные события"
            2 -> "Законченные события"
            else -> "Выполненные мною события"
        }


        if(code == 1 || code == 2) {

            myRefUser?.child(uid!!)?.child("events")!!.addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = ArrayList<Event>()
                    if (code == 1){
                        if (snapshot.exists()) {
                            snapshot.children.forEach {
                                val event = it.getValue<Event>(Event::class.java)
                                event?.eventId = it.key
                                if (event?.workingStatus == 1 || event?.workingStatus == 2) {
                                    list.add(event)
                                }

                            }
                            adapter?.setEvents(list)
                        }
                    }
                    else{
                        if (snapshot.exists()) {
                            snapshot.children.forEach {
                                val event = it.getValue<Event>(Event::class.java)
                                event?.eventId = it.key
                                if (event?.workingStatus == 0) {
                                    list.add(event)
                                }

                            }
                            adapter?.setEvents(list)
                        }
                    }
                }
            })
        } else{
            myRefUser?.child(uid!!)?.child("respondedEvents")!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}


                override fun onDataChange(snapshot: DataSnapshot) {

                    val eventIds = ArrayList<String>()
                    if (snapshot.exists()) {
                        snapshot.children.forEach {
                            eventIds.add(it.key.toString())
                        }


                        findEvents(eventIds)
                        //adapter?.setEvents(list)
                    }
                }
            })
        }
    }

    private fun findEvents(eventIds: ArrayList<String>){
        val list = ArrayList<Event>()
        myRef?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p1: DatabaseError) {
            }

            override fun onDataChange(p1: DataSnapshot){
                if(p1.exists()){
                    p1.children.forEach {
                        eventIds.forEach {myKey->
                            if (myKey == it.key){
                                val event: Event? = it.getValue(Event::class.java)
                                list.add(event!!)
                            }
                        }
                    }
                    adapter?.setEvents(list)
                }
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
