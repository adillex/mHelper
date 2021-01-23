package com.adillex.mhelper

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.adillex.mhelper.databinding.ActivityEventShowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class EventShowActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var myRef: DatabaseReference? = null
    private var myRefUser: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var binding: ActivityEventShowBinding? = null
    private var dialogRaiting: DialogRaitingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val arguments = intent.extras
        val eventId = arguments?.get("eventId").toString()

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_show)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        dialogRaiting = DialogRaitingFragment()

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("events")
        myRefUser = database!!.getReference("users")


        myRef?.child(eventId!!)?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val event = p0.getValue(Event::class.java)
                binding?.headerEventNickName?.text = event?.nickname
                binding?.description?.text = event?.description
                if(event?.workingStatus == 1){
                    binding?.headerWorkingStatus?.text = "в поисках волотеров"
                    if(event.userId == mAuth?.uid) {

                        binding?.klick?.text = "завершить досрочно"
                        binding?.klick?.setOnClickListener {
                            myRef?.child(eventId)!!.child("workingStatus").setValue(0)
                            myRefUser?.child(mAuth?.uid!!)?.child("events")?.child(eventId)
                                ?.child("workingStatus")?.setValue(0)

                        }
                    }else{
                        binding?.klick?.text = "откликнуться"
                        binding?.klick?.setOnClickListener {
                            myRef?.child(eventId)!!.child("workingStatus").setValue(2)
                            myRefUser?.child(event.userId!!)?.child("events")?.child(eventId)
                                ?.child("workingStatus")?.setValue(2)
                        }
                    }

                }else if(event?.workingStatus == 2){
                    if(event.userId!! != mAuth?.uid!!){
                        binding?.klick?.visibility = View.GONE
                    }
                    binding?.headerWorkingStatus?.text = "в процессе выполнения"
                    binding?.klick?.setOnClickListener{
                        myRef?.child(eventId)!!.child("workingStatus").setValue(0)
                        myRefUser?.child(event.userId!!)?.child("events")?.child(eventId)
                            ?.child("workingStatus")?.setValue(0)
                        //dialogRaiting!!.show(fragmentManager, "DialogRaitingFragment")
                        //val num: String = App.readSharedPreferences(this?, "stars")
//                        var numArray: Array<Int>? = null
//                        if(myRefUser?.child(mAuth?.uid!!)?.child("stars") == null){
//                            numArray = arrayOf()
//                        }else{
//                            numArray = myRefUser?.child(mAuth?.uid!!)?.child("stars") as Array<Int>
//                        }
                        val intent = Intent(this@EventShowActivity, RaitingBarActivity::class.java)
                        intent.putExtra("eventid", eventId)
                        startActivity(intent)
                        finish()
                    }

                }else{//if(event?.workingStatus == 0)
                    binding?.headerWorkingStatus?.text = "завершен"
                    binding?.klick?.visibility = View.GONE
                    binding?.klickOut?.visibility = View.GONE
                }
            }

        })


    }
}

