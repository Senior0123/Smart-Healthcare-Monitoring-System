package com.example.smarthcms.views.nurse

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthcms.databinding.FragmentNotificationsBinding
import com.example.smarthcms.models.Notification
import com.example.smarthcms.utils.NotificationAdapter
import com.example.smarthcms.utils.NotificationReceiver
import com.example.smarthcms.viewmodel.MainRepository
import com.example.smarthcms.viewmodel.MainViewModel
import com.example.smarthcms.viewmodel.MainViewModelFactory

class Notifications : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val mainRepository = MainRepository(requireActivity())
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(mainRepository))[MainViewModel::class.java]
        if (isAdded){
            mainViewModel.getNotifications()
            mainViewModel.notificationsResult.observe(requireActivity()){ notification ->

                val adapter = NotificationAdapter(requireContext(), notification ?: arrayListOf())
                binding.rvPatients.adapter = adapter
                binding.rvPatients.layoutManager = LinearLayoutManager(requireContext())
                setupSearchView(notification ?: arrayListOf(), adapter)
            }


            mainViewModel.notificationPrompt()
            mainViewModel.notificationPromptResult.observe(requireActivity()){ msg ->
                if (msg != null) {
                    binding.textMsg.text = msg
                    binding.close.setOnClickListener {
                        binding.alert.visibility = View.GONE
                    }
                    mainViewModel.getFlag()
                    mainViewModel.flagResult.observe(requireActivity()){ flag ->
                        if (flag == "1") {
                            if (isAdded && areNotificationsEnabled()) {
                                scheduleNotification(msg)
                                binding.apply {
                                    alert.visibility = View.VISIBLE
                                }
                            } else if (isAdded) {
                                showNotificationSettingsDialog()
                            }
                        }
                    }
                }
            }
        }
        return binding.root
    }

    private fun setupSearchView(
        list: ArrayList<Notification>,
        adapter: NotificationAdapter
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
        list: ArrayList<Notification>,
        adapter: NotificationAdapter
    ) {
        val filteredList = ArrayList<Notification>()
        list.forEach { patient ->
            when {
                patient.username.lowercase().contains(newText.lowercase())
                -> filteredList.add(patient)

                patient.room.lowercase().contains(newText.lowercase())
                -> filteredList.add(patient)

                patient.msg.lowercase().contains(newText.lowercase())
                -> filteredList.add(patient)
            }
        }

        if (filteredList.isEmpty())
            adapter.filterList(arrayListOf())
        else
            adapter.filterList(filteredList)
    }


    private fun scheduleNotification(msg: String) {
        val notificationIntent = Intent(requireActivity(), NotificationReceiver::class.java)
        notificationIntent.putExtra("msg", msg)
        requireActivity().sendBroadcast(notificationIntent)
        mainViewModel.changeFlag()
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            showManualNotificationEnableDialog()
            false
        }

    }


    private fun showManualNotificationEnableDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Notification Permissions Required")
            .setMessage("Please enable notification permissions for this app.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", requireActivity().packageName, null)
        startActivity(intent)
    }


    private fun showNotificationSettingsDialog() {
        val dialogIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireActivity().packageName, null)
            )
        }
        startActivity(dialogIntent)
    }
}