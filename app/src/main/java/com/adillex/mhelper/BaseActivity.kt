package com.adillex.mhelper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class BaseActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }
    private fun updateUI(currentUser : FirebaseUser?){
        if(currentUser == null){
            startActivity(Intent(this,LogInActivity::class.java))
        }else{
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}
