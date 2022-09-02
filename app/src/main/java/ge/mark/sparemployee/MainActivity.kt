package ge.mark.sparemployee

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.MediaStore.MediaColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import ge.mark.sparemployee.databinding.ActivityMainBinding
import ge.mark.sparemployee.db.DbManager
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit


class MainActivity : AppCompatActivity() {

    private var dbManager: DbManager? = null

    val CAMERA_IMAGE_BUCKET_NAME = (Environment.getExternalStorageDirectory().toString()
            + "/DCIM/Camera")
    val CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME)

    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    fun getBucketId(path: String): String {
        return path.lowercase(Locale.getDefault()).hashCode().toString()
    }

    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbManager= DbManager(this)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listeners for number buttons
        viewBinding.numberBackSpace.setOnClickListener { deleteNumber() }
        viewBinding.number0.setOnClickListener { defNumber("0") }
        viewBinding.number1.setOnClickListener { defNumber("1") }
        viewBinding.number2.setOnClickListener { defNumber("2") }
        viewBinding.number3.setOnClickListener { defNumber("3") }
        viewBinding.number4.setOnClickListener { defNumber("4") }
        viewBinding.number5.setOnClickListener { defNumber("5") }
        viewBinding.number6.setOnClickListener { defNumber("6") }
        viewBinding.number7.setOnClickListener { defNumber("7") }
        viewBinding.number8.setOnClickListener { defNumber("8") }
        viewBinding.number9.setOnClickListener { defNumber("9") }
        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }
        viewBinding.fileButton.setOnClickListener {
            getOfflinePhotos()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @SuppressLint("SetTextI18n")
    private fun defNumber(str: String){
        val tmpTextViewNumber = viewBinding.textViewNumber
        if(tmpTextViewNumber.length() < tmpTextViewNumber.maxLines){
            viewBinding.textViewNumber.text = tmpTextViewNumber.text.toString() + str
        }
        if(tmpTextViewNumber.length() == tmpTextViewNumber.maxLines){
            checkEmployee(tmpTextViewNumber.text.toString())
        }
    }

    private fun checkEmployee(code: String) {
//
    }

    private fun deleteNumber(){
        val tmpTextViewNumber = viewBinding.textViewNumber.text
        if(tmpTextViewNumber.isNotEmpty()){
            val s = tmpTextViewNumber.subSequence(0, tmpTextViewNumber.length-1)
            viewBinding.textViewNumber.text = s
        }
    }

    private fun getOfflinePhotos() {
        val filePathImg = Environment.getExternalStoragePublicDirectory("Pictures/Spar").toString()
        val root = File(filePathImg)

        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.isNotEmpty()) {
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".jpg")) {
                    // File absolute path
                    Log.e("downloadFilePath", currentFile.absolutePath)
                    // File Name
                    Log.e("downloadFileName", currentFile.name)
                    fileList.add(currentFile.absoluteFile)
                }
            }
            Log.w("fileList", "" + fileList.size)
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = "spar - " + Math.random().toString() + " - " + SimpleDateFormat(
            FILENAME_FORMAT,
            Locale.US
        ).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaColumns.DISPLAY_NAME, name)
            put(MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(Images.Media.RELATIVE_PATH, "Pictures/Spar")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    dbManager?.insertToDb("01234",output.savedUri.toString() + "/" +name,"0")
                    viewBinding.textView.text = ""
                    for (worker in dbManager!!.fromDb) {
                        viewBinding.textView.append(worker.photoPath)
                        viewBinding.textView.append("\n")
                    }


                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        viewBinding.videoCaptureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.stop_capture)
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(
                                TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}"
                            )
                        }
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.start_capture)
                            isEnabled = true
                        }
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                )
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            imageCapture = ImageCapture.Builder().build()

            /*
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }
             */

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera

                // For image
                //  cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)

                // For Video
                //cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)

                // For image and video
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        dbManager?.closeDb()
    }

    override fun onResume() {
        super.onResume()
        dbManager?.openDb()
        viewBinding.textView.text = ""
        for (worker in dbManager!!.fromDb) {
            viewBinding.textView.append(worker.photoPath)
            viewBinding.textView.append("\n")
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}