package com.example.smarthcms.views.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthcms.databinding.ActivityPatientListBinding
import com.example.smarthcms.models.Patient
import com.example.smarthcms.utils.PatientListRecyclerAdapter
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class PatientList : AppCompatActivity() {

    private var _binding: ActivityPatientListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mainRepository = MainRepository(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        binding.apply {


            if (intent != null) {
                when (intent.getStringExtra("key")) {
                    "add" -> {
                        mainViewModel.patientsResult.observe(this@PatientList) { list ->
                            setAdapter(list ?: arrayListOf())
                        }
                    }

                    "delete" -> {
                        mainViewModel.patientsResult.observe(this@PatientList) { list ->
                            setAdapter(list ?: arrayListOf())
                        }
                    }
                }
            }
        }
        mainViewModel.getPatients()
    }

    private fun setAdapter(list: ArrayList<Patient.PatientForList>) {
        val adapter = PatientListRecyclerAdapter(
            this,
            mainViewModel,
            list,
            intent.getStringExtra("key").toString(),
            intent.getStringExtra("workId").toString()
        )
        setupSearchView(list, adapter)
        binding.rvPatients.adapter = adapter
        binding.rvPatients.layoutManager = LinearLayoutManager(this@PatientList)
    }

    private fun setupSearchView(
        list: ArrayList<Patient.PatientForList>,
        adapter: PatientListRecyclerAdapter
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
        adapter: PatientListRecyclerAdapter
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