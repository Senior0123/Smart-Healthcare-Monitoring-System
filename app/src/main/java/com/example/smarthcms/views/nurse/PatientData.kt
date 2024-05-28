package com.example.smarthcms.views.nurse

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.databinding.ActivityPatientDataBinding
import com.example.smarthcms.utils.NotificationReceiver
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import com.example.smarthcms.views.admin.UpdateData
import com.example.smarthcms.views.patient.PatientSigns

class PatientData : AppCompatActivity() {
    private var _binding: ActivityPatientDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        if (intent != null) {
            val nationalId = intent.getStringExtra("nationalId")
            if (nationalId != null) {
                mainViewModel.getPatientDataForNurse(nationalId)

                mainViewModel.patientDataForNurseResult.observe(this){ data ->
                    binding.apply {
                        tvUsername.text = data?.name
                        tvHealthId.text = data?.healthId
                        tvBloodType.text = data?.bloodType
                        tvAge.text = data?.age
                        tvHeight.text = data?.height
                        tvWeight.text = data?.weight
                    }
                }
            }
            mainViewModel.notificationPrompt()
            mainViewModel.notificationPromptResult.observe(this){ msg ->
                mainViewModel.getFlag()
                mainViewModel.flagResult.observe(this){ flag ->
                    if (flag == "1") {
                        if (msg != null && msg.contains(nationalId.toString())) {
                            if (areNotificationsEnabled()) {
                                scheduleNotification(msg)
                            } else {
                                showNotificationSettingsDialog()
                            }
                        }
                    }
                }
            }
            binding.btnSigns.setOnClickListener {
                startActivity(
                    Intent(this, PatientSigns::class.java).putExtra(
                        "nationalId",
                        nationalId
                    )
                )
            }
            binding.btnUpdate.setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        UpdateData::class.java
                    ).putExtra("nationalId", nationalId)
                        .putExtra("name", binding.tvUsername.text.toString())
                        .putExtra("key", "updatePatient")
                        .putExtra("image", intent.getByteArrayExtra("image") ?: intArrayOf())
                        .putExtra("room", intent.getStringExtra("room") ?: "")
                )
            }
        }
    }

    private fun scheduleNotification(msg: String) {
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
        notificationIntent.putExtra("msg", msg)
        sendBroadcast(notificationIntent)
        mainViewModel.changeFlag()
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            showManualNotificationEnableDialog()
            false
        }
    }

    private fun showManualNotificationEnableDialog() {
        AlertDialog.Builder(this)
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
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }


    private fun showNotificationSettingsDialog() {
        val dialogIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        }
        startActivity(dialogIntent)
    }
}