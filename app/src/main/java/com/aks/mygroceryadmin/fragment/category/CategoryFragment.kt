package com.aks.mygroceryadmin.fragment.category

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.base.BaseActivity
import com.aks.mygroceryadmin.base.BaseFragment
import com.aks.mygroceryadmin.databinding.FragmentCategoryBinding
import com.aks.mygroceryadmin.models.ProfileModel
import com.aks.mygroceryadmin.utils.Constants
import com.aks.mygroceryadmin.utils.SharedPreference
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CategoryFragment : BaseFragment<FragmentCategoryBinding>() {
    override fun getFragmentId(): Int {
        return R.layout.fragment_category
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private  var location: Location?=null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreference
    private var profileModel: ProfileModel? = null
    private var customerUid: String? = null
    private lateinit var geocoder: Geocoder
    private var currentAddress: String? = null

    companion object {
        val TAG = CategoryFragment::class.java.simpleName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = FusedLocationProviderClient(requireContext())
        sharedPreferences = SharedPreference(requireContext())
        (activity as BaseActivity).bottomNavView.visibility = View.VISIBLE
        initializeValue()
        requestLocation()

        performOnClickEvent()

    }

    private fun initializeValue() {
        firestore = AdminApp.instance.firebaseFirestore
        customerUid = AdminApp.instance.firebaseAuth.currentUser!!.uid
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        if (sharedPreferences.fetchBooleanDataFromSharedPref(Constants.IS_NUMBER_ADDED) == false) {
            checkIsNumberAvailable()
        }
    }

    private fun checkIsNumberAvailable() {
        Log.e(TAG, "checkIsNumberAvailable: $customerUid")
        customerUid?.let {
            firestore.collection("AdminMasterDetails").document(it).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        profileModel = task.result.toObject(ProfileModel::class.java)
                        if (profileModel?.userMobileNo.toString() == "null") {
                            openCompleteBottomSheet()
                        } else {
                            Log.e(TAG, "checkIsNumberAvailable: true")
                        }
                    } else {
                        Log.e(TAG, "checkIsNumberAvailable: failed")
                    }
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openCompleteBottomSheet() {
        //open complete bottom sheet
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog)
        val view = layoutInflater.inflate(R.layout.complete_profile, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)
        val editText: EditText = view.findViewById(R.id.editNumber)
        val textViewMsg: TextView = view.findViewById(R.id.txtWelcome)
        val btnSubmit: Button = view.findViewById(R.id.btnSubmit)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        textViewMsg.text = "Hey, Welcome ${profileModel?.username}!"
        bottomSheetDialog.show()
        btnSubmit.setOnClickListener {
            if (editText.text.isNotEmpty() && editText.text.toString().length == 10) {
                profileModel?.userMobileNo = editText.text.toString()
                fetchLatitudeLongitude()
                getAddress()
                profileModel?.userLatLng = "${location?.latitude} ${location?.longitude}"
                profileModel?.userAddress = currentAddress
                btnSubmit.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                customerUid?.let {
                    profileModel?.let { model ->
                        firestore.collection("AdminMasterDetails").document(it).set(model)
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                bottomSheetDialog.dismiss()
                                sharedPreferences.saveBooleanDataToSharedPref(
                                    Constants.IS_NUMBER_ADDED,
                                    true
                                )
                                Toast.makeText(
                                    requireContext(),
                                    "Saved Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter correct mobile number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getAddress() {

        location?.let { it ->
            val address: List<Address> = geocoder.getFromLocation(it.latitude, it.longitude, 1)

            currentAddress = address.let {it2->
                it2[0].getAddressLine(0)
            }
        }


    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                //finally get latitude and longitude
                fetchLatitudeLongitude()

            } else {
                //open setting here
                Toast.makeText(requireContext(), "Please turn on Internet", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.forEach { result ->
                if (!result.value) {
                    //permission denied
                    Toast.makeText(
                        requireContext(),
                        "Please allow all permission",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestLocationPermission()
                } else {
                    //permission granted
                    requestLocation()
                }
            }
        }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun fetchLatitudeLongitude() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocation()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
            location = task.result
        }
    }

    private fun performOnClickEvent(){
        binding.cardCategory.setOnClickListener{
            val addCategoryFragment = AddCategoryFragment()
            val manager = requireActivity().supportFragmentManager
            addCategoryFragment.isCancelable = false
            addCategoryFragment.show(manager,addCategoryFragment.tag)
        }

        binding.cardProduct.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_productDashboardFragment)
        }
        binding.cardBanner.setOnClickListener{
            val addBannerFragment = AddBannerFragment()
            val manager = requireActivity().supportFragmentManager
            addBannerFragment.isCancelable = false
            addBannerFragment.show(manager,addBannerFragment.tag)
        }
    }
}