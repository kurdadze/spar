package ge.mark.sparemployee

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ge.mark.sparemployee.adapters.WorkerAdapter
import ge.mark.sparemployee.databinding.ActivityPhotoBinding
import ge.mark.sparemployee.helpers.DbHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.services.SparJobService
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class PhotoActivity : AppCompatActivity() {
//    private var dbManager: DbManager? = null

    private val TAG = "JobService"

    private lateinit var viewBinding: ActivityPhotoBinding

    private lateinit var dbHelper: DbHelper

    private lateinit var recyclerView: RecyclerView
    private var adapter: WorkerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DbHelper(this)

        viewBinding = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.getAllWorker.setOnClickListener { getAllWorker() }
        viewBinding.insert.setOnClickListener {
//            val timeStamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())
//            Toast.makeText(this, timeStamp, Toast.LENGTH_SHORT).show()

            insertNewUser(User(first_name = "სოსო", last_name = "ქურდაძე", pass_code = "11111"))
            insertNewUser(User(first_name = "ცოტნე", last_name = "ქურდაძე", pass_code = "22222"))
            insertNewUser(User(first_name = "თემურ", last_name = "კევლიშვილი", pass_code = "33333"))
        }

        viewBinding.startJob.setOnClickListener { startJob() }
        viewBinding.stopJob.setOnClickListener { stopJob() }

        recyclerView = findViewById(R.id.recycleView)
        initRecyclerView()

        adapter?.setOnClickDeleteItem {
            dbHelper.deleteWorker(it.photo)
            getAllWorker()
        }
    }

    private fun insertNewUser(user: User) {
        dbHelper.insertUser(user)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WorkerAdapter()
        recyclerView.adapter = adapter
    }

    private fun getAllWorker() {
        val wrkList = dbHelper.getAllWorker()
        adapter?.addItems(wrkList)
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
                    Log.i("downloadFilePath", currentFile.absolutePath)
                    // File Name
                    Log.i("downloadFileName", currentFile.name)
                    fileList.add(currentFile.absoluteFile)
                }
            }
            Log.i("fileList", "" + fileList.size)
        }
    }

    private fun fileToBitmap(f: String): ByteArray {
        val filePath = File(f).path
        val bitmap = BitmapFactory.decodeFile(filePath)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)
        return stream.toByteArray()
    }

    private fun startJob() {

        val componentName = ComponentName(this, SparJobService::class.java)
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

    private fun stopJob() {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(123)
        Log.i(TAG, "Job cancelled")
    }
}