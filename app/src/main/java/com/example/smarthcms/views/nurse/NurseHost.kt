package com.example.smarthcms.views.nurse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.smarthcms.R
import com.example.smarthcms.databinding.ActivityNurseHostBinding


class NurseHost : AppCompatActivity() {
    private var _binding: ActivityNurseHostBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNurseHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =  supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNavigation,navController)


        binding.bottomNavigation.setOnItemSelectedListener {item ->
            when(item.itemId) {
                R.id.patientsInfoFragment -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.patientsInfoFragment)
                    true
                }

                R.id.notificationsFragment -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.notificationsFragment)
                    true
                }

                R.id.profileFragment -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
