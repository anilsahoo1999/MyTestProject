package com.aks.mygroceryadmin.fragment.category

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.base.BaseActivity
import com.aks.mygroceryadmin.base.BaseFragment
import com.aks.mygroceryadmin.databinding.FragmentProductDetailBinding
import com.aks.mygroceryadmin.fragment.category.CategoryFragment.Companion.TAG
import com.aks.mygroceryadmin.fragment.category.adapter.CategoryAdapter
import com.aks.mygroceryadmin.fragment.category.adapter.PriceAdapter
import com.aks.mygroceryadmin.models.CategoryModel
import com.aks.mygroceryadmin.models.PricePerKgModel
import com.aks.mygroceryadmin.models.ProductModel
import com.aks.mygroceryadmin.utils.Dialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductDetailFragment : BaseFragment<FragmentProductDetailBinding>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProductDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun getFragmentId(): Int {
        return R.layout.fragment_product_detail
    }

    private lateinit var productModel: ProductModel
    lateinit var priceAdapter: PriceAdapter
    private var finalCategoryList: ArrayList<CategoryModel>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productModel = requireArguments().getSerializable("ProductModel") as ProductModel
        Log.e(TAG, "onViewCreated: $productModel")
        setUpProduct()
        initializeView()

        val toolbar: Toolbar = binding.toolBarLayout
        (activity as BaseActivity).setSupportActionBar(toolbar)
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as BaseActivity).supportActionBar?.title = productModel.name
        val drawable: Drawable? =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_arrow_back_24)
        (activity as BaseActivity).supportActionBar?.setHomeAsUpIndicator(drawable)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initializeView() {
        finalCategoryList = arrayListOf()
        priceAdapter = PriceAdapter(productModel.priceList as ArrayList<PricePerKgModel>)
        binding.recyclerPriceDetails.adapter = priceAdapter


        priceAdapter.setOnClickListener { pricePerKgModel, i, status ->
            if (pricePerKgModel.productStatus == ProductStatus.AVAILABLE && status == "S") {
                binding.btnSubmitProduct.isEnabled = false
            } else if (pricePerKgModel.productStatus == ProductStatus.OUT_OF_STOCK && status == "OS") {
                binding.btnSubmitProduct.isEnabled = false
            } else {
                binding.btnSubmitProduct.isEnabled = true
                binding.btnSubmitProduct.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green
                    )
                )
                if (pricePerKgModel.productStatus == ProductStatus.AVAILABLE && status == "OS") {
                    (productModel.priceList as ArrayList<PricePerKgModel>)[i].productStatus =
                        ProductStatus.OUT_OF_STOCK
                } else {
                    (productModel.priceList as ArrayList<PricePerKgModel>)[i].productStatus =
                        ProductStatus.AVAILABLE
                }
                Log.e(TAG, "Product model: $productModel")
            }
        }

        binding.cbOutOfStock.setOnCheckedChangeListener { compoundButton, isChecked ->
            binding.btnSubmitProduct.isEnabled = true
            binding.btnSubmitProduct.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
            productModel.isProductOutOfStock = isChecked
        }
        binding.cbIsSellingFast.setOnCheckedChangeListener { compoundButton, isChecked ->
            binding.btnSubmitProduct.isEnabled = true
            binding.btnSubmitProduct.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
            productModel.isSellingFast = isChecked
        }
        binding.btnSubmitProduct.setOnClickListener {
            submitDetails()
        }
    }

    private fun setUpProduct() {
        Glide.with(requireContext()).load(productModel.imageUrl).error(R.drawable.image)
            .into(binding.appBarImage)
        binding.txtProductName.text = "Product name : ${productModel.name}"
        binding.txtProductBrand.text = "Product Brand : ${productModel.brandName}"
        binding.txtProductCat.text = "Product Category : ${productModel.categoryName}"
        binding.txtProductSubCat.text = "Product sub-category : ${productModel.subCategoryID}"

        if (productModel.isProductOutOfStock == true){
            binding.cbOutOfStock.isChecked = true
        }
        if (productModel.isSellingFast == true){
            binding.cbIsSellingFast.isChecked = true
        }
    }
    private fun submitDetails(){
        Dialog.showProgressDialog(true,"Loading...",requireActivity())
        val db = AdminApp.instance.firebaseFirestore
        val currentUser = AdminApp.instance.firebaseAuth.currentUser!!.uid
        currentUser.let {
            productModel.productID?.let { it1 ->
                db.collection("AdminMasterDetails").document(currentUser).collection("ProductList").document(
                    it1
                ).set(productModel).addOnSuccessListener {
                    Dialog.showProgressDialog(false,"Loading...",requireActivity())
                    Toast.makeText(requireContext(),"Updated successfully",Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.addOnFailureListener {
                    Dialog.showProgressDialog(false,"Loading...",requireActivity())
                    Toast.makeText(requireContext(),"Failed with ${it.message}",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}