package com.udacity.project4.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.alfayedoficial.kotlinutils.kuChangeBackgroundTint
import com.alfayedoficial.kotlinutils.kuHide
import com.alfayedoficial.kotlinutils.kuShow
import com.udacity.project4.R
import com.udacity.project4.utils.AppConstant.EXTRA_ReminderDataItem
import com.google.android.material.button.MaterialButton
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//fun Fragment.setDisplayHomeAsUpEnabled(bool: Boolean) {
//    if (activity is AppCompatActivity) {
//        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
//            bool
//        )
//    }
//}

@BindingAdapter("app:address")
fun TextView.setAddress(address: MutableLiveData<String>) {
    if (address.value != null) {
        text = address.value
        kuShow()
    }else{
        text = ""
        kuHide()
    }
}



@BindingAdapter("app:saveEnable")
fun MaterialButton.setSaveEnable(bool: MutableLiveData<Boolean>) {
    isEnabled = if (bool.value != null) {
        bool.value!!
    }else{
        false
    }
    kuChangeBackgroundTint(if (bool.value == true) R.color.TemplateGreen else R.color.gray)

}


fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
    val intent = Intent(context, ReminderDescriptionActivity::class.java)
    intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
    return intent
}

fun Context.checkPermissionUtils(permission: String) = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, permission)


