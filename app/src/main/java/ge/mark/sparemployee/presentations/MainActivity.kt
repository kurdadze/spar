package ge.mark.sparemployee.presentations

import android.Manifest
import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.wifi.WifiManager
import android.os.*
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.MediaStore.MediaColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import ge.mark.sparemployee.R
import ge.mark.sparemployee.databinding.ActivityMainBinding
import ge.mark.sparemployee.helpers.DbHelper
import ge.mark.sparemployee.helpers.Network
import ge.mark.sparemployee.helpers.SysHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.models.Worker
import ge.mark.sparemployee.network.calls.ApiCalls
import ge.mark.sparemployee.services.MyReceiver
import ge.mark.sparemployee.services.SparGetUserJobService
import ge.mark.sparemployee.services.SparPingJobService
import ge.mark.sparemployee.services.SparSendOfflineDataJobService
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


//typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private lateinit var sysHelper: SysHelper
    private lateinit var myReceiver: BroadcastReceiver
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService


//    val CAMERA_IMAGE_BUCKET_NAME = (Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera")
//    val CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME)

//    fun getBucketId(path: String): String {
//        return path.lowercase(Locale.getDefault()).hashCode().toString()
//    }

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myReceiver = MyReceiver()
        IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION).also {
            registerReceiver(
                myReceiver,
                it
            )
        }

        sysHelper = SysHelper(this)
        dbHelper = DbHelper(this)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

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

        viewBinding.photoActivity.setOnClickListener {
            Toast.makeText(this, sysHelper.getDeviceID(), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PhotoActivity::class.java)
            startActivity(intent)
        }
        viewBinding.numberBackSpace.setOnClickListener { deleteNumber() }
        viewBinding.clearAll.setOnClickListener { clearAll() }
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }
        viewBinding.fileButton.setOnClickListener {
            getOfflinePhotos()
        }

        viewBinding.textViewNumber.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 5) {
                    viewBinding.imageCaptureButton.visibility = VISIBLE;
                    checkEmployee(s.toString())
                } else {
                    viewBinding.imageCaptureButton.visibility = GONE;
                    viewBinding.toastTextView.setBackgroundColor(0x00000000)
                }
            }
        })

        cameraExecutor = Executors.newSingleThreadExecutor()

        Handler(Looper.getMainLooper()).postDelayed({
            startPingJob()
            startGetUserJob()
            startGetOfflineJob()
        }, 20000)
    }

    private fun showHideButtons(state: String) {
        if (state == "show") {
            viewBinding.number0.visibility = VISIBLE
            viewBinding.number1.visibility = VISIBLE
            viewBinding.number2.visibility = VISIBLE
            viewBinding.number3.visibility = VISIBLE
            viewBinding.number4.visibility = VISIBLE
            viewBinding.number5.visibility = VISIBLE
            viewBinding.number6.visibility = VISIBLE
            viewBinding.number7.visibility = VISIBLE
            viewBinding.number8.visibility = VISIBLE
            viewBinding.number9.visibility = VISIBLE
            viewBinding.clearAll.visibility = VISIBLE
            viewBinding.numberBackSpace.visibility = VISIBLE
        } else {
            viewBinding.number0.visibility = GONE
            viewBinding.number1.visibility = GONE
            viewBinding.number2.visibility = GONE
            viewBinding.number3.visibility = GONE
            viewBinding.number4.visibility = GONE
            viewBinding.number5.visibility = GONE
            viewBinding.number6.visibility = GONE
            viewBinding.number7.visibility = GONE
            viewBinding.number8.visibility = GONE
            viewBinding.number9.visibility = GONE
            viewBinding.clearAll.visibility = GONE
            viewBinding.numberBackSpace.visibility = GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun defNumber(str: String) {
        val tmpTextViewNumber = viewBinding.textViewNumber
        if (tmpTextViewNumber.length() < tmpTextViewNumber.maxLines) {
            viewBinding.textViewNumber.text = tmpTextViewNumber.text.toString() + str
        }
        if (tmpTextViewNumber.length() == tmpTextViewNumber.maxLines) {
            checkEmployee(tmpTextViewNumber.text.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkEmployee(code: String) {
        showHideButtons("hide")
        viewBinding.toastTextView.text = ""
        val userCode: User = dbHelper.checkUsers(code)
        if (userCode.pass_code == viewBinding.textViewNumber.text.toString()) {
            viewBinding.toastTextView.setBackgroundColor(getColor(R.color.white))
            viewBinding.toastTextView.setTextColor(getColor(R.color.green))
            viewBinding.toastTextView.text =
                "მოგესალმებით ${userCode.first_name} ${userCode.last_name}/n გთხოვთ გადაიღოთ ფოტო"
            val timer = 11

            object : CountDownTimer(11000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    viewBinding.imageCaptureButton.text = (millisUntilFinished / 1000).toString()
                }

                override fun onFinish() {
                    viewBinding.imageCaptureButton.text = ""
                }
            }.start()

            Handler(Looper.getMainLooper()).postDelayed({
                viewBinding.toastTextView.setBackgroundColor(0x00000000)
                viewBinding.toastTextView.text = ""
                viewBinding.textViewNumber.text = ""
                showHideButtons("show")
            }, (timer * 1000).toLong())
        } else {
            viewBinding.toastTextView.setBackgroundColor(getColor(R.color.red))
            viewBinding.toastTextView.setTextColor(getColor(R.color.white))
            viewBinding.toastTextView.text = "კოდი არასწორია"
            Handler(Looper.getMainLooper()).postDelayed({
                viewBinding.toastTextView.setBackgroundColor(0x00000000)
                viewBinding.toastTextView.text = ""
                viewBinding.textViewNumber.text = ""
                showHideButtons("show")
            }, 2000)
        }
    }

    private fun clearAll() {
        viewBinding.textViewNumber.text = ""
        viewBinding.toastTextView.text = ""
    }

    private fun deleteNumber() {
        val tmpTextViewNumber = viewBinding.textViewNumber.text
        viewBinding.toastTextView.text = ""
        if (tmpTextViewNumber.isNotEmpty()) {
            val s = tmpTextViewNumber.subSequence(0, tmpTextViewNumber.length - 1)
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
        val pinCode = viewBinding.textViewNumber.text.toString()

        val pictureName = "spar - $pinCode - ${sysHelper.getDateTimeNow("yyyy-MM-dd HH_mm_ss")}"

        val contentValues = ContentValues().apply {
            put(MediaColumns.DISPLAY_NAME, pictureName)
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

                    val filePathImg =
                        Environment.getExternalStoragePublicDirectory("Pictures/Spar").toString()
                    val ba = sysHelper.fileToBase64("$filePathImg/$pictureName.jpg")

                    val pin = viewBinding.textViewNumber.text.toString()

                    val hash =
                        sysHelper.stringToMD5("${sysHelper.getDeviceID()}|$pin|${sysHelper.getDateTimeNow()}|$ba")
                    val worker = Worker(
                        controller_code = sysHelper.getDeviceID(),
                        pin = pin,
                        datetime = sysHelper.getDateTimeNow(),
                        picture = ba!!,
                        hash = hash,
                        photo_name = "$pictureName.jpg"
                    )
                    val insertStatus = dbHelper.insertWorker(worker)
                    if (insertStatus) {
                        if (Network.isNetworkAvailable(context = applicationContext)) {
                            ApiCalls.sendDataToServer(
                                context = applicationContext,
                                controller_code = worker.controller_code,
                                pin = worker.pin,
                                datetime = worker.datetime,
                                picture = worker.picture,
                                hash = worker.hash,
                                photo_name = worker.photo_name
                            )
                        }
                        showHideButtons("hide")
                        viewBinding.textViewNumber.text = ""
                        viewBinding.toastTextView.setBackgroundColor(getColor(R.color.green))
                        viewBinding.toastTextView.setTextColor(getColor(R.color.white))
                        viewBinding.toastTextView.text = "თქვენ წარმატებით დაფიქსირდით სისტემაში"
                        Handler(Looper.getMainLooper()).postDelayed({
                            viewBinding.toastTextView.text = ""
                            viewBinding.toastTextView.setBackgroundColor(0x00000000)
                            showHideButtons("show")
                        }, 2000)
                    }
                }
            }
        )
    }

//    private fun fileToBitmap(f: String): ByteArray {
//        val filePath = File(f).path
//        val bitmap = BitmapFactory.decodeFile(filePath)
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)
//        return stream.toByteArray()
//    }

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
            put(MediaColumns.DISPLAY_NAME, name)
            put(MediaColumns.MIME_TYPE, "video/mp4")
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

    override fun onStart() {
        super.onStart()
        Log.d("qq", "Start")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("qq", "Restart")
        startPingJob()
        startGetUserJob()
        startGetOfflineJob()
    }

    override fun onResume() {
        super.onResume()
        Log.d("qq", "Resume")
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        dbHelper.close()
        stopJob(123)
        stopJob(321)
        stopJob(111)
    }

//    override fun onStop() {
//        super.onStop()
//    }

    override fun onPause() {
        super.onPause()
        stopJob(123)
        stopJob(321)
        stopJob(111)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
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

//    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {
//
//        private fun ByteBuffer.toByteArray(): ByteArray {
//            rewind()    // Rewind the buffer to zero
//            val data = ByteArray(remaining())
//            get(data)   // Copy the buffer into a byte array
//            return data // Return the byte array
//        }
//
//        override fun analyze(image: ImageProxy) {
//
//            val buffer = image.planes[0].buffer
//            val data = buffer.toByteArray()
//            val pixels = data.map { it.toInt() and 0xFF }
//            val luma = pixels.average()
//
//            listener(luma)
//
//            image.close()
//        }
//    }

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

    private fun startGetOfflineJob() {
        val componentName = ComponentName(this, SparSendOfflineDataJobService::class.java)
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            JobInfo.Builder(111, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic((15 * 60 * 1000).toLong())
                .setRequiresCharging(false)
                .setPersisted(true)
                .build()
        } else {
            TODO("VERSION.SDK_INT < P")
        }

        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Job scheduled")
        } else {
            Log.i(TAG, "Job scheduling failed")
        }
    }

    private fun startPingJob() {

        val componentName = ComponentName(this, SparPingJobService::class.java)
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic((15 * 60 * 1000).toLong())
                .setRequiresCharging(false)
                .setPersisted(true)
                .build()
        } else {
            TODO("VERSION.SDK_INT < P")
        }

        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Job scheduled")
        } else {
            Log.i(TAG, "Job scheduling failed")
        }
    }

    private fun startGetUserJob() {

        val componentName = ComponentName(this, SparGetUserJobService::class.java)
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            JobInfo.Builder(321, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic((15 * 60 * 1000).toLong())
                .setRequiresCharging(false)
                .setPersisted(true)
                .build()
        } else {
            TODO("VERSION.SDK_INT < P")
        }

        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.i(TAG, "Job scheduled")
        } else {
            Log.i(TAG, "Job scheduling failed")
        }
    }

