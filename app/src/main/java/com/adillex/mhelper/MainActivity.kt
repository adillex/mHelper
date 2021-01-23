package com.adillex.mhelper

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.adillex.mhelper.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        val bottomNavigationView = binding.bottomNavigationView
        val navController = findNavController( R.id.fragment)
        supportActionBar?.hide()
        bottomNavigationView.setupWithNavController(navController)
    }

//    override fun onBackPressed() {
//        showDialog()
//    }

    private fun showDialog(){
        val alertDialog= AlertDialog.Builder(this)
        alertDialog.setTitle("Закрыть проиложение?");
        alertDialog.setPositiveButton("OK"
        ) { dialog, which ->
            dialog.dismiss()
            finishAffinity()
        }
        alertDialog.setNegativeButton("ОТМЕНА"
        ) { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
    }



}
