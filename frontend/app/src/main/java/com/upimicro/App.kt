package com.upimicro

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        Log.d("FIREBASE_INIT", "Firebase initialized successfully")
    }
}