package com.example.smarthcms.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarthcms.R
import com.example.smarthcms.databinding.NursesItemBinding
import com.example.smarthcms.models.Nurse
import com.example.smarthcms.views.admin.PatientAdminPanel
import java.io.ByteArrayOutputStream

class NursesRecyclerAdapter(
    private var list: ArrayList<Nurse.NursesForAdminHub>,
    private val context: Context,
) : RecyclerView.Adapter<NursesRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(private val nursesItemBinding: NursesItemBinding) :
        RecyclerView.ViewHolder(nursesItemBinding.root) {
        fun bind(nursesForAdminHub: Nurse.NursesForAdminHub) {
            nursesItemBinding.tvPatientName.text = nursesForAdminHub.name
            nursesItemBinding.tvWorkId.text =
                context.getString(R.string.work_id_, nursesForAdminHub.workId)
            if (nursesForAdminHub.image!= ""){
                Glide.with(context)
                    .load(nursesForAdminHub.image)
                    .into(nursesItemBinding.ivNurse)
            }

            val bitmap = nursesItemBinding.ivNurse.drawable?.toBitmap()
            nursesItemBinding.nurseCard.setOnClickListener {
                context.startActivity(
                    Intent(
                        context,
                        PatientAdminPanel::class.java
                    ).putExtra("name", nursesForAdminHub.name)
                        .putExtra("workId", nursesForAdminHub.workId)
                        .putExtra("image", bitmap?.toByteArray())
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NursesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun filterList(filteredList: ArrayList<Nurse.NursesForAdminHub>) {
        list = filteredList
        notifyDataSetChanged()
    }

    private fun Drawable.toBitmap(): Bitmap? {
        if (this is BitmapDrawable) {
            if (bitmap != null) {
                return bitmap
            }
        }

        val bitmap: Bitmap? = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap!!)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)

        return bitmap
    }

    private fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }


}