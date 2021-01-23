package com.adillex.mhelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.adillex.mhelper.databinding.ActivityRaitingBarBinding
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_raiting_bar.*

class RaitingBarActivity : AppCompatActivity() {
    private var binding: ActivityRaitingBarBinding? = null
    private var myRef: DatabaseReference? = null
    private var myRefUser: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var event: Event? = null
    private var uid:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_raiting_bar);
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("events")
        myRefUser = database!!.getReference("users")
        binding?.getRatingButtonVery?.setOnClickListener {
            val numStar = binding?.ratingBar?.numStars
            val eventid = intent.getStringExtra("eventid")

            myRef?.child(eventid!!)?.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    event = p0.getValue(Event::class.java)
                    uid = event?.responds
                }

            })

            myRefUser?.child(uid!!)?.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    val num = user?.starNum
                    val star = user?.stars
                    val total = num!!.times(star!!).plus(numStar!!).div(numStar+1)
                    user.stars = total
                    user.starNum = num.plus(1)

                    myRefUser?.child(uid!!)?.setValue(user)
            }
        })
        }
    }

}