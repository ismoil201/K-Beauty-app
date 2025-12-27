package com.example.shopingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopingapp.R
import com.example.shopingapp.model.ProductImage

class ImagePagerAdapter(
    private val images: List<ProductImage>
) : RecyclerView.Adapter<ImagePagerAdapter.VH>() {

    inner class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val iv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_pager, parent, false) as ImageView
        return VH(iv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Glide.with(holder.iv)
            .load(images[position].imageUrl)
            .into(holder.iv)
    }

    override fun getItemCount() = images.size
}
