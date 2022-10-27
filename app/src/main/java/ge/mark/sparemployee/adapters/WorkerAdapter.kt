package ge.mark.sparemployee.adapters

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ge.mark.sparemployee.R
import ge.mark.sparemployee.models.Worker


class WorkerAdapter : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    private var wrkList: ArrayList<Worker> = ArrayList()
    private var onClickDeleteItem: ((Worker) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: ArrayList<Worker>) {
        this.wrkList = items
        notifyDataSetChanged()
    }

    fun setOnClickDeleteItem(callback: (Worker) -> Unit) {
        this.onClickDeleteItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WorkerViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_items_work, parent, false)
    )

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val wrk = wrkList[position]
        holder.bindView(wrk)
        holder.delete.setOnClickListener { onClickDeleteItem?.invoke(wrk) }
    }

    override fun getItemCount(): Int {
        return wrkList.size
    }

    class WorkerViewHolder(private var view: View) : RecyclerView.ViewHolder(view) {

        private var controllerCode = view.findViewById<TextView>(R.id.tvControllerCode)
        private var pin = view.findViewById<TextView>(R.id.tvPin)
        private var dateTime = view.findViewById<TextView>(R.id.tvDateTime)
        private var picture = view.findViewById<TextView>(R.id.tvPicture)
        private var hash = view.findViewById<TextView>(R.id.tvHash)
        private var photoName = view.findViewById<TextView>(R.id.tvPhotoName)
        private var photoPreview = view.findViewById<ImageView>(R.id.photoPreview)
        var delete = view.findViewById<TextView>(R.id.btnDelete)!!

        fun bindView(wrk: Worker) {
            controllerCode.text = wrk.controller_code
            pin.text = wrk.pin
            dateTime.text = wrk.datetime
            picture.text = "wrk.picture"
            hash.text = wrk.hash
            photoName.text = wrk.photo_name
            photoPreview.setImageBitmap(fileToBitmap(wrk.photo_name))
        }

        private fun fileToBitmap(f: String): Bitmap? {
            val photoPath: String =
                Environment.getExternalStorageDirectory().toString() + "/Pictures/Spar/" + f
            val options = BitmapFactory.Options()
            options.inSampleSize = 8
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            return BitmapFactory.decodeFile(photoPath, options)
        }
    }


}