package com.example.myphonec

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. Khởi tạo FirebaseApp trước tiên
        FirebaseApp.initializeApp(this)
        Log.d("AppCheck", "FirebaseApp initialized. Build Type: ${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}")

        // 2. Cài đặt App Check Provider TRƯỚC KHI gọi bất kỳ dịch vụ Firebase nào khác
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        
        if (BuildConfig.DEBUG) {
            Log.d("AppCheck", "Installing DebugAppCheckProviderFactory (Emulator Mode)")
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            Log.d("AppCheck", "Installing PlayIntegrityAppCheckProviderFactory (Production Mode)")
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
        
        Log.d("AppCheck", "Firebase App Check installation complete.")
    }
}