//    private fun stopGetOfflineJob() {
//        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
//        scheduler.cancel(111)
//        Log.i(TAG, "Job cancelled")
//    }
//
//    private fun stopPingJob() {
//        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
//        scheduler.cancel(123)
//        Log.i(TAG, "Job cancelled")
//    }
//
//    private fun stopGetUserJob() {
//        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
//        scheduler.cancel(321)
//        Log.i(TAG, "Job cancelled")
//    }

    private fun stopJob(jobId: Int) {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(jobId)
        Log.i(TAG, "Job cancelled")
    }
//
//    private fun fileToBase64(filePath: String?): String? {
//        var base64File: String? = ""
//        val file = File(filePath)
//        try {
//            FileInputStream(file).use { imageInFile ->
//                // Reading a file from file system
//                val fileData = ByteArray(file.length().toInt())
//                imageInFile.read(fileData)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    base64File = Base64.getEncoder().encodeToString(fileData)
//                }
//            }
//        } catch (e: FileNotFoundException) {
//            println("File not found$e")
//        } catch (ioe: IOException) {
//            println("Exception while reading the file $ioe")
//        }
//        return base64File
//    }
//
//    private fun stringToMD5(str: String): String {
//        val md = MessageDigest.getInstance("MD5")
//        return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
//    }

}