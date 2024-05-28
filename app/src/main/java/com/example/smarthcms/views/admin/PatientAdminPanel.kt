package com.example.smarthcms.views.admin

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.R
import com.example.smarthcms.databinding.ActivityPatientAdminPanelBinding
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class PatientAdminPanel : AppCompatActivity() {
    private var _binding: ActivityPatientAdminPanelBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientAdminPanelBinding.inflate(layoutInflater)
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        setContentView(binding.root)
        binding.apply {
            if (intent != null) {
                val byteArray = intent?.getByteArrayExtra("image")
                val bitmap = byteArray?.size?.let {
                    BitmapFactory.decodeByteArray(byteArray, 0, it)
                }
                ivNurse.setImageBitmap(bitmap)
                tvNurseName.text = intent.getStringExtra("name")
                tvWorkId.text = getString(R.string.work_id_, intent.getStringExtra("workId"))
            }

            btnAddPatient.setOnClickListener {
                startActivity(
                    Intent(this@PatientAdminPanel, PatientList::class.java)
                        .putExtra("workId", intent.getStringExtra("workId")).putExtra("key", "add")
                )
            }

            btnDeletePatient.setOnClickListener {
                startActivity(
                    Intent(this@PatientAdminPanel, PatientList::class.java)
                        .putExtra("workId", intent.getStringExtra("workId"))
                        .putExtra("key", "delete")
                )
            }

            btnUpdate.setOnClickListener {
                val byteArray = intent?.getByteArrayExtra("image")
                startActivity(
                    Intent(
                        this@PatientAdminPanel,
                        UpdateData::class.java
                    ).putExtra("workId", intent.getStringExtra("workId"))
                        .putExtra("name", tvNurseName.text.toString())
                        .putExtra("key", "updateNurse")
                        .putExtra("image", byteArray)
                )
            }

            btnDeleteNurse.setOnClickListener {
                showAlert()
            }
        }

    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove Nurse")
        builder.setMessage("Are you sure?")
        builder.setPositiveButton("OK") { _, _ ->
            mainViewModel.removeNurse(intent.getStringExtra("workId")) {
                if (it == "done")
                    finish()
            }
        }

        builder.setNegativeButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}