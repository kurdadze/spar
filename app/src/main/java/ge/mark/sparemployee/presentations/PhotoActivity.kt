package ge.mark.sparemployee.presentations

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ge.mark.sparemployee.R
import ge.mark.sparemployee.adapters.WorkerAdapter
import ge.mark.sparemployee.databinding.ActivityPhotoBinding
import ge.mark.sparemployee.helpers.DbHelper
import ge.mark.sparemployee.models.User
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
        viewBinding.closeIntent.setOnClickListener { finish() }
        viewBinding.getAllWorker.setOnClickListener { getAllWorker() }
        viewBinding.insert.setOnClickListener {
//            val timeStamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())
//            Toast.makeText(this, timeStamp, Toast.LENGTH_SHORT).show()

            insertNewUser(User(first_name = "სოსო", last_name = "ქურდაძე", pass_code = "11111"))
            insertNewUser(User(first_name = "ცოტნე", last_name = "ქურდაძე", pass_code = "22222"))
            insertNewUser(User(first_name = "თემურ", last_name = "კევლიშვილი", pass_code = "33333"))
        }

        viewBinding.startJob.setOnClickListener {  }
        viewBinding.stopJob.setOnClickListener {  }

        recyclerView = findViewById(R.id.recycleView)
        initRecyclerView()

        adapter?.setOnClickDeleteItem {
            dbHelper.deleteWorker(it.hash)
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

}