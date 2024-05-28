package com.example.smarthcms.views.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.smarthcms.R
import com.example.smarthcms.databinding.ActivityAdminHostBinding

class AdminHost : AppCompatActivity() {
    private var _binding: ActivityAdminHostBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAdminHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =  supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigation,navController)
        binding.bottomNavigation.setOnItemSelectedListener {item ->
            when(item.itemId) {
                R.id.adminHubFragment -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.adminHubFragment)
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
