package com.example.smarthcms.views.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthcms.databinding.FragmentAdminHubBinding
import com.example.smarthcms.models.Nurse
import com.example.smarthcms.utils.NewNurseDialog
import com.example.smarthcms.utils.NursesRecyclerAdapter
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class AdminHub : Fragment() {
    private var _binding: FragmentAdminHubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainRepository = MainRepository(requireContext())
        val mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        binding.apply {
            fbAddNurse.setOnClickListener {
                startActivity(Intent(requireActivity(), NewNurseDialog::class.java))
            }
            if (isAdded){
                mainViewModel.getNursesForAdminHub()
                mainViewModel.nursesForAdminHubResult.observe(requireActivity()) {
                    val adapter = NursesRecyclerAdapter(it ?: arrayListOf(), requireActivity())
                    rvNurses.adapter = adapter
                    rvNurses.layoutManager = LinearLayoutManager(requireActivity())
                    setupSearchView(it ?: arrayListOf(), adapter)
                }
            }
        }
    }

    private fun setupSearchView(
        list: ArrayList<Nurse.NursesForAdminHub>,
        adapter: NursesRecyclerAdapter
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
        list: ArrayList<Nurse.NursesForAdminHub>,
        adapter: NursesRecyclerAdapter
    ) {
        val filteredList = ArrayList<Nurse.NursesForAdminHub>()
        list.forEach { nurse ->
            when {
                nurse.workId.lowercase().contains(newText.lowercase())
                -> filteredList.add(nurse)

                nurse.name.lowercase().contains(newText.lowercase())
                -> filteredList.add(nurse)
            }
        }

        if (filteredList.isEmpty())
            adapter.filterList(arrayListOf())
        else
            adapter.filterList(filteredList)
    }
}