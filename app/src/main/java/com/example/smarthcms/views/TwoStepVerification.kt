package com.example.smarthcms.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.R
import com.example.smarthcms.databinding.ActivityTwoStepVerificationBinding
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.MultiFactorAssertion
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import java.util.concurrent.TimeUnit

class TwoStepVerification : AppCompatActivity() {
    private var _binding: ActivityTwoStepVerificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var verificationId: String
    private lateinit var countDownTimer: CountDownTimer
    private val OTP_TIMEOUT_MILLIS = 30000
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTwoStepVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val phoneNumber = intent.getStringExtra("phone")
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        countDownTimer = object : CountDownTimer(OTP_TIMEOUT_MILLIS.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                binding.tvTime.text = getString(R.string.time_remaining, formattedTime)
            }

            override fun onFinish() {
                binding.tvTime.text = getString(R.string.timeout_occurred)
            }
        }
        mainViewModel.getPhone()
        mainViewModel.getType()
        if (phoneNumber != null) {
            val textWatcher = createTextWatcher()
            binding.apply {
                editTextDigit1.addTextChangedListener(textWatcher)
                editTextDigit2.addTextChangedListener(textWatcher)
                editTextDigit3.addTextChangedListener(textWatcher)
                editTextDigit4.addTextChangedListener(textWatcher)
                editTextDigit5.addTextChangedListener(textWatcher)
                editTextDigit6.addTextChangedListener(textWatcher)
                tvShowNumber.text = getString(
                    R.string.please_enter_the_verification_code_sent_to,
                    obfuscatePhoneNumber(phoneNumber)
                )
                btnResend.setOnClickListener {
                    sendSMS(phoneNumber)
                    countDownTimer.start()
                }
                sendSMS(phoneNumber)
                countDownTimer.start()

                btnChangePhone.setOnClickListener {
                    newPhoneLayout.visibility = View.VISIBLE
                }
                btnSend.setOnClickListener {
                    if (edNewPhone.text.toString().isNotEmpty()) {
                        if (isValidPhoneNumber(edNewPhone.text.toString())) {
                            sendSMS(phoneNumber)
                            countDownTimer.start()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "Invalid Phone Format",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        Toast.makeText(this@TwoStepVerification, "Fill Blanks", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    }


    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    when (s) {
                        binding.editTextDigit1.text -> moveFocus(binding.editTextDigit2)
                        binding.editTextDigit2.text -> moveFocus(binding.editTextDigit3)
                        binding.editTextDigit3.text -> moveFocus(binding.editTextDigit4)
                        binding.editTextDigit4.text -> moveFocus(binding.editTextDigit5)
                        binding.editTextDigit5.text -> moveFocus(binding.editTextDigit6)
                    }

                    if (allDigitsEntered()) {
                        signIn(buildEnteredOTP())
                    }
                }
            }
        }
    }

    private fun moveFocus(editText: EditText) {
        editText.requestFocus()
    }

    private fun buildEnteredOTP(): String {
        val enteredOTP = StringBuilder()
        binding.apply {
            enteredOTP.append(editTextDigit1.text)
            enteredOTP.append(editTextDigit2.text)
            enteredOTP.append(editTextDigit3.text)
            enteredOTP.append(editTextDigit4.text)
            enteredOTP.append(editTextDigit5.text)
            enteredOTP.append(editTextDigit6.text)
        }
        return enteredOTP.toString()
    }

    private fun allDigitsEntered(): Boolean {
        return binding.editTextDigit1.text.isNotEmpty() &&
                binding.editTextDigit2.text.isNotEmpty() &&
                binding.editTextDigit3.text.isNotEmpty() &&
                binding.editTextDigit4.text.isNotEmpty() &&
                binding.editTextDigit5.text.isNotEmpty() &&
                binding.editTextDigit6.text.isNotEmpty()
    }


    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val numericPhoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber)

        return PhoneNumberUtils.isGlobalPhoneNumber(numericPhoneNumber)
    }

    private fun obfuscatePhoneNumber(phoneNumber: String): String {
        val obfuscatedLength = 3
        val obfuscatedPrefix = "*".repeat(maxOf(0, phoneNumber.length - obfuscatedLength))
        return if (phoneNumber.length > obfuscatedLength) {
            val unobfuscatedPart = phoneNumber.takeLast(obfuscatedLength)
            obfuscatedPrefix + unobfuscatedPart
        } else {
            obfuscatedPrefix + phoneNumber.substring(1)
        }
    }

    private fun generateCallbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential.smsCode.toString())
                countDownTimer.cancel()
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                countDownTimer.cancel()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                this@TwoStepVerification.verificationId = verificationId
            }
        }
    }

    private fun sendSMS(phoneNumber: String) {
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder()
                .setActivity(this)
                .setPhoneNumber(phoneNumber)
                .setCallbacks(generateCallbacks())
                .setTimeout(30L, TimeUnit.SECONDS)
                .build()
        )
    }

    private fun signIn(verificationCode: String) {
        val credential =
            PhoneAuthProvider.getCredential(verificationId, verificationCode)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultIntent = Intent()
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }else{
                    Toast.makeText(this, "Wrong Code!!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}