package com.aks.mygroceryadmin.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.databinding.ActivityBaseBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class BaseActivity : AppCompatActivity() {
    private lateinit var binding:ActivityBaseBinding
    lateinit var bottomNavView : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_base)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        bottomNavView = binding.bottomNavView
        val navController = navHostFragment.navController
        bottomNavView.setupWithNavController(navController)

    }
}