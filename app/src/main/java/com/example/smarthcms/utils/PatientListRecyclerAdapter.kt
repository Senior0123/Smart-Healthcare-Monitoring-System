package com.example.smarthcms.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarthcms.databinding.PatientsItemsBinding
import com.example.smarthcms.models.Patient
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.views.admin.UpdateData
import java.io.ByteArrayOutputStream

class PatientListRecyclerAdapter(
    private val context: Context,
    private val mainViewModel: MainViewModel,
    private var list: ArrayList<Patient.PatientForList>,
    private val key: String,
    private val workId: String
) :
    RecyclerView.Adapter<PatientListRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(private val patientsItemsBinding: PatientsItemsBinding) :
        RecyclerView.ViewHolder(patientsItemsBinding.root) {
        fun bind(patientForList: Patient.PatientForList) {
            patientsItemsBinding.tvPatientName.text = patientForList.name
            patientsItemsBinding.tvRoom.text = patientForList.room
            if (patientForList.image!= ""){
                Glide.with(context)
                    .load(patientForList.image)
                    .into(patientsItemsBinding.ivPatient)
            }

            patientsItemsBinding.root.setOnClickListener {
                when (key) {
                    "delete" -> {
                        mainViewModel.deletePatientFromList(workId, patientForList.nationalId) {
                            if (it == "done")
                                notifyItemRemoved(adapterPosition)
                        }
                    }

                    "add" -> {
                        mainViewModel.addPatientToNurse(workId, patientForList) {
                            if (it == "done")
                                Toast.makeText(context, "added!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    "updatePatient" -> {
                        val bitmap = patientsItemsBinding.ivPatient.drawable?.toBitmap()
                        context.startActivity(
                            Intent(
                                context,
                                UpdateData::class.java
                            ).putExtra("nationalId", patientForList.nationalId)
                                .putExtra("room", patientForList.room)
                                .putExtra("name", patientForList.name)
                                .putExtra("key", "updatePatient")
                                .putExtra("image",bitmap?.toByteArray())

                        )
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PatientsItemsBinding.inflate(
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

    fun filterList(filteredList: ArrayList<Patient.PatientForList>) {
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