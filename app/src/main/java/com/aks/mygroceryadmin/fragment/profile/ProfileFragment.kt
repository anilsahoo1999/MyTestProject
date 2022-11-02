package com.aks.mygroceryadmin.fragment.profile

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import android.widget.Toast
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.base.BaseFragment
import com.aks.mygroceryadmin.databinding.FragmentProfileBinding
import com.aks.mygroceryadmin.models.ProfileModel
import com.aks.mygroceryadmin.utils.Constants
import com.aks.mygroceryadmin.utils.SharedPreference
import com.bumptech.glide.Glide


class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    override fun getFragmentId(): Int {
        return R.layout.fragment_profile
    }

    lateinit var sharedPreference: SharedPreference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        requireActivity().window.statusBarColor = Color.TRANSPARENT

        sharedPreference = SharedPreference(requireContext())

        Glide.with(requireContext()).load(sharedPreference.fetchDetailsFromSharedPref(Constants.PROFILE_LINK))
            .into(binding.profileImage)
        binding.profileName.text = sharedPreference.fetchDetailsFromSharedPref(Constants.USERNAME)
//        getProfileDetails()

        performOnClickEvent()

    }


    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = requireActivity().window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
    private fun performOnClickEvent(){
        binding.btnMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(),binding.btnMenu)
            popupMenu.menuInflater.inflate(R.menu.profile_menu,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.btnLogout->{
                        Toast.makeText(requireContext(),"Signout Successfully",Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun getProfileDetails() {
        try {
            val currentUser = AdminApp.instance.firebaseAuth.currentUser!!.uid
            val dbRef = AdminApp.instance.firebaseFirestore
            dbRef.collection("AdminMasterDetails").document(currentUser).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        Log.e("ProfileData", "getProfileDetails: ${documentSnapshot.data}")
                        val profileModel: ProfileModel =
                            documentSnapshot.toObject(ProfileModel::class.java) as ProfileModel
                        if (profileModel.userMobileNo != null) {
                            Glide.with(requireContext()).load(profileModel.photoUrl)
                                .into(binding.profileImage)
                        }
                    }
                }.addOnFailureListener { exception ->
                Log.d("ProfileDataException", "get failed with ${exception.localizedMessage}")
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}