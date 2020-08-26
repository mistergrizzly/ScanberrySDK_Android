package com.mistergrizzly.scanberryskdandroid

import android.app.Application
import io.scanberry.sdk.api.Scanberry
import io.scanberry.sdk.api.ScanberryDocumentDetector
import io.scanberry.sdk.api.ScanberryUtils

class ScanBerryApplication : Application() {

    lateinit var scanBerry: Scanberry

    override fun onCreate() {
        super.onCreate()
        scanBerry = Scanberry.Builder(this)
            .license("REPLACE_YOUR_LICENSE_KEY")
            .build()
    }

    fun getScanberryDocumentDetector(): ScanberryDocumentDetector {
        return scanBerry.getDocumentDetector()
    }

    fun getScanberryUtils(): ScanberryUtils {
        return scanBerry.getUtils()
    }
}