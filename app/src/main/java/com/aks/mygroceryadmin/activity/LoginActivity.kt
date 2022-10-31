package com.aks.mygroceryadmin.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.base.BaseActivity
import com.aks.mygroceryadmin.databinding.ActivityLoginBinding
import com.aks.mygroceryadmin.models.ProfileModel
import com.aks.mygroceryadmin.utils.AppUtils
import com.aks.mygroceryadmin.utils.Constants
import com.aks.mygroceryadmin.utils.SharedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding
    private var loadingDialog: AlertDialog?=null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var sharedPreference : SharedPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        firebaseAuth = AdminApp.instance.firebaseAuth
        sharedPreference = SharedPreference(this)
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        binding.cardLogin.setOnClickListener{
            if (AppUtils.isNetworkConnected(this)){
                loginWithGoogle()
            }else{
                Toast.makeText(this,"Internet not available",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loginWithGoogle(){
        showProgressDialog(true,"Loading...")
        resultLauncher.launch(googleSignInClient.signInIntent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it != null) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleResult(task)
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>){

        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            showProgressDialog(false,"Signing...")
            Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
        }


    }

    private fun updateUI(account: GoogleSignInAccount){
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {

                        showProgressDialog(true,"Getting ready...")

                        val database = AdminApp.instance.firebaseFirestore
                        val uid = AdminApp.instance.firebaseAuth.currentUser?.uid
                        Log.e("account", "uid: $uid" )
                        val doc = uid?.let {
                            database.collection("AdminMasterDetails").document(it)
                        }
                        Log.e("account", "document id: ${doc?.id}" )
                        Log.e("account", "uid: ${uid.toString()}" )
                        Log.e("account", "uid: ${account.email}" )
                        Log.e("account", "uid: ${account.displayName}" )
                        Log.e("account", "uid: ${account.photoUrl}" )
                        sharedPreference.saveDetailsToSharedPref(
                            Constants.USER_ID,
                            doc?.id.toString())
                        sharedPreference.saveDetailsToSharedPref(Constants.CUSTOMER_ID,
                            uid.toString())
                        sharedPreference.saveDetailsToSharedPref(Constants.USER_MAIL_ID,
                            account.email.toString())
                        sharedPreference.saveDetailsToSharedPref(Constants.USERNAME,
                            account.displayName.toString())
                        sharedPreference.saveDetailsToSharedPref(Constants.PROFILE_LINK,
                            account.photoUrl.toString())
                        sharedPreference.saveDetailsToSharedPref(Constants.MOBILE_NO, "")
                        sharedPreference.saveBooleanDataToSharedPref(Constants.IS_LOGIN_DONE,true)

                        showProgressDialog(false,"Getting ready...")


                        val profileModel = ProfileModel(
                            userId = doc?.id.toString(),
                            account.idToken.toString(),
                            account.displayName.toString(),
                            account.email.toString(),
                            null,
                            null,
                            account.photoUrl.toString(),
                            null,
                        )

                        uid?.let {
                            database.collection("AdminMasterDetails").document(uid).set(profileModel)
                                .addOnSuccessListener {
                                    sharedPreference.saveBooleanDataToSharedPref(Constants.IS_PROFILE_DONE,true)
                                    showProgressDialog(false,"Getting ready...")
                                    val intent = Intent(this, BaseActivity::class.java)
                                    startActivity(intent)
                                    finish()


                                }.addOnFailureListener {
                                    Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
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

    private fun showProgressDialog(state:Boolean,tittle:String) {
        if (state){
            if (loadingDialog !=null){
                if (loadingDialog?.isShowing == true){
                    loadingDialog?.dismiss()
                }
            }
            val popUp= MaterialAlertDialogBuilder(this,R.style.TransparentDialog)
            val view = layoutInflater.inflate(R.layout.loading_dialog, null)
            val loadingText: TextView =view.findViewById(R.id.loadingText)
            loadingText.text= tittle
            popUp.setView(view)
            loadingDialog=popUp.create()
            loadingDialog?.setCancelable(false)
            loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog?.show()
        }else{
            if (loadingDialog !=null){
                if (loadingDialog?.isShowing == true){
                    loadingDialog?.dismiss()
                }
            }

        }
    }

}