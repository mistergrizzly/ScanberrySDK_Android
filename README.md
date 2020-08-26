# Scanberry SDK documentation

## Getting started

Add maven repo url inside the allprojects repositories to the project `build.gradle` file:
```groovy
allprojects {
    repositories {
        maven {
            credentials {
                username "trial"
                password "trial_usage"
            }
            url "https://waspbean.jfrog.io/artifactory/lib-scanberry-local"
        }
        ...
    }
}
```

Then add the following dependency to the `build.gradle` app module file:
```groovy
implementation 'io.scanberry.sdk:core:1.0.0'
```

### Initialize SDK

```kotlin
val license_key = "YOUR_LICENSE_KEY"
val scanberry = Scanberry.Builder(this)
.license(license_key)
.build()
```

It is recommended to initialize the sdk inside the Application class. This way it will be easier to access Scanberry sdk components. Following snippet shows how it can be done:
```kotlin
class ScanberryApplication : Application() {

    lateinit var scanberry: Scanberry

    override fun onCreate() {
        super.onCreate()
        scanberry = Scanberry.Builder(this).build()
    }

    fun getScanberryDocumentDetector(): ScanberryDocumentDetector {
        return scanberry.getDocumentDetector()
    }

    fun getScanberryUtils(): ScanberryUtils {
        return scanberry.getUtils()
    }
}
```

Then in your activity/fragment get ScanberryDocumentDetector or ScanberryUtils using:
```kotlin
val scanberry = (application as ScanberryApplication).scanberry
val scanberryUtils = (application as ScanberryApplication).getScanberryUtils()
val scanberryDetector = (application as ScanberryApplication).getScanberryDocumentDetector()
```

Don't forget to add application name inside `AndroidManifest.xml` file :
```xml
<application
    android:name=".ScanberryApplication"
    ...
```

## Ready to use UI
Scanberry SDK provides ready to use UI for document detection with user guidance, cropping, image enhancing, optical character recognition, PDF creation. 
To start the document detection flow, use below code:
```kotlin
val REQUEST_CODE_DOC_DETECTION = 123
scanberry.startReadyToUseUi(activity, REQUEST_CODE_DOC_DETECTION)
```
To receive result from the ready to use ui `override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)` from the calling activity.
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_DOC_DETECTION) {
            data?.let {
                val fileNameOriginalImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CAPTURED_PICTURE)
                val corners = it.getParcelableArrayListExtra<PointF>(Scanberry.EXTRA_DOC_DETECTION_RECTANGLE_CORNERS)
                val fileNameCroppedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CROPPED_PICTURE)
                val fileNameEnhancedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_ENHANCED_PICTURE)
                val ocr = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_ENHANCED_PICTURE)

                val capturedBitmap: Bitmap? = scanberry.getScanberryUtils().getBitmap(this, fileNameCapturedImg)
                val croppedBitmap: Bitmap? = scanberry.getScanberryUtils().getBitmap(this, fileNameCroppedImg)
                val enhancedBitmap: Bitmap? = scanberry.getScanberryUtils().getBitmap(this, fileNameEnhancedImg)
                //do something with obtained result
            }
        }
    }
```

If you need only document detection screen, then use :
```kotlin
val REQUEST_CODE_DOC_DETECTION = 123
scanberry.startReadyToUseUi(activity, REQUEST_CODE_DOC_DETECTION, Scanberry.ScanberryFlow.DOC_DETECTION_ONLY)
```
After image capturing, the result will be received in `override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)` from the calling activity.
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_DOC_DETECTION) {
            data?.let {
                val fileNameOriginalImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CAPTURED_PICTURE)
                val corners = it.getParcelableArrayListExtra<PointF>(Scanberry.EXTRA_DOC_DETECTION_RECTANGLE_CORNERS)
                
                val capturedBitmap: Bitmap? = scanberry.getScanberryUtils().getBitmap(this, fileNameCapturedImg)
                
                //do something with obtained result
            }
        }
    }
```

