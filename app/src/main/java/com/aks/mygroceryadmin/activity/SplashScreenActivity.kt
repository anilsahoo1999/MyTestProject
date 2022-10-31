package com.aks.mygroceryadmin.activity

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieAnimationView
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.base.BaseActivity
import com.aks.mygroceryadmin.databinding.ActivitySplashScreenBinding
import com.aks.mygroceryadmin.utils.Constants
import com.aks.mygroceryadmin.utils.SharedPreference

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var sharedPreference: SharedPreference
    lateinit var binding : ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash_screen)
        sharedPreference = SharedPreference(this)
        binding.lottie.addAnimatorListener(listener)
    }
    val listener = object: Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator?) {
            Log.d("TAG", "onAnimationStart")
        }

        override fun onAnimationEnd(p0: Animator?) {
            if (sharedPreference.fetchBooleanDataFromSharedPref(Constants.IS_LOGIN_DONE)==true){
                startActivity(Intent(this@SplashScreenActivity , BaseActivity::class.java))
                finish()
            }else {
                startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                finish()
            }
        }

        override fun onAnimationCancel(p0: Animator?) {
            TODO("Not yet implemented")
        }

        override fun onAnimationRepeat(p0: Animator?) {
            TODO("Not yet implemented")
        }

    }
}