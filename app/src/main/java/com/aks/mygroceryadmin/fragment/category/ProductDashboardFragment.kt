package com.aks.mygroceryadmin.fragment.category

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.app.AdminApp
import com.aks.mygroceryadmin.base.BaseActivity
import com.aks.mygroceryadmin.base.BaseFragment
import com.aks.mygroceryadmin.databinding.FragmentProductDashboardBinding
import com.aks.mygroceryadmin.fragment.category.CategoryFragment.Companion.TAG
import com.aks.mygroceryadmin.fragment.category.adapter.ProductAdapter
import com.aks.mygroceryadmin.models.ProductModel
import com.aks.mygroceryadmin.utils.Dialog

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductDashboardFragment : BaseFragment<FragmentProductDashboardBinding>(){
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
         * @return A new instance of fragment ProductDashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProductDashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun getFragmentId(): Int {
        return R.layout.fragment_product_dashboard
    }

    private lateinit var productAdapter: ProductAdapter
    private var productList = arrayListOf<ProductModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as BaseActivity).bottomNavView.visibility = View.GONE
        initializeView()
        performOnClickEvent()
    }

    override fun onResume() {
        super.onResume()
        Log.d("checkResume", "onResume: ")
        getAllProductDetails()
    }

    override fun onStart() {
        Log.d("checkResume", "onResume: start")
        super.onStart()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("checkResume", "onResume: start")
    }


    private fun initializeView() {
        productAdapter = ProductAdapter()
        binding.recyclerView.adapter = productAdapter
    }

    private fun performOnClickEvent() {
        binding.fab.setOnClickListener {
            val addProductFragment = AddProductFragment()
            val manager = requireActivity().supportFragmentManager
            addProductFragment.isCancelable = false
            addProductFragment.show(manager, addProductFragment.tag)
        }

        productAdapter.setOnClickListener { productModel, _ ->
            //navigate to detailed fragment
            val bundle = Bundle().apply {
                putSerializable("ProductModel", productModel)
            }
            findNavController().navigate(
                R.id.action_productDashboardFragment_to_productDetailFragment,
                bundle
            )
        }

        productAdapter.setOnMenuDeleteClickListener { productModel, i ->
            Dialog.messageDialog(
                true,
                requireActivity(),
                "Hey Folks😎",
                "Are you sure you want to delete the product",
                R.drawable.ic_cancel_close_svgrepo_com,
                false
            ) {
                if (it) {
                    deleteProduct(productModel, i)
                }
            }
        }
        productAdapter.setOnMenuEditClickListener { productModel, _ ->
            editProduct(productModel)
        }
        binding.swipeRefresh.setOnRefreshListener {
            getAllProductDetails()
            Handler().postDelayed({
                binding.swipeRefresh.isRefreshing = false
            },4000)
        }
        binding.imageBack.setOnClickListener{
            findNavController().popBackStack()
        }
    }

    private fun getAllProductDetails() {
        try {
            val fireStoreDb = AdminApp.instance.firebaseFirestore
            val authId = AdminApp.instance.firebaseAuth.currentUser!!.uid
            fireStoreDb.collection("AdminMasterDetails").document(authId).collection("ProductList")
                .get().addOnSuccessListener { documentSnapShot ->
                    if (documentSnapShot != null) {
                        productList =
                            documentSnapShot.toObjects(ProductModel::class.java) as ArrayList<ProductModel>
                        Log.e(TAG, "getAllProductDetails: $productList")
                        if (productList.isEmpty()) {
                            binding.lottieNotFound.visibility = View.VISIBLE
                        } else {
                            binding.lottieNotFound.visibility = View.GONE
                            productAdapter.differ.submitList(productList)
                            productAdapter.notifyDataSetChanged()
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteProduct(productModel: ProductModel, position: Int) {
        try {
            Dialog.showProgressDialog(true, "Loading...", requireActivity())
            val db = AdminApp.instance.firebaseFirestore
            val currentUser = AdminApp.instance.firebaseAuth.currentUser!!.uid
            productModel.productID?.let {
                db.collection("AdminMasterDetails").document(currentUser).collection("ProductList")
                    .document(
                        it
                    ).delete().addOnSuccessListener {
                        Dialog.showProgressDialog(false, "Loading...", requireActivity())
                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT)
                            .show()
                        productList.removeAt(position)
                        productAdapter.notifyDataSetChanged()

                    }.addOnFailureListener { exception ->
                        Dialog.showProgressDialog(false, "Loading...", requireActivity())
                        Toast.makeText(
                            requireContext(),
                            "Failed with ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun editProduct(productModel: ProductModel) {
        val addProductFragment = AddProductFragment()
        val manager = requireActivity().supportFragmentManager
        addProductFragment.isCancelable = false
        val bundle = Bundle().apply {
            putSerializable("ProductModel", productModel)
        }
        addProductFragment.arguments = bundle
        addProductFragment.show(manager, addProductFragment.tag)
    }
}


enum class ProductStatus {
    AVAILABLE,
    OUT_OF_STOCK
}