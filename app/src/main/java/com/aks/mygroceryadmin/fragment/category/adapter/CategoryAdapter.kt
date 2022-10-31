package com.aks.mygroceryadmin.fragment.category.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aks.mygroceryadmin.R
import com.aks.mygroceryadmin.models.CategoryModel
import com.bumptech.glide.Glide

class CategoryAdapter(private val list: ArrayList<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var checkedPosition = -1;

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.categoryName)
        val imageGo: ImageView = itemView.findViewById(R.id.imgArrowGo)
        val background: RelativeLayout = itemView.findViewById(R.id.relativeLL)
        fun bind(categoryModel: CategoryModel, position: Int) {
            Glide.with(imageView.context).load(categoryModel.imageUrl).centerCrop()
                .placeholder(R.drawable.ic_image_photo_loading).into(imageView)
            textView.text = categoryModel.name
            itemView.setOnClickListener {
                imageGo.visibility = View.VISIBLE
                background.setBackgroundResource(R.drawable.selectable_background)
                if (checkedPosition != position) {
                    notifyItemChanged(checkedPosition)
                    checkedPosition = position
                }
            }
            imageGo.setOnClickListener{
               onItemClick?.invoke(categoryModel,position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.category_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
        if (checkedPosition == -1) {
            holder.imageGo.visibility = View.GONE
        } else {
            if (checkedPosition == position) {
                holder.imageGo.visibility = View.VISIBLE
                holder.background.setBackgroundResource(R.drawable.selectable_background)
            } else {
                holder.imageGo.visibility = View.GONE
                holder.background.setBackgroundResource(0)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private var onItemClick : ((CategoryModel,Int) -> Unit)? = null

    fun setOnClickListener(callback: ((CategoryModel,Int) -> Unit)?=null){
        onItemClick = callback
    }

}