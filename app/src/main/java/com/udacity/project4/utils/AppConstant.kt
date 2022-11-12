package com.udacity.project4.utils

import com.google.android.gms.location.Geofence
import com.udacity.project4.BuildConfig

object AppConstant {

    const val USER = "USER"

    const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
    const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

    const val TAG = "SaveReminderFragment"
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val GEOFENCE_EXPIRY = Geofence.NEVER_EXPIRE

    const val PERMISSION_CODE_LOCATION_REQUEST = 1
    const val DEFAULT_ZOOM_LEVEL = 15f
}