package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.alfayedoficial.kotlinutils.kuToast
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.AppConstant

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {


    private lateinit var binding: ActivityReminderDescriptionBinding
    private val geofencingClient: GeofencingClient by lazy { LocationServices.getGeofencingClient(this) }
    private lateinit var mGoogleMap: GoogleMap
    private var userLocation: LatLng? = null

    private val callback = OnMapReadyCallback { googleMap ->
        mGoogleMap = googleMap

        //Create a LatLngBounds object. --> start to egypt map
        val egyptBounds = LatLngBounds.builder()
            .include(LatLng(31.4021, 25.0534))
            .include(LatLng( 21.8623, 36.7628))
            .build()

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(egyptBounds , 20))


        moveCameraToLocation()

    }

    private fun moveCameraToLocation() {
        val marker = MarkerOptions()
            .position(userLocation!!)
            .icon(getBitmapFromVector(this,R.drawable.ic_marker_user))

        mGoogleMap.addMarker(marker)
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!,15.0f))
    }

    private fun getBitmapFromVector(context: Context, resId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context,resId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (intent.getSerializableExtra(AppConstant.EXTRA_ReminderDataItem) as? ReminderDataItem?)?.let {
            userLocation = LatLng(it.latitude!!,it.longitude!!)
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(callback)
            binding.reminderDataItem = it
            geofencingClient.removeGeofences(listOf(it.id)).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    kuToast("Remove Geofence Successfully")
                }else{
                    kuToast("Remove Geofence Failed")
                }
            }
        }
    }
}
