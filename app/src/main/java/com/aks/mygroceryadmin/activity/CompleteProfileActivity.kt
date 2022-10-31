package com.aks.mygroceryadmin.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.databinding.CompleteProfileBinding
import com.aks.mygroceryadmin.utils.SharedPreference
import com.google.android.gms.location.FusedLocationProviderClient

class CompleteProfileActivity : AppCompatActivity() {
    private lateinit var sharedPreference: SharedPreference
    private lateinit var binding: CompleteProfileBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var location: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.complete_profile)
        sharedPreference = SharedPreference(this)
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        requestLocation()


    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            if (isLocationEnabled()) {
                //finally get latitude and longitude
                fetchLatitudeLongitude()

            } else {
                //open setting here
                Toast.makeText(this, "Please turn on Internet", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission(){
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        it.forEach { result ->
            if (!result.value) {
                //permission denied
                Toast.makeText(
                    this,
                    "Please allow all permission",
                    Toast.LENGTH_SHORT
                ).show()
                requestLocationPermission()
            } else {
                //permission granted
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
                requestLocation()
            }
        }
    }
    private fun isLocationEnabled():Boolean{
        val locationManager : LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun fetchLatitudeLongitude(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
           requestLocation()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){task->
            location= task.result
        }
    }
}