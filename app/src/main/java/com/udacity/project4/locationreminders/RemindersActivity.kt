package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.alfayedoficial.kotlinutils.kuToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.utils.LocationUtil.isLocationEnabled
import permissions.dispatcher.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
@RuntimePermissions
class RemindersActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityReminderDescriptionBinding

    private val setupLocationServiceResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        setupLocationServiceWithPermissionCheck()
    }

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navHostFragment.navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // Mark -*- handle Permissions
    // NeedsPermission method is called when the user has not granted the permission
    @NeedsPermission(android.Manifest.permission.ACCESS_FINE_LOCATION , android.Manifest.permission.ACCESS_COARSE_LOCATION)
    fun setupLocationService(){
        if (isLocationEnabled()) {
            // Do the task needing access to the location

        }else{
            // Show dialog to enable location
            MaterialAlertDialogBuilder(this)
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
    @OnShowRationale(android.Manifest.permission.ACCESS_FINE_LOCATION , android.Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onRationaleAskLocation(request : PermissionRequest) {
        // Show the rationale
        MaterialAlertDialogBuilder(this)
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
    @OnPermissionDenied(android.Manifest.permission.ACCESS_FINE_LOCATION , android.Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onDeniedAskLocation() {
        kuToast(getString(R.string.location_permission_denied))
    }

    // OnNeverAskAgain method is called if the user has denied the permission and checked "Never ask again"
    @OnNeverAskAgain(android.Manifest.permission.ACCESS_FINE_LOCATION , android.Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onNeverAskLocation() {
        val onApplicationSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        onApplicationSettings.data = Uri.parse("package:${packageName}")
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
