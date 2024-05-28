package com.example.smarthcms.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthcms.databinding.ForgetLayoutBinding
import com.example.smarthcms.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class Forget : AppCompatActivity() {
    private var _binding: ForgetLayoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ForgetLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            buttonResetPassword.setOnClickListener {
                val email = editTextEmail.text.toString().trim()

                if (email.isEmpty()) {
                    Constants().createToast(
                        this@Forget,
                        msg.toastText,
                        msg.root,
                        "Please enter your email"
                    )
                    return@setOnClickListener
                }

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Constants().createToast(
                                this@Forget,
                                msg.toastText,
                                msg.root,
                                "Password reset email sent"
                            )
                            finish()
                        } else {
                            Constants().createToast(
                                this@Forget,
                                msg.toastText,
                                msg.root,
                                "Failed to send reset email: ${task.exception?.message}"
                            )
                        }
                    }
            }
        }
    }
}