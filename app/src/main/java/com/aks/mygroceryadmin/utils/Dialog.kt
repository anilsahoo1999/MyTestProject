package com.aks.mygroceryadmin.utils

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.aks.mygroceryadmin.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Dialog {
    private var loadingDialog: AlertDialog?=null

    fun showProgressDialog(state:Boolean,tittle:String, context:Activity) {
        if (state){
            if (loadingDialog !=null){
                if (loadingDialog?.isShowing == true){
                    loadingDialog?.dismiss()
                }
            }
            val popUp= MaterialAlertDialogBuilder(context, R.style.TransparentDialog)
            val view = context.layoutInflater.inflate(R.layout.loading_dialog, null)
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

    fun messageDialog(
        status: Boolean,
        context: Activity,
        tittle: String,
        message: String,
        image: Int,
        isCancellable: Boolean,
        callBack: (Boolean) -> Unit
    ) {
        context.runOnUiThread {

            if (status) {
                if (loadingDialog != null) {
                    if (loadingDialog?.isShowing == true) {
                        loadingDialog?.dismiss()
                    }
                }
                val popUp = MaterialAlertDialogBuilder(context, R.style.TransparentDialog)
                val view = context.layoutInflater.inflate(R.layout.pop_up_message_dialog, null)
                popUp.setView(view)
                val contentImage: ImageView = view.findViewById(R.id.warningImage)
                val tittleHeading: TextView = view.findViewById(R.id.tittle)
                val description: TextView = view.findViewById(R.id.description)
                val closeDialog: ImageButton = view.findViewById(R.id.closeDialog)
                val btnOk: MaterialButton = view.findViewById(R.id.btnOk)
                contentImage.setImageResource(image)
                tittleHeading.text = tittle
                description.text = message
                loadingDialog = popUp.create()
                loadingDialog?.setCancelable(isCancellable)
                loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                loadingDialog?.show()
                closeDialog.setOnClickListener {
                    loadingDialog?.dismiss()
                }
                btnOk.setOnClickListener {
                    loadingDialog?.dismiss()
                    callBack.invoke(true)
                }


            } else {
                if (loadingDialog != null) {
                    if (loadingDialog?.isShowing == true) {
                        loadingDialog?.dismiss()
                    }
                }
            }
        }

    }

    }