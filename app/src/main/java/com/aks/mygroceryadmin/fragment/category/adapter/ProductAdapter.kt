package com.aks.mygroceryadmin.fragment.category.adapter


import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.models.ProductModel
import com.bumptech.glide.Glide


class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {


    private val diffUtilCallBack = object : DiffUtil.ItemCallback<ProductModel>() {
        override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem.productID == newItem.productID
        }

        override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem == newItem
        }

    }
    var differ = AsyncListDiffer(this, diffUtilCallBack)
    private var callBack: ((ProductModel, Int) -> Unit)? = null

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productMenu: ImageButton = itemView.findViewById(R.id.productMenu)
        private val productAvailability: TextView = itemView.findViewById(R.id.productStock)
        private val txtIsSellingFast: TextView = itemView.findViewById(R.id.txtIsSellingFast)
        fun bind(productModel: ProductModel,position:Int) {
            Glide.with(productImage.context).load(productModel.imageUrl).placeholder(R.drawable.image).error(R.drawable.image)
                .into(productImage)
            productName.text = productModel.name
            if (productModel.isProductOutOfStock==true) {
                productAvailability.text = "Out of Stock"
                productAvailability.background = ContextCompat.getDrawable(productAvailability.context,R.drawable.background_rounded_red)
                productAvailability.setTextColor(ContextCompat.getColor(productAvailability.context,R.color.red))
            }else{
                productAvailability.text = "In Stock"
                productAvailability.setTextColor(ContextCompat.getColor(productAvailability.context,R.color.green))
                productAvailability.background = ContextCompat.getDrawable(productAvailability.context,R.drawable.background_rounded_green)
            }
            if (productModel.isSellingFast == true){
                txtIsSellingFast.text = "Selling Fast"
            }else{
                txtIsSellingFast.visibility = View.GONE
            }
            itemView.setOnClickListener {
                callBack?.invoke(productModel,position)
            }

            productMenu.setOnClickListener {
                openPopUpMenu(productModel,productMenu.context,productMenu,position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.product_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(differ.currentList[position],position)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    fun setOnClickListener(callBack:(ProductModel,Int)->Unit){
        this.callBack = callBack
    }

    private var callBackMenuDelete :((ProductModel,Int)->Unit)?=null

    fun setOnMenuDeleteClickListener(callBack : ((ProductModel,Int)->Unit)){
        this.callBackMenuDelete = callBack
    }

    private var callBackMenuEdit :((ProductModel,Int)->Unit)?=null

    fun setOnMenuEditClickListener(callBack : ((ProductModel,Int)->Unit)){
        this.callBackMenuEdit = callBack
    }

    private fun openPopUpMenu(productModel: ProductModel,context: Context,imageButton: ImageButton,position: Int){
        val wrapper: Context = ContextThemeWrapper(context, R.style.popupMenuStyle)
        val popUpMenu = PopupMenu(wrapper,imageButton)
        popUpMenu.menuInflater.inflate(R.menu.action_product_menu,popUpMenu.menu)
        popUpMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.delete->{
                    callBackMenuDelete?.invoke(productModel,position)
                }
                R.id.edit->{
                    callBackMenuEdit?.invoke(productModel,position)
                }
                else -> {
                    callBackMenuEdit?.invoke(productModel,position)
                }
            }
            true
        }
        popUpMenu.show()
    }
}