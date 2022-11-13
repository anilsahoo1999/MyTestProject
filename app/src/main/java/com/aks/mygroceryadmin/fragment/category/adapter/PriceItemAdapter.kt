package com.aks.mygroceryadmin.fragment.category.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.models.PricePerKgModel

class PriceItemAdapter : RecyclerView.Adapter<PriceItemAdapter.ViewHolder>(){

    private val diffUtilCallBack  = object : DiffUtil.ItemCallback<PricePerKgModel>(){
        override fun areItemsTheSame(oldItem: PricePerKgModel, newItem: PricePerKgModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PricePerKgModel,
            newItem: PricePerKgModel
        ): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,diffUtilCallBack)
    private var onItemClick : ((PricePerKgModel, Int)->Unit)?=null
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        private val textName : TextView = itemView.findViewById(R.id.textView)
        private val imageDelete : ImageView = itemView.findViewById(R.id.imageDelete)

        fun bind(pricePerKgModel : PricePerKgModel, position: Int){
            textName.text = "${pricePerKgModel.gram} / ${pricePerKgModel.price}"
            imageDelete.setOnClickListener{
               onItemClick?.invoke(differ.currentList[position],position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_chip_view,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position],position)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    fun setOnClickListener(callback: ((PricePerKgModel, Int) -> Unit)?=null){
        onItemClick = callback
    }
}