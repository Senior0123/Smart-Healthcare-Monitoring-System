package com.example.smarthcms.views.patient

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.databinding.ActivityPatientSignsBinding
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class PatientSigns : AppCompatActivity() {
    private var _binding: ActivityPatientSignsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientSignsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        mainViewModel.getHeartRate()
        mainViewModel.heartRateResult.observe(this){ heartRate ->
            if (heartRate != null) {
                binding.tvHeartbeat.text = heartRate
            }
        }
        mainViewModel.getOxygenRate()
        mainViewModel.oxygenRateResult.observe(this){ breathRate ->
            if (breathRate != null) {
                binding.tvBreathRate.text = breathRate
            }
        }
        mainViewModel.getTemperature()
        mainViewModel.temperatureResult.observe(this){ temperature ->
            if (temperature != null) {
                binding.tvTemperature.text = temperature
            }
        }
        binding.btnPush.setOnClickListener {
            showAlert()
        }
        mainViewModel.type.observe(this) {
            if (it == "patient")
                binding.btnPush.visibility = View.VISIBLE
        }
        mainViewModel.getId()
        mainViewModel.getType()
    }

    private fun setNotification(
        heartRate: Int,
        breathRate: Int,
        temperature: Int,
        id: String
    ) {
        if (checkHeartRate(heartRate))
            mainViewModel.pushNotification(
                "user with national Id: $id has bad heart rate record: $heartRate", id
            )
        else if (checkBreathRate(breathRate))
            mainViewModel.pushNotification(
                "user with national Id: $id may have  hypoxemia (low blood oxygen) and hypoxia (low oxygen in tissues)",
                id
            )
        else if (checkTemperature(temperature))
            mainViewModel.pushNotification(
                "user with national Id: $id temperature above 104°F (40°C), can be serious and require medical attention.",
                id
            )
        else
            Toast.makeText(
                this,
                "Its may be a normal values, try again if there are dangers",
                Toast.LENGTH_SHORT
            ).show()
    }

    private fun checkHeartRate(heartRate: Int): Boolean {
        return heartRate > 100 || heartRate < 60
    }

    private fun checkBreathRate(breathRate: Int): Boolean {
        return (breathRate < 90)
    }

    private fun checkTemperature(temperature: Int): Boolean {
        return (temperature >= 32)
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Send Alert")
        builder.setMessage("Are you sure?")
        builder.setPositiveButton("OK") { _, _ ->
            binding.apply {
                if (!tvHeartbeat.text.isNullOrEmpty() && !tvTemperature.text.isNullOrEmpty() && !tvBreathRate.text.isNullOrEmpty()) {
                    mainViewModel.nationalId.observe(this@PatientSigns) { id ->
                        setNotification(
                            tvHeartbeat.text.toString().toInt(),
                            tvBreathRate.text.toString().toInt(),
                            tvTemperature.text.toString().toInt(),
                            id
                        )
                    }
                }
            }

        }

        builder.setNegativeButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}