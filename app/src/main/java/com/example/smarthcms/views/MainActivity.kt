package com.example.smarthcms.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.databinding.ActivityMainBinding
import com.example.smarthcms.utils.Constants
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import com.example.smarthcms.views.admin.AdminHost
import com.example.smarthcms.views.nurse.NurseHost
import com.example.smarthcms.views.patient.PatientHost

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val mainRepository = MainRepository(this)
        val mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        setContentView(binding.root)
        Constants().load {
            runOnUiThread {
                mainViewModel.nationalId.observe(this) { nationalId ->
                    if (!nationalId.isNullOrEmpty()) {
                        mainViewModel.type.observe(this) { type ->
                            when (type) {
                                "patient" -> {
                                    startActivity(Intent(this, PatientHost::class.java))
                                    finishAffinity()
                                }
                                "admin" -> {
                                    startActivity(Intent(this, AdminHost::class.java))
                                    finishAffinity()
                                }
                                "nurse" -> {
                                    startActivity(Intent(this, NurseHost::class.java))
                                    finishAffinity()
                                }
                            }
                        }
                    } else {
                        startActivity(Intent(this, Intro::class.java))
                        finishAffinity()
                    }
                }
                mainViewModel.getId()
                mainViewModel.getType()
            }
        }

    }
}