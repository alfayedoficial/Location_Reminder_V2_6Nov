package com.udacity.project4.utils

import com.google.android.gms.location.Geofence
import com.udacity.project4.BuildConfig

object AppConstant {

    const val LOCATION_KEY = "LOCATION_KEY"
    const val USER = "USER"

    const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
    const val Geofence_JOB_ID = 573
    const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
    const val GEOFENCE_RADIUS = 300f
    const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    const val TAG = "SaveReminderFragment"
    const val LOCATION_PERMISSION_INDEX = 0
    const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
    const val GEOFENCE_EXPIRY = Geofence.NEVER_EXPIRE

    const val PERMISSION_CODE_LOCATION_REQUEST = 1
    const val DEFAULT_ZOOM_LEVEL = 15f
}