package com.example.shopingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopingapp.databinding.ItemOnboardingBinding

class OnboardingAdapter(private val list: List<OnboardingData>) :
    RecyclerView.Adapter<OnboardingAdapter.Vh>() {

    inner class Vh(val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        val item = list[position]
        holder.binding.imgOnboard.setImageResource(item.image)
        holder.binding.tvTitle.text = item.title
        holder.binding.tvDescription.text = item.desc
    }

    override fun getItemCount(): Int = list.size
}
