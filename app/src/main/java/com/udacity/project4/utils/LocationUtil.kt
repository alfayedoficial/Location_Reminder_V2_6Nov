package com.udacity.project4.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager

object LocationUtil {

    var lastKnownLocation: Location? = null


    fun Context.isLocationEnabled() : Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

}