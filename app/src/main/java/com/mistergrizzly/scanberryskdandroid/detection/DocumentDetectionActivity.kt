package com.mistergrizzly.scanberryskdandroid.detection

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mistergrizzly.scanberryskdandroid.R
import com.mistergrizzly.scanberryskdandroid.ScanBerryApplication
import io.scanberry.sdk.api.ScanberryUtils
import io.scanberry.sdk.model.SupportedPictureSize
import kotlinx.android.synthetic.main.activity_document_detection.scanberryCameraPreview

class DocumentDetectionActivity : AppCompatActivity() {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val REQUEST_CODE_PERMISSIONS = 11
    }

    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var isCameraFocused = false

    private lateinit var scanBerryUtils: ScanberryUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_detection)

        scanBerryUtils = (application as ScanBerryApplication).getScanberryUtils()

        if (allPermissionsGranted()) {
            turnCameraOn()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                turnCameraOn()
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // This is used to remove status bar and bottom navigation bar. It will help in calculating correctly screen ratio
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun setFocusParameters() {
        val param: Camera.Parameters? = camera?.parameters
        if (packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) == true) {
            try {
                camera?.setAutoFocusMoveCallback(cameraAutoFocusMoveCallback)
            } catch (e: Exception) {
                Log.d("Test", "Autofocus failed: ${e.message}")
            }
            param?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            camera?.parameters = param
        } else {
            isCameraFocused = true
        }
    }

    private fun turnCameraOn() {
        // Get surface holder from custom view io.scanberry.sdk.api.view.ScanBerryCameraPreview
        surfaceHolder = scanberryCameraPreview.getSurfaceHolder()
        surfaceHolder?.addCallback(surfaceHolderCallback)
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    private val surfaceHolderCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            try {
                camera?.stopPreview()
            } catch (e: Exception) {
                Log.d("Test", "Camera stop preview failed: ${e.message}")
            }

            try {
                camera?.setPreviewDisplay(surfaceHolder)
                camera?.startPreview()
                camera?.setPreviewCallback(previewCallback)
            } catch (e: Exception) {
                Log.d("Test", "Camera start preview failed: ${e.message}")
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            camera?.let {
                it.stopPreview()
                it.setPreviewCallback(null)
                it.release()
                camera = null
            }
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                val cameraId: Int = getBackCamera()
                camera = Camera.open(cameraId)
            } catch (e: RuntimeException) {
                Log.d("Test", "Unable to open camera: ${e.message}")
                return
            }

            val param: Camera.Parameters? = camera?.parameters
            val pSize: Camera.Size? = if (camera != null) getPreviewResolution(camera!!) else null

            pSize?.let {
                param?.setPreviewSize(pSize.width, pSize.height)
                param?.setPictureSize(pSize.width, pSize.height)
                camera?.parameters = param
                camera?.setDisplayOrientation(90)
                setFocusParameters()
                isCameraFocused = true
                val display = windowManager.defaultDisplay
                if (camera != null && display != null) {
                    // Setup custom view io.scanberry.sdk.api.view.ScanBerryCameraPreview
                    scanberryCameraPreview.setUp(SupportedPictureSize(pSize.width, pSize.height), display)
                }
            }
        }
    }

    private val previewCallback = Camera.PreviewCallback { data, _ ->
        if (isCameraFocused) {
            // Pass data byte array to io.scanberry.sdk.api.view.ScanBerryCameraPreview for detecting document an drawing rectangle on top of it and showing hint
            scanberryCameraPreview.detectDocument(data)
        }
    }

    private val cameraAutoFocusMoveCallback = Camera.AutoFocusMoveCallback { start, _ ->
        isCameraFocused = !start
    }

    private fun getPreviewResolution(camera: Camera): Camera.Size? {
        var maxWidth = 0
        var curRes: Camera.Size? = null
        camera.lock()
        for (r in camera.parameters.supportedPreviewSizes) {
            if (r.width > maxWidth) {
                maxWidth = r.width
                curRes = r
            }
        }
        return curRes
    }

    private fun getBackCamera(): Int {
        var cameraId = -1
        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                break
            }
            cameraId = i
        }
        return cameraId
    }
}