package com.example.smarthcms.views.nurse

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthcms.databinding.FragmentPatientsInfoBinding
import com.example.smarthcms.models.Patient
import com.example.smarthcms.utils.NotificationReceiver
import com.example.smarthcms.utils.PatientInfoRecyclerAdapter
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class PatientsInfo : Fragment() {

    private var _binding: FragmentPatientsInfoBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientsInfoBinding.inflate(inflater, container, false)
        val mainRepository = MainRepository(requireActivity())
        val mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        binding.apply {

            mainViewModel.getId()
            mainViewModel.nationalId.observe(requireActivity()) { id ->
                if (isAdded) {
                    mainViewModel.getPatientByNurseNationalId(id)
                    mainViewModel.patientByNurseNationalIdResult.observe(requireActivity()){ patientForLists ->
                        val adapter = PatientInfoRecyclerAdapter(
                            patientForLists ?: arrayListOf(),
                            requireActivity(),
                            mainViewModel,
                            requireActivity()
                        )
                        rvPatients.adapter = adapter

                        rvPatients.layoutManager = LinearLayoutManager(requireActivity())
                        setupSearchView(patientForLists ?: arrayListOf(), adapter)
                    }

                }
            }
        }
        return binding.root
    }

    private fun setupSearchView(
        list: ArrayList<Patient.PatientForList>,
        adapter: PatientInfoRecyclerAdapter
    ) {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText.toString(), list, adapter)
                return false
            }
        })
    }

    private fun filter(
        newText: String,
        list: ArrayList<Patient.PatientForList>,
        adapter: PatientInfoRecyclerAdapter
    ) {
        val filteredList = ArrayList<Patient.PatientForList>()
        list.forEach { patient ->
            when {
                patient.nationalId.lowercase().contains(newText.lowercase())
                -> filteredList.add(patient)

                patient.name.lowercase().contains(newText.lowercase())
                -> filteredList.add(patient)

                patient.room.lowercase().contains(newText.lowercase())
                -> filteredList.add(patient)
            }
        }

        if (filteredList.isEmpty())
            adapter.filterList(arrayListOf())
        else
            adapter.filterList(filteredList)
    }
}