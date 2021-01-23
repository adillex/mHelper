package com.adillex.mhelper

object phoneNumber {
    private var num: Int? = null
    fun setPhoneNumber(newNumber: Int?){
        if(checkNumber(newNumber)){
            num = newNumber
        }
    }
    fun checkNumber(newNumber: Int?): Boolean{
        if((newNumber.toString()).length == 11){
            return true
        }
        return false
    }
    fun getPhoneNumber(): Int? {
        return num
    }
}