package com.udacity.project4

import android.app.Application
import com.alfayedoficial.kotlinutils.KUPreferences
import com.udacity.project4.authentication.LoginViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.AppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {

    companion object{

        private var _appPreferences: KUPreferences? = null
        var appPreferences: KUPreferences
            get() = _appPreferences!!
            set(value) { _appPreferences = value }
    }

    override fun onCreate() {
        super.onCreate()
        _appPreferences = AppPreferences.initAppPreferences(this.applicationContext)

        /**
         * use Koin Library as a service locator
         */
        val viewModelModule3 = module {
            viewModel { LoginViewModel() }
        }

        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@MyApp) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule , viewModelModule3))
        }
    }
}