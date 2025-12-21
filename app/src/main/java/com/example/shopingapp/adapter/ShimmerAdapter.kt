package com.example.shopingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopingapp.R

class ShimmerAdapter : RecyclerView.Adapter<ShimmerAdapter.Vh>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_shimmer, parent, false)
        return Vh(view)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {}

    override fun getItemCount(): Int = 9 // nechta skeleton ko‘rinsin

    class Vh(view: View) : RecyclerView.ViewHolder(view)
}
