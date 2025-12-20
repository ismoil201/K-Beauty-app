package com.example.shopingapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopingapp.R
import com.example.shopingapp.databinding.ItemProductBinding
import com.example.shopingapp.model.Product

class ProductAdapter(
    private val onClickItem: (Long) -> Unit,      // ✅ Long
    private val onLikeClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductVh>() {

    private val list = mutableListOf<Product>()

    fun submitData(newList: List<Product>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateFavorites(map: Map<Long, Boolean>) {
        list.forEach { product ->
            product.id.let { id ->
                product.isFavorite = map[id] ?: product.isFavorite
            }
        }
        notifyDataSetChanged()
    }

    inner class ProductVh(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVh {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductVh(binding)
    }

    override fun onBindViewHolder(holder: ProductVh, position: Int) {
        val item = list[position]

        holder.binding.tvBrand.text = item.category
        holder.binding.tvName.text = item.name
        holder.binding.tvPrice.text = "${item.price}$"
        if(item.isFavorite){
            holder.binding.btnLike.setImageResource(R.drawable.heart_clicked_svg)
        }

        holder.binding.btnLike.setImageResource(
            if (item.isFavorite)
                R.drawable.heart_clicked_svg
            else
                R.drawable.heart_svg
        )

        holder.binding.btnLike.setOnClickListener {
            onLikeClick(item)
        }

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
//            .placeholder(R.drawable.placeholder) // ixtiyoriy
//            .error(R.drawable.placeholder)
            .into(holder.binding.imgProduct)

        holder.itemView.setOnClickListener {
            Log.d("CLICK", "open detail id=${item.id}")

            onClickItem(item.id)   // ✅ Long
        }
    }

    override fun getItemCount(): Int = list.size
}