If opting for document detection screen and image cropping screen, use below code:
```kotlin
val REQUEST_CODE_DOC_DETECTION = 123
scanberry.startReadyToUseUi(activity, REQUEST_CODE_DOC_DETECTION, Scanberry.ScanberryFlow.CROP_SCANNED_DOCUMENT)
```
After image capturing, the result will be received in `override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)` from the calling activity.
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_DOC_DETECTION) {
            data?.let {
                val fileNameOriginalImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CAPTURED_PICTURE)
                val fileNameCroppedImg = it.getStringExtra(Scanberry.EXTRA_DOC_DETECTION_CROPPED_PICTURE)
                val corners = it.getParcelableArrayListExtra<PointF>(Scanberry.EXTRA_DOC_DETECTION_RECTANGLE_CORNERS)
                
                val capturedBitmap: Bitmap? = scanberry.getScanberryUtils().getBitmap(this, fileNameCapturedImg)
                val croppedBitmap: Bitmap? = scanberry.getScanberryUtils().getBitmap(this, fileNameCroppedImg)
                //do something with obtained result
            }
        }
    }
```
## Scanberry views

Alternative to using ready to use ui, there are custom views that can be used instead.
`ScanberryCameraPreview` is suitable for the document detection screen when camera is on and byte array is received for each frame. It is used to see detected corners of the document on the image in real time:

Insert below view inside your screen layout.
```xml
    <io.scanberry.sdk.api.view.ScanberryCameraPreview
        android:id="@+id/scanberryCameraPreview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

Then you need to initialize it inside your fragment or activity 
```kotlin
val display = windowManager.defaultDisplay
val supportedPictureSize = SupportedPictureSize(picture_height, picture_width)
scanberryCameraPreview.setUp(supportedPictureSize, display)
```

Using android Camera API, inside the preview callback data byteArray can be passed to the `detectDocument(data)` method of the `ScanberryCameraPreview`
```kotlin
private val previewCallback = Camera.PreviewCallback { data, camera ->
    // Pass data byte array to io.scanberry.sdk.api.view.ScanberryCameraPreview for detecting document an drawing rectangle on top of it and showing hint 
    scanberryCameraPreview.detectDocument(data)
}
```

For capturing the image use:
```kotlin
private val pictureCallback = Camera.PictureCallback { _, camera ->
    // Use this method just to show capturing hint. Other hints are shown automatically using ScanberryCameraPreview.detectDocument(data) in Camera.PreviewCallback
    scanberryCameraPreview.showCapturingHint()
    
    // getCapturedBitmap() gives the captured image in Bitmap
    scanberryCameraPreview.getCapturedBitmap()?.let { capturedBitmap ->
         // Document corners can be obtained using detect method from ScanberryDocumentDetector   
        val points = scanberry.getScanberryDocumentDetector().detect(capturedBitmap)
        // Do something with obtained points and captuedBitmap
        }
    }
```

Another helper view is `ScanberryEditDocView`. It is used to show detected document corners on the image and edit the rectangle. 

Insert below view inside your screen layout.
```xml
    <io.scanberry.sdk.api.view.ScanberryEditDocView
        android:id="@+id/scanberryEditDocView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

Then you need to initialize it inside your fragment or activity 
```kotlin
val display = windowManager.defaultDisplay
val supportedPictureSize = SupportedPictureSize(picture_height, picture_width)
scanberryEditDocView.setUp(supportedPictureSize, display)
```

To be able to see document edge and corners on the image use below method byt passing image as bitmap and detected corner points. 
Note that image size should be the same as `SupportedPictureSize` passed to the ScanberryEditDocView during the `setUp()` step
```kotlin
scanberryEditDocView.setImageWithDocRectangle(scaled, cornerPoints)
```

## Document Detection and Image processing
Apart from ready to use ui, there is an API that can be used individually to detect document edges and getting document's corners, cropping a document based on the detected corners, enhancing a document that can be used for a better text recognition, and also getting text from the image.
Firstly we need to get an instance of `ScanberryDocumentDetector`, then use it as shown below:
```kotlin
val scanberryDetector: ScanberryDocumentDetector = scanberry.getDocumentDetector()

// Detect document corners
val corners: Map<Int, android.graphics.PointF> = scanberryDetector.detect(bitmap)

// Crop image 
val points: Map<Int, PointF> = scanberryEditDocView.getRectangleCorners()
val croppedBitmap: android.graphics.Bitmap = scanberryDetector.crop(bitmap, points)

// Enhance image
val enhancedBitmap: android.graphics.Bitmap = scanberryDetector.enhance(bitmap)

// Get text from image
val textFromImage: String? = scanberryDetector.getOcr(context, enhancedBitmap, language = "eng")
```
