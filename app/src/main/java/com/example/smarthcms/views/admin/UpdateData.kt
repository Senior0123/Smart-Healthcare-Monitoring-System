package com.example.smarthcms.views.admin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.databinding.ActivityUpdateDataBinding
import com.example.smarthcms.models.Nurse
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class UpdateData : AppCompatActivity() {
    private var _binding: ActivityUpdateDataBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUpdateDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]

        if (intent != null) {
            when (intent.getStringExtra("key")) {
                "updateNurse" -> {
                    val id = intent.getStringExtra("workId") ?: ""
                    val name = intent.getStringExtra("name") ?: ""
                    val byteArray = intent?.getByteArrayExtra("image")
                    val bitmap = byteArray?.size?.let {
                        BitmapFactory.decodeByteArray(byteArray, 0, it)
                    }
                    binding.apply {
                        binding.nurseLayout.visibility = View.VISIBLE
                        binding.patientLayout.visibility = View.GONE
                        nurseItem.ivArrow.visibility = View.GONE
                        nurseItem.tvPatientName.text = name
                        nurseItem.tvWorkId.text = id
                        nurseItem.ivNurse.setImageBitmap(bitmap)
                        btnUpdate.setOnClickListener {
                            if (edPassword.text.isNotEmpty() && edPhone.text.isNotEmpty() && edEmail.text.isNotEmpty()) {
                                if (isValidPhoneNumber(edPhone.text.toString()))
                                    updateNurse(
                                        Nurse.NursesForUpdate(
                                            id,
                                            name,
                                            edPassword.text.toString(),
                                            edPhone.text.toString(),
                                            edEmail.text.toString()
                                        )
                                    )
                                else
                                    Toast.makeText(
                                        this@UpdateData,
                                        "Invalid Phone Format",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            } else {
                                Toast.makeText(this@UpdateData, "Fill Blanks!!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }
                }

                "updatePatient" -> {
                    val byteArray = intent?.getByteArrayExtra("image")
                    val bitmap = byteArray?.size?.let {
                        BitmapFactory.decodeByteArray(byteArray, 0, it)
                    }
                    val id = intent.getStringExtra("nationalId") ?: ""
                    val name = intent.getStringExtra("name") ?: ""
                    val room = intent.getStringExtra("room") ?: ""
                    binding.apply {
                        item.ivArrow.visibility = View.GONE
                        nurseLayout.visibility = View.GONE
                        patientLayout.visibility = View.VISIBLE
                        item.tvRoom.text = id
                        item.tvPatientName.text = name
                        item.ivPatient.setImageBitmap(bitmap)
                        edRoom.setText(room)
                        btnUpdatePatient.setOnClickListener {
                            if (edRoom.text.isNotEmpty()
                                && edBloodType.text.isNotEmpty()
                                && edWeight.text.isNotEmpty()
                                && edHeight.text.isNotEmpty()
                            ) {
                                update(
                                    id,
                                    edRoom.text.toString(),
                                    edHeight.text.toString(),
                                    edWeight.text.toString(),
                                    edBloodType.text.toString()
                                )
                            } else {
                                Toast.makeText(this@UpdateData, "Fill Blanks!!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun update(
        id: String,
        room: String,
        height: String,
        weight: String,
        bloodType: String
    ) {
        mainViewModel.updatePatient(id, room, height, weight, bloodType) {
            if (it == "done") finish()
        }
    }

    private fun updateNurse(nursesForUpdate: Nurse.NursesForUpdate) {
        mainViewModel.updateNurse(nursesForUpdate) {
            if (it == "done") finish()
            else Log.e("update", it.toString())
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val numericPhoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber)

        return PhoneNumberUtils.isGlobalPhoneNumber(numericPhoneNumber)
    }

}