package com.example.smarthcms.views

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.databinding.ActivityIntroBinding
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import com.example.smarthcms.views.admin.AdminHost
import com.example.smarthcms.views.nurse.NurseHost
import com.example.smarthcms.views.patient.PatientHost

class Intro : AppCompatActivity() {
    private var _binding: ActivityIntroBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mainRepository = MainRepository(this)
        val mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        binding.apply {
            btnGetStarted.setOnClickListener {
                val userType =
                    findViewById<RadioButton>(userTypeGroup.checkedRadioButtonId).text.toString()
                mainViewModel.saveType(userType)
                startActivity(Intent(this@Intro, Login::class.java))
            }
        }
        mainViewModel.nationalId.observe(this){
            if (!it.isNullOrEmpty()){
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
            }
        }
        mainViewModel.getId()
        mainViewModel.getType()
    }
}