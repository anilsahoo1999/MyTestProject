package com.aks.mygroceryadmin.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.base.BaseActivity
import com.aks.mygroceryadmin.databinding.ActivityLoginBinding
import com.aks.mygroceryadmin.fragment.category.CategoryFragment
import com.aks.mygroceryadmin.models.ProfileModel
import com.aks.mygroceryadmin.utils.AppUtils
import com.aks.mygroceryadmin.utils.Constants
import com.aks.mygroceryadmin.utils.Dialog
import com.aks.mygroceryadmin.utils.SharedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private var loadingDialog: AlertDialog? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreference: SharedPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        firebaseAuth = AdminApp.instance.firebaseAuth
        sharedPreference = SharedPreference(this)
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.cardLogin.setOnClickListener {
            if (AppUtils.isNetworkConnected(this)) {
                loginWithGoogle()
            } else {
                Toast.makeText(this, "Internet not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginWithGoogle() {
        showProgressDialog(true, "Loading...")
        resultLauncher.launch(googleSignInClient.signInIntent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it != null) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(it.data)
                handleResult(task)
            }
        }

    private fun handleResult(task: Task<GoogleSignInAccount>) {

        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            showProgressDialog(false, "Signing...")
            Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
        }


    }

    private fun updateUI(account: GoogleSignInAccount) {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {
                        showProgressDialog(false, "Getting ready...")
                        //TODO open tell us about you dialog

                        checkIsNewUser() { isNewUser, profileModel ->
                            if (isNewUser) {
                                openCompleteBottomSheet(account)
                            } else {
                                submitLoginDetails(account, profileModel?.userMobileNo, profileModel?.storeName)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun openCompleteBottomSheet(account: GoogleSignInAccount) {
        //open complete bottom sheet
        val bottomSheetDialog = BottomSheetDialog(this@LoginActivity, R.style.TransparentDialog)
        val view = layoutInflater.inflate(R.layout.complete_profile, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)
        val edUserMobileNum: EditText = view.findViewById(R.id.editNumber)
        val edStoreName: EditText = view.findViewById(R.id.edStoreName)
        val textViewMsg: TextView = view.findViewById(R.id.txtWelcome)
        val btnSubmit: Button = view.findViewById(R.id.btnSubmit)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        textViewMsg.text = "Hey, Welcome ${account.displayName} !"
        bottomSheetDialog.show()
        btnSubmit.setOnClickListener {
            if (edUserMobileNum.text.isNotEmpty() && edUserMobileNum.text.toString().length == 10) {
                if (edStoreName.text.isNotEmpty()) {
                    btnSubmit.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    submitLoginDetails(account, edUserMobileNum.text.toString().trim(),edStoreName.text.toString())
                }else{
                    Toast.makeText(
                        this,
                        "Please enter Store name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Please enter correct mobile number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun submitLoginDetails(account: GoogleSignInAccount, userMobileNum: String? = null, storeName: String? = null) {
        showProgressDialog(true, "Getting ready...")
        try {
            val database = AdminApp.instance.firebaseFirestore
            val uid = AdminApp.instance.firebaseAuth.currentUser?.uid
            Log.e("account", "uid: $uid")
            val doc = uid?.let {
                database.collection("AdminMasterDetails").document(it)
            }
            Log.e("account", "document id: ${doc?.id}")
            Log.e("account", "uid: ${uid.toString()}")
            Log.e("account", "uid: ${account.email}")
            Log.e("account", "uid: ${account.displayName}")
            Log.e("account", "uid: ${account.photoUrl}")
            sharedPreference.saveDetailsToSharedPref(
                Constants.USER_ID,
                doc?.id.toString()
            )
            sharedPreference.saveDetailsToSharedPref(
                Constants.CUSTOMER_ID,
                uid.toString()
            )
            sharedPreference.saveDetailsToSharedPref(
                Constants.USER_MAIL_ID,
                account.email.toString()
            )
            sharedPreference.saveDetailsToSharedPref(
                Constants.USERNAME,
                account.displayName.toString()
            )
            storeName?.let {
                sharedPreference.saveDetailsToSharedPref(
                    Constants.STORE_NAME,
                    it
                )
            }
            sharedPreference.saveDetailsToSharedPref(
                Constants.PROFILE_LINK,
                account.photoUrl.toString()
            )
            sharedPreference.saveDetailsToSharedPref(Constants.MOBILE_NO, "")
            sharedPreference.saveBooleanDataToSharedPref(Constants.IS_LOGIN_DONE, true)

            showProgressDialog(false, "Getting ready...")


            val profileModel = ProfileModel(
                userId = doc?.id.toString(),
                account.idToken.toString(),
                account.displayName.toString(),
                account.email.toString(),
                null,
                null,
                account.photoUrl.toString(),
                userMobileNum,
                storeName
            )

            uid?.let {
                database.collection("AdminMasterDetails").document(uid).set(profileModel)
                    .addOnSuccessListener {
                        sharedPreference.saveBooleanDataToSharedPref(
                            Constants.IS_PROFILE_DONE,
                            true
                        )
                        showProgressDialog(false, "Getting ready...")
                        val intent = Intent(this, BaseActivity::class.java)
                        startActivity(intent)
                        finish()


                    }.addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showProgressDialog(false, "Getting ready...")
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkIsNewUser(callBack: ((Boolean, ProfileModel?) -> Unit)) {
        //if new user then return true else return false
        try {
            val customerUid = AdminApp.instance.firebaseAuth.currentUser?.uid
            val database = AdminApp.instance.firebaseFirestore
            var profileModel : ProfileModel?=null
            customerUid?.let {
                database.collection("AdminMasterDetails").document(it).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            profileModel = task.result.toObject(ProfileModel::class.java)
                            if (profileModel?.userMobileNo.toString() == "null") {
                                callBack.invoke(true, profileModel)
                            } else {
                                callBack.invoke(false, profileModel)
                                Log.e(CategoryFragment.TAG, "checkIsNumberAvailable: true")
                            }
                        } else {
                            callBack.invoke(true,profileModel)
                            Log.e(CategoryFragment.TAG, "checkIsNumberAvailable: failed")
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showProgressDialog(state: Boolean, tittle: String) {
        if (state) {
            if (loadingDialog != null) {
                if (loadingDialog?.isShowing == true) {
                    loadingDialog?.dismiss()
                }
            }
            val popUp = MaterialAlertDialogBuilder(this, R.style.TransparentDialog)
            val view = layoutInflater.inflate(R.layout.loading_dialog, null)
            val loadingText: TextView = view.findViewById(R.id.loadingText)
            loadingText.text = tittle
            popUp.setView(view)
            loadingDialog = popUp.create()
            loadingDialog?.setCancelable(false)
            loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog?.show()
        } else {
            if (loadingDialog != null) {
                if (loadingDialog?.isShowing == true) {
                    loadingDialog?.dismiss()
                }
            }

        }
    }

}