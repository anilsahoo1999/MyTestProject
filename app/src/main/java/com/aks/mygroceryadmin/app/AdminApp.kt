package com.aks.mygroceryadmin.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminApp : Application() {
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var firebaseAuth: FirebaseAuth

    companion object{
        lateinit var instance : AdminApp
    }
    init {
        instance = this
    }
    override fun onCreate() {
        super.onCreate()
        provideFirebaseInstance()
    }
    private fun provideFirebaseInstance(){
        CoroutineScope(Dispatchers.IO).launch {
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
            firebaseStorage = FirebaseStorage.getInstance()
            firebaseAuth= FirebaseAuth.getInstance()
        }
    }
}