package com.aks.mygroceryadmin.fragment.category

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.databinding.FragmentAddCategoryBinding
import com.aks.mygroceryadmin.models.CategoryModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddCategoryFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddCategoryBinding
    private var uri: Uri? = null
    private lateinit var imageName : String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_category, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        performOnClickEvent()

    }

    private fun performOnClickEvent() {
        binding.relativeSelectFile.setOnClickListener {
            pickImage()
        }
        binding.imageClose.setOnClickListener{
            this.dismiss()
        }
        binding.btnSubmit.setOnClickListener{
            if (!binding.editName.text.isNullOrBlank()){
                if (uri == null){
                    Toast.makeText(requireContext(),"Please select an image",Toast.LENGTH_SHORT).show()
                }else {
                    binding.btnSubmit.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    uploadImage(uri)
                }
            }else{
                binding.textInputLayout.error = "Please enter category name"
            }
        }
    }

    private fun pickImage() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            val intent = Intent(
//                Intent.ACTION_PICK,
//                MediaStore.Images.Media.INTERNAL_CONTENT_URI
//            )
//            intent.type = "image/*"
//            intent.putExtra("crop", "true")
//            intent.putExtra("scale", true)
//            intent.putExtra("aspectX", 16)
//            intent.putExtra("aspectY", 9)
//            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
            launchFileChooser.launch("image/*")
        } else {
            storagePermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }
    }

    private val launchFileChooser =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            Log.e("imageUri", "getImageUri $it ")
            it?.let {
                uri = it
                imageName = uri2filename(uri)
                Log.e("filename", "uploadImage: $imageName")
                binding.imageName.text = imageName
            }
        }

    private fun uploadImage(uri: Uri?) {
        uri?.let {
            val storageRef = AdminApp.instance.firebaseStorage.reference
            val dataRef = storageRef.child("image/$imageName")
            dataRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                dataRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    uploadDataToFirestore(binding.editName.text.toString(),downloadUri)
                    Log.e("download url", "uploadImage: $downloadUri")
                } else {
                    Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun uploadDataToFirestore(categoryName: String, downloadUri: Uri?) {
        val db = AdminApp.instance.firebaseFirestore
        val currentUser = AdminApp.instance.firebaseAuth.currentUser!!.uid
        currentUser.let {
            val document = db.collection("AdminMasterDetails").document(currentUser).collection("CategoryList").document()
            val documentId= document.id
            val categoryModel = CategoryModel(downloadUri.toString(),categoryName,documentId)
            document.set(categoryModel).addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.visibility = View.VISIBLE
                Toast.makeText(requireContext(),"Saved Successfully",Toast.LENGTH_SHORT).show()
                this.dismiss()
            }
                .addOnFailureListener{
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.visibility = View.VISIBLE
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }

        }

    }

    @SuppressLint("Range")
    private fun uri2filename(uri: Uri?): String {
        var ret: String? = null
        val scheme: String? = uri?.scheme
        if (scheme == "file") {
            ret = uri.lastPathSegment.toString()
        } else if (scheme == "content") {
            val cursor: Cursor? =
                requireActivity().contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                ret = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return ret.toString()
    }

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            }
        }


}