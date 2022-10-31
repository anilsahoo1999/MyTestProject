package com.aks.mygroceryadmin.fragment.category.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.fragment.category.ProductStatus
import com.aks.mygroceryadmin.models.CategoryModel
import com.aks.mygroceryadmin.models.PricePerKgModel
import com.bumptech.glide.Glide

class PriceAdapter(private val list: ArrayList<PricePerKgModel>) :
    RecyclerView.Adapter<PriceAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        private val rbOutOfStock: RadioButton = itemView.findViewById(R.id.rbOutOfStock)
        private val rbStock: RadioButton = itemView.findViewById(R.id.rbStock)
        private val toggle: RadioGroup = itemView.findViewById(R.id.toggle)
        fun bind(pricePerKgModel: PricePerKgModel, position: Int) {
            txtPrice.text = pricePerKgModel.price
            if (pricePerKgModel.productStatus == ProductStatus.AVAILABLE) {
                rbStock.isChecked = true
            } else {
                rbOutOfStock.isChecked = true
            }
            toggle.setOnCheckedChangeListener { _, i ->
                when (i) {
                    R.id.rbStock -> {
                        onItemClick?.invoke(pricePerKgModel, position, "S")//S- stock
                    }
                    R.id.rbOutOfStock -> {
                        onItemClick?.invoke(pricePerKgModel, position, "OS")//OS- out of stock
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.price_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onItemClick: ((PricePerKgModel, Int, String) -> Unit)? = null

    fun setOnClickListener(callback: ((PricePerKgModel, Int, String) -> Unit)? = null) {
        onItemClick = callback
    }

}