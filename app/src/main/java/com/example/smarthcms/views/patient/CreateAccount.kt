package com.example.smarthcms.views.patient

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.R
import com.example.smarthcms.databinding.ActivityCreateAccountBinding
import com.example.smarthcms.models.Patient
import com.example.smarthcms.utils.Constants
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import com.example.smarthcms.views.Login
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateAccount : AppCompatActivity() {
    private var _binding: ActivityCreateAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        binding.apply {
            login.setOnClickListener {
                startActivity(Intent(this@CreateAccount, Login::class.java))
            }
            showDatePickerButton.setOnClickListener {
                showDatePickerDialog()
            }

            edPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val password = s.toString()
                    val strengthColor = getPasswordStrengthColor(password)
                    edPassword.setTextColor(strengthColor)
                }
            })
            btnCreate.setOnClickListener {
                if (isEmpty()) {
                    Constants().createToast(
                        this@CreateAccount,
                        msg.toastText,
                        msg.root,
                        "Fill Blanks!!"
                    )
                } else {
                    formValidation(
                        edPhone.text.toString().trim(),
                        edNationalId.text.toString()
                    ) { formMsg ->
                        if (formMsg != null) {
                            Constants().createToast(
                                this@CreateAccount,
                                msg.toastText,
                                msg.root,
                                formMsg
                            )
                        } else {

                            val color = getPasswordStrengthColor(edPassword.text.toString())
                            if (getPasswordStrengthMessage(color) == "done") {
                                createAccount(
                                    Patient(
                                        edNationalId.text.toString().trim(),
                                        generateHealthID(edNationalId.text.toString().trim()),
                                        edPhone.text.toString().trim(),
                                        selectedDateTextView.text.toString(),
                                        edName.text.toString(),
                                        edEmail.text.toString(),
                                        "",
                                        "",
                                        "",
                                        ""
                                    ),
                                    edPassword.text.toString()
                                )
                            } else {
                                Toast.makeText(
                                    this@CreateAccount,
                                    getPasswordStrengthMessage(color),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }
            }


        }


    }


    private fun createAccount(patient: Patient, password: String) {
        if (Constants().isNetworkAvailable(this)) {
            binding.progress.visibility = View.VISIBLE
            mainViewModel.createAccount(patient, password) {
                if (it == "done") {
                    binding.progress.visibility = View.GONE
                    startActivity(
                        Intent(this, Login::class.java).putExtra(
                            "nationalId",
                            patient.nationalId
                        )
                    )
                    finishAffinity()
                } else {
                    binding.progress.visibility = View.GONE
                    Constants().createToast(
                        this,
                        binding.msg.toastText,
                        binding.msg.root,
                        it ?: ""
                    )
                }
            }
        } else {
            Constants().networkMsg(binding.root)
        }

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, y: Int, m: Int, d: Int ->
                onDateSelected(y, m, d)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun onDateSelected(year: Int, month: Int, day: Int) {
        val selectedDate = Calendar.getInstance()
        selectedDate.set(year, month, day)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)

        binding.selectedDateTextView.text = formattedDate
    }

    private fun isEmpty(): Boolean {
        binding.apply {
            return (edNationalId.text.toString().isEmpty()
                    || edPassword.text.toString().isEmpty()
                    || edPhone.text.toString().isEmpty()
                    || edEmail.text.toString().isEmpty()
                    || edName.text.toString().isEmpty()
                    || selectedDateTextView.text.isNullOrEmpty())
        }
    }

    private fun formValidation(
        phone: String,
        nationalID: String,
        callback: (String?) -> Unit
    ) {
        if (!isValidPhoneNumber(phone))
            callback(getString(R.string.invalid_phone_format))
        else if (!isNationalIDValid(nationalID))
            callback("Invalid national id format: must be 10 digits")
        else
            callback(null)
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val numericPhoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber)

        return PhoneNumberUtils.isGlobalPhoneNumber(numericPhoneNumber)
    }

    private fun generateHealthID(nationalID: String): String {
        val timestamp = System.currentTimeMillis()
        return nationalID + timestamp.toString().takeLast(4)
    }

    private fun isNationalIDValid(nationalID: String): Boolean {
        val regex = Regex("\\d{10}")
        return regex.matches(nationalID)
    }

    private fun getPasswordStrengthColor(password: String): Int {
        return when {
            password.matches(Regex("^[0-9]+$")) -> Color.RED
            password.matches(Regex("^[a-zA-Z0-9]+$")) -> Color.YELLOW
            else -> Color.GREEN
        }
    }

    private fun getPasswordStrengthMessage(color: Int): String {
        return when (color) {
            Color.RED -> "Weak password: should contain letters, numbers, and special characters"
            Color.YELLOW -> "Medium strength password: should contain letters and numbers"
            Color.GREEN -> "done"
            else -> "Password strength unknown"
        }
    }


}