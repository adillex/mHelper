package com.adillex.mhelper

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth


class App : Application(){

    companion object{
        public fun generateId(): String {
            return "${(100..999).random()}${System.currentTimeMillis()}"
        }
        public fun saveSharedPreferences(context: Context,key:String,value: String){
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.putString(key,value)
            editor.commit()
        }
        public fun readSharedPreferences(context: Context,key:String):String{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(key,"null").toString()
        }
    }
}