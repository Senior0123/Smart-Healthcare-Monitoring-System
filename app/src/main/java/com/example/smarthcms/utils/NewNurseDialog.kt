package com.example.smarthcms.utils

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.R
import com.example.smarthcms.databinding.NewNurseDialogBinding
import com.example.smarthcms.models.Nurse
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewNurseDialog : AppCompatActivity() {

    private var _binding: NewNurseDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = NewNurseDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        binding.apply {
            showDatePickerButton.setOnClickListener {
                showDatePickerDialog()
            }
            etPassword.addTextChangedListener(object : TextWatcher {
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
                    etPassword.setTextColor(strengthColor)
                }
            })

            btnCreate.setOnClickListener {
                if (isEmpty()) {
                    Constants().createToast(
                        this@NewNurseDialog,
                        msg.toastText,
                        msg.root,
                        "Fill Blanks!!"
                    )
                } else {
                    formValidation(
                        etPhone.text.toString().trim(),
                        etNationalId.text.toString(),
                    ) { formMsg ->
                        if (formMsg != null) {
                            Constants().createToast(
                                this@NewNurseDialog,
                                msg.toastText,
                                msg.root,
                                formMsg
                            )
                        } else {
                            val color = getPasswordStrengthColor(etPassword.text.toString())
                            if (getPasswordStrengthMessage(color) == "done") {
                                createNurse(
                                    Nurse(
                                        etName.text.toString(),
                                        generateHealthID(etNationalId.text.toString()),
                                        etNationalId.text.toString(),
                                        etEmail.text.toString(),
                                        selectedDateTextView.text.toString(),
                                        etPhone.text.toString(),
                                        ""
                                    ),
                                    etPassword.text.toString()
                                )
                            } else {
                                Constants().createToast(this@NewNurseDialog,
                                    msg.toastText,
                                    msg.root,
                                    getPasswordStrengthMessage(color)
                                    )
                            }

                        }
                    }
                }
            }
            close.setOnClickListener {
                finish()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun createNurse(nurse: Nurse,password: String) {
        if (Constants().isNetworkAvailable(this)) {
            mainViewModel.createNurse(nurse,password) {
                if (it == "done") {
                    Constants().createToast(
                        this,
                        binding.msg.toastText,
                        binding.msg.root,
                        "added!!"
                    )
                    finish()
                } else {
                    Constants().createToast(
                        this,
                        binding.msg.toastText,
                        binding.msg.root,
                        it ?: ""
                    )                }
            }
        } else {
            Constants().networkMsg(binding.root)
        }

    }

    private fun isEmpty(): Boolean {
        binding.apply {
            return (etNationalId.text.toString().isEmpty()
                    || etPassword.text.toString().isEmpty()
                    || etPhone.text.toString().isEmpty()
                    || etEmail.text.toString().isEmpty()
                    || etName.text.toString().isEmpty()
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
        else callback(null)
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