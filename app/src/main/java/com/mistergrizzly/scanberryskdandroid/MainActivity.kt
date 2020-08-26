package com.mistergrizzly.scanberryskdandroid

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mistergrizzly.scanberryskdandroid.detection.DocumentDetectionActivity
import com.mistergrizzly.scanberryskdandroid.edit.EditDocumentActivity
import io.scanberry.sdk.api.Scanberry
import io.scanberry.sdk.api.ScanberryDocumentDetector
import io.scanberry.sdk.api.ScanberryUtils
import kotlinx.android.synthetic.main.activity_main.startDocCropFlow
import kotlinx.android.synthetic.main.activity_main.startDocDetection
import kotlinx.android.synthetic.main.activity_main.startDocDetectionFlow
import kotlinx.android.synthetic.main.activity_main.startDocEdit
import kotlinx.android.synthetic.main.activity_main.startFullFlow

class MainActivity : AppCompatActivity() {

    private val REQUEST_FULL_PATH_CODE = 33
    private val REQUEST_DOC_DETECTION_CODE = 34
    private val REQUEST_CROPPED_DOC_CODE = 35

    private lateinit var scanBerryUtils: ScanberryUtils
    private lateinit var scanBerryDetector: ScanberryDocumentDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanBerry = (application as ScanBerryApplication).scanBerry
        scanBerryUtils = (application as ScanBerryApplication).getScanberryUtils()
        scanBerryDetector = (application as ScanBerryApplication).getScanberryDocumentDetector()

        startFullFlow.setOnClickListener {
            scanBerry.startReadyToUseUi(this, REQUEST_FULL_PATH_CODE)
        }

        startDocDetectionFlow.setOnClickListener {
            scanBerry.startReadyToUseUi(this, REQUEST_DOC_DETECTION_CODE, Scanberry.ScanBerryFlow.DOC_DETECTION_ONLY)
        }

        startDocCropFlow.setOnClickListener {
            scanBerry.startReadyToUseUi(this, REQUEST_CROPPED_DOC_CODE, Scanberry.ScanBerryFlow.CROP_SCANNED_DOCUMENT)
        }

        startDocDetection.setOnClickListener {
            startActivity(Intent(this, DocumentDetectionActivity::class.java))
        }

        startDocEdit.setOnClickListener {
            startActivity(Intent(this, EditDocumentActivity::class.java))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_FULL_PATH_CODE -> handleResponseFromFullPath(data)
                REQUEST_CROPPED_DOC_CODE -> handleResponseFromCroppedDocPath(data)
                REQUEST_DOC_DETECTION_CODE -> handleResponseFromDocDetectionOnlyPath(data)
            }
        }
    }

    private fun handleResponseFromFullPath(data: Intent?) {
        data?.let {
            val fileNameCapturedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CAPTURED_PICTURE)
            val fileNameCroppedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CROPPED_PICTURE)
            val fileNameEnhancedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_ENHANCED_PICTURE)

            val corners: List<PointF>? = it.getParcelableArrayListExtra(Scanberry.EXTRA_DOC_DETECTION_RECTANGLE_CORNERS)
            val ocr: String? = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_ENHANCED_PICTURE)

            val capturedBitmap: Bitmap? = scanBerryUtils.getBitmap(this, fileNameCapturedImg)
            val croppedBitmap: Bitmap? = scanBerryUtils.getBitmap(this, fileNameCroppedImg)
            val enhancedBitmap: Bitmap? = scanBerryUtils.getBitmap(this, fileNameEnhancedImg)

            /**
             * Do something with obtained data from the full flow
             * @capturedBitmap - original captured image
             * @croppedBitmap - cropped image based on detected document corners
             * @enhancedBitmap - enhanced cropped image that is prepared for the ocr step
             * @corners - list of PointF which are the corners of the detected document
             * @ocr - detected text from the document image
             */
        }
    }

    private fun handleResponseFromCroppedDocPath(data: Intent?) {
        data?.let {
            val fileNameCapturedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CAPTURED_PICTURE)
            val fileNameCroppedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CROPPED_PICTURE)

            val corners: List<PointF>? = it.getParcelableArrayListExtra(Scanberry.EXTRA_DOC_DETECTION_RECTANGLE_CORNERS)

            val capturedBitmap: Bitmap? = scanBerryUtils.getBitmap(this, fileNameCapturedImg)
            val croppedBitmap: Bitmap? = scanBerryUtils.getBitmap(this, fileNameCroppedImg)

            /**
             * Do something with obtained data from the full flow
             * @capturedBitmap - original captured image
             * @croppedBitmap - cropped image based on detected document corners
             * @corners - list of PointF which are the corners of the detected document
             */
        }
    }

    private fun handleResponseFromDocDetectionOnlyPath(data: Intent?) {
        data?.let {
            val fileNameCapturedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CAPTURED_PICTURE)

            val corners: List<PointF>? = it.getParcelableArrayListExtra(Scanberry.EXTRA_DOC_DETECTION_RECTANGLE_CORNERS)

            val capturedBitmap: Bitmap? = scanBerryUtils.getBitmap(this, fileNameCapturedImg)

            /**
             * Do something with obtained data from the full flow
             * @capturedBitmap - original captured image
             * @corners - list of PointF which are the corners of the detected document
             */
        }
    }
}