package com.mistergrizzly.scanberryskdandroid.edit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mistergrizzly.scanberryskdandroid.R
import com.mistergrizzly.scanberryskdandroid.ScanBerryApplication
import io.scanberry.sdk.api.ScanberryDocumentDetector
import io.scanberry.sdk.model.SupportedPictureSize
import java.io.InputStream
import kotlinx.android.synthetic.main.activity_edit_document.scanberryEditDocView

class EditDocumentActivity : AppCompatActivity() {
    private val REQUEST_PICK_PHOTO = 1

    private lateinit var detector: ScanberryDocumentDetector

    companion object{
        const val PIC_HEIGHT = 1920
        const val PIC_WIDTH = 1080
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_document)

        detector = (application as ScanBerryApplication).getScanberryDocumentDetector()

        scanberryEditDocView.setUp(SupportedPictureSize(PIC_HEIGHT, PIC_WIDTH), windowManager.defaultDisplay)

        pickImage()
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

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_PHOTO) {
            data?.data?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val scaled = Bitmap.createScaledBitmap(bitmap, PIC_WIDTH, PIC_HEIGHT, false)

                // Use ScanBerry sdk to detect document corners
                val cornerPoints = detector.detect(scaled)
                // Set scaled image and document corners to custom view io.scanberry.sdk.api.view.ScanBerryEditDocView
                scanberryEditDocView.setImageWithDocRectangle(scaled, cornerPoints)
            }
        }
    }
}