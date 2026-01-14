package com.beballer.beballer

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.beballer.beballer.base.AppLifecycleListener
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(this@App))
    }

}
