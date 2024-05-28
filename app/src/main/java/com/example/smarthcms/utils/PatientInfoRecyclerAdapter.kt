package com.example.smarthcms.utils

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarthcms.databinding.PatientsItemsBinding
import com.example.smarthcms.models.Patient
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.views.nurse.PatientData
import java.io.ByteArrayOutputStream

class PatientInfoRecyclerAdapter(
    private var list: ArrayList<Patient.PatientForList>,
    private val context: Context,
    private val mainViewModel: MainViewModel,
    private val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<PatientInfoRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(private val patientsItemsBinding: PatientsItemsBinding) :
        RecyclerView.ViewHolder(patientsItemsBinding.root) {
        fun bind(patientForList: Patient.PatientForList) {
            patientsItemsBinding.tvPatientName.text = patientForList.name
            patientsItemsBinding.tvRoom.text = patientForList.room
            if (patientForList.image != "") {
                Glide.with(context)
                    .load(patientForList.image)
                    .into(patientsItemsBinding.ivPatient)
            }
            mainViewModel.notificationPrompt()
            mainViewModel.notificationPromptResult.observe(lifecycleOwner){ msg ->
                mainViewModel.getFlag()
                mainViewModel.flagResult.observe(lifecycleOwner){ flag ->
                    if (flag == "1") {
                        if (msg != null && msg.contains(patientForList.nationalId)) {
                            if (areNotificationsEnabled()) {
                                scheduleNotification(msg)
                            } else {
                                showNotificationSettingsDialog()
                            }
                        }
                    }
                }
            }

            patientsItemsBinding.root.setOnClickListener {
                val bitmap = patientsItemsBinding.ivPatient.drawable?.toBitmap()
                context.startActivity(
                    Intent(
                        context,
                        PatientData::class.java
                    ).putExtra("nationalId", patientForList.nationalId)
                        .putExtra("room", patientForList.room)
                        .putExtra("name", patientForList.name)
                        .putExtra("key", "updatePatient")
                        .putExtra("image", bitmap?.toByteArray())
                )
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

    private fun scheduleNotification(msg: String) {
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        notificationIntent.putExtra("msg", msg)
        context.sendBroadcast(notificationIntent)
        mainViewModel.changeFlag()
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            showManualNotificationEnableDialog()
            false
        }
    }

    private fun showManualNotificationEnableDialog() {
        AlertDialog.Builder(context)
            .setTitle("Notification Permissions Required")
            .setMessage("Please enable notification permissions for this app.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }


    private fun showNotificationSettingsDialog() {
        val dialogIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
        }
        context.startActivity(dialogIntent)
    }
}