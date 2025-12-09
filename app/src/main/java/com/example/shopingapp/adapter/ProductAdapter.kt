package com.example.shopingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopingapp.databinding.ItemProductBinding
import com.example.shopingapp.model.Product

class ProductAdapter(
    private val onClickItem: onClickItem? = null
) : RecyclerView.Adapter<ProductAdapter.ProductVh>() {

    private val list = ArrayList<Product>()

    fun submitData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ProductVh(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVh {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductVh(binding)
    }

    override fun onBindViewHolder(holder: ProductVh, position: Int) {
        val item = list[position]

        holder.binding.tvName.text = item.name
        holder.binding.tvDesc.text = item.description
        holder.binding.tvPrice.text = "${item.price}$"

        Glide.with(holder.itemView)
            .load(item.imageUrl)
            .into(holder.binding.imgProduct)

        // click for both Home + Similar items
        holder.itemView.setOnClickListener {
            onClickItem?.onClick(item.id)
        }
    }

    override fun getItemCount(): Int = list.size
}

interface onClickItem {
    fun onClick(id: Int?)
}
