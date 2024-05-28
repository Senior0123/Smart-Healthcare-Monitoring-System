package com.example.smarthcms.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.databinding.ActivityLoginBinding
import com.example.smarthcms.utils.Constants
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import com.example.smarthcms.views.admin.AdminHost
import com.example.smarthcms.views.nurse.NurseHost
import com.example.smarthcms.views.patient.CreateAccount
import com.example.smarthcms.views.patient.PatientHost
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent != null) {
            binding.edNationalId.setText(intent.getStringExtra("nationalId"))
        }
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        mainViewModel.type.observe(this) { type ->
            binding.apply {
                if (type.equals("patient")) {
                    register.visibility = View.VISIBLE
                }
                register.setOnClickListener {
                    startActivity(Intent(this@Login, CreateAccount::class.java))
                }

                btnLogin.setOnClickListener {
                    if (isEmpty()) {
                        Constants().createToast(this@Login, msg.toastText, msg.root, "Fill Blanks!")
                    } else {
                        when (type) {
                            "patient" -> {
                                login(edNationalId.text.toString(), edPassword.text.toString())
                            }

                            "nurse" -> {
                                nurseLogin(edNationalId.text.toString(), edPassword.text.toString())
                            }

                            "admin" -> {
                                adminLogin(edNationalId.text.toString(), edPassword.text.toString())
                            }
                        }
                    }
                }

                forgetPassword.setOnClickListener {
                    startActivity(Intent(this@Login, Forget::class.java))
                }
            }
        }
        mainViewModel.getType()
    }


    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.progress.visibility = View.GONE
                mainViewModel.type.observe(this) {
                    when (it) {
                        "patient" -> {
                            mainViewModel.saveId(binding.edNationalId.text.toString())
                            startActivity((Intent(this, PatientHost::class.java)))
                            finishAffinity()
                        }

                        "nurse" -> {
                            mainViewModel.saveId(binding.edNationalId.text.toString())
                            startActivity((Intent(this, NurseHost::class.java)))
                            finishAffinity()
                        }

                        "admin" -> {
                            mainViewModel.saveId(binding.edNationalId.text.toString())
                            startActivity((Intent(this, AdminHost::class.java)))
                            finishAffinity()
                        }
                    }
                }
            }
        }

    private fun login(nationalId: String, password: String) {
        if (Constants().isNetworkAvailable(this)) {
            binding.progress.visibility = View.VISIBLE
            mainViewModel.login(nationalId,password) { patient ->
                if (patient == null) {
                    Constants().createToast(
                        this,
                        binding.msg.toastText,
                        binding.msg.root,
                        "Invalid username or password.."
                    )
                    binding.progress.visibility = View.GONE
                } else {
                    mainViewModel.savePhone(patient.phone)
                    val intent = Intent(this, TwoStepVerification::class.java).putExtra(
                        "phone",
                        patient.phone
                    )
                    someActivityResultLauncher.launch(intent)
                }
            }
        } else {
            Constants().networkMsg(binding.root)
        }
    }


    private fun nurseLogin(nationalId: String, password: String) {
        if (Constants().isNetworkAvailable(this)) {
            binding.progress.visibility = View.VISIBLE
            mainViewModel.nurseLogin(nationalId,password) { nurse ->
                if (nurse == null) {
                    Constants().createToast(
                        this,
                        binding.msg.toastText,
                        binding.msg.root,
                        "Invalid username or password.."
                    )
                    binding.progress.visibility = View.GONE
                } else {
                    mainViewModel.savePhone(nurse.phone)
                    val intent =
                        Intent(this, TwoStepVerification::class.java).putExtra("phone", nurse.phone)
                    someActivityResultLauncher.launch(intent)
                }
            }
        } else {
            Constants().networkMsg(binding.root)
        }
    }

    private fun adminLogin(nationalId: String, password: String) {
        if (Constants().isNetworkAvailable(this)) {
            binding.progress.visibility = View.VISIBLE
            mainViewModel.adminLogin(nationalId,password) { admin ->
                if (admin == null) {
                    Constants().createToast(
                        this,
                        binding.msg.toastText,
                        binding.msg.root,
                        "Invalid username or password.."
                    )
                    binding.progress.visibility = View.GONE
                } else {
                    mainViewModel.savePhone(admin.phone)
                    val intent =
                        Intent(this, TwoStepVerification::class.java).putExtra("phone", admin.phone)
                    someActivityResultLauncher.launch(intent)
                    binding.progress.visibility = View.GONE
                }
            }
        } else {
            Constants().networkMsg(binding.root)
        }
    }

    private fun isEmpty(): Boolean {
        return (binding.edNationalId.text.toString().isEmpty()
                || binding.edPassword.text.toString().isEmpty())
    }
}