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
        val b = holder.binding

        // BRAND
        b.tvBrand.text = "${item.category} · Korea"

        // NAME
        b.tvName.text = item.name

        // PRICE
        b.tvFinalPrice.text = "${item.discountPrice}$"

        // ❤️ FAVORITE
        b.btnLike.setImageResource(
            if (item.isFavorite)
                R.drawable.heart_clicked_svg
            else
                R.drawable.heart_svg
        )

        b.btnLike.setOnClickListener {
            onLikeClick(item)
        }

        // 🔥 IMAGE (ENG MUHIM FIX)
        Glide.with(holder.itemView.context)
            .load(item.mainImage())
            .placeholder(R.drawable.img)
            .into(b.imgProduct)

        // CLICK
        holder.itemView.setOnClickListener {
            Log.d("CLICK", "clicked product id=${item.id}")

            onClickItem(item.id)
        }
    }

    override fun getItemCount(): Int = list.size
}
