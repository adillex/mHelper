package com.adillex.mhelper

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
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
    private var event: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val arguments = intent.extras
        val eventId = arguments?.get("eventId").toString()

        title = "описание события"

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_show)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        dialogRaiting = DialogRaitingFragment()

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("events")
        myRefUser = database!!.getReference("users")

        binding?.showMapButton?.setOnClickListener{
            val intent = Intent(this@EventShowActivity, HelpMapActivity::class.java)
            intent.putExtra("event", event)
            startActivity(intent)
        }


        myRef?.child(eventId!!)?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                event = p0.getValue(Event::class.java)

                val content = SpannableString(event?.usermail)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)

                binding?.masterMail?.text = content
                binding?.masterMail?.setOnClickListener{
                    val intent = Intent(this@EventShowActivity, MyAccountActivity::class.java)
                    intent.putExtra("userId",event?.userId)
                    startActivity(intent)                }
                binding?.headerEventNickName?.text = event?.nickname
                binding?.description?.text = event?.description
                if(event?.workingStatus == 1){
                    binding?.headerWorkingStatus?.text = "в поисках волотеров"
                    if(event!!.userId == mAuth?.uid) {

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
                            myRefUser?.child(event!!.userId!!)?.child("events")?.child(eventId)
                                ?.child("workingStatus")?.setValue(2)
                        }
                    }

                }else if(event?.workingStatus == 2){
                    if(event!!.userId!! != mAuth?.uid!!){
                        binding?.klick?.visibility = View.GONE
                    }

                    binding?.headerWorkingStatus?.text = "в процессе выполнения"
                    binding?.klick?.setOnClickListener{
                        myRef?.child(eventId)!!.child("workingStatus").setValue(0)
                        myRefUser?.child(event?.userId!!)?.child("events")?.child(eventId)
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
                        intent.putExtra("event", event)
                        startActivity(intent)
                        finish()
                    }

                }else{//if(event?.workingStatus == 0)
                    binding?.headerWorkingStatus?.text = "завершен"
                    binding?.klick?.visibility = View.GONE
                    binding?.klickOut?.visibility = View.GONE
                    binding?.ratingBar?.visibility = View.VISIBLE
                    binding?.showMapButton?.visibility = View.GONE
                    event?.star?.let {star->
                        binding?.ratingBar?.rating = star
                    }
                    binding?.LinearResponded?.visibility = View.VISIBLE
                    myRefUser?.child(event?.responds!!)?.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val user = p0.getValue(User::class.java)
                            val content = SpannableString(user?.name)
                            content.setSpan(UnderlineSpan(), 0, content.length, 0)

                            binding?.headerResponded?.text = content
                            binding?.headerResponded?.setOnClickListener{
                                val intent = Intent(this@EventShowActivity, MyAccountActivity::class.java)
                                intent.putExtra("userId",event?.responds)
                                startActivity(intent)
                            }
                        }
                    })

                }
            }

        })


    }
}

