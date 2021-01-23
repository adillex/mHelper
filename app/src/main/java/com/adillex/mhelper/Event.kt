package com.adillex.mhelper

import java.io.Serializable

class Event: Serializable {
    var lon: Double? = null
    var lat: Double? = null
    var workingStatus: Int? = null
    //var exCharecteristecs : ExCharecteristecs? = null
    //var responded: Array<String>? = null
    var responds: String? = null
    var nickname: String? = null
    var description: String? = null
    var usermail: String? = null
    var userId: String? = null
    var eventId: String? = null
    var star: Int? = null
}