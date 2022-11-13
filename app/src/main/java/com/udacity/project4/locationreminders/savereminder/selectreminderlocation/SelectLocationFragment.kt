package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.alfayedoficial.kotlinutils.kuToast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.setupLocationServiceWithPermissionCheck
import com.udacity.project4.utils.AppConstant.DEFAULT_ZOOM_LEVEL
import com.udacity.project4.utils.AppConstant.PERMISSION_CODE_LOCATION_REQUEST
import com.udacity.project4.utils.AppConstant.TAG
import com.udacity.project4.utils.LocationUtil.isLocationEnabled
import com.udacity.project4.utils.checkPermissionUtils
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import permissions.dispatcher.*
import java.util.*

@RuntimePermissions
class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val mViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private var marker: Marker? = null

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val setupLocationServiceResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        setupLocationServiceWithPermissionCheck()
    }


    @SuppressLint("MissingPermission")
    val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        //Create a LatLngBounds object. --> start to egypt map
        val egyptBounds = LatLngBounds.builder()
            .include(LatLng(31.4021, 25.0534))
            .include(LatLng( 21.8623, 36.7628))
            .build()

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(egyptBounds , 20))


        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)

        getUserLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this
        binding.fragment = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        setupLocationServiceWithPermissionCheck()

        return binding.root
    }

    fun onLocationSelected() {
        if (!requireContext().checkPermissionUtils(Manifest.permission.ACCESS_FINE_LOCATION) || !requireContext().checkPermissionUtils(Manifest.permission.ACCESS_FINE_LOCATION)){
            setupLocationServiceWithPermissionCheck()
            return
        }
        marker?.let { marker ->
            mViewModel.latitude.value = marker.position.latitude
            mViewModel.longitude.value = marker.position.longitude
            mViewModel.reminderSelectedLocationStr.value = marker.title
            mViewModel.navigationCommand.value = NavigationCommand.Back
        }
    }



    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name))
            marker?.showInfoWindow()

            map.animateCamera(CameraUpdateFactory.newLatLng(poi.latLng))
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.

            val snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude)
            map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )

            marker?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        map.setOnMapClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.

            val snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude)
            map.clear()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )

            marker?.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        map.isMyLocationEnabled = true
        Log.d("MapsActivity", "getLastLocation Called")
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM_LEVEL))
                    marker = map.addMarker(
                        MarkerOptions()
                            .position(userLocation)
                            .title(getString(R.string.my_location))
                    )
                    marker?.showInfoWindow()
                }
            }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Styling  failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Style not found. Error: ", e)
        }
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId){
                    R.id.normal_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                        return true
                    }
                    R.id.hybrid_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_HYBRID
                        return true
                    }
                    R.id.satellite_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        return true
                    }
                    R.id.terrain_map -> {
                        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        return true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // Mark -*- handle Permissions
    // NeedsPermission method is called when the user has not granted the permission
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION)
    fun setupLocationService(){
        if (requireContext().isLocationEnabled()) {
            // Do the task needing access to the location
            val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync(callback)

            onLocationSelected()

            setMenu()

//            getUserLocation()
        }else{
            // Show dialog to enable location
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.enable_location))
                .setMessage(getString(R.string.message_location))
                .setPositiveButton(getString(R.string.locationSettings)) { dialog, _ ->
                    // Open location settings
                    onSettingScreen()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    // Do nothing
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun onSettingScreen() {
        setupLocationServiceResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    // OnShowRationale method is called if the user has denied the permission before
    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onRationaleAskLocation(request : PermissionRequest) {
        // Show the rationale
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.location_permission))
            .setMessage(getString(R.string.location_permission_message))
            .setPositiveButton(getString(R.string._ok)) { dialog, _ ->
                request.proceed()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                // Do nothing
                request.cancel()
                dialog.dismiss()
            }
            .show()
    }

    // OnPermissionDenied method is called if the user has denied the permission
    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onDeniedAskLocation() {
        kuToast(getString(R.string.location_permission_denied))
    }

    // OnNeverAskAgain method is called if the user has denied the permission and checked "Never ask again"
    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onNeverAskLocation() {
        val onApplicationSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        onApplicationSettings.data = Uri.parse("package:${requireActivity().packageName}")
        setupLocationServiceResult.launch(onApplicationSettings)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }


    // Mark -*- handle Permissions



}
