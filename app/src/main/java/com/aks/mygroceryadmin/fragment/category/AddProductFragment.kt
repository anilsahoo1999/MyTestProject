package com.aks.mygroceryadmin.fragment.category

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.databinding.FragmentAddProductBinding
import com.aks.mygroceryadmin.fragment.category.CategoryFragment.Companion.TAG
import com.aks.mygroceryadmin.fragment.category.adapter.CategoryAdapter
import com.aks.mygroceryadmin.fragment.category.adapter.PriceItemAdapter
import com.aks.mygroceryadmin.models.CategoryModel
import com.aks.mygroceryadmin.models.PricePerKgModel
import com.aks.mygroceryadmin.models.ProductModel
import com.aks.mygroceryadmin.utils.Dialog
import com.aks.mygroceryadmin.utils.GenerationMethod
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

import android.widget.FrameLayout

import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED


class AddProductFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddProductBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private var finalCategoryList: ArrayList<CategoryModel>? = null
    private var pricePerKgList = arrayListOf<PricePerKgModel>()
    var listSubCategory = arrayListOf<String>()
    private lateinit var priceItemAdapter: PriceItemAdapter
    private var selectedCategory: CategoryModel? = null
    private var imageUri: Uri? = null
    private var imageUrl: String? = null
    private var subcategoryId: String? = null
    private var spinnerSubCategoryAdapter: ArrayAdapter<String>? = null
    private lateinit var imageName: String
    private var count = 0
    private var productModel: ProductModel?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_product, container, false)
        return binding.root
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//
//        dialog.setOnShowListener {
//            val bottomSheet: FrameLayout? = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
//            val behavior = BottomSheetBehavior.from(bottomSheet)
//            behavior.skipCollapsed = true
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        }
//        return dialog
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
        } catch (e: Exception) {
        }
        initializeValue()
        performItemClick()

        val bundle: Bundle? = arguments
        if (bundle != null) {
            productModel =  bundle.getSerializable("ProductModel") as ProductModel
            Log.e(TAG, "Product for edit: $productModel")
            productModel?.let {
                updateProduct(it)
            }

        }

    }

    private fun initializeValue() {
        finalCategoryList = arrayListOf()
        priceItemAdapter = PriceItemAdapter()
        priceItemAdapter.differ.submitList(pricePerKgList)
        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(3, LinearLayoutManager.HORIZONTAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        binding.recyclerView.adapter = priceItemAdapter
        priceItemAdapter.setOnClickListener { pricePerKgModel, position ->
            pricePerKgList.removeAt(position)
            priceItemAdapter.notifyItemRangeRemoved(position + 1, pricePerKgList.size + 1)
            priceItemAdapter.differ.submitList(pricePerKgList)
        }
        getAllCategory()

        listSubCategory.add("--Select Sub Category--")
        listSubCategory.add("Quantity in KG")
        listSubCategory.add("Unit")

        spinnerSubCategoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            listSubCategory
        )
        binding.spinnerSubCategory.adapter = spinnerSubCategoryAdapter
        binding.spinnerSubCategory.onItemSelectedListener = onItemSelectListener
    }

    private fun updateProduct(productModel: ProductModel) {
        binding.textAddCategory.text = "Update Product"
        binding.btnSubmit.text = "Update"
        binding.editName.setText(productModel.name)
        binding.editBrandName.setText(productModel.brandName)
        binding.txtSelectCategory.text = productModel.categoryName
        binding.spinnerSubCategory.setSelection(listSubCategory.indexOf(productModel.subCategoryID))
        pricePerKgList =
            productModel.priceList as ArrayList<PricePerKgModel> /* = java.util.ArrayList<com.aks.mygroceryadmin.models.PricePerKgModel> */
        priceItemAdapter.differ.submitList(pricePerKgList)
        binding.btnAddImage.visibility = View.GONE
        Glide.with(requireContext()).load(productModel.imageUrl).placeholder(R.drawable.image)
            .into(binding.productImageView)
        count = (productModel.priceList as ArrayList<PricePerKgModel>).size
        selectedCategory?.categoryID = productModel.categoryID
        imageUrl = productModel.imageUrl
    }

    private val onItemSelectListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            if (p2 == 0) {
                return
            } else {
                subcategoryId = listSubCategory[p2]
//                Toast.makeText(requireContext(),listSubCategory[p2],Toast.LENGTH_SHORT).show()
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            TODO("Not yet implemented")
        }

    }

    private fun performItemClick() {
        binding.txtSelectCategory.isEnabled = false
        binding.txtSelectCategory.setOnClickListener {
            if (finalCategoryList?.size!! > 0) {
                openBottomSheetDialog()
            } else {
                Toast.makeText(requireContext(), "No category found", Toast.LENGTH_SHORT).show()
            }
        }
        binding.productImageView.setOnClickListener {
            pickImage()
        }
        binding.btnAddImage.setOnClickListener {
            pickImage()
        }
        binding.imageClose.setOnClickListener {
            this.dismiss()
        }
        binding.btnAddPrice.setOnClickListener {
            if (!binding.txtUnityPrice.text.isNullOrBlank()) {
                binding.inputLayout3.error = null
                addPricePerKg(binding.txtUnityPrice.text.toString(), "Unit")
                binding.txtUnityPrice.text!!.clear()
            } else {
                binding.inputLayout3.error = "Please enter price per unit"
            }
        }

        binding.btnAddPricePerQuantity.setOnClickListener {
            if (binding.txtKgOrUnit.text.isNullOrBlank()) {
                binding.inputLayout.error = "Please enter unit in Kg/Gm"
                binding.inputLayout2.error = null
            } else if (binding.txtPrice.text.isNullOrBlank()) {
                binding.inputLayout2.error = "Please enter price per Kg/Gm"
                binding.inputLayout.error = null
            } else {
                binding.inputLayout2.error = null
                binding.inputLayout.error = null
                addPricePerKg(
                    binding.txtPrice.text.toString(),
                    "KG",
                    binding.txtKgOrUnit.text.toString()
                )
                binding.txtPrice.text!!.clear()
                binding.txtKgOrUnit.text!!.clear()
            }
        }

        binding.cbAddUnit.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                binding.linear1.visibility = View.VISIBLE
            } else {
                binding.linear1.visibility = View.GONE
            }
        }

        binding.cbAddInQuantityPerPrice.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                binding.linear2.visibility = View.VISIBLE
            } else {
                binding.linear2.visibility = View.GONE
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (binding.editName.text.toString().isEmpty()) {
                binding.textInputLayout.error = "Please enter product name"
            } else if (selectedCategory?.categoryID == null && productModel?.categoryID.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please choose category", Toast.LENGTH_SHORT)
                    .show()
            } else if (imageUri == null && imageUrl.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select image", Toast.LENGTH_SHORT).show()
            } else if (binding.editBrandName.text.isNullOrBlank()) {
                binding.textInputLayout2.error = "Please enter brand name"
            } else if (subcategoryId == null) {
                Toast.makeText(requireContext(), "Please select sub category", Toast.LENGTH_SHORT)
                    .show()
            } else if (pricePerKgList.size == 0) {
                Toast.makeText(requireContext(), "Please add price", Toast.LENGTH_SHORT).show()
            } else {
                binding.btnSubmit.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                if (!imageUrl.isNullOrEmpty()) {
                    val categoryId = if (selectedCategory?.categoryID != null){
                        selectedCategory?.categoryID
                    }else{
                        productModel?.categoryID
                    }
                    val categoryName = if (selectedCategory?.categoryID!=null){
                        selectedCategory?.name
                    }else{
                        productModel?.name
                    }
                    val productModel = ProductModel(imageUrl,binding.editName.text.toString().trim(),productModel?.productID,categoryId,categoryName,subcategoryId,pricePerKgList,binding.editBrandName.text.toString())
                    updateProductDetails(productModel)
                } else {
                    imageUri?.let {
                        uploadImage(
                            binding.editName.text.toString().trim(),
                            it,
                            selectedCategory?.name.toString(),
                            selectedCategory?.categoryID.toString()
                        )
                    }
                }
            }
        }
    }

    private val launchFileChooser =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.e("imageUri", "getImageUri $uri ")
            uri?.let {
                imageUri = it
                binding.btnAddImage.visibility = View.GONE
                binding.productImageView.setImageURI(it)
                imageName = GenerationMethod.convertUriToFileName(requireContext(), uri)
                Log.e("filename", "uploadImage: $imageName")
                binding.imageName.text = imageName
            }
        }

    private fun uploadImage(
        productName: String,
        imageUri: Uri,
        categoryName: String,
        categoryID: String
    ) {
        val storageRef = AdminApp.instance.firebaseStorage.reference
        val dataRef = storageRef.child("image/$imageName")
        dataRef.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { exception ->
                    throw exception
                }
            }
            dataRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                uploadDataToFirestore(
                    downloadUri.toString(),
                    productName,
                    categoryName,
                    categoryID
                )
                Log.e("download url", "uploadImage: $downloadUri")
            } else {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            binding.btnSubmit.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addPricePerKg(price: String, type: String, gram: String? = null) {
        if (type == "Unit") {
            pricePerKgList.add(
                PricePerKgModel(
                    count++,
                    "Unit",
                    "$price Rs",
                    ProductStatus.AVAILABLE
                )
            )
        } else {
            pricePerKgList.add(
                PricePerKgModel(
                    count++,
                    "$gram Kg",
                    "$price Rs",
                    ProductStatus.AVAILABLE
                )
            )
        }
        priceItemAdapter.differ.submitList(pricePerKgList)
        priceItemAdapter.notifyItemInserted(pricePerKgList.size)
    }

    private fun uploadDataToFirestore(
        url: String,
        productName: String,
        categoryName: String,
        categoryID: String
    ) {
        val db = AdminApp.instance.firebaseFirestore
        val currentUser = AdminApp.instance.firebaseAuth.currentUser!!.uid
        currentUser.let {
            val document =
                db.collection("AdminMasterDetails").document(currentUser).collection("ProductList")
                    .document()
            val documentId = document.id
            val productModel = ProductModel(
                url,
                productName,
                documentId,
                categoryID,
                categoryName,
                subcategoryId,
                pricePerKgList,
                binding.editBrandName.text.toString()
            )
            document.set(productModel).addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Saved Successfully", Toast.LENGTH_SHORT).show()
                this.dismiss()
            }.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.visibility = View.VISIBLE
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
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
            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            }
        }

    private fun updateProductDetails(productModel: ProductModel) {
        Dialog.showProgressDialog(true, "Loading...", requireActivity())
        val db = AdminApp.instance.firebaseFirestore
        val currentUser = AdminApp.instance.firebaseAuth.currentUser!!.uid
        currentUser.let {
            productModel.productID?.let { it1 ->
                db.collection("AdminMasterDetails").document(currentUser).collection("ProductList")
                    .document(
                        it1
                    ).set(productModel).addOnSuccessListener {
                        Dialog.showProgressDialog(false, "Loading...", requireActivity())
                        Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT)
                            .show()
                        this.dismiss()
                    }.addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSubmit.visibility = View.VISIBLE
                        Dialog.showProgressDialog(false, "Loading...", requireActivity())
                        Toast.makeText(
                            requireContext(),
                            "Failed with ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun openBottomSheetDialog() {
        val bottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottomsheet_dialog, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.create()
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val textView: TextView = view.findViewById(R.id.title)
        textView.text = getString(R.string.category_list)
        finalCategoryList?.let { list ->
            categoryAdapter = CategoryAdapter(list)
            recyclerView.adapter = categoryAdapter
        }
        categoryAdapter.setOnClickListener { categoryModel, position ->
            bottomSheetDialog.dismiss()
            selectedCategory = CategoryModel()
            selectedCategory = categoryModel
            binding.txtSelectCategory.text = categoryModel.name
            Toast.makeText(requireContext(), categoryModel.name, Toast.LENGTH_SHORT).show()
        }
        bottomSheetDialog.show()
    }

    private fun getAllCategory() {
        binding.progress.visibility = View.VISIBLE
        val authId = AdminApp.instance.firebaseAuth.currentUser!!.uid
        val dbRef = AdminApp.instance.firebaseFirestore
        val categoryList = arrayListOf<CategoryModel>()
        dbRef.collection("AdminMasterDetails").document(authId)
            .collection("CategoryList").get()
            .addOnSuccessListener { documnetSnapShot ->
                binding.progress.visibility = View.GONE
                binding.txtSelectCategory.isEnabled = true
                val listDocument: List<CategoryModel> =
                    documnetSnapShot.toObjects(CategoryModel::class.java)
                Log.d("CategoryList", "getAllCategory: $listDocument")
                if (listDocument.isNotEmpty()) {
                    finalCategoryList = listDocument as ArrayList
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
    }

    /*private fun setUpSpinner(list: ArrayList<CategoryModel>){
        val arrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,list)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.apply {
            adapter = arrayAdapter
            setSelection(0,false)
            prompt = "--Select Category--"
        }

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Toast.makeText(requireContext(),list[position].name,Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }*/
}