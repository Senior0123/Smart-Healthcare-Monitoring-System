package com.example.smarthcms.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.smarthcms.R
import com.example.smarthcms.databinding.ProfileLayoutBinding
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class Profile : Fragment() {
    private var _binding: ProfileLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainRepository = MainRepository(requireActivity())
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]

        binding.progress.visibility = View.VISIBLE

        binding.logout.setOnClickListener {
            mainViewModel.logout()
            startActivity(Intent(requireActivity(), Intro::class.java))
            requireActivity().finishAffinity()
        }
        if (isAdded){

            mainViewModel.image.observe(requireActivity()) { image ->
                if (image != null) {
                    Glide.with(requireActivity())
                        .load(image)
                        .into(binding.imageView)

                }
                binding.progress.visibility = View.GONE
            }

            binding.imageView.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    selectImage()
                }
            }
            mainViewModel.type.observe(requireActivity()) { type ->
                mainViewModel.nationalId.observe(requireActivity()) { nationalId ->
                    when (type) {
                        "admin" -> {
                            mainViewModel.getProfile("admins", nationalId)
                            mainViewModel.getProfileResult.observe(requireActivity()){ profile ->
                                if (profile != null) {
                                    binding.apply {
                                        tvUsername.text = profile.name
                                        tvEmail.text = profile.email
                                        tvHealthId.text = getString(R.string.work_id__, profile.id)
                                        tvPhone.text = profile.phone
                                        dateOfBirth.text = profile.dateOfBirth
                                        idOrIqama.text = profile.nationalId
                                    }
                                }
                                binding.progress.visibility = View.GONE
                            }
                            mainViewModel.loadImage("admins", nationalId)
                        }

                        "nurse" -> {
                            mainViewModel.getProfile("nurses", nationalId)
                            mainViewModel.getProfileResult.observe(requireActivity()){ profile ->
                                if (profile != null) {
                                    binding.apply {
                                        tvUsername.text = profile.name
                                        tvEmail.text = profile.email
                                        tvHealthId.text = getString(R.string.work_id__, profile.id)
                                        tvPhone.text = profile.phone
                                        dateOfBirth.text = profile.dateOfBirth
                                        idOrIqama.text = profile.nationalId
                                    }
                                }
                                binding.progress.visibility = View.GONE
                            }
                            mainViewModel.loadImage("nurses", nationalId)

                        }

                        "patient" -> {
                            mainViewModel.getProfile("patients", nationalId)
                            mainViewModel.getProfileResult.observe(requireActivity()){ profile ->
                                if (profile != null) {
                                    binding.apply {
                                        tvUsername.text = profile.name
                                        tvEmail.text = profile.email
                                        tvHealthId.text = getString(R.string.helthId, profile.id)
                                        tvPhone.text = profile.phone
                                        dateOfBirth.text = profile.dateOfBirth
                                        idOrIqama.text = profile.nationalId
                                    }
                                }
                                binding.progress.visibility = View.GONE
                            }
                            mainViewModel.loadImage("patients", nationalId)
                        }
                    }
                }
            }
        }
        mainViewModel.getId()
        mainViewModel.getType()
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        pickImageLauncher.launch(intent)
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                selectImage()
            } else {
                Toast.makeText(requireActivity(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                if (imageUri != null) {
                    startCrop(imageUri)
                } else {
                    Toast.makeText(requireActivity(), "Failed to get image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }


    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            MainScope().launch(Dispatchers.Main) {
                mainViewModel.nationalId.observe(requireActivity()) { id ->
                    mainViewModel.type.observe(requireActivity()) { type ->
                        when (type) {
                            "admin" -> mainViewModel.saveImage(result.uriContent, "admins", id)
                            "patient" -> mainViewModel.saveImage(result.uriContent, "patients", id)
                            "nurse" -> mainViewModel.saveImage(result.uriContent, "nurses", id)
                        }
                    }
                }
            }
        } else {
            val exception = result.error
            Toast.makeText(
                requireActivity(),
                exception.toString(),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun startCrop(uri: Uri) {
        val cropImageOptions = CropImageOptions().apply {
            imageSourceIncludeGallery = true
            imageSourceIncludeCamera = false
        }
        val cropImageContractOptions = CropImageContractOptions(uri = uri, cropImageOptions)
        cropImage.launch(cropImageContractOptions)
    }


}