package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.alfayedoficial.kotlinutils.kuSnackBar
import com.alfayedoficial.kotlinutils.kuToast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.setupLocationServiceWithPermissionCheck
import com.udacity.project4.utils.AppConstant.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import permissions.dispatcher.*

@RuntimePermissions
class SaveReminderFragment : BaseFragment() {

    private var _binding: FragmentSaveReminderBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //Get the view model this time as a single to be shared with the another fragment
    override val mViewModel: SaveReminderViewModel by inject()

    private val geofencingClient by lazy {
        LocationServices.getGeofencingClient(requireActivity())
    }

    private lateinit var reminderDataItem: ReminderDataItem

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val setupLocationServiceResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        setupLocationServiceWithPermissionCheck()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = this@SaveReminderFragment
            viewModel = mViewModel
            fragment = this@SaveReminderFragment
        }
        setDisplayHomeAsUpEnabled(true)
        setupLocationServiceWithPermissionCheck()
    }

    fun selectLocation() {
        //   Navigate to another fragment to get the user location
        mViewModel.navigationCommand.value = NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    fun onValidateAndSaveReminder() {
        val title = mViewModel.reminderTitle.value
        val description = mViewModel.reminderDescription.value
        val location = mViewModel.reminderSelectedLocationStr.value
        val latitude = mViewModel.latitude.value
        val longitude = mViewModel.longitude.value

        reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

        if (mViewModel.validateEnteredData(reminderDataItem)) {
            if (checkLocationPermissions()){
                val locationRequest = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_LOW_POWER
                }
                val locationBuilder = LocationSettingsRequest.Builder().apply {
                    addLocationRequest(locationRequest)
                }
                LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(locationBuilder.build()).apply {
                    addOnSuccessListener {
                        createGeoFence()
                    }
                    addOnFailureListener {
                        kuSnackBar(getString(R.string.Please_Enable))!!.setAction(getString(R.string.try_again)) {
                            onValidateAndSaveReminder()
                        }.show()
                    }
                }

            }else{
                setupLocationServiceWithPermissionCheck()
                (activity as RemindersActivity).setupLocationServiceWithPermissionCheck()
            }
        }
    }

    // Mark -*- handle Permissions

    private fun checkLocationPermissions(): Boolean {
        return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
        return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) } else  true
    }

    private fun checkPermission(permission: String) = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(), permission)
    // NeedsPermission method is called when the user has not granted the permission
    @RequiresApi(Build.VERSION_CODES.Q)
    @NeedsPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    fun setupLocationService(){
        if (checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            // Do the task needing access to the location
            return
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
    @RequiresApi(Build.VERSION_CODES.Q)
    @OnShowRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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
    @RequiresApi(Build.VERSION_CODES.Q)
    @OnPermissionDenied(Manifest.permission.ACCESS_BACKGROUND_LOCATION )
    fun onDeniedAskLocation() {
        kuToast(getString(R.string.location_permission_denied))
    }

    // OnNeverAskAgain method is called if the user has denied the permission and checked "Never ask again"
    @RequiresApi(Build.VERSION_CODES.Q)
    @OnNeverAskAgain(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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


    private fun createGeoFence() {

        val geofenceBuilder = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(500)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()


        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofenceBuilder)
            .build()


        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            setupLocationServiceWithPermissionCheck()
            (activity as RemindersActivity).setupLocationServiceWithPermissionCheck()
            return
        }

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                mViewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                kuSnackBar(it.message.toString())!!.setAction(getString(R.string.try_again)) {
                    onValidateAndSaveReminder()
                }.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        mViewModel.onClear()
    }
}
