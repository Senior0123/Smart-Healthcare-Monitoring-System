package com.example.smarthcms.views.patient

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smarthcms.databinding.FragmentPatientHomeBinding
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class PatientHome : Fragment() {
    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var mainViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainRepository = MainRepository(requireActivity())
        mainViewModel =
            ViewModelProvider(
                requireActivity(),
                MainViewModelFactory(mainRepository)
            )[MainViewModel::class.java]
        binding.btnSigns.setOnClickListener {
            startActivity(Intent(requireActivity(), PatientSigns::class.java))
        }
        binding.apply {
            mainViewModel.nationalId.observe(requireActivity()) { id ->
                if (id != null) {
                    if (isAdded){
                        mainViewModel.getPatientData(id)
                        mainViewModel.patientDataResult.observe(requireActivity()){ data ->
                            tvBloodType.text = data?.bloodType
                            tvWeight.text = data?.weight
                            tvHeight.text = data?.height
                        }
                    }
                }
            }
        }
        mainViewModel.getId()
    }
}